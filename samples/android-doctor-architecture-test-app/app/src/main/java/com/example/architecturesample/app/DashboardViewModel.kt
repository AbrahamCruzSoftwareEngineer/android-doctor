package com.example.architecturesample.app

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {
    val state = mutableStateOf("loading")

    fun loadDashboard(users: List<String>) {
        state.value = "Users loaded: ${users.size}"
    }
}
