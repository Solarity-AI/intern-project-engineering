//
//  ProductReviewApp.swift
//  ProductReview
//
//  Main entry point for the Product Review iOS application
//

import SwiftUI
import UIKit
import UserNotifications
import OSLog

// MARK: - Notification Names Extension
extension Notification.Name {
    static let navigateToProduct = Notification.Name("navigateToProduct")
    static let updateBadgeCount = Notification.Name("updateBadgeCount")
    static let wishlistChanged = Notification.Name("wishlistChanged")
    static let notificationDeleted = Notification.Name("notificationDeleted")
}

private enum NotificationDeepLinkStore {
    private static let pendingProductIdKey = "pendingNotificationProductId"

    static func save(productId: Int) {
        UserDefaults.standard.set(productId, forKey: pendingProductIdKey)
    }

    static func consume() -> Int? {
        defer { clear() }
        if let rawInt = UserDefaults.standard.object(forKey: pendingProductIdKey) as? Int {
            return rawInt
        }
        if let rawString = UserDefaults.standard.string(forKey: pendingProductIdKey) {
            return Int(rawString)
        }
        return nil
    }

    static func clear() {
        UserDefaults.standard.removeObject(forKey: pendingProductIdKey)
    }
}

@main
struct ProductReviewApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @StateObject private var appState = AppState()
    @StateObject private var themeManager = ThemeManager.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
                .environmentObject(themeManager)
                .preferredColorScheme(themeManager.currentTheme.colorScheme)
                .tint(Color("AccentColor"))
        }
    }
}

// MARK: - App State
@MainActor
class AppState: ObservableObject {
    @Published var isOnline: Bool = true
    @Published var selectedTab: AppTab = .products
    @Published var pendingProductId: Int? = nil
    @Published var notificationBadgeCount: Int = 0
    private var navigateToProductObserver: NSObjectProtocol?
    private var updateBadgeCountObserver: NSObjectProtocol?

    init() {
        // Setup notification observers
        setupNotificationObservers()

        // Consume deep link captured before observers were ready (terminated/cold start tap).
        if let startupProductId = NotificationDeepLinkStore.consume() {
            Task { @MainActor [weak self] in
                self?.pendingProductId = startupProductId
            }
        }

        // Bootstrap unread count so tab bar badge is available at launch.
        Task { [weak self] in
            await self?.refreshNotificationBadgeCount()
        }
    }

    // MARK: - Notification Observers

    private func setupNotificationObservers() {
        // Deep link to product from notification
        navigateToProductObserver = NotificationCenter.default.addObserver(
            forName: .navigateToProduct,
            object: nil,
            queue: .main
        ) { [weak self] notification in
            guard let self = self else { return }
            if let productId = notification.userInfo?["productId"] as? Int {
                NotificationDeepLinkStore.clear()
                Task { @MainActor in
                    self.pendingProductId = productId
                }
            }
        }

        // Update badge count
        updateBadgeCountObserver = NotificationCenter.default.addObserver(
            forName: .updateBadgeCount,
            object: nil,
            queue: .main
        ) { [weak self] notification in
            guard let self = self else { return }
            if let count = notification.userInfo?["count"] as? Int {
                Task { @MainActor in
                    self.notificationBadgeCount = count
                }
                LocalNotificationManager.shared.updateBadgeCount(count)
            }
        }
    }

    func refreshNotificationBadgeCount() async {
        do {
            let unreadCount = try await NotificationRepository().getUnreadCount()
            notificationBadgeCount = unreadCount
            LocalNotificationManager.shared.updateBadgeCount(unreadCount)
        } catch {
            // Keep existing value when the count cannot be loaded (offline, server error, etc.).
        }
    }

    deinit {
        if let navigateToProductObserver {
            NotificationCenter.default.removeObserver(navigateToProductObserver)
        }
        if let updateBadgeCountObserver {
            NotificationCenter.default.removeObserver(updateBadgeCountObserver)
        }
    }
}

