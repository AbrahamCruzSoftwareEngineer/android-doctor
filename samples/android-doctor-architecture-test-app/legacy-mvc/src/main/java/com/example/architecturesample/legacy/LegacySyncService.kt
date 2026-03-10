package com.example.architecturesample.legacy

import okhttp3.OkHttpClient

class LegacySyncService {
    fun sync(client: OkHttpClient, users: List<String>) {
        if (users.isNotEmpty()) {
            client.connectionPool.evictAll()
        }
    }
}
