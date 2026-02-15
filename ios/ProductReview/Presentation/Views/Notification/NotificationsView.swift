//
//  NotificationsView.swift
//  ProductReview
//
//  Notifications list screen
//

import SwiftUI
import Foundation

// MARK: - Type Color & Icon Helpers

private func typeColor(for type: String) -> Color {
    switch type {
    case "review": return AppColors.primary
    case "order":  return AppColors.orderBlue
    case "system": return AppColors.aiPurple
    default:       return AppColors.primary
    }
}

private func typeIconName(for type: String) -> String {
    switch type {
    case "review": return "star.fill"
    case "order":  return "shippingbox.fill"
    default:       return "bell.fill"
    }
}

private func relativeDate(from date: Date?) -> String {
    guard let date = date else { return "" }
    let formatter = RelativeDateTimeFormatter()
    formatter.unitsStyle = .full
    return formatter.localizedString(for: date, relativeTo: Date())
}

private func fullTimestamp(from date: Date?) -> String {
    guard let date = date else { return "" }
    let formatter = DateFormatter()
    formatter.dateFormat = "MMM d, yyyy 'at' HH:mm"
    return formatter.string(from: date)
}

// MARK: - Notifications View

struct NotificationsView: View {
    @EnvironmentObject var navigationRouter: NavigationRouter
    @StateObject private var viewModel = NotificationsViewModel()
    @State private var selectedType: String? = nil

    private var filteredNotifications: [AppNotification] {
        guard let type = selectedType else { return viewModel.notifications }
        return viewModel.notifications.filter { $0.notificationType == type }
    }

    var body: some View {
        ZStack {
            Color("AppBackground")
                .ignoresSafeArea()

            VStack(spacing: 0) {
                // Custom header
                HStack(alignment: .center, spacing: AppSpacing.sm) {
                    Text("Notifications")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(AppColors.foreground)

                    if viewModel.unreadCount > 0 {
                        Text("\(viewModel.unreadCount)")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 7)
                            .padding(.vertical, 3)
                            .background(AppColors.primary, in: Capsule())
                    }
                    Spacer()
                }
                .padding(.horizontal, AppSpacing.lg)
                .padding(.top, AppSpacing.sm)
                .padding(.bottom, AppSpacing.md)

                // Filter chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: AppSpacing.sm) {
                        NotificationFilterChip(label: "All",     type: nil,      selectedType: $selectedType)
                        NotificationFilterChip(label: "Reviews", type: "review", selectedType: $selectedType)
                        NotificationFilterChip(label: "Orders",  type: "order",  selectedType: $selectedType)
                        NotificationFilterChip(label: "System",  type: "system", selectedType: $selectedType)
                    }
                    .padding(.horizontal, AppSpacing.lg)
                    .padding(.vertical, AppSpacing.xs)
                }
                .padding(.bottom, AppSpacing.sm)

