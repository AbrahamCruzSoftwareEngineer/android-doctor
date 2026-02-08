package com.example.architecturesample.mvp

interface LoginContract {
    interface View {
        fun showLoginSuccess(user: String)
        fun showLoginError(error: String)
    }

    interface Presenter {
        fun handleLogin(username: String, password: String)
    }
}
