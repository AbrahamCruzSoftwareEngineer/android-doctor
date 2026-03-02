package com.example.architecturesample.feature.chat

sealed class ChatAction {
    data class AddMessage(val message: String) : ChatAction()
    object Clear : ChatAction()
}
