//
//  AIAssistantView.swift
//  ProductReview
//
//  AI Chat assistant for product review analysis
//

import SwiftUI

struct AIAssistantView: View {
    @EnvironmentObject var navigationRouter: NavigationRouter
    @Environment(\.dismiss) private var dismiss

    let productId: Int
    let productName: String

    @StateObject private var viewModel: AIAssistantViewModel
    @State private var inputText = ""
    @State private var reviewCount = 0
    @State private var dotOpacity: [Double] = [1, 1, 1]
    @FocusState private var isInputFocused: Bool

    private let repository: ProductRepositoryProtocol

    init(
        productId: Int,
        productName: String,
        repository: ProductRepositoryProtocol = ProductRepository()
    ) {
        self.productId = productId
        self.productName = productName
        self.repository = repository
        _viewModel = StateObject(wrappedValue: AIAssistantViewModel(productId: productId, repository: repository))
    }

    private let preDefinedQuestions = [
        "How many reviews does this product have?",
        "What do customers say about quality?",
        "Are there any complaints?",
        "What are the main praises?"
    ]

    private let questionColumns = [
        GridItem(.flexible(), spacing: AppSpacing.sm),
        GridItem(.flexible(), spacing: AppSpacing.sm)
    ]

    private var followupOptions: [String] {
        viewModel.messages.reversed().compactMap(\.options).first ?? ["Yes", "No"]
    }

    private func handleBack() {
        if navigationRouter.path.isEmpty {
            dismiss()
        } else {
            navigationRouter.pop()
        }
    }

    var body: some View {
        ZStack {
            AppColors.background
                .ignoresSafeArea()

            VStack(spacing: 0) {
                headerView

                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: AppSpacing.md) {
                            ForEach(viewModel.messages) { message in
                                MessageBubble(message: message)
                                    .id(message.id)
                            }

                            if viewModel.isLoading {
                                TypingIndicatorBubble(dotOpacity: dotOpacity)
                                    .id("typing-indicator")
                            }

                            if viewModel.messages.isEmpty || viewModel.waitingForMore {
                                quickQuestionsView
                                    .id("quick-questions")
                            }
                        }
                        .padding(.horizontal, AppSpacing.lg)
                        .padding(.vertical, AppSpacing.lg)
                        .containerRelativeFrame(.horizontal, alignment: .leading)
                    }
                    .background {
                        DecorativeOrbsView()
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .onChange(of: viewModel.messages.count) { _, _ in
                        if let lastId = viewModel.messages.last?.id {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                proxy.scrollTo(lastId, anchor: .bottom)
                            }
                        }
                    }
                    .onChange(of: viewModel.isLoading) { _, isLoading in
                        guard isLoading else { return }
                        withAnimation(.easeInOut(duration: 0.2)) {
                            proxy.scrollTo("typing-indicator", anchor: .bottom)
                        }
                    }
                    .onChange(of: viewModel.waitingForMore) { _, waiting in
                        guard waiting else { return }
                        withAnimation(.easeInOut(duration: 0.2)) {
                            proxy.scrollTo("quick-questions", anchor: .bottom)
                        }
                    }
                }

