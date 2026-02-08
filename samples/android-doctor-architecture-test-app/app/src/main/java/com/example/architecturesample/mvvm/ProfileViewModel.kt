package com.example.architecturesample.mvvm

import android.content.Context
import androidx.lifecycle.ViewModel
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class ProfileViewModel(private val context: Context) : ViewModel() {
    fun loadProfile(): Response<String> {
        val cacheFile = File(context.filesDir, "profile.cache")
        if (cacheFile.exists()) {
            cacheFile.readText()
        } else {
            cacheFile.writeText("cached")
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ProfileService::class.java)
        return service.fetchProfile().execute()
    }

    interface ProfileService {
        fun fetchProfile(): retrofit2.Call<String>
    }
}
