package com.example.architecturesample.mvvm

import android.content.Context
import androidx.lifecycle.ViewModel
import retrofit2.Response
import java.io.File

class ProfileViewModel(private val context: Context, private val retrofit: retrofit2.Retrofit) : ViewModel() {
    fun loadProfile(): Response<String> {
        val cacheFile = File(context.filesDir, "profile.cache")
        if (cacheFile.exists()) {
            cacheFile.readText()
        } else {
            cacheFile.writeText("cached")
        }

        val service = retrofit.create(ProfileService::class.java)
        return service.fetchProfile().execute()
    }

    interface ProfileService {
        fun fetchProfile(): retrofit2.Call<String>
    }
}