                inputBar
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        }
        .overlay(alignment: .topLeading) {
            Button {
                handleBack()
            } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundStyle(Color.primary)
                    .frame(width: 40, height: 40)
                    .glassCard(AppGlass.card, cornerRadius: AppRadius.full)
                    .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 2)
            }
            .buttonStyle(.plain)
            .padding(.horizontal, AppSpacing.lg)
            .padding(.top, AppSpacing.sm)
            .safeAreaPadding(.top)
        }
        .navigationBarBackButtonHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .toolbarBackground(.hidden, for: .navigationBar)
        .alert("Error", isPresented: Binding(get: { viewModel.error != nil }, set: { if !$0 { viewModel.error = nil } })) {
            Button("OK") { viewModel.error = nil }
        } message: {
            Text(viewModel.error ?? "")
        }
        .task {
            await loadReviewCount()
        }
        .task(id: viewModel.isLoading) {
            await animateTypingDots()
        }
    }

    @ViewBuilder
    private var headerView: some View {
        VStack(spacing: AppSpacing.xs) {
            Image(systemName: "sparkles")
                .font(.system(size: 32, weight: .semibold))
                .foregroundStyle(.white)

            Text("AI Assistant")
                .font(.system(size: AppFontSize.xl, weight: .bold))
                .foregroundStyle(.white)

            Text(productName)
                .font(.system(size: AppFontSize.sm, weight: .medium))
                .foregroundStyle(Color.white.opacity(0.7))
                .multilineTextAlignment(.center)
                .lineLimit(1)

            Text("Based on \(reviewCount) reviews")
                .font(.system(size: AppFontSize.xs, weight: .medium))
                .foregroundStyle(Color.white.opacity(0.55))
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, AppSpacing.lg)
        .padding(.top, AppSpacing.sm)
        .padding(.bottom, AppSpacing.lg)
        .safeAreaPadding(.top)
        .background(AppGradients.ai)
        .glow(AppGlow.ai)
    }

    @ViewBuilder
    private var quickQuestionsView: some View {
        VStack(alignment: .leading, spacing: AppSpacing.sm) {
            if viewModel.waitingForMore {
                HStack(spacing: AppSpacing.sm) {
                    ForEach(followupOptions, id: \.self) { option in
                        Button {
                            viewModel.handleFollowupChoice(option)
                        } label: {
                            OptionButtonLabel(text: option)
                        }
                        .buttonStyle(.plain)
                        .frame(maxWidth: .infinity)
                        .opacity(viewModel.isLoading ? 0.5 : 1)
                        .disabled(viewModel.isLoading)
                    }
                }
                .frame(maxWidth: .infinity)
            } else {
                Text("Ask about \(productName)")
                    .font(.system(size: AppFontSize.sm, weight: .semibold))
                    .foregroundStyle(AppColors.foreground.opacity(0.7))

                LazyVGrid(columns: questionColumns, spacing: AppSpacing.sm) {
                    ForEach(preDefinedQuestions, id: \.self) { question in
                        Button {
                            Task {
                                await viewModel.sendMessage(question)
                            }
                        } label: {
                            OptionButtonLabel(text: question)
                        }
                        .buttonStyle(.plain)
                        .opacity(viewModel.isLoading ? 0.5 : 1)
                        .disabled(viewModel.isLoading)
                    }
                }
                .frame(maxWidth: .infinity)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.bottom, AppSpacing.sm)
    }

    @ViewBuilder
    private var inputBar: some View {
        HStack(spacing: AppSpacing.sm) {
            TextField(
                "",
                text: $inputText,
                prompt: Text("Ask about this product...")
                    .foregroundStyle(AppColors.foreground.opacity(0.4))
            )
            .focused($isInputFocused)
            .disabled(viewModel.isLoading)
            .foregroundStyle(AppColors.foreground)
            .onSubmit {
                sendMessage()
            }

            Button {
                sendMessage()
            } label: {
                Image(systemName: "arrow.up")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(.white)
                    .frame(width: 36, height: 36)
                    .background(AppGradients.brand, in: Circle())
            }
            .buttonStyle(.plain)
            .opacity(viewModel.isLoading ? 0.4 : 1)
            .disabled(inputText.isEmpty || viewModel.isLoading)
        }
        .padding(.horizontal, AppSpacing.md)
        .padding(.vertical, AppSpacing.sm)
        .glassCard(AppGlass.subtle, cornerRadius: AppRadius.x2l)
        .padding(.horizontal, AppSpacing.lg)
        .padding(.top, AppSpacing.sm)
        .padding(.bottom, AppSpacing.md)
        .safeAreaPadding(.bottom, AppSpacing.xs)
    }

    private func sendMessage() {
        guard !inputText.isEmpty else { return }
        let message = inputText
        inputText = ""
        Task {
            await viewModel.sendMessage(message)
        }
    }

    private func loadReviewCount() async {
        guard reviewCount == 0 else { return }

        do {
            let product = try await repository.getProduct(id: productId)
            reviewCount = product.reviewCount
        } catch {
            reviewCount = 0
        }
    }

    private func animateTypingDots() async {
        guard viewModel.isLoading else {
            dotOpacity = [1, 1, 1]
            return
        }

        while viewModel.isLoading {
            for activeIndex in 0..<3 {
                withAnimation(.easeInOut(duration: 0.5)) {
                    for index in 0..<3 {
                        dotOpacity[index] = index == activeIndex ? 1.0 : 0.3
                    }
                }

                try? await Task.sleep(nanoseconds: 170_000_000)

                if !viewModel.isLoading {
                    break
                }
            }
        }

        dotOpacity = [1, 1, 1]
    }
}

