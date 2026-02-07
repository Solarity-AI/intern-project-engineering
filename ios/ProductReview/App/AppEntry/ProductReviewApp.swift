//
//  ProductReviewApp.swift
//  ProductReview
//
//  Main entry point for the Product Review iOS application
//

import SwiftUI
import UIKit
import UserNotifications

// MARK: - Notification Names Extension
extension Notification.Name {
    static let navigateToProduct = Notification.Name("navigateToProduct")
    static let updateBadgeCount = Notification.Name("updateBadgeCount")
}

@main
struct ProductReviewApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @StateObject private var appState = AppState()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
                .preferredColorScheme(appState.colorScheme)
        }
    }
}

// MARK: - App State
@MainActor
class AppState: ObservableObject {
    @Published var colorScheme: ColorScheme? = nil
    @Published var isOnline: Bool = true
    @Published var pendingProductId: Int? = nil
    @Published var notificationBadgeCount: Int = 0

    init() {
        // Load saved theme preference
        if let savedTheme = UserDefaults.standard.string(forKey: "theme") {
            colorScheme = savedTheme == "dark" ? .dark : .light
        }

        // Setup notification observers
        setupNotificationObservers()

        // Bootstrap unread count so tab bar badge is available at launch.
        Task { [weak self] in
            await self?.refreshNotificationBadgeCount()
        }
    }

    func toggleTheme() {
        if colorScheme == .dark {
            colorScheme = .light
            UserDefaults.standard.set("light", forKey: "theme")
        } else {
            colorScheme = .dark
            UserDefaults.standard.set("dark", forKey: "theme")
        }
    }

    // MARK: - Notification Observers

    private func setupNotificationObservers() {
        // Deep link to product from notification
        NotificationCenter.default.addObserver(
            forName: .navigateToProduct,
            object: nil,
            queue: .main
        ) { [weak self] notification in
            guard let self = self else { return }
            if let productId = notification.userInfo?["productId"] as? Int {
                Task { @MainActor in
                    self.pendingProductId = productId
                }
            }
        }

        // Update badge count
        NotificationCenter.default.addObserver(
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
        NotificationCenter.default.removeObserver(self)
    }
}

// MARK: - App Delegate

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {

        print("🚀 AppDelegate didFinishLaunchingWithOptions called")

        // Setup notification center
        UNUserNotificationCenter.current().delegate = self

        // Request notification permissions
        requestNotificationPermission()

        // Register notification categories
        registerNotificationCategories()

        return true
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Clear badge when app becomes active
        application.applicationIconBadgeNumber = 0
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
        let userInfo = response.notification.request.content.userInfo
        await handleNotificationPayload(userInfo)
    }

    @MainActor
    private func handleNotificationPayload(_ userInfo: [AnyHashable: Any]) {
        // Extract productId from notification payload (supports both String and Int).
        let productId: Int?
        if let rawInt = userInfo["productId"] as? Int {
            productId = rawInt
        } else if let rawString = userInfo["productId"] as? String {
            productId = Int(rawString)
        } else {
            productId = nil
        }

        if let productId = productId {
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
        print("📱 Device token: \(token)")
        // TODO: Send token to backend server
    }

    func application(_ application: UIApplication,
                     didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("❌ Failed to register for remote notifications: \(error.localizedDescription)")
    }
}

// MARK: - Local Notification Manager

class LocalNotificationManager {
    static let shared = LocalNotificationManager()

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

            let content = UNMutableNotificationContent()
            content.title = "Product Review"
            content.body = message
            content.sound = .default
            content.badge = NSNumber(value: UIApplication.shared.applicationIconBadgeNumber + 1)
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
                    print("✅ Scheduled notification for product \(productId)")
                }
            }
        }
    }

    /// Clear all pending notifications
    func clearAllNotifications() {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
        UNUserNotificationCenter.current().removeAllDeliveredNotifications()
        DispatchQueue.main.async {
            UIApplication.shared.applicationIconBadgeNumber = 0
        }
    }

    /// Update badge count
    func updateBadgeCount(_ count: Int) {
        DispatchQueue.main.async {
            UIApplication.shared.applicationIconBadgeNumber = count
        }
    }
}
