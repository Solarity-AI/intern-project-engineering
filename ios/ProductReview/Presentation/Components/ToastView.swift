//
//  ToastView.swift
//  ProductReview
//
//  Toast notification component for feedback messages
//

import SwiftUI

enum ToastType {
    case success
    case error
    case warning
    case info

    var icon: String {
        switch self {
        case .success: return "checkmark.circle.fill"
        case .error: return "xmark.circle.fill"
        case .warning: return "exclamationmark.triangle.fill"
        case .info: return "info.circle.fill"
        }
    }

    var color: Color {
        switch self {
        case .success: return .green
        case .error: return .red
        case .warning: return .orange
        case .info: return .blue
        }
    }
}

struct ToastView: View {
    let message: String
    let type: ToastType
    let actionTitle: String?
    let onAction: (() -> Void)?

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: type.icon)
                .foregroundColor(type.color)
                .font(.title3)

            Text(message)
                .font(.subheadline)
                .foregroundColor(.primary)

            Spacer()

            if let actionTitle, let onAction {
                Button(actionTitle) {
                    onAction()
                }
                .font(.subheadline.weight(.semibold))
                .buttonStyle(.plain)
                .foregroundColor(type.color)
            }
        }
        .padding()
        .background(.ultraThinMaterial)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.15), radius: 8, x: 0, y: 4)
        .padding(.horizontal)
    }
}

// MARK: - Toast Modifier
struct ToastModifier: ViewModifier {
    @Binding var isPresented: Bool
    let message: String
    let type: ToastType
    let duration: Double
    let actionTitle: String?
    let onAction: (() -> Void)?

    func body(content: Content) -> some View {
        ZStack {
            content

            VStack {
                if isPresented {
                    ToastView(
                        message: message,
                        type: type,
                        actionTitle: actionTitle,
                        onAction: {
                            withAnimation {
                                isPresented = false
                            }
                            onAction?()
                        }
                    )
                        .transition(.move(edge: .top).combined(with: .opacity))
                        .onAppear {
                            DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
                                withAnimation {
                                    isPresented = false
                                }
                            }
                        }
                }
                Spacer()
            }
            .animation(.spring(), value: isPresented)
        }
    }
}

extension View {
    func toast(
        isPresented: Binding<Bool>,
        message: String,
        type: ToastType = .info,
        duration: Double = 3.0,
        actionTitle: String? = nil,
        onAction: (() -> Void)? = nil
    ) -> some View {
        modifier(
            ToastModifier(
                isPresented: isPresented,
                message: message,
                type: type,
                duration: duration,
                actionTitle: actionTitle,
                onAction: onAction
            )
        )
    }
}

#Preview {
    VStack(spacing: 20) {
        ToastView(message: "Item added to wishlist", type: .success, actionTitle: nil, onAction: nil)
        ToastView(message: "Failed to load data", type: .error, actionTitle: nil, onAction: nil)
        ToastView(message: "Check your connection", type: .warning, actionTitle: nil, onAction: nil)
        ToastView(message: "New products available", type: .info, actionTitle: "Undo", onAction: {})
    }
    .padding()
}