private struct DecorativeOrbsView: View {
    var body: some View {
        ZStack {
            RadialGradient(
                colors: [AppColors.aiPurple.opacity(0.08), .clear],
                center: .center,
                startRadius: 0,
                endRadius: 280
            )
            .frame(width: 560, height: 560)
            .offset(x: 180, y: -260)

            RadialGradient(
                colors: [AppColors.primary.opacity(0.05), .clear],
                center: .center,
                startRadius: 0,
                endRadius: 220
            )
            .frame(width: 440, height: 440)
            .offset(x: -180, y: 300)
        }
        .allowsHitTesting(false)
    }
}

private struct OptionButtonLabel: View {
    let text: String

    var body: some View {
        HStack {
            Text(text)
                .font(.system(size: AppFontSize.sm, weight: .medium))
                .foregroundStyle(AppColors.foreground)
                .multilineTextAlignment(.leading)
                .lineLimit(2)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.leading, AppSpacing.md)
        .padding(.trailing, AppSpacing.sm)
        .padding(.vertical, AppSpacing.md)
        .frame(maxWidth: .infinity, minHeight: 58, alignment: .leading)
        .glassCard(AppGlass.subtle, cornerRadius: AppRadius.lg)
        .overlay(alignment: .leading) {
            Rectangle()
                .fill(AppColors.primary)
                .frame(width: 2)
                .padding(.vertical, 10)
        }
    }
}

private struct TypingIndicatorBubble: View {
    let dotOpacity: [Double]

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: AppSpacing.sm) {
                    ForEach(0..<3, id: \.self) { index in
                        Circle()
                            .fill(AppColors.aiPurple)
                            .frame(width: 8, height: 8)
                            .opacity(dotOpacity[index])
                    }
                }
                .padding(.horizontal, AppSpacing.md)
                .padding(.vertical, AppSpacing.md)
                .background(alignment: .leading) {
                    Rectangle()
                        .fill(AppColors.aiPurple)
                        .frame(width: 3)
                }
                .glassCard(AppGlass.card, cornerRadius: AppRadius.xl)
                .clipShape(RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous))
            }

            Spacer(minLength: 56)
        }
    }
}

// MARK: - Message Model
struct ChatMessage: Identifiable {
    let id = UUID()
    let content: String
    let isUser: Bool
    let timestamp: Date
    let options: [String]?

    init(content: String, isUser: Bool, timestamp: Date, options: [String]? = nil) {
        self.content = content
        self.isUser = isUser
        self.timestamp = timestamp
        self.options = options
    }

    var formattedTime: String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: timestamp)
    }
}

// MARK: - Message Bubble
struct MessageBubble: View {
    let message: ChatMessage

