package com.sonisoft.parkpulse.feature.parks.presentation

import com.sonisoft.parkpulse.feature.auth.domain.usecase.LogoutUseCase
import com.sonisoft.parkpulse.feature.parks.domain.model.DashboardStatistics
import com.sonisoft.parkpulse.feature.parks.domain.model.Park
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.usecase.GetParkStatisticsUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.InitializeParkDatabaseUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.SearchParksUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParksViewModelTest {

    private lateinit var viewModel: ParksViewModel

    private val initUseCase: InitializeParkDatabaseUseCase = mockk(relaxed = true)
    private val searchUseCase: SearchParksUseCase = mockk()
    private val statsUseCase: GetParkStatisticsUseCase = mockk()
    private val logoutUseCase: LogoutUseCase = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { searchUseCase(any(), any()) } returns flowOf(emptyList())
        every { statsUseCase() } returns flowOf(DashboardStatistics(10, 5, 3, 2))

        viewModel = ParksViewModel(initUseCase, searchUseCase, statsUseCase, logoutUseCase)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization triggers database seed and updates state`() = runTest {
        advanceUntilIdle() // Wait for init block coroutines to finish

        coVerify { initUseCase() }

        assertEquals(false, viewModel.uiState.value.isInitializingDatabase)
        assertEquals(10, viewModel.uiState.value.statistics.totalParks)
        assertEquals(emptyList<Park>(), viewModel.uiState.value.parks)
    }

    @Test
    fun `search query updates state and fetches parks`() = runTest {
        val mockedParks = listOf<Park>(mockk(relaxed = true))
        every { searchUseCase("Central", null) } returns flowOf(mockedParks)

        viewModel.onSearchQueryChange("Central")
        advanceUntilIdle()

        assertEquals("Central", viewModel.uiState.value.searchQuery)
        assertEquals(mockedParks, viewModel.uiState.value.parks)
    }

    @Test
    fun `status filter updates state and fetches parks`() = runTest {
        val mockedParks = listOf<Park>(mockk(relaxed = true))
        every { searchUseCase("", ParkStatus.OPEN) } returns flowOf(mockedParks)

        viewModel.onStatusFilterChange(ParkStatus.OPEN)
        advanceUntilIdle()

        assertEquals(ParkStatus.OPEN, viewModel.uiState.value.selectedStatus)
        assertEquals(mockedParks, viewModel.uiState.value.parks)
    }
}
