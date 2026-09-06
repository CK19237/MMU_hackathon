package com.colleen.s36349879.medtrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel
import com.colleen.s36349879.medtrack.ui.localization.LocalStrings
import com.colleen.s36349879.medtrack.ui.theme.MedtrackTheme
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader


//Login Screen
@Composable
fun LoginScreen(navController: NavHostController, patientViewModel: PatientViewModel) {
    val strings = LocalStrings.current

    // Input variables
    var patientId by remember { mutableStateOf("") }
    var password by remember {mutableStateOf("")}

    // Error variables
    var patientIdError by remember {mutableStateOf(false)}
    var passwordError by remember {mutableStateOf(false)}
    var patientIdErrorMessage by remember { mutableStateOf("") }
    var passwordErrorMessage by remember {mutableStateOf("")}

    Column(
        modifier= Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        // Title
        Text(
            text = strings.loginTitle,
            style = TextStyle(
                fontSize = 50.sp,
                color = colorResource(R.color.LightBlue),
                fontWeight = FontWeight.Bold
            )
        )

        Spacer (modifier = Modifier.height(32.dp))

        // Enter Patient ID
        OutlinedTextField(
            value = patientId,
            onValueChange = {
                patientId = it
                patientIdError = false
            },

            label = {Text(strings.loginPatientId)},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = patientIdError,
            singleLine = true
        )

        // Show the error message if phoneError is true
        if (patientIdError) {
            Text(
                text = patientIdErrorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        // Validate if empty
        if (patientId.isBlank()) {
            patientIdError = true
            patientIdErrorMessage = strings.loginPatientIdRequired
        }

        Spacer (modifier = Modifier.height(16.dp))

        // Enter password
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
            },

            label = {Text(strings.loginPassword)},
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordError,
            singleLine = true
        )

        // If passwordError is true, show error message
        if (passwordError) {
            Text(
                text = passwordErrorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        // Validate if empty
        if (password.isBlank()) {
            passwordError = true
            passwordErrorMessage = strings.loginPasswordRequired
        }

        Spacer (modifier = Modifier.height(24.dp))

        // Login button
        Button(
            onClick = {
                if (patientId.isNotBlank() && password.isNotBlank()) {

                    // The callback { success -> ... } runs when the login check is done
                    patientViewModel.login(patientId.trim(), password.trim()){ success ->
                        if (success){
                            // Credentials matched so navigate to the home screen
                            navController.navigate("fact_check"){
                                popUpTo("welcome") {inclusive = true}
                            }
                        } else {
                            patientIdError = true
                            passwordError = true
                            passwordErrorMessage = strings.loginInvalidCredentials
                        }
                    }
                }

            },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.LightBlue),
                contentColor = Color.White,
            ),

            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ){
            Text(text = strings.loginTitle,
                fontSize = 20.sp)
        }

    }

}

