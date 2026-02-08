package com.example.architecturesample.mvp

import android.util.Log

class LoginPresenter(private val view: LoginContract.View) : LoginContract.Presenter {
    override fun handleLogin(username: String, password: String) {
        if (username == "admin" && password == "password") {
            view.showLoginSuccess(username)
        } else {
            view.showLoginError("Invalid credentials")
            Log.w("LoginPresenter", "Login failed for $username")
        }
    }
}
