package com.example.architecturesample.feature.chat

data class ChatState(
    var messages: MutableList<String> = mutableListOf(),
    var unreadCount: Int = 0
)
