package com.colleen.s36349879.medtrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.healthassistant.HealthAssistantUiState
import com.colleen.s36349879.medtrack.data.healthassistant.HealthAssistantViewModel
import com.colleen.s36349879.medtrack.ui.localization.LocalStrings

/**
 * Health Assistant tab: general informational guidance from symptoms + vitals.
 * Kept deliberately simple and safety-first — see [HealthAssistantViewModel] /
 * [com.colleen.s36349879.medtrack.data.healthassistant.HealthAssistantRepository]
 * for the two-layer medication/dosage filtering.
 */
@Composable
fun HealthAssistantScreen(
    navController: NavHostController,
    viewModel: HealthAssistantViewModel
) {
    val strings = LocalStrings.current
    var symptoms by remember { mutableStateOf("") }
    var bloodPressure by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    val uiState = viewModel.uiState

    Scaffold(
        bottomBar = { MedTrackBottomBar(navController, currentRoute = "health_assistant") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Text(
                text = strings.healthAssistantTitle,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.LightBlue)
            )
            Text(
                text = strings.healthAssistantSubtitle,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                color = Color.Gray
            )

            OutlinedTextField(
                value = symptoms,
                onValueChange = { symptoms = it },
                label = { Text(strings.healthAssistantSymptomsLabel) },
                placeholder = { Text("e.g. headache and mild fever since this morning") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = bloodPressure,
                    onValueChange = { bloodPressure = it },
                    label = { Text(strings.healthAssistantBloodPressureLabel) },
                    placeholder = { Text("120/80") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text(strings.healthAssistantTemperatureLabel) },
                    placeholder = { Text("37.5°C") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val vitals = listOfNotNull(
                        bloodPressure.takeIf { it.isNotBlank() }?.let { "BP: $it" },
                        temperature.takeIf { it.isNotBlank() }?.let { "Temp: $it" }
                    ).joinToString(", ")
                    viewModel.requestGuidance(symptoms, vitals)
                },
                enabled = uiState !is HealthAssistantUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.LightBlue))
            ) {
                if (uiState is HealthAssistantUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(strings.healthAssistantSubmit)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (uiState) {
                is HealthAssistantUiState.Success -> GuidanceCard(guidance = uiState.guidance, onFlag = { viewModel.flagCurrentResponse() })
                is HealthAssistantUiState.Error -> Text(text = uiState.message, color = Color.Red)
                else -> {}
            }
        }
    }
}

@Composable
private fun GuidanceCard(guidance: String, onFlag: () -> Unit) {
    val strings = LocalStrings.current
    var flagged by remember(guidance) { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = guidance)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = { onFlag(); flagged = true }, enabled = !flagged) {
                Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (flagged) strings.healthAssistantFlagged else strings.healthAssistantFlagResponse)
            }
        }
    }
}
