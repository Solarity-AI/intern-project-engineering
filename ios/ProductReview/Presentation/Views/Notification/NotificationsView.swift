//
//  NotificationsView.swift
//  ProductReview
//
//  Notifications list screen
//

import SwiftUI

struct NotificationsView: View {
    @EnvironmentObject var navigationRouter: NavigationRouter
    @StateObject private var viewModel = NotificationsViewModel()

    var body: some View {
        Group {
            if viewModel.notifications.isEmpty && !viewModel.isLoading {
                // Empty state
                VStack(spacing: 16) {
                    Image(systemName: "bell.slash")
                        .font(.system(size: 60))
                        .foregroundColor(.secondary)
                    Text("No notifications")
                        .font(.title2)
                        .fontWeight(.medium)
                    Text("You're all caught up!")
                        .font(.body)
                        .foregroundColor(.secondary)
                }
                .padding()
            } else {
                List {
                    ForEach(viewModel.notifications) { notification in
                        NotificationRow(notification: notification)
                            .onTapGesture {
                                Task {
                                    await viewModel.markAsRead(notificationId: notification.id)
                                }
                                navigationRouter.navigate(to: .notificationDetail(notificationId: notification.id))
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
            }
        }
        .navigationTitle("Notifications")
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
            }
        }
        .refreshable {
            await viewModel.loadNotifications()
        }
        .task {
            await viewModel.loadNotifications()
        }
        .alert("Error", isPresented: .constant(viewModel.error != nil)) {
            Button("OK") { viewModel.error = nil }
        } message: {
            Text(viewModel.error ?? "")
        }
    }
}

// MARK: - Notification Row
struct NotificationRow: View {
    let notification: AppNotification

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // Unread indicator
            Circle()
                .fill(notification.isRead ? Color.clear : Color.blue)
                .frame(width: 8, height: 8)
                .padding(.top, 6)

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(notification.title)
                        .font(.subheadline)
                        .fontWeight(notification.isRead ? .regular : .semibold)
                    Spacer()
                    Text(notification.formattedDate)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }

                Text(notification.message)
                    .font(.body)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
            }
        }
        .padding(.vertical, 4)
        .contentShape(Rectangle())
    }
}

// MARK: - Notification Detail View
struct NotificationDetailView: View {
    @EnvironmentObject var navigationRouter: NavigationRouter
    let notificationId: Int

    @State private var notification: AppNotification?

    var body: some View {
        Group {
            if let notification = notification {
                VStack(alignment: .leading, spacing: 16) {
                    Text(notification.title)
                        .font(.title2)
                        .fontWeight(.bold)

                    Text(notification.formattedDate)
                        .font(.caption)
                        .foregroundColor(.secondary)

                    Divider()

                    Text(notification.message)
                        .font(.body)

                    if let productId = notification.productId {
                        Button {
                            navigationRouter.navigate(to: .productDetail(productId: productId))
                        } label: {
                            Label("View Product", systemImage: "arrow.right.circle")
                        }
                        .buttonStyle(.borderedProminent)
                    }

                    Spacer()
                }
                .padding()
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Notification")
        .navigationBarTitleDisplayMode(.inline)
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

    func loadNotifications() async {
        isLoading = true
        error = nil

        do {
            notifications = try await repository.getNotifications()
            unreadCount = try await repository.getUnreadCount()
        } catch {
            self.error = error.localizedDescription
        }

        isLoading = false
    }

    func markAsRead(notificationId: Int) async {
        // Optimistic update
        if let index = notifications.firstIndex(where: { $0.id == notificationId }) {
            notifications[index].isRead = true
            unreadCount = max(0, unreadCount - 1)
        }

        do {
            try await repository.markAsRead(notificationId: notificationId)
        } catch {
            await loadNotifications()
            self.error = error.localizedDescription
        }
    }

    func markAllAsRead() async {
        // Optimistic update
        for i in notifications.indices {
            notifications[i].isRead = true
        }
        unreadCount = 0

        do {
            try await repository.markAllAsRead()
        } catch {
            await loadNotifications()
            self.error = error.localizedDescription
        }
    }

    func deleteNotification(notificationId: Int) async {
        // Optimistic update
        let wasUnread = notifications.first(where: { $0.id == notificationId })?.isRead == false
        notifications.removeAll { $0.id == notificationId }
        if wasUnread { unreadCount = max(0, unreadCount - 1) }

        do {
            try await repository.deleteNotification(notificationId: notificationId)
        } catch {
            await loadNotifications()
            self.error = error.localizedDescription
        }
    }

    func deleteAll() async {
        // Optimistic update
        notifications.removeAll()
        unreadCount = 0

        do {
            try await repository.deleteAllNotifications()
        } catch {
            await loadNotifications()
            self.error = error.localizedDescription
        }
    }
}

#Preview {
    NavigationStack {
        NotificationsView()
            .environmentObject(NavigationRouter())
    }
}