    var body: some View {
        HStack {
            if message.isUser {
                Spacer(minLength: 56)
            }

            VStack(alignment: message.isUser ? .trailing : .leading, spacing: 4) {
                if message.isUser {
                    Text("You")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundStyle(AppColors.foreground.opacity(0.5))
                }

                if message.isUser {
                    Text(message.content)
                        .font(.system(size: 15, weight: .regular))
                        .foregroundStyle(.white)
                        .padding(.horizontal, AppSpacing.md)
                        .padding(.vertical, 10)
                        .background {
                            UnevenRoundedRectangle(
                                cornerRadii: RectangleCornerRadii(
                                    topLeading: AppRadius.xl,
                                    bottomLeading: AppRadius.xl,
                                    bottomTrailing: 4,
                                    topTrailing: AppRadius.xl
                                ),
                                style: .continuous
                            )
                            .fill(AppGradients.brand)
                        }
                } else {
                    HStack(alignment: .top, spacing: AppSpacing.sm) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 20, weight: .semibold))
                            .foregroundStyle(AppColors.aiPurple)

                        Text(message.content)
                            .font(.system(size: 15, weight: .regular))
                            .foregroundStyle(AppColors.foreground)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .padding(.horizontal, AppSpacing.md)
                    .padding(.vertical, 10)
                    .background(alignment: .leading) {
                        Rectangle()
                            .fill(AppColors.aiPurple)
                            .frame(width: 3)
                    }
                    .glassCard(AppGlass.card, cornerRadius: AppRadius.xl)
                    .clipShape(RoundedRectangle(cornerRadius: AppRadius.xl, style: .continuous))
                }

                Text(message.formattedTime)
                    .font(.system(size: 10, weight: .regular))
                    .foregroundStyle(AppColors.foreground.opacity(0.4))
            }

            if !message.isUser {
                Spacer(minLength: 56)
            }
        }
        .frame(maxWidth: .infinity, alignment: message.isUser ? .trailing : .leading)
    }
}

// MARK: - ViewModel
@MainActor
class AIAssistantViewModel: ObservableObject {
    @Published var messages: [ChatMessage] = []
    @Published var isLoading = false
    @Published var error: String?
    @Published var waitingForMore = false

    private let productId: Int
    private let repository: ProductRepositoryProtocol

    init(productId: Int, repository: ProductRepositoryProtocol = ProductRepository()) {
        self.productId = productId
        self.repository = repository
    }

    func sendMessage(_ content: String) async {
        // Add user message
        let userMessage = ChatMessage(content: content, isUser: true, timestamp: Date())
        messages.append(userMessage)

        isLoading = true
        error = nil
        waitingForMore = false

        do {
            let response = try await repository.chatWithAI(productId: productId, question: content)

            // Add AI response
            let aiMessage = ChatMessage(content: response, isUser: false, timestamp: Date())
            messages.append(aiMessage)
        } catch {
            self.error = error.localizedDescription

            // Add error message
            let errorMessage = ChatMessage(
                content: "Sorry, I couldn't process your request. Please try again.",
                isUser: false,
                timestamp: Date()
            )
            messages.append(errorMessage)
        }

        let followupMessage = ChatMessage(
            content: "Do you have more questions?",
            isUser: false,
            timestamp: Date(),
            options: ["Yes", "No"]
        )
        messages.append(followupMessage)
        waitingForMore = true

        isLoading = false
    }

    func handleFollowupChoice(_ choice: String) {
        let userMessage = ChatMessage(content: choice, isUser: true, timestamp: Date())
        messages.append(userMessage)
        waitingForMore = false

        if choice.lowercased() == "yes" {
            let continueMessage = ChatMessage(
                content: "Great! What would you like to know?",
                isUser: false,
                timestamp: Date()
            )
            messages.append(continueMessage)
        } else {
            let closingMessage = ChatMessage(
                content: "Thanks for chatting. Feel free to ask another question anytime.",
                isUser: false,
                timestamp: Date()
            )
            messages.append(closingMessage)
        }
    }
}

#Preview {
    NavigationStack {
        AIAssistantView(productId: 1, productName: "iPhone 15 Pro")
            .environmentObject(NavigationRouter())
    }
}
