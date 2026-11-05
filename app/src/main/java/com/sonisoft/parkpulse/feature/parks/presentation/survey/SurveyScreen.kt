package com.sonisoft.parkpulse.feature.parks.presentation.survey

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonisoft.parkpulse.core.designsystem.theme.CardWhite
import com.sonisoft.parkpulse.core.designsystem.theme.Charcoal
import com.sonisoft.parkpulse.core.designsystem.theme.CharcoalLight
import com.sonisoft.parkpulse.core.designsystem.theme.ErrorRed
import com.sonisoft.parkpulse.core.designsystem.theme.ForestGreen
import com.sonisoft.parkpulse.core.designsystem.theme.ForestGreenDark
import com.sonisoft.parkpulse.core.designsystem.theme.LightGrey
import com.sonisoft.parkpulse.core.designsystem.theme.OffWhite
import com.sonisoft.parkpulse.feature.parks.domain.model.CleanlinessRating
import com.sonisoft.parkpulse.feature.parks.domain.model.Survey
import com.sonisoft.parkpulse.feature.parks.domain.usecase.ValidateCheckInUseCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(
    viewModel: SurveyViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        viewModel.checkLocationPermission()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.park?.name ?: "Park Survey",
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenDark
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Charcoal)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhite)
                )

                if (uiState.step != SurveyStep.FORM && uiState.step != SurveyStep.REVIEW) {
                    TabRow(
                        selectedTabIndex = if (uiState.step == SurveyStep.HISTORY) 1 else 0,
                        containerColor = OffWhite
                    ) {
                        Tab(
                            selected = uiState.step != SurveyStep.HISTORY,
                            onClick = { viewModel.setStep(SurveyStep.CHECK_IN) },
                            text = { Text("Conduct Survey") }
                        )
                        Tab(
                            selected = uiState.step == SurveyStep.HISTORY,
                            onClick = { viewModel.setStep(SurveyStep.HISTORY) },
                            text = { Text("History (${uiState.pastSurveys.size})") }
                        )
                    }
                }
            }
        },
        containerColor = OffWhite
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.step) {
                SurveyStep.CHECK_IN -> CheckInView(
                    uiState = uiState,
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onConfirmCheckIn = { viewModel.performCheckIn() }
                )

                SurveyStep.FORM -> InspectionFormView(
                    uiState = uiState,
                    onTreeCountChanged = { viewModel.onTreeCountChanged(it) },
                    onLightingSelected = { viewModel.onLightingSelected(it) },
                    onCleanlinessSelected = { viewModel.onCleanlinessSelected(it) },
                    onNotesChanged = { viewModel.onNotesChanged(it) },
                    onProceedToReview = { viewModel.proceedToReview() },
                    onCancel = { viewModel.setStep(SurveyStep.CHECK_IN) }
                )

                SurveyStep.REVIEW -> ReviewSummaryView(
                    uiState = uiState,
                    onBackToEdit = { viewModel.setStep(SurveyStep.FORM) },
                    onSubmit = { viewModel.saveSurvey() }
                )

                SurveyStep.SUCCESS -> SuccessView(
                    onViewHistory = { viewModel.setStep(SurveyStep.HISTORY) },
                    onDone = onBackClick
                )

                SurveyStep.HISTORY -> HistoryView(
                    surveys = uiState.pastSurveys
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun CheckInView(
    uiState: SurveyUiState,
    onRequestPermission: () -> Unit,
    onConfirmCheckIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Geofence Check-in",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "To conduct a survey, you must be physically inside the park boundary with GPS accuracy of 25m or better.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalLight
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GPS & Boundary Meter
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = ForestGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Location Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val loc = uiState.userLocation
                if (loc != null) {
                    Text(
                        text = String.format(Locale.getDefault(), "Coords: %.5f, %.5f", loc.latitude, loc.longitude),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Charcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val accColor = if (loc.accuracyMeters <= 25f) ForestGreen else ErrorRed
                    Text(
                        text = String.format(Locale.getDefault(), "GPS Accuracy: %.1fm (Target: <= 25m)", loc.accuracyMeters),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = accColor
                    )
                } else {
                    Text(
                        text = "Acquiring GPS location...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Validation Result Banner
        val result = uiState.checkInResult
        val isSuccess = result is ValidateCheckInUseCase.Result.Success

        val bannerColor = if (isSuccess) ForestGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)
        val textColor = if (isSuccess) ForestGreenDark else ErrorRed
        val icon = if (isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline

        val message = when (result) {
            is ValidateCheckInUseCase.Result.Success -> result.message
            is ValidateCheckInUseCase.Result.Failure -> result.reason
            null -> "Initializing location validation..."
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = bannerColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = textColor)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!uiState.hasLocationPermission) {
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Location Permission")
            }
        } else {
            Button(
                onClick = onConfirmCheckIn,
                enabled = isSuccess,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Check-in & Start Form")
            }
        }
    }
}

@Composable
private fun InspectionFormView(
    uiState: SurveyUiState,
    onTreeCountChanged: (String) -> Unit,
    onLightingSelected: (Boolean) -> Unit,
    onCleanlinessSelected: (CleanlinessRating) -> Unit,
    onNotesChanged: (String) -> Unit,
    onProceedToReview: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Confirmed check-in banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ForestGreen.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = ForestGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Check-in Confirmed", fontWeight = FontWeight.Bold, color = ForestGreenDark)
                    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(uiState.checkInTimestamp))
                    Text("Time: $timeStr | Accuracy: ${uiState.validatedLocation?.accuracyMeters?.toInt() ?: 0}m", style = MaterialTheme.typography.bodySmall, color = ForestGreenDark)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tree Count Field
        Text("A. Trees Count", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Charcoal)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.treeCountText,
            onValueChange = onTreeCountChanged,
            label = { Text("Number of Trees") },
            isError = uiState.treeCountError != null,
            supportingText = { uiState.treeCountError?.let { Text(it, color = ErrorRed) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lighting Field
        Text("B. Park Lighting Available?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Charcoal)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(
                selected = uiState.hasLighting == true,
                onClick = { onLightingSelected(true) },
                label = { Text("Yes") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreen, selectedLabelColor = CardWhite)
            )
            FilterChip(
                selected = uiState.hasLighting == false,
                onClick = { onLightingSelected(false) },
                label = { Text("No") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreen, selectedLabelColor = CardWhite)
            )
        }
        uiState.lightingError?.let { Text(it, color = ErrorRed, style = MaterialTheme.typography.bodySmall) }

        Spacer(modifier = Modifier.height(16.dp))

        // Cleanliness Field
        Text("C. Park Cleanliness Rating", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Charcoal)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                CleanlinessRating.GOOD to "Good",
                CleanlinessRating.FAIR to "Fair",
                CleanlinessRating.POOR to "Poor"
            ).forEach { (rating, label) ->
                FilterChip(
                    selected = uiState.cleanliness == rating,
                    onClick = { onCleanlinessSelected(rating) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreen, selectedLabelColor = CardWhite)
                )
            }
        }
        uiState.cleanlinessError?.let { Text(it, color = ErrorRed, style = MaterialTheme.typography.bodySmall) }

        Spacer(modifier = Modifier.height(16.dp))

        // Notes Field
        Text("D. Additional Notes (Optional)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Charcoal)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.notes,
            onValueChange = onNotesChanged,
            placeholder = { Text("Enter observations, damages, maintenance needs...") },
            supportingText = { Text("${uiState.notes.length}/500 chars") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = onProceedToReview,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.weight(1f)
            ) {
                Text("Review Survey")
            }
        }
    }
}

