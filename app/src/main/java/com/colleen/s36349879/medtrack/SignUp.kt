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
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.patient.Patient
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel
import com.colleen.s36349879.medtrack.ui.theme.MedtrackTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

//Signup Screen
@Composable
fun SignUpScreen(navController: NavHostController, patientViewModel: PatientViewModel) {

    // Snack bar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Input variables
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Error variables
    var phoneUniqueError by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Sign Up",
                style = TextStyle(
                    fontSize = 50.sp,
                    color = colorResource(R.color.LightBlue),
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            //Make list of errors in name
            val nameErrors = mutableListOf<String>()
            // If it is blank, add the error message to the list
            if (name.isBlank()) nameErrors.add("Full Name is required")

            //Enter Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name (required)") },
                isError = nameErrors.isNotEmpty(),
                singleLine = true,
                supportingText = {
                    if (nameErrors.isNotEmpty()) {
                        Text(
                            text = nameErrors.joinToString("\n"),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Make a list of all errors in phone to validate
            val phoneErrors = mutableListOf<String>()
            if(phone.isBlank()) phoneErrors.add("Phone number is required")
            if (!phone.startsWith("04")) phoneErrors.add("Must start with 04")
            if (phone.length != 10) phoneErrors.add("Must be exactly 10 digits")
            if (!phone.isDigitsOnly()) phoneErrors.add ("Must contain only digits")
            if (phoneUniqueError.isNotEmpty()) phoneErrors.add(phoneUniqueError)

            //Enter Phone Number
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneUniqueError = ""
                },
                label = { Text("Phone Number (required)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneErrors.isNotEmpty(),
                singleLine = true,
                supportingText = {
                    if (phoneErrors.isNotEmpty()){
                        Text(
                            text = phoneErrors.joinToString("\n"),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password validation
            var hasLetter = false
            var hasNumber = false

            for (char in password) {
                if (char.isLetter()) {
                    hasLetter = true
                }
                if (char.isDigit()) {
                    hasNumber = true
                }
            }

            //Make a list of all errors in password to validate
            val passwordErrors = mutableListOf<String>()
            if (password.isBlank()) passwordErrors.add("Password is required")
            if(password.length < 8) passwordErrors.add("Must be at least 8 characters")
            if (!hasLetter) passwordErrors.add("Must contain at least one letter")
            if (!hasNumber) passwordErrors.add("Must contain at least one number")

            //Enter Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (required)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = password.isBlank() || password.length < 8 || !hasLetter || !hasNumber,
                singleLine = true,
                supportingText = {
                    if (passwordErrors.isNotEmpty()) {
                        Text(
                            text = passwordErrors.joinToString("\n"),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Make a list for errors in Confirm Password to validate
            val confirmPasswordErrors = mutableListOf<String>()
            if (confirmPassword.isBlank()) confirmPasswordErrors.add("Please confirm your password")
            if (confirmPassword.isNotBlank() && password != confirmPassword) {
                confirmPasswordErrors.add("Passwords don't match")
            }

            //Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = confirmPasswordErrors.isNotEmpty(),
                singleLine = true,
                supportingText = {
                    if(confirmPasswordErrors.isNotEmpty()){
                        Text(
                            text = confirmPasswordErrors.joinToString("\n"),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Sign up button
            Button(
                onClick = {
                    // Check if there are any errors
                    val hasError = nameErrors.isNotEmpty()
                            || phoneErrors.isNotEmpty()
                            || passwordErrors.isNotEmpty()
                            || confirmPasswordErrors.isNotEmpty()


                    //Check for duplicate phone
                    if (!hasError) {
                        // All validation passed so attempt to register the new patient
                        // The callback receives the new patient ID on success, or null if the phone is taken
                        patientViewModel.register(name, phone, password){resultId ->

                            if (resultId == null) {
                                phoneUniqueError = "This phone number is already registered"

                            } else {
                                // Registration successful
                                scope.launch {
                                    snackbarHostState.showSnackbar("Account $resultId Created!")
                                    navController.navigate("login") {
                                            popUpTo("signup") { inclusive = true }
                                    }
                                }
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
            ) {
                Text("Sign Up", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Go to login screen
            TextButton(onClick = {
                navController.navigate("login"){
                    popUpTo("signup"){inclusive = true}
                }
            }) {
                Text("Already have an account? Login", color = colorResource(R.color.LightBlue))
            }
        }
    }
}