                // Content
                if filteredNotifications.isEmpty && !viewModel.isLoading {
                    Spacer()
                    VStack(spacing: AppSpacing.lg) {
                        Image(systemName: "bell.slash")
                            .font(.system(size: 60))
                            .foregroundColor(AppColors.foreground.opacity(0.3))
                        VStack(spacing: 6) {
                            Text("No notifications")
                                .font(.title2)
                                .fontWeight(.medium)
                                .foregroundColor(AppColors.foreground)
                            Text("You're all caught up!")
                                .font(.body)
                                .foregroundColor(AppColors.foreground.opacity(0.5))
                        }
                    }
                    .padding()
                    Spacer()
                } else {
                    List {
                        ForEach(filteredNotifications) { notification in
                            NotificationRow(notification: notification)
                                .listRowBackground(Color.clear)
                                .listRowSeparator(.hidden)
                                .listRowInsets(EdgeInsets(
                                    top: 4,
                                    leading: AppSpacing.lg,
                                    bottom: 4,
                                    trailing: AppSpacing.lg
                                ))
                                .onTapGesture {
                                    Task { @MainActor in
                                        await viewModel.markAsRead(notificationId: notification.id)
                                        let updatedNotification = viewModel.notifications.first { $0.id == notification.id } ?? notification
                                        navigationRouter.navigate(to: .notificationDetail(notification: updatedNotification))
                                    }
                                }
                                .swipeActions(edge: .trailing) {
                                    Button(role: .destructive) {
                                        Task {
                                            await viewModel.deleteNotification(notificationId: notification.id)
                                        }
                                    } label: {
                                        Label("Delete", systemImage: "trash")
                                    }
                                }
                        }
                    }
                    .listStyle(.plain)
                    .scrollContentBackground(.hidden)
                    .refreshable {
                        await viewModel.loadNotifications()
                    }
                }
            }
        }
        .navigationTitle("")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Menu {
                    Button {
                        Task { await viewModel.markAllAsRead() }
                    } label: {
                        Label("Mark all as read", systemImage: "checkmark.circle")
                    }
                    .disabled(viewModel.unreadCount == 0)

                    Button(role: .destructive) {
                        Task { await viewModel.deleteAll() }
                    } label: {
                        Label("Delete all", systemImage: "trash")
                    }
                    .disabled(viewModel.notifications.isEmpty)
                } label: {
                    Image(systemName: "ellipsis.circle")
                }
                .tint(AppColors.primary)
            }
        }
        .task {
            await viewModel.loadNotifications()
        }
        .onReceive(NotificationCenter.default.publisher(for: .notificationDeleted)) { _ in
            Task { await viewModel.loadNotifications() }
        }
        .alert("Error", isPresented: Binding<Bool>(
            get: { viewModel.error != nil },
            set: { if !$0 { viewModel.error = nil } }
        )) {
            Button("OK") { viewModel.error = nil }
        } message: {
            Text(viewModel.error ?? "")
        }
    }
}

// MARK: - Filter Chip

private struct NotificationFilterChip: View {
    let label: String
    let type: String?
    @Binding var selectedType: String?

    private var isActive: Bool { selectedType == type }

    private var chipColor: Color {
        guard let t = type else { return AppColors.primary }
        return typeColor(for: t)
    }

    var body: some View {
        Button {
            selectedType = type
        } label: {
            if isActive {
                Text(label)
                    .font(.system(size: AppFontSize.sm, weight: .medium))
                    .foregroundColor(.white)
                    .padding(.horizontal, AppSpacing.md)
                    .padding(.vertical, 6)
                    .background(Capsule().fill(chipColor))
            } else {
                Text(label)
                    .font(.system(size: AppFontSize.sm, weight: .medium))
                    .foregroundColor(AppColors.foreground.opacity(0.7))
                    .padding(.horizontal, AppSpacing.md)
                    .padding(.vertical, 6)
                    .glassCard(AppGlass.subtle, cornerRadius: AppRadius.full)
            }
        }
        .buttonStyle(.plain)
        .animation(.easeInOut(duration: 0.2), value: selectedType)
    }
}

// MARK: - Notification Row

struct NotificationRow: View {
    let notification: AppNotification

    private var color: Color    { typeColor(for: notification.notificationType) }
    private var iconName: String { typeIconName(for: notification.notificationType) }

    var body: some View {
        HStack(alignment: .top, spacing: 0) {
            // 3pt left accent line
            Rectangle()
                .fill(color)
                .frame(width: 3)

            HStack(alignment: .top, spacing: AppSpacing.md) {
                // Circular type icon
                ZStack {
                    Circle()
                        .fill(color.opacity(0.15))
                        .frame(width: 32, height: 32)
                    Image(systemName: iconName)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(color)
                }

                // Text content
                VStack(alignment: .leading, spacing: 4) {
                    HStack(alignment: .top) {
                        Text(notification.title)
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(AppColors.foreground)
                        Spacer()
                        Text(relativeDate(from: notification.createdAt))
                            .font(.system(size: AppFontSize.xs))
                            .foregroundColor(AppColors.foreground.opacity(0.5))
                    }

                    Text(notification.message)
                        .font(.system(size: AppFontSize.sm))
                        .foregroundColor(AppColors.foreground.opacity(0.6))
                        .lineLimit(2)
                }
            }
            .padding(.horizontal, AppSpacing.md)
            .padding(.vertical, AppSpacing.md)
            .frame(maxWidth: .infinity)
        }
        .glassCard()
        .clipShape(RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous))
        .shadow(
            color: notification.isRead ? .clear : color.opacity(0.35),
            radius: 12,
            x: 0,
            y: 0
        )
        .opacity(notification.isRead ? 0.7 : 1.0)
        .contentShape(Rectangle())
    }
}

