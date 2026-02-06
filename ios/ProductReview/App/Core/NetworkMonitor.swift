//
//  NetworkMonitor.swift
//  ProductReview
//
//  Network connectivity monitoring
//

import SwiftUI
import Network

@MainActor
class NetworkMonitor: ObservableObject {
    static let shared = NetworkMonitor()

    @Published private(set) var isConnected = true
    @Published private(set) var connectionType: ConnectionType = .unknown

    enum ConnectionType {
        case wifi
        case cellular
        case ethernet
        case unknown
    }

    private var monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "NetworkMonitor")
    private var isMonitoring = false

    private init() {
        startMonitoring()
    }

    func startMonitoring() {
        guard !isMonitoring else { return }

        monitor = NWPathMonitor()
        monitor.pathUpdateHandler = { [weak self] path in
            Task { @MainActor in
                self?.isConnected = path.status == .satisfied
                self?.connectionType = self?.getConnectionType(path) ?? .unknown
            }
        }
        monitor.start(queue: queue)
        isMonitoring = true
    }

    func stopMonitoring() {
        monitor.cancel()
        isMonitoring = false
    }

    private func getConnectionType(_ path: NWPath) -> ConnectionType {
        if path.usesInterfaceType(.wifi) {
            return .wifi
        } else if path.usesInterfaceType(.cellular) {
            return .cellular
        } else if path.usesInterfaceType(.wiredEthernet) {
            return .ethernet
        }
        return .unknown
    }
}

// MARK: - Offline Banner View
struct OfflineBannerView: View {
    @ObservedObject var networkMonitor = NetworkMonitor.shared

    var body: some View {
        if !networkMonitor.isConnected {
            HStack {
                Image(systemName: "wifi.slash")
                Text("No internet connection")
                    .font(.subheadline)
            }
            .foregroundColor(.white)
            .padding(.vertical, 8)
            .frame(maxWidth: .infinity)
            .background(Color.red)
            .transition(.move(edge: .top).combined(with: .opacity))
        }
    }
}

#Preview {
    VStack {
        OfflineBannerView()
        Spacer()
    }
}
