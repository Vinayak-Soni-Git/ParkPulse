package com.sonisoft.parkpulse.feature.parks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonisoft.parkpulse.feature.auth.domain.usecase.LogoutUseCase
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import com.sonisoft.parkpulse.feature.parks.domain.usecase.GetParkStatisticsUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.InitializeParkDatabaseUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.SearchParksUseCase
import com.sonisoft.parkpulse.feature.parks.domain.usecase.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ParksViewModel @Inject constructor(
    private val initializeParkDatabaseUseCase: InitializeParkDatabaseUseCase,
    private val searchParksUseCase: SearchParksUseCase,
    private val getParkStatisticsUseCase: GetParkStatisticsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatus = MutableStateFlow<ParkStatus?>(null)
    private val _isInitializing = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val filtersFlow = combine(
        _searchQuery,
        _selectedStatus
    ) { query, status ->
        Pair(query, status)
    }

    val uiState: StateFlow<ParksUiState> = combine(
        filtersFlow,
        filtersFlow.flatMapLatest { (query, status) -> searchParksUseCase(query, status) },
        getParkStatisticsUseCase(),
        _isInitializing,
        _errorMessage,
        syncManager.syncState
    ) { args: Array<Any?> ->
        val filters = args[0] as Pair<String, ParkStatus?>
        val query = filters.first
        val status = filters.second
        val parks = args[1] as List<com.sonisoft.parkpulse.feature.parks.domain.model.Park>
        val stats = args[2] as com.sonisoft.parkpulse.feature.parks.domain.model.DashboardStatistics
        val isInit = args[3] as Boolean
        val error = args[4] as String?
        val syncState = args[5] as com.sonisoft.parkpulse.feature.parks.domain.usecase.SyncState
        
        ParksUiState(
            parks = parks,
            searchQuery = query,
            selectedStatus = status,
            statistics = stats,
            isLoading = false,
            isInitializingDatabase = isInit,
            errorMessage = error,
            syncState = syncState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ParksUiState(isLoading = true)
    )

    init {
        initializeDatabase()
    }

    private fun initializeDatabase() {
        viewModelScope.launch {
            try {
                _isInitializing.value = true
                initializeParkDatabaseUseCase()
                _isInitializing.value = false
            } catch (e: Exception) {
                _isInitializing.value = false
                _errorMessage.value = "Failed to initialize database: ${e.message}"
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterChange(status: ParkStatus?) {
        _selectedStatus.value = status
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            syncManager.syncNow()
        }
    }
}
