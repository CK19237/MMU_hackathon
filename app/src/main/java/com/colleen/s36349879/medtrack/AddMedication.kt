package com.colleen.s36349879.medtrack

import android.icu.util.Calendar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.medication.MedicationViewModel
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel
import com.colleen.s36349879.medtrack.ui.localization.LocalStrings
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerFun(
    onConfirm: (TimePickerState) -> Unit,
){
    // Get the current time for initial value
    val currentTime = Calendar.getInstance()

    // Create and remember the time picker state with current time as default
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false, // use 12-hour format
    )

    Column {
        // Show the clock
        TimePicker(state = timePickerState)

        // Confirm button that can pass the selected time to the parent function
        Button (onClick = {onConfirm(timePickerState)}){
            Text(LocalStrings.current.confirmTime)
        }
    }
}

//Add Medications Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    navController: NavHostController,
    medicationViewModel: MedicationViewModel,
    patientViewModel: PatientViewModel
){
    val strings = LocalStrings.current

    // Get the ID of the patient who is currently logged in
    val patientId = patientViewModel.getLoggedInPatientId()

    //Snack bar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Input variables
    var name by remember {mutableStateOf("")}
    var dosage by remember {mutableStateOf("")}
    var selectedTime: TimePickerState? by remember {mutableStateOf(null)}
    var notes by remember {mutableStateOf("")}

    // Frequency dropdown options and variables
    val frequencyOptions = listOf("Once daily", "Twice daily", "Three times daily", "As needed")
    var selectedFrequency by remember { mutableStateOf("") }
    var frequencyExpanded by remember {mutableStateOf(false)}

    // Medication type dropdown options and variables
    val typeOptions = listOf("Tablet", "Capsule", "Liquid", "Injection", "Topical", "Other")
    var selectedType by remember { mutableStateOf("") }
    var typeExpanded by remember {mutableStateOf(false)}

    // Error variables
    var nameError by remember { mutableStateOf(false) }
    var frequencyError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(true) }
    var typeError by remember { mutableStateOf(false) }

    // Formatter for displaying selected time
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // Regex to check dosage format
    val dosageRegex = Regex("^\\d+(\\.\\d+)?(mg|ml|g)\$")

    // Collect all dosage errors into a list
    val dosageErrors = mutableListOf<String>()
    if (dosage.isBlank()) {
        dosageErrors.add(strings.addMedicationDosageRequired)
    }
    if (dosage.isNotBlank() && !dosageRegex.matches(dosage.trim())){
        dosageErrors.add(strings.addMedicationDosageInvalid)
    }

    Scaffold(
        snackbarHost = {SnackbarHost(hostState = snackbarHostState)},

        ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = strings.addMedicationTitle,
                    style = TextStyle(
                        fontSize = 40.sp,
                        color = colorResource(R.color.LightBlue),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Enter medication name
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text(strings.addMedicationNameLabel) },
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text(text = strings.addMedicationNameRequired,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Validate if it's empty
                if (name.isBlank()) {
                    nameError = true
                }
            }

            // Enter dosage
            item {
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(strings.addMedicationDosageLabel) },
                    isError = dosageErrors.isNotEmpty(),
                    supportingText = {
                        if (dosageErrors.isNotEmpty()) {
                            Text(text = dosageErrors.joinToString("\n"),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Frequency dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = frequencyExpanded,
                    onExpandedChange = { frequencyExpanded = !frequencyExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedFrequency,
                        onValueChange = {},
                        readOnly = true, // User cannot type, only selecte from dropdown
                        label = { Text(strings.addMedicationFrequencyLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                        isError = frequencyError,
                        supportingText = {
                            if (frequencyError) {
                                Text(text = strings.addMedicationFrequencyRequired,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )

                    // Validate if frequency is empty
                    if (selectedFrequency.isBlank()) {
                        frequencyError = true
                    }

                    ExposedDropdownMenu(
                        expanded = frequencyExpanded,
                        onDismissRequest = { frequencyExpanded = false }
                    ) {
                        frequencyOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedFrequency = option
                                    frequencyExpanded = false
                                    frequencyError = false
                                }
                            )
                        }
                    }
                }
            }

            // Time Picker
            item {
                TimePickerFun (
                    onConfirm = {
                            time ->
                        selectedTime = time
                        timeError = false
                    }
                )

                // Validate and display selected time
                if (selectedTime != null){
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, selectedTime!!.hour)
                    cal.set(Calendar.MINUTE, selectedTime!!.minute)
                    cal.isLenient = false
                    Text("Selected time = ${formatter.format(cal.time)}")
                } else {
                    Text(
                        text = strings.addMedicationNoTimeSelected,
                        color = MaterialTheme.colorScheme.error
                    )
                }

            }

            // Medication type dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.addMedicationTypeLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        isError = typeError,
                        supportingText = {
                            if (typeError) {
                                Text(text = strings.addMedicationTypeRequired,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )

                    // Validate if it's empty
                    if (selectedType.isBlank()) {
                        typeError = true
                    }

                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedType = option
                                    typeExpanded = false
                                    typeError = false
                                }
                            )
                        }
                    }
                }
            }

            // Enter notes
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.addMedicationNotesLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Save and clear buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Save button
                    // Validate before saving
                    Button(
                        onClick = {
                            // Check that there are no errors before saving
                            // All error flags must be false and dosage errors must be empty
                            if (!nameError && dosageErrors.isEmpty() && !frequencyError && !timeError && !typeError) {

                                // Format selected time
                                val cal = Calendar.getInstance()
                                cal.set(Calendar.HOUR_OF_DAY, selectedTime!!.hour)
                                cal.set(Calendar.MINUTE, selectedTime!!.minute)
                                val formattedTime = formatter.format(cal.time)

                                // Send all the field values to the ViewModel to be saved in the database
                                medicationViewModel.addMedication(
                                    patientId = patientId,
                                    name = name,
                                    dosage = dosage,
                                    frequency = selectedFrequency,
                                    time = formattedTime,
                                    type = selectedType,
                                    notes = notes
                                )

                                scope.launch {
                                    snackbarHostState.showSnackbar(strings.addMedicationSaved)
                                    navController.navigate("home")
                                }
                            }
                        }
                    ) { Text(strings.save) }

                    // Clear button
                    Button(
                        onClick = {
                            name = ""
                            dosage = ""
                            selectedFrequency = ""
                            selectedTime = null
                            selectedType = ""
                            notes = ""
                        }
                    ){Text(strings.clear)}
                }
            }
        }
    }
}