package com.example.architecturesample.legacy

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.architecturesample.core.data.UserRepository
import okhttp3.OkHttpClient

class LegacyDashboardActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = TextView(this)
        title.text = "Legacy Dashboard"
        val button = Button(this)
        button.text = "Sync"

        val users = UserRepository().fetchUsers()
        val filtered = users.filter { it.startsWith("a") }.map { it.uppercase() }

        val okHttp = OkHttpClient()
        LegacySyncService().sync(okHttp, filtered)

        title.text = "Synced ${filtered.size} users"
    }
}
