package com.colleen.s36349879.medtrack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.patient.AppLanguage
import com.colleen.s36349879.medtrack.data.patient.FontSizeOption
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel
import com.colleen.s36349879.medtrack.data.patient.appLanguage
import com.colleen.s36349879.medtrack.data.patient.effectiveFontSizeOption
import com.colleen.s36349879.medtrack.ui.localization.LocalStrings

// The settings screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, patientViewModel: PatientViewModel){
    val strings = LocalStrings.current

    // Load the currently logged-in patient's details as a reactive stream
    val patient by patientViewModel.getCurrentPatient().collectAsState(initial = null)
    val patientId = patientViewModel.getLoggedInPatientId()

    // Age field: a local editable draft, reset whenever the underlying patient record
    // changes (e.g. after saving, or on first load) so it never fights the user's typing.
    var ageDraft by remember { mutableStateOf("") }
    LaunchedEffect(patient?.age) {
        ageDraft = patient?.age?.toString() ?: ""
    }

    var languageMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { MedTrackBottomBar(navController, currentRoute = "settings") }
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
                text = strings.settingsTitle,
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
                        text = strings.settingsNameLabel.format(patient?.patientName ?: ""),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = strings.settingsPhoneLabel.format(patient?.phoneNumber ?: ""),
                        fontSize = 18.sp
                    )

                    Text(
                        text = strings.settingsPatientIdLabel.format(patient?.patientId ?: ""),
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Editable age field
                    OutlinedTextField(
                        value = ageDraft,
                        onValueChange = { input -> if (input.length <= 3 && input.all { it.isDigit() }) ageDraft = input },
                        label = { Text(strings.settingsAgeFieldLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val newAge = ageDraft.toIntOrNull()?.takeIf { it in 0..120 }
                            patientViewModel.updateAge(patientId, newAge)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.LightBlue))
                    ) {
                        Text(strings.settingsAgeSave)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Language section
            Text(
                text = strings.settingsLanguageSection,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = languageMenuExpanded,
                onExpandedChange = { languageMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = patient.appLanguage().displayName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                )
                ExposedDropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = { languageMenuExpanded = false }
                ) {
                    AppLanguage.entries.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language.displayName) },
                            onClick = {
                                patientViewModel.updateLanguage(patientId, language.code)
                                languageMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Font size section
            Text(
                text = strings.settingsFontSizeSection,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            val currentFontSize = patient.effectiveFontSizeOption()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FontSizeOption.entries.forEach { option ->
                    val label = when (option) {
                        FontSizeOption.DEFAULT -> strings.settingsFontSizeDefault
                        FontSizeOption.LARGE -> strings.settingsFontSizeLarge
                        FontSizeOption.EXTRA_LARGE -> strings.settingsFontSizeExtraLarge
                    }
                    FilterChip(
                        selected = currentFontSize == option,
                        onClick = { patientViewModel.updateFontSizePreference(patientId, option.name) },
                        label = { Text(label) }
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
                Text(strings.logout)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clinician login button
            OutlinedButton(
                onClick = { navController.navigate("clinician_login") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = strings.settingsClinicianLogin,
                    color = colorResource(R.color.LightBlue)
                )
            }
        }
    }
}
