package com.example.architecturesample.feature.chat

interface ChatContract {
    interface View {
        fun showMessage(message: String)
    }

    interface Presenter {
        fun attach()
    }
}