// MARK: - Notification Detail View

struct NotificationDetailView: View {
    @EnvironmentObject var navigationRouter: NavigationRouter
    let notification: AppNotification

    @State private var isDeleting = false
    @State private var deleteError: String? = nil
    @State private var productName: String? = nil

    private var color: Color    { typeColor(for: notification.notificationType) }
    private var iconName: String { typeIconName(for: notification.notificationType) }

    var body: some View {
        ZStack {
            Color("AppBackground").ignoresSafeArea()

            ScrollView {
                VStack(spacing: 0) {
                    // Mini gradient hero
                    ZStack {
                        LinearGradient(
                            colors: [color.opacity(0.6), color.opacity(0)],
                            startPoint: .top,
                            endPoint: .bottom
                        )

                        Image(systemName: iconName)
                            .font(.system(size: 48))
                            .foregroundColor(.white)
                            .shadow(color: color.opacity(0.5), radius: 16)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 140)

                    VStack(alignment: .leading, spacing: AppSpacing.lg) {
                        // Type badge
                        Text(notification.notificationType.uppercased())
                            .font(.system(size: AppFontSize.xs, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, AppSpacing.md)
                            .padding(.vertical, AppSpacing.xs)
                            .background(color, in: Capsule())

                        // Full timestamp
                        if notification.createdAt != nil {
                            Text(fullTimestamp(from: notification.createdAt))
                                .font(.system(size: AppFontSize.sm))
                                .foregroundColor(AppColors.foreground.opacity(0.6))
                        }

                        // Gradient divider
                        LinearGradient(
                            colors: [AppColors.primary.opacity(0.5), .clear],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                        .frame(height: 1)

                        // Title
                        Text(notification.title)
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(AppColors.foreground)

                        // Body
                        Text(notification.message)
                            .font(.body)
                            .foregroundColor(AppColors.foreground)

                        // Product info card
                        if let productId = notification.productId {
                            Button {
                                navigationRouter.navigate(to: .productDetail(productId: productId))
                            } label: {
                                HStack {
                                    VStack(alignment: .leading, spacing: AppSpacing.xs) {
                                        Text("Related Product")
                                            .font(.system(size: AppFontSize.xs))
                                            .foregroundColor(AppColors.foreground.opacity(0.5))
                                        Text(productName ?? "Product #\(productId)")
                                            .font(.system(size: AppFontSize.base, weight: .medium))
                                            .foregroundColor(AppColors.foreground)
                                    }
                                    Spacer()
                                    Text("View Product →")
                                        .font(.system(size: AppFontSize.sm, weight: .semibold))
                                        .foregroundColor(AppColors.primary)
                                }
                                .padding(AppSpacing.lg)
                                .glassCard()
                            }
                            .buttonStyle(.plain)
                        }

                        // Action buttons
                        VStack(spacing: AppSpacing.sm) {
                            if let productId = notification.productId {
                                Button {
                                    navigationRouter.navigate(to: .productDetail(productId: productId))
                                } label: {
                                    Text("View Product")
                                        .font(.system(size: AppFontSize.base, weight: .semibold))
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, AppSpacing.md)
                                        .background(
                                            AppGradients.brand,
                                            in: RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous)
                                        )
                                }
                                .buttonStyle(.plain)
                            }

                            Button {
                                Task { await performDelete() }
                            } label: {
                                HStack(spacing: AppSpacing.sm) {
                                    if isDeleting {
                                        ProgressView()
                                            .tint(AppColors.destructive)
                                            .scaleEffect(0.8)
                                    }
                                    Text("Delete")
                                        .font(.system(size: AppFontSize.base, weight: .semibold))
                                        .foregroundColor(AppColors.destructive)
                                }
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, AppSpacing.md)
                                .overlay(
                                    RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous)
                                        .stroke(AppColors.destructive, lineWidth: 1.5)
                                )
                            }
                            .buttonStyle(.plain)
                            .disabled(isDeleting)
                        }
                    }
                    .padding(AppSpacing.lg)
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
        }
        .navigationTitle(notification.title)
        .navigationBarTitleDisplayMode(.inline)
        .task {
            guard let productId = notification.productId else { return }
            if let product = try? await ProductRepository().getProduct(id: productId) {
                productName = product.name
            }
        }
        .alert("Error", isPresented: Binding<Bool>(
            get: { deleteError != nil },
            set: { if !$0 { deleteError = nil } }
        )) {
            Button("OK") { deleteError = nil }
        } message: {
            Text(deleteError ?? "")
        }
    }

