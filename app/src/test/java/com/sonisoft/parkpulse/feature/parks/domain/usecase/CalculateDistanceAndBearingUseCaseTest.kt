package com.sonisoft.parkpulse.feature.parks.domain.usecase

import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class CalculateDistanceAndBearingUseCaseTest {

    private lateinit var useCase: CalculateDistanceAndBearingUseCase

    @Before
    fun setup() {
        useCase = CalculateDistanceAndBearingUseCase()
    }

    @Test
    fun `compass direction logic works`() {
        val method = CalculateDistanceAndBearingUseCase::class.java.getDeclaredMethod("getCompassDirection", Float::class.java)
        method.isAccessible = true
        
        assert(method.invoke(useCase, 0f) == "N")
        assert(method.invoke(useCase, 45f) == "NE")
        assert(method.invoke(useCase, 90f) == "E")
        assert(method.invoke(useCase, 180f) == "S")
        assert(method.invoke(useCase, 270f) == "W")
        assert(method.invoke(useCase, 350f) == "N")
    }
}