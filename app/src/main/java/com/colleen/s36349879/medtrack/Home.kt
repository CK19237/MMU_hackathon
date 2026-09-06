package com.colleen.s36349879.medtrack


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.medication.MedicationViewModel
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


// Home screen
@Composable
fun HomeScreen(
    navController: NavHostController,
    patientViewModel: PatientViewModel,
    medicationViewModel: MedicationViewModel
){
    // Get the logged-in patient's ID to load their specific data
    val patientId = patientViewModel.getLoggedInPatientId()

    // collectAsState() converts the Flow into a value the UI can read and react to
    val patient by patientViewModel.getCurrentPatient().collectAsState(initial = null)
    val medications by medicationViewModel.getMedicationsForPatient(patientId).collectAsState(initial = emptyList())

    // Count how many medications have been marked as taken today
    // This updates automatically whenever the medications list changes
    val takenCount = medications.count { it.isTaken }

    // Today's date that is formatted
    val today = LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())
    )

    Scaffold(
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(60.dp),
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ){
                        IconButton(onClick = {navController.navigate("home")}) {
                            Icon(Icons.Filled.Home, contentDescription = "Go Home")
                        }
                        IconButton(onClick = {navController.navigate("fact_check")}) {
                            Icon(Icons.Filled.FactCheck, contentDescription = "Fact-Check")
                        }
                        IconButton(onClick = {navController.navigate("health_assistant")}) {
                            Icon(Icons.Filled.HealthAndSafety, contentDescription = "Health Assistant")
                        }
                        IconButton(onClick = {navController.navigate("symptoms")}) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Symptoms")
                        }
                        IconButton(onClick = {navController.navigate("settings")}) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = {navController.navigate("med_coach")}) {
                            Icon(Icons.Filled.SupportAgent, contentDescription = "MedCoach")
                        }
                    }

                }
            )
        },
        // Add medications button
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.width(250.dp),
                onClick= {navController.navigate("add_medication")})
            {
                Row(){
                    Text(
                        text = "Add Medications",
                        fontSize = 14.sp,
                    )

                    Icon(Icons.Filled.Add, contentDescription = "Add Medication")
                }
            }
        }
    ){ innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ){
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    // Date
                    Text(
                        text = today,
                        fontSize = 14.sp,
                    )

                    // Logout button
                    Button(onClick = {

                        patientViewModel.logout()

                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true } // Clear Backstack
                        }
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.LightBlue),
                            contentColor = Color.White,
                        ),
                    ){
                        Text(
                            text = "Logout",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Greeting
                Text(
                    text = "Hello, ${patient?.patientName ?: "Patient"}",
                    style = TextStyle(
                        fontSize = 30.sp,
                        color = colorResource(R.color.LightBlue),
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "ID: ${patient?.patientId ?: "Patient ID"}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item{
                // Summary counter
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.LightBlue).copy(alpha = 0.3f)
                    )
                ){
                    Text(
                        text = "$takenCount of ${medications.size} medications taken today",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 16.sp,
                    )
                }
            }

            // Title
            item {
                Text(
                    text = "Today's Medications",
                    fontSize = 18.sp
                )
            }

            // Validate if it's empty then display message
            if (medications.isEmpty()) {
                item {
                    Text(
                        text = "No medications scheduled.",
                        fontSize = 14.sp
                    )
                }
            }

            // Loop through every medication and display it as a card
            items(medications) {medication ->

                // Read the taken status directly from the database record
                val isTaken = medication.isTaken

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if(isTaken) Color.LightGray.copy(alpha = 0.5f)
                        else colorResource(R.color.LightBlue).copy(alpha = 0.5f)
                    )
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Column(){
                            Text(
                                text = medication.medicationName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(
                                    textDecoration = if (isTaken) TextDecoration.LineThrough
                                    else TextDecoration.None
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Dosage: ${medication.dosage}",
                                fontSize = 14.sp
                            )

                            Text(
                                text = "Frequency: ${medication.frequency}",
                                fontSize = 14.sp
                            )

                            Text(
                                text = "Time: ${medication.medicationTime}",
                                fontSize = 14.sp
                            )
                        }

                        // Checkbox to let user mark as taken
                        // When ticked or unticked, it calls the ViewModel to update the database
                        // The summary counter then update automatically
                        Checkbox(
                            checked = medication.isTaken,
                            onCheckedChange = { isChecked ->
                                medicationViewModel.toggleMedicationTaken(medication, isChecked)
                            }
                        )
                    }
                }
            }

            item {
                // Spacer so the AddMedications button won't overlap with
                // the medication cards
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

    }
}