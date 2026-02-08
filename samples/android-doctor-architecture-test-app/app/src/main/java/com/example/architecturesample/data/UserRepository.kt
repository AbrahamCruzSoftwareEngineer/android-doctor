package com.example.architecturesample.data

import android.widget.TextView
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserRepository(private val statusView: TextView) {
    fun fetchUser(): Response<String> {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        statusView.text = "Loading..."
        val service = retrofit.create(UserService::class.java)
        return service.user().execute()
    }

    interface UserService {
        fun user(): retrofit2.Call<String>
    }
}
