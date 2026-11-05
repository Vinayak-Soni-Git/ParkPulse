package com.sonisoft.parkpulse.feature.auth.domain.usecase

import com.sonisoft.parkpulse.feature.auth.domain.model.AuthResult
import com.sonisoft.parkpulse.feature.auth.domain.model.AuthUser
import com.sonisoft.parkpulse.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult<AuthUser> {
        return authRepository.login(email, password)
    }
}
