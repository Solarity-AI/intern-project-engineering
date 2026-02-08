package com.productreview.app.ui.screens.aiassistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productreview.app.core.FWResult
import com.productreview.app.core.logging.*
import com.productreview.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val options: List<String>? = null,
    val timestamp: String,
    val hideIcon: Boolean = false
)

enum class MessageRole { USER, ASSISTANT }

data class AIAssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val waitingForMore: Boolean = false,
    val lastActiveMessageId: String = "",
    val isExiting: Boolean = false
)

val QUESTIONS = listOf(
    "How many reviews are there?",
    "What do customers say about quality?",
    "When were most reviews posted?",
    "What are the main complaints?",
    "Any common praise patterns?"
)

@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIAssistantUiState())
    val uiState: StateFlow<AIAssistantUiState> = _uiState.asStateFlow()

    private var productId: String = ""
    private var productName: String = ""

    fun initialize(productId: String, productName: String) {
        this.productId = productId
        this.productName = productName

        val welcomeId = System.currentTimeMillis().toString()
        val welcomeMessage = ChatMessage(
            id = welcomeId,
            role = MessageRole.ASSISTANT,
            content = "Hi! I'm your AI assistant for $productName. I can help you understand customer reviews better.",
            options = QUESTIONS,
            timestamp = getCurrentTime()
        )

        _uiState.update {
            it.copy(messages = listOf(welcomeMessage), lastActiveMessageId = welcomeId)
        }
    }

    fun selectQuestion(question: String, messageId: String) {
        val state = _uiState.value
        if (state.isLoading || state.isProcessing || state.isExiting) return
        if (messageId != state.lastActiveMessageId) return

        _uiState.update { it.copy(isProcessing = true, isLoading = true) }
        consumeOptions(messageId)

        val userMessage = ChatMessage(
            id = System.currentTimeMillis().toString(),
            role = MessageRole.USER,
            content = question,
            timestamp = getCurrentTime()
        )
        _uiState.update { it.copy(messages = it.messages + userMessage) }

        viewModelScope.launch {
            when (val result = productRepository.chatWithAI(productId, question)) {
                is FWResult.Success -> {
                    val answerId = (System.currentTimeMillis() + 1).toString()
                    val answerMessage = ChatMessage(
                        id = answerId,
                        role = MessageRole.ASSISTANT,
                        content = result.value.answer,
                        timestamp = getCurrentTime()
                    )

                    val followupId = (System.currentTimeMillis() + 2).toString()
                    val followupMessage = ChatMessage(
                        id = followupId,
                        role = MessageRole.ASSISTANT,
                        content = "Do you have more questions?",
                        options = listOf("Yes", "No"),
                        timestamp = getCurrentTime(),
                        hideIcon = true
                    )

                    _uiState.update {
                        it.copy(
                            messages = it.messages + answerMessage + followupMessage,
                            isLoading = false, isProcessing = false,
                            waitingForMore = true, lastActiveMessageId = followupId
                        )
                    }
                }
                is FWResult.Failure -> {
                    val errorMessage = ChatMessage(
                        id = (System.currentTimeMillis() + 1).toString(),
                        role = MessageRole.ASSISTANT,
                        content = "Sorry, I had trouble connecting to the server. Please try again.",
                        timestamp = getCurrentTime()
                    )

                    val retryId = (System.currentTimeMillis() + 2).toString()
                    val retryMessage = ChatMessage(
                        id = retryId,
                        role = MessageRole.ASSISTANT,
                        content = "Do you have more questions?",
                        options = listOf("Yes", "No"),
                        timestamp = getCurrentTime(),
                        hideIcon = true
                    )

                    _uiState.update {
                        it.copy(
                            messages = it.messages + errorMessage + retryMessage,
                            isLoading = false, isProcessing = false,
                            waitingForMore = true, lastActiveMessageId = retryId
                        )
                    }
                }
            }
        }
    }

    fun handleMoreQuestions(choice: String, messageId: String, onExit: () -> Unit) {
        val state = _uiState.value
        if (state.isProcessing || state.isExiting) return
        if (messageId != state.lastActiveMessageId) return

        _uiState.update { it.copy(isProcessing = true, waitingForMore = false) }
        consumeOptions(messageId)

        val userMessage = ChatMessage(
            id = System.currentTimeMillis().toString(),
            role = MessageRole.USER,
            content = choice,
            timestamp = getCurrentTime()
        )
        _uiState.update { it.copy(messages = it.messages + userMessage) }

        if (choice == "No") {
            _uiState.update { it.copy(isExiting = true) }

            viewModelScope.launch {
                delay(300)
                val exitMessage = ChatMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    role = MessageRole.ASSISTANT,
                    content = "Thank you for using AI Assistant! Feel free to come back anytime. 👋",
                    timestamp = getCurrentTime()
                )
                _uiState.update {
                    it.copy(messages = it.messages + exitMessage, lastActiveMessageId = "", isProcessing = false)
                }
                delay(3000)
                onExit()
            }
        } else {
            viewModelScope.launch {
                delay(300)
                val newId = (System.currentTimeMillis() + 1).toString()
                val restartMessage = ChatMessage(
                    id = newId,
                    role = MessageRole.ASSISTANT,
                    content = "Great! What would you like to know?",
                    options = QUESTIONS,
                    timestamp = getCurrentTime()
                )
                _uiState.update {
                    it.copy(messages = it.messages + restartMessage, lastActiveMessageId = newId, isProcessing = false)
                }
            }
        }
    }

    private fun consumeOptions(messageId: String) {
        _uiState.update { state ->
            state.copy(messages = state.messages.map { if (it.id == messageId) it.copy(options = null) else it })
        }
    }

    private fun getCurrentTime(): String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
}