// MARK: - App Delegate

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "ProductReview", category: "PushNotifications")

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {

        print("🚀 AppDelegate didFinishLaunchingWithOptions called")

        // Setup notification center
        UNUserNotificationCenter.current().delegate = self

        // Request notification permissions
        requestNotificationPermission()

        // Register notification categories
        registerNotificationCategories()

        if let launchOptions {
            handleNotificationLaunchOptions(launchOptions)
        }

        return true
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Refresh unread count on foreground instead of force-resetting badge state.
        Task {
            do {
                let unreadCount = try await NotificationRepository().getUnreadCount()
                NotificationCenter.default.post(
                    name: .updateBadgeCount,
                    object: nil,
                    userInfo: ["count": unreadCount]
                )
            } catch {
                // Keep existing badge state when refresh fails.
            }
        }
    }

    // MARK: - Notification Permission

    private func requestNotificationPermission() {
        print("📱 Requesting notification permission...")

        UNUserNotificationCenter.current().getNotificationSettings { settings in
            switch settings.authorizationStatus {
            case .authorized, .provisional, .ephemeral:
                print("✅ Notification permission already granted")
                DispatchQueue.main.async {
                    UIApplication.shared.registerForRemoteNotifications()
                }
            case .notDetermined:
                UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, error in
                    if let error = error {
                        print("❌ Notification permission error: \(error.localizedDescription)")
                        return
                    }

                    if granted {
                        print("✅ Notification permission granted")
                        DispatchQueue.main.async {
                            UIApplication.shared.registerForRemoteNotifications()
                        }
                    } else {
                        print("⚠️ Notification permission denied")
                    }
                }
            case .denied:
                print("⚠️ Notification permission denied")
            @unknown default:
                print("⚠️ Unknown notification authorization status")
            }
        }
    }

    // MARK: - Notification Categories

    private func registerNotificationCategories() {
        // Define actions
        let viewAction = UNNotificationAction(
            identifier: "VIEW_PRODUCT",
            title: "View Product",
            options: [.foreground]
        )

        let dismissAction = UNNotificationAction(
            identifier: "DISMISS",
            title: "Dismiss",
            options: []
        )

        // Define category
        let productCategory = UNNotificationCategory(
            identifier: "PRODUCT_NOTIFICATION",
            actions: [viewAction, dismissAction],
            intentIdentifiers: [],
            options: []
        )

        // Register categories
        UNUserNotificationCenter.current().setNotificationCategories([productCategory])
    }

    // MARK: - UNUserNotificationCenterDelegate

    // Handle notification when app is in foreground
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification) async -> UNNotificationPresentationOptions {
        // Show notification even when app is in foreground
        return [.banner, .sound, .badge]
    }

    // Handle notification tap
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse) async {
        let supportedActionIdentifiers: Set<String> = [
            UNNotificationDefaultActionIdentifier,
            "VIEW_PRODUCT"
        ]

        guard supportedActionIdentifiers.contains(response.actionIdentifier) else {
            return
        }

        let userInfo = response.notification.request.content.userInfo
        await handleNotificationPayload(userInfo)
    }

    private func handleNotificationLaunchOptions(_ launchOptions: [UIApplication.LaunchOptionsKey: Any]) {
        guard let remoteNotificationPayload = launchOptions[.remoteNotification] as? [AnyHashable: Any] else {
            return
        }

        if let productId = extractProductId(from: remoteNotificationPayload) {
            NotificationDeepLinkStore.save(productId: productId)
        }
    }

    private func extractProductId(from userInfo: [AnyHashable: Any]) -> Int? {
        if let rawInt = userInfo["productId"] as? Int {
            return rawInt
        }
        if let rawString = userInfo["productId"] as? String {
            return Int(rawString)
        }
        return nil
    }

    @MainActor
    private func handleNotificationPayload(_ userInfo: [AnyHashable: Any]) {
        if let productId = extractProductId(from: userInfo) {
            NotificationDeepLinkStore.save(productId: productId)
            // Post notification for deep linking
            NotificationCenter.default.post(
                name: .navigateToProduct,
                object: nil,
                userInfo: ["productId": productId]
            )

            print("📱 Deep link to product: \(productId)")
        }
    }

    // MARK: - Remote Notifications (for future push notifications)

    func application(_ application: UIApplication,
                     didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let token = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        #if DEBUG
        logger.debug("Device token received: \(token, privacy: .private(mask: .hash))")
        #endif
        // TODO: Send token to backend server
    }

    func application(_ application: UIApplication,
                     didFailToRegisterForRemoteNotificationsWithError error: Error) {
        logger.error("Failed to register for remote notifications: \(error.localizedDescription, privacy: .public)")
    }
}

// MARK: - Local Notification Manager

class LocalNotificationManager {
    static let shared = LocalNotificationManager()
    private var currentBadgeCount = 0

    private init() {}

    /// Schedule a local notification for a product review
    func scheduleProductNotification(productId: Int, productName: String, message: String, delay: TimeInterval = 5) {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            let isAuthorized = settings.authorizationStatus == .authorized
                || settings.authorizationStatus == .provisional
                || settings.authorizationStatus == .ephemeral

            guard isAuthorized else {
                if settings.authorizationStatus == .notDetermined {
                    UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, error in
                        if let error = error {
                            print("❌ Failed to request notification permission: \(error.localizedDescription)")
                            return
                        }

                        if granted {
                            self.scheduleProductNotification(
                                productId: productId,
                                productName: productName,
                                message: message,
                                delay: delay
                            )
                        } else {
                            print("⚠️ Cannot schedule notification because permission was denied")
                        }
                    }
                } else {
                    print("⚠️ Cannot schedule notification because permission is denied")
                }
                return
            }

            DispatchQueue.main.async {
                self.currentBadgeCount = max(0, self.currentBadgeCount + 1)
                let nextBadgeCount = self.currentBadgeCount
                let content = UNMutableNotificationContent()
                content.title = "Product Review"
                content.body = message
                content.sound = .default
                content.badge = NSNumber(value: nextBadgeCount)
                content.categoryIdentifier = "PRODUCT_NOTIFICATION"
                content.userInfo = ["productId": String(productId)]

                // Create trigger
                let trigger = UNTimeIntervalNotificationTrigger(timeInterval: delay, repeats: false)

                // Create request
                let request = UNNotificationRequest(
                    identifier: "product-\(productId)-\(Date().timeIntervalSince1970)",
                    content: content,
                    trigger: trigger
                )

                // Schedule notification
                UNUserNotificationCenter.current().add(request) { error in
                    if let error = error {
                        print("❌ Failed to schedule notification: \(error.localizedDescription)")
                    } else {
                        NotificationCenter.default.post(
                            name: .updateBadgeCount,
                            object: nil,
                            userInfo: ["count": nextBadgeCount]
                        )
                        print("✅ Scheduled notification for product \(productId)")
                    }
                }
            }
        }
    }

    /// Clear all pending notifications
    func clearAllNotifications() {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
        UNUserNotificationCenter.current().removeAllDeliveredNotifications()
        DispatchQueue.main.async {
            NotificationCenter.default.post(
                name: .updateBadgeCount,
                object: nil,
                userInfo: ["count": 0]
            )
        }
    }

    /// Update badge count
    func updateBadgeCount(_ count: Int) {
        DispatchQueue.main.async {
            self.currentBadgeCount = max(0, count)
            UIApplication.shared.applicationIconBadgeNumber = self.currentBadgeCount
        }
    }
}
