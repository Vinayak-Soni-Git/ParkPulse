package com.sonisoft.parkpulse.feature.auth.domain.usecase

import com.sonisoft.parkpulse.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