    private func performDelete() async {
        isDeleting = true
        do {
            try await NotificationRepository().deleteNotification(notificationId: notification.id)
            NotificationCenter.default.post(name: .notificationDeleted, object: notification.id)
            navigationRouter.pop()
        } catch {
            isDeleting = false
            deleteError = error.localizedDescription
        }
    }
}

// MARK: - ViewModel
@MainActor
class NotificationsViewModel: ObservableObject {
    @Published var notifications: [AppNotification] = []
    @Published var unreadCount = 0
    @Published var isLoading = false
    @Published var error: String?

    private let repository: NotificationRepositoryProtocol

    init(repository: NotificationRepositoryProtocol = NotificationRepository()) {
        self.repository = repository
    }

    private func pushBadgeUpdate() {
        NotificationCenter.default.post(
            name: .updateBadgeCount,
            object: nil,
            userInfo: ["count": unreadCount]
        )
    }

    private func rollbackAfterFailure(_ originalError: Error) async {
        await loadNotifications()
        if self.error == nil {
            self.error = originalError.localizedDescription
        }
    }

    func loadNotifications() async {
        isLoading = true
        error = nil
        defer { isLoading = false }

        do {
            let fetchedNotifications = try await repository.getNotifications()
            let fetchedUnreadCount: Int
            do {
                fetchedUnreadCount = try await repository.getUnreadCount()
            } catch is CancellationError {
                throw CancellationError()
            } catch {
                fetchedUnreadCount = fetchedNotifications.filter { !$0.isRead }.count
            }

            notifications = fetchedNotifications
            unreadCount = fetchedUnreadCount
            pushBadgeUpdate()
        } catch is CancellationError {
            // Ignore cancellation errors
            return
        } catch {
            self.error = error.localizedDescription
        }
    }

    func markAsRead(notificationId: Int) async {
        // Optimistic update
        if let index = notifications.firstIndex(where: { $0.id == notificationId }) {
            if !notifications[index].isRead {
                notifications[index].isRead = true
                unreadCount = max(0, unreadCount - 1)
                pushBadgeUpdate()
            }
        }

        do {
            try await repository.markAsRead(notificationId: notificationId)
        } catch is CancellationError {
            // Ignore cancellation errors
            return
        } catch {
            await rollbackAfterFailure(error)
        }
    }

    func markAllAsRead() async {
        // Optimistic update
        for i in notifications.indices {
            notifications[i].isRead = true
        }
        unreadCount = 0
        pushBadgeUpdate()

        do {
            try await repository.markAllAsRead()
        } catch is CancellationError {
            // Ignore cancellation errors
            return
        } catch {
            await rollbackAfterFailure(error)
        }
    }

    func deleteNotification(notificationId: Int) async {
        // Optimistic update
        let wasUnread = notifications.first(where: { $0.id == notificationId })?.isRead == false
        notifications.removeAll { $0.id == notificationId }
        if wasUnread {
            unreadCount = max(0, unreadCount - 1)
            pushBadgeUpdate()
        }

        do {
            try await repository.deleteNotification(notificationId: notificationId)
        } catch is CancellationError {
            // Ignore cancellation errors
            return
        } catch {
            await rollbackAfterFailure(error)
        }
    }

    func deleteAll() async {
        // Optimistic update
        notifications.removeAll()
        unreadCount = 0
        pushBadgeUpdate()

        do {
            try await repository.deleteAllNotifications()
        } catch is CancellationError {
            // Ignore cancellation errors
            return
        } catch {
            await rollbackAfterFailure(error)
        }
    }

}

#Preview {
    NavigationStack {
        NotificationsView()
            .environmentObject(NavigationRouter())
    }
}
