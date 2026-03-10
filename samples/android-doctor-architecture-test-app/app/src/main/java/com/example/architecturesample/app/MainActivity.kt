package com.example.architecturesample.app

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.architecturesample.core.data.UserRepository
import com.example.architecturesample.feature.chat.ChatPresenter
import com.example.architecturesample.legacy.LegacySyncService
import okhttp3.OkHttpClient

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)
        textView.text = "Android Doctor sandbox"
        setContentView(textView)

        val repository = UserRepository()
        val oldNetworkClient = OkHttpClient()
        val users = repository.fetchUsers().filter { it.isNotBlank() }.map { it.uppercase() }

        val viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        viewModel.loadDashboard(users)

        val presenter = ChatPresenter(repository)
        presenter.attachView(textView)

        LegacySyncService().sync(oldNetworkClient, users)

        setContent {
            DashboardScreen(viewModel.state.value)
        }
    }
}
