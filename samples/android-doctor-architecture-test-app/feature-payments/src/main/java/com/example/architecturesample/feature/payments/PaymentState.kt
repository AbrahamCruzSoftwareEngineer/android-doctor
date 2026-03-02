package com.example.architecturesample.feature.payments

data class PaymentState(
    val isLoading: Boolean = false,
    val status: String = "idle"
)
