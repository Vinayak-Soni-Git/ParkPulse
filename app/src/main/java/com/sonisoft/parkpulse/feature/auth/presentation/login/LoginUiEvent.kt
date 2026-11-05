package com.sonisoft.parkpulse.feature.auth.presentation.login

sealed class LoginUiEvent {
    data object LoginSuccess : LoginUiEvent()
    data class ShowError(val message: String) : LoginUiEvent()
}
