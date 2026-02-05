//
//  AIAssistantView.swift
//  ProductReview
//
//  AI Chat assistant for product review analysis
//

import SwiftUI

struct AIAssistantView: View {
    let productId: Int
    let productName: String

    @StateObject private var viewModel: AIAssistantViewModel
    @State private var inputText = ""
    @FocusState private var isInputFocused: Bool

    init(productId: Int, productName: String) {
        self.productId = productId
        self.productName = productName
        _viewModel = StateObject(wrappedValue: AIAssistantViewModel(productId: productId))
    }

    private let preDefinedQuestions = [
        "How many reviews does this product have?",
        "What do customers say about quality?",
        "Are there any complaints?",
        "What are the main praises?"
    ]

    var body: some View {
        VStack(spacing: 0) {
            // Messages
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.messages) { message in
                            MessageBubble(message: message)
                                .id(message.id)
                        }

                        if viewModel.isLoading {
                            HStack {
                                ProgressView()
                                    .padding()
                                Spacer()
                            }
                            .padding(.horizontal)
                        }
                    }
                    .padding()
                }
                .onChange(of: viewModel.messages.count) { _, _ in
                    if let lastId = viewModel.messages.last?.id {
                        withAnimation {
                            proxy.scrollTo(lastId, anchor: .bottom)
                        }
                    }
                }
            }

            // Quick questions
            if viewModel.messages.isEmpty {
                VStack(spacing: 12) {
                    Text("Ask about \(productName)")
                        .font(.headline)
                        .foregroundColor(.secondary)

                    ForEach(preDefinedQuestions, id: \.self) { question in
                        Button {
                            Task {
                                await viewModel.sendMessage(question)
                            }
                        } label: {
                            Text(question)
                                .font(.subheadline)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(Color.blue.opacity(0.1))
                                .cornerRadius(20)
                        }
                        .disabled(viewModel.isLoading)
                    }
                }
                .padding()
            }

            Divider()

            // Input area
            HStack(spacing: 12) {
                TextField("Ask about this product...", text: $inputText)
                    .textFieldStyle(.roundedBorder)
                    .focused($isInputFocused)
                    .disabled(viewModel.isLoading)
                    .onSubmit {
                        sendMessage()
                    }

                Button {
                    sendMessage()
                } label: {
                    Image(systemName: "arrow.up.circle.fill")
                        .font(.title2)
                        .foregroundColor(inputText.isEmpty || viewModel.isLoading ? .gray : .blue)
                }
                .disabled(inputText.isEmpty || viewModel.isLoading)
            }
            .padding()
            .background(Color(.systemBackground))
        }
        .navigationTitle("AI Assistant")
        .navigationBarTitleDisplayMode(.inline)
        .alert("Error", isPresented: .constant(viewModel.error != nil)) {
            Button("OK") { viewModel.error = nil }
        } message: {
            Text(viewModel.error ?? "")
        }
    }

    private func sendMessage() {
        guard !inputText.isEmpty else { return }
        let message = inputText
        inputText = ""
        Task {
            await viewModel.sendMessage(message)
        }
    }
}

// MARK: - Message Model
struct ChatMessage: Identifiable {
    let id = UUID()
    let content: String
    let isUser: Bool
    let timestamp: Date

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
            if message.isUser { Spacer() }

            VStack(alignment: message.isUser ? .trailing : .leading, spacing: 4) {
                HStack(spacing: 8) {
                    if !message.isUser {
                        Image(systemName: "sparkles")
                            .foregroundColor(.purple)
                    }

                    Text(message.content)
                        .font(.body)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(message.isUser ? Color.blue : Color(.systemGray5))
                .foregroundColor(message.isUser ? .white : .primary)
                .cornerRadius(20)

                Text(message.formattedTime)
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }

            if !message.isUser { Spacer() }
        }
    }
}

// MARK: - ViewModel
@MainActor
class AIAssistantViewModel: ObservableObject {
    @Published var messages: [ChatMessage] = []
    @Published var isLoading = false
    @Published var error: String?

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

        isLoading = false
    }
}

#Preview {
    NavigationStack {
        AIAssistantView(productId: 1, productName: "iPhone 15 Pro")
    }
}
