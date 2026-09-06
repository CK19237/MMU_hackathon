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
import com.colleen.s36349879.medtrack.data.patient.AppLanguage
import com.colleen.s36349879.medtrack.data.patient.Patient
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel
import com.colleen.s36349879.medtrack.ui.localization.LocalStrings
import com.colleen.s36349879.medtrack.ui.theme.MedtrackTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

//Signup Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavHostController, patientViewModel: PatientViewModel) {
    val strings = LocalStrings.current

    // Snack bar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Input variables
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(AppLanguage.DEFAULT) }
    var languageMenuExpanded by remember { mutableStateOf(false) }

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
                text = strings.signUpTitle,
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
            if (name.isBlank()) nameErrors.add(strings.signUpFullNameRequired)

            //Enter Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(strings.signUpFullName) },
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
            if(phone.isBlank()) phoneErrors.add(strings.signUpPhoneRequired)
            if (!phone.startsWith("04")) phoneErrors.add(strings.signUpPhoneMustStartWith04)
            if (phone.length != 10) phoneErrors.add(strings.signUpPhoneMustBe10Digits)
            if (!phone.isDigitsOnly()) phoneErrors.add (strings.signUpPhoneDigitsOnly)
            if (phoneUniqueError.isNotEmpty()) phoneErrors.add(phoneUniqueError)

            //Enter Phone Number
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneUniqueError = ""
                },
                label = { Text(strings.signUpPhone) },
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
            if (password.isBlank()) passwordErrors.add(strings.signUpPasswordRequired)
            if(password.length < 8) passwordErrors.add(strings.signUpPasswordMinLength)
            if (!hasLetter) passwordErrors.add(strings.signUpPasswordNeedsLetter)
            if (!hasNumber) passwordErrors.add(strings.signUpPasswordNeedsNumber)

            //Enter Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(strings.signUpPassword) },
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
            if (confirmPassword.isBlank()) confirmPasswordErrors.add(strings.signUpConfirmPasswordRequired)
            if (confirmPassword.isNotBlank() && password != confirmPassword) {
                confirmPasswordErrors.add(strings.signUpPasswordsDontMatch)
            }

            //Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(strings.signUpConfirmPassword) },
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

            // Age (optional)
            val ageError = age.isNotBlank() && age.toIntOrNull()?.let { it !in 0..120 } ?: true
            OutlinedTextField(
                value = age,
                onValueChange = { input -> if (input.length <= 3 && input.all { it.isDigit() }) age = input },
                label = { Text(strings.signUpAge) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = ageError,
                singleLine = true,
                supportingText = {
                    if (ageError) {
                        Text(text = strings.signUpAgeInvalid, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preferred language
            ExposedDropdownMenuBox(
                expanded = languageMenuExpanded,
                onExpandedChange = { languageMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedLanguage.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(strings.signUpLanguage) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = { languageMenuExpanded = false }
                ) {
                    AppLanguage.entries.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language.displayName) },
                            onClick = {
                                selectedLanguage = language
                                languageMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Sign up button
            Button(
                onClick = {
                    // Check if there are any errors
                    val hasError = nameErrors.isNotEmpty()
                            || phoneErrors.isNotEmpty()
                            || passwordErrors.isNotEmpty()
                            || confirmPasswordErrors.isNotEmpty()
                            || ageError


                    //Check for duplicate phone
                    if (!hasError) {
                        // All validation passed so attempt to register the new patient
                        // The callback receives the new patient ID on success, or null if the phone is taken
                        patientViewModel.register(
                            name = name,
                            phone = phone,
                            pass = password,
                            age = age.toIntOrNull(),
                            preferredLanguage = selectedLanguage.code
                        ) { resultId ->

                            if (resultId == null) {
                                phoneUniqueError = strings.signUpPhoneTaken

                            } else {
                                // Registration successful
                                scope.launch {
                                    snackbarHostState.showSnackbar(strings.signUpAccountCreated.format(resultId))
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
                Text(strings.signUpButton, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Go to login screen
            TextButton(onClick = {
                navController.navigate("login"){
                    popUpTo("signup"){inclusive = true}
                }
            }) {
                Text(strings.signUpAlreadyHaveAccount, color = colorResource(R.color.LightBlue))
            }
        }
    }
}
