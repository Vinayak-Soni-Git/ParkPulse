package com.sonisoft.parkpulse.feature.auth.domain.usecase

import com.sonisoft.parkpulse.feature.auth.domain.model.AuthUser
import com.sonisoft.parkpulse.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAuthSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthUser?> {
        return authRepository.getAuthSession()
    }
}
