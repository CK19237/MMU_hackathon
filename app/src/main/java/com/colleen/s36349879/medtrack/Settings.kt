package com.colleen.s36349879.medtrack

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel

// The settings screen
@Composable
fun SettingsScreen(navController: NavHostController, patientViewModel: PatientViewModel){

    // Load the currently logged-in patient's details as a reactive stream
    val patient by patientViewModel.getCurrentPatient().collectAsState(initial = null)

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
        }
    ){ innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ){
            // Screen title
            Text(
                text = "Account Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.LightBlue)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card displaying the patient's account information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Gray.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ){
                Column(modifier = Modifier.padding(16.dp)){
                    // The ?: operator provides a fallback empty string while the data is still loading
                    Text(
                        text = "Name: ${patient?.patientName ?: ""}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Phone: ${patient?.phoneNumber ?: ""}",
                        fontSize = 18.sp
                    )

                    Text(
                        text = "Patient ID: ${patient?.patientId ?: ""}",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Logout button
            Button(
                onClick = {
                    patientViewModel.logout() // Clears the patient session from AuthManager

                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true } //Back button won't return to Home
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.LightBlue),
                    contentColor = Color.White
                )
            ){
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clinician login button
            OutlinedButton(
                onClick = { navController.navigate("clinician_login") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Clinician Login",
                    color = colorResource(R.color.LightBlue)
                )
            }
        }
    }
}
