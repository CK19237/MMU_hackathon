package com.colleen.s36349879.medtrack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel
import kotlin.text.iterator

// This screen lets a csv patient claim their account
// by verifying their Patient ID and phone number, then setting a new password
@Composable
fun ClaimAccountScreen(navController: NavHostController, patientViewModel: PatientViewModel){

    // Input field values
    var patientId by remember {mutableStateOf("")}
    var phone by remember {mutableStateOf("")}
    var newPassword by remember {mutableStateOf("")}
    var errorMessage by remember {mutableStateOf("")}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        // Screen title
        Text(
            text = "Claim Account",
            style = MaterialTheme.typography.headlineMedium,
            color = colorResource(R.color.LightBlue),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Patient ID input field
        // isError turns the field red if the input is blank
        OutlinedTextField(
            value = patientId,
            onValueChange = { patientId = it },
            label = { Text("Patient ID (e.g. P1001)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = patientId.isBlank(),
            supportingText = {
                if (patientId.isBlank()) {
                    Text(text = "Patient ID is required", color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone number input field
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = phone.isBlank(),
            supportingText = {
                if (phone.isBlank()) {
                    Text(text = "Phone Number is required", color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password validation logic
        // loop through each character in the password to check what it contains
        var hasLetter = false
        var hasNumber = false
        for (char in newPassword) {
            if (char.isLetter()) hasLetter = true // Found at least one letter
            if (char.isDigit()) hasNumber = true // Found at least one number
        }

        // Build a list of error messages based on which rules are broken
        val passwordErrors = mutableListOf<String>()
        if (newPassword.isNotEmpty()) {
            if (newPassword.length < 8) passwordErrors.add("Must be at least 8 characters")
            if (!hasLetter) passwordErrors.add("Must contain at least one letter")
            if (!hasNumber) passwordErrors.add("Must contain at least one number")
        }

        // Password input field
        OutlinedTextField(value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Set New Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordErrors.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                if (passwordErrors.isNotEmpty()) {
                    Text(text = passwordErrors.joinToString("\n"), color = MaterialTheme.colorScheme.error)
                }
            }
        )

        // Show the error message
        if (errorMessage.isNotEmpty()){
            Text(errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        // Claim button
        Button(
            onClick = {
                // Check all fields are filled in and password has no errors before proceeding
                val isFormValid = patientId.isNotBlank() &&
                        phone.isNotBlank() &&
                        newPassword.isNotBlank() &&
                        passwordErrors.isEmpty()

                if (isFormValid){
                    // Call the ViewModel to verify the ID and phone, then update the password
                    // .trim() removes any accidental spaces the user may have typed
                    patientViewModel.claimAccount(
                        id = patientId.trim(),
                        phone = phone.trim(),
                        newPass = newPassword.trim()
                    ) { success ->
                        // This is the callback that it runs when claimAccount finishes
                        if (success){
                            // Account claimed successfully then navigate to login
                            navController.navigate("login"){
                                popUpTo("claim_account") {inclusive = true}
                            }
                        } else{
                            errorMessage = "No matching Patient ID and Phone Number found."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.LightBlue))
        ) { Text("Set Password & Claim") }
    }

}