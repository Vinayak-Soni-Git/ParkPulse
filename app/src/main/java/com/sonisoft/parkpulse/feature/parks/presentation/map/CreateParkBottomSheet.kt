package com.sonisoft.parkpulse.feature.parks.presentation.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sonisoft.parkpulse.core.designsystem.theme.CardWhite
import com.sonisoft.parkpulse.core.designsystem.theme.Charcoal
import com.sonisoft.parkpulse.core.designsystem.theme.CharcoalLight
import com.sonisoft.parkpulse.core.designsystem.theme.ForestGreen
import com.sonisoft.parkpulse.core.designsystem.theme.ForestGreenDark
import com.sonisoft.parkpulse.core.designsystem.theme.OffWhite
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkType
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateParkBottomSheet(
    initialName: String = "",
    initialType: ParkType = ParkType.GARDEN,
    areaInSquareMeters: Double,
    perimeterInMeters: Double,
    pointsCount: Int,
    drawingError: String? = null,
    onDismiss: () -> Unit,
    onSave: (String, ParkType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(initialName) }
    var selectedType by remember { mutableStateOf(initialType) }
    var nameError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = OffWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "New Park Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Park Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("Park Name") },
                isError = nameError,
                supportingText = {
                    if (nameError) {
                        Text("Park name cannot be empty")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Park Type Selection
            Text(
                text = "Park Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    ParkType.GARDEN to "Garden",
                    ParkType.PLAYGROUND to "Playground",
                    ParkType.FOREST_PARK to "Forest Park",
                    ParkType.LAKE_FRONT to "Lake Front"
                ).forEach { (type, label) ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = CardWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Measurements Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Calculated Geometry",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val areaInAcres = areaInSquareMeters * 0.000247105
                    val perimeterInKm = perimeterInMeters / 1000.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Area", style = MaterialTheme.typography.bodySmall, color = CharcoalLight)
                            Text(
                                String.format(Locale.getDefault(), "%,.0f m² (%.2f acres)", areaInSquareMeters, areaInAcres),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Perimeter", style = MaterialTheme.typography.bodySmall, color = CharcoalLight)
                            Text(
                                String.format(Locale.getDefault(), "%,.0f m (%.2f km)", perimeterInMeters, perimeterInKm),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Vertices", style = MaterialTheme.typography.bodySmall, color = CharcoalLight)
                            Text(
                                "$pointsCount points",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save / Cancel Buttons
            if (drawingError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = com.sonisoft.parkpulse.core.designsystem.theme.ErrorRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Rounded.ErrorOutline, contentDescription = "Error", tint = com.sonisoft.parkpulse.core.designsystem.theme.ErrorRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = drawingError,
                            color = com.sonisoft.parkpulse.core.designsystem.theme.ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalLight.copy(alpha = 0.2f), contentColor = Charcoal),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = true
                        } else if (drawingError == null) {
                            onSave(name, selectedType)
                        }
                    },
                    enabled = pointsCount >= 3 && drawingError == null,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Park")
                }
            }
        }
    }
}
