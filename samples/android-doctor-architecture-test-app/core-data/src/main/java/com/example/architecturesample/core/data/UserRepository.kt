package com.example.architecturesample.core.data

class UserRepository {
    fun fetchUsers(): List<String> {
        return mutableListOf("alice", "bob", "carol")
    }
}
