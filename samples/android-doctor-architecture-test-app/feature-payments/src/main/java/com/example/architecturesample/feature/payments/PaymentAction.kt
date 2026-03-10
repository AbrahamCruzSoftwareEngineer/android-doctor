package com.example.architecturesample.feature.payments

sealed class PaymentAction {
    data object Initialize : PaymentAction()
    data class Submit(val amount: Double) : PaymentAction()
}
