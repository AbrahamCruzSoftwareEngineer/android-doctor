package com.example.architecturesample.feature.payments

class PaymentReducer {
    fun reduce(previous: PaymentState, action: PaymentAction): PaymentState {
        return when (action) {
            PaymentAction.Initialize -> previous.copy(isLoading = true, status = "loading")
            is PaymentAction.Submit -> previous.copy(isLoading = false, status = "submitted-${action.amount}")
        }
    }
}
