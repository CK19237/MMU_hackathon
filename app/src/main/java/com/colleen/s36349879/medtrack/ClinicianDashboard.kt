package com.colleen.s36349879.medtrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.clinician.ClinicianPasswordViewModel
import com.colleen.s36349879.medtrack.data.genAI.GenAIViewModel
import com.colleen.s36349879.medtrack.data.medication.MedicationViewModel
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel
import com.colleen.s36349879.medtrack.data.symptom.SymptomViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicianDashboardScreen(
    navController: NavHostController,
    patientViewModel: PatientViewModel,
    medicationViewModel: MedicationViewModel,
    symptomViewModel: SymptomViewModel,
    clinicianPasswordViewModel: ClinicianPasswordViewModel,
    genAIViewModel: GenAIViewModel
){
    // variables for aggregate info
    // collectAsState() turns each Flow into a value the UI can read
    val totalPatients by patientViewModel.getTotalPatientCount().collectAsState(initial = 0)
    val totalMeds by medicationViewModel.getTotalMedicationCount().collectAsState(initial = 0)
    val commonSymptom by symptomViewModel.getMostCommonCategory().collectAsState(initial = "Loading...")
    val avgSeverity by symptomViewModel.getAverageSeverity().collectAsState(initial = 0.0)

    // The last 7 severity scores used to draw the trend chart
    val severities by symptomViewModel.getRecentSeverities().collectAsState(initial = emptyList())

    // Calculate Average Meds per Patient
    // Guard against dividing by zero if there are no patients yet
    val avgMeds = if (totalPatients > 0) totalMeds.toDouble() / totalPatients else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Clinician Dashboard",
                        color = colorResource(R.color.LightBlue),
                        fontWeight = FontWeight.Bold
                    ) },
                actions = {
                    // Logout button
                    IconButton(onClick = {
                        clinicianPasswordViewModel.logout()

                        //Clear the AI insights so they don't persist
                        genAIViewModel.aiInsights = emptyList()

                        //Navigate back to the start and clear the backstack
                        navController.navigate("settings") {
                            popUpTo("clinician_dashboard") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = colorResource(R.color.LightBlue)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) {
        innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Symptom severity trend chart at the top of the screen
            item {
                SymptomTrendChart(severities = severities)
            }

            // Aggregate statistics section
            // Each StatCard shows one statistic with an icon and a coloured background
            item{
                Text(
                    text = "Aggregate Statistics",
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                    color = colorResource(R.color.LightBlue)
                )

                Spacer(modifier = Modifier.height(16.dp))

                //Total Patients Card
                StatCard(
                    label = "Total Patients",
                    value = totalPatients.toString(),
                    icon = Icons.Default.Person,
                    color = Color.Blue.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Average Medications Card
                StatCard(
                    label = "Avg Medications / Patient",
                    value = String.format("%.1f", avgMeds),
                    icon = Icons.Default.MedicalServices,
                    color = Color.Green.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Most Common Symptom Card
                StatCard(
                    label = "Most Common Symptom",
                    value = commonSymptom,
                    icon = Icons.Default.Warning,
                    color = Color.Yellow.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Average Severity Card
                StatCard(
                    label = "Avg Symptom Severity",
                    value = "${String.format("%.1f", avgSeverity)} / 10",
                    icon = Icons.Default.Speed,
                    color = Color.Red.copy(alpha = 0.1f)
                )
            }

            // AI pattern finder section
            item{
                // Send the current stats to the AI and ask it to find clinical patterns
                // The button is disabled while the AI is loading to prevent duplicate requests
                Button(
                    onClick = {
                        genAIViewModel.findPatterns(totalPatients, avgMeds, commonSymptom, avgSeverity)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !genAIViewModel.isLoading, // Grey out the button while waiting for AI
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.LightBlue))
                ) {
                    Text("Find Patterns")
                }

                // Show a loading spinner while the AI is generating insights
                if (genAIViewModel.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colorResource(R.color.LightBlue))
                            Spacer(Modifier.height(8.dp))
                            Text("Coach is thinking...", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display each AI insight as its own card once they are available
                // forEach loops through each insight string in the list and creates a card for it
                if (genAIViewModel.aiInsights.isNotEmpty()) {
                    genAIViewModel.aiInsights.forEach { insight ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = "• $insight",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Show an error message if the AI request failed
                // ?.let only runs if errorMessage is not null
                genAIViewModel.errorMessage?.let { msg ->
                    Text(text = msg, color = Color.Red, fontSize = 12.sp)
                }
            }
        }
    }
}

// A bar chart that visually shows the severity of the most recent symptoms
@Composable
fun SymptomTrendChart(severities: List<Int>){
    // take the last 7 entries and reverse them to show oldest to newest
    val chartData = severities.reversed()
    val maxBarHeight = 120.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ){
        Column(modifier = Modifier.padding(16.dp)){
            Text(
                text = "Symptom Severity Trend Chart",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.LightBlue)
            )

            Text(
                text = "Tracking the last ${chartData.size} entries",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // This Row holds all the bars, aligned to the bottom so bars grow upward
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((150.dp)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ){
                if (chartData.isEmpty()){
                    // Show a placeholder message if there is no symptom data yet
                    Box(
                      modifier = Modifier
                          .fillMaxSize(),
                      contentAlignment = Alignment.Center
                    ){
                        Text("No data available", color = Color.LightGray)
                    }
                } else {
                    // Draw one bar for each severity score
                    chartData.forEachIndexed { index, severity ->
                        // Calculate bar height as a proportion of the max height
                        // e.g. severity 7 out of 10 = 70% of 120dp = 84dp tall
                        // coerceAtLeast(0.1f) ensures the bar is never completely invisible
                        val calculatedHeight = maxBarHeight * (severity / 10f).coerceAtLeast(0.1f)

                        // Color the bar based on how severe the symptom is
                        val barColor = when{
                            severity >= 7 -> Color.Red
                            severity >= 4 -> colorResource(R.color.coral)
                            else -> Color.Green
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ){
                            // The colored bar itself
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(calculatedHeight)
                                    .background(
                                        color = barColor,
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Show the severity number underneath each bar
                            Text(
                                text = severity.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-axis label to explain the direction of the chart
            Text(
                text = "Oldest to Newest Entries",
                fontSize = 10.sp,
                color = Color.LightGray
            )

        }
    }
}

// A reusable card component that displays a single statistic with an icon
// Used by the clinician dashboard to show totals and averages
@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon on the left side of the card
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.DarkGray
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Label and value stacked vertically on the right side
            Column {
                Text(text = label, fontSize = 14.sp, color = Color.Gray)
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}