package com.example.architecturesample.feature.chat

class ChatReducer {
    fun reduce(state: ChatState, action: ChatAction): ChatState {
        return when (action) {
            is ChatAction.AddMessage -> {
                state.messages.add(action.message)
                state.unreadCount += 1
                state
            }
            ChatAction.Clear -> {
                state.messages.clear()
                state.unreadCount = 0
                state
            }
        }
    }
}
