package com.colleen.s36349879.medtrack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.clinician.ClinicianPasswordViewModel

@Composable
fun ClinicianLoginScreen(
    navController: NavHostController,
    clinicianPasswordViewModel: ClinicianPasswordViewModel // Handles the access key logic
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        // Lock icon at the top
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = colorResource(R.color.LightBlue)
        )

        Spacer(Modifier.height(16.dp))

        // Title and subtitle text
        Text("Clinician Access Only", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Please enter your clinical access key",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        // Access key input field
        // The value and changes are managed directly by the ViewModel
        OutlinedTextField(
            value = clinicianPasswordViewModel.accessKeyInput,
            onValueChange = { clinicianPasswordViewModel.onAccessKeyChange(it) }, // Sends each keystroke to the ViewModel
            label = { Text("Access Key") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = clinicianPasswordViewModel.errorMessage != null,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.LightBlue),
                focusedLabelColor = colorResource(R.color.LightBlue)
            )
        )

        // Show the error message if the access key was wrong
        // ?.let only runs if errorMessage is not null
        clinicianPasswordViewModel.errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.Start) // Align the error to the left, under the input field
            )
        }

        Spacer(Modifier.height(24.dp))

        // Verify button that calls the ViewModel to check if the access key is correct
        Button(
            onClick = {
                // validateAccessKey checks the key and calls this callback if it is correct
                clinicianPasswordViewModel.validateAccessKey {
                    // Only runs if the key was correct and navigate to the dashboard
                    navController.navigate("clinician_dashboard"){
                        popUpTo("clinician_login") {inclusive = true}
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.LightBlue))
        ){
            Text("Verify & Enter")
        }

        // popBackStack() to go back one screen
        TextButton(onClick = {navController.popBackStack()}){
            Text("Back to Settings")
        }
    }
}