@Composable
private fun ReviewSummaryView(
    uiState: SurveyUiState,
    onBackToEdit: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Survey Summary Review", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Charcoal)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(uiState.park?.name ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForestGreenDark)
                Spacer(modifier = Modifier.height(12.dp))

                val timeStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(uiState.checkInTimestamp))
                ReviewRow("Check-in Time", timeStr)
                ReviewRow("GPS Accuracy", "${uiState.validatedLocation?.accuracyMeters?.toInt() ?: 0} metres")
                ReviewRow("Trees Count", uiState.treeCountText)
                ReviewRow("Lighting Available", if (uiState.hasLighting == true) "Yes" else "No")
                ReviewRow("Cleanliness Rating", uiState.cleanliness?.name ?: "")
                if (uiState.notes.isNotBlank()) {
                    ReviewRow("Notes", uiState.notes)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBackToEdit, modifier = Modifier.weight(1f)) {
                Text("Edit Form")
            }
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.weight(1f)
            ) {
                Text("Submit & Save")
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$label: ", fontWeight = FontWeight.SemiBold, color = CharcoalLight, modifier = Modifier.width(130.dp))
        Text(value, fontWeight = FontWeight.Medium, color = Charcoal)
    }
}

@Composable
private fun SuccessView(
    onViewHistory: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = ForestGreen,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Survey Submitted!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Charcoal)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your survey record has been saved locally to Room and marked for offline synchronization.", color = CharcoalLight, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onViewHistory,
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Survey History")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
private fun HistoryView(surveys: List<Survey>) {
    if (surveys.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Rounded.History, contentDescription = null, tint = CharcoalLight, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No Surveys Yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Charcoal)
            Spacer(modifier = Modifier.height(8.dp))
            Text("There are no recorded surveys for this park. Conduct a survey to see it here.", color = CharcoalLight, style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            surveys.forEach { survey ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val timeStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(survey.createdAt))
                            Text(timeStr, fontWeight = FontWeight.Bold, color = Charcoal)

                            val (badgeColor, badgeText) = when (survey.cleanliness) {
                                CleanlinessRating.GOOD -> ForestGreen to "GOOD"
                                CleanlinessRating.FAIR -> Color(0xFFF57C00) to "FAIR"
                                CleanlinessRating.POOR -> ErrorRed to "POOR"
                            }
                            Box(
                                modifier = Modifier
                                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(badgeText, color = badgeColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Trees: ${survey.treeCount}", style = MaterialTheme.typography.bodyMedium, color = CharcoalLight)
                            Text("Lighting: ${if (survey.hasLighting) "Yes" else "No"}", style = MaterialTheme.typography.bodyMedium, color = CharcoalLight)
                            Text("GPS: ${survey.accuracyMeters.toInt()}m", style = MaterialTheme.typography.bodyMedium, color = CharcoalLight)
                        }

                        survey.notes?.let { notes ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Notes: $notes", style = MaterialTheme.typography.bodySmall, color = Charcoal)
                        }
                    }
                }
            }
        }
    }
}
