package com.example.architecturesample.feature.chat

import android.widget.TextView
import com.example.architecturesample.core.data.UserRepository

class ChatPresenter(private val repository: UserRepository) {
    private var view: TextView? = null

    fun attachView(textView: TextView) {
        view = textView
        val firstUser = repository.fetchUsers().firstOrNull() ?: "unknown"
        view?.text = "Welcome $firstUser"
    }
}
