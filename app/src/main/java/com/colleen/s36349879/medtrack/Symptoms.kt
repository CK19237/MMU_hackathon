package com.colleen.s36349879.medtrack


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel
import com.colleen.s36349879.medtrack.data.symptom.Symptom
import com.colleen.s36349879.medtrack.data.symptom.SymptomViewModel
import com.colleen.s36349879.medtrack.ui.localization.LocalStrings
import com.colleen.s36349879.medtrack.ui.localization.Strings
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


// Returns a label based on severity value
fun getSeverityLabel(severity: Int, strings: Strings): String {
    return when (severity){
        in 1..3 -> strings.severityMild
        in 4..6 -> strings.severityModerate
        in 7..10 -> strings.severityHigh
        else -> strings.severityUnknown
    }
}

// Returns a color based on severity value
@Composable
fun getSeverityColor(severity: Int): Color{
    return when (severity){
        in 1..3 -> Color.Green
        in 4..6 -> colorResource(R.color.coral)
        in 7..10 -> Color.Red
        else -> Color.Gray
    }
}

// Shows a date picker and returns the selected date
// onConfirm is used so the SymptomsScreen can store the selected date
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerFun(
    onConfirm: (String) -> Unit
){
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        // The calendar UI
        DatePicker(state = datePickerState)

        // When button pressed, convert the selected date from millis to
        // string and return it
        Button (onClick = {
            datePickerState.selectedDateMillis?.let{millis ->
                onConfirm(convertMillisToDate(millis))
            }
        }){
            Text(LocalStrings.current.confirmDate)
        }
    }
}

// Converts milliseconds to a formatted date string
fun convertMillisToDate(millis: Long): String{
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}

//Symptoms Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsScreen(
    navController: NavHostController,
    symptomViewModel: SymptomViewModel,
    patientViewModel: PatientViewModel
){

    val strings = LocalStrings.current

    //Snackbar values
    val snackbarHostState = remember{ SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Get current logged in patient ID
    val patientId = patientViewModel.getLoggedInPatientId()
    val symptomsList by symptomViewModel.getSymptomsForPatient(patientId).collectAsState(initial = emptyList())

    // variables to store the inputs
    var selectedCategory by remember { mutableStateOf("") }
    var expanded by remember {mutableStateOf(false)}
    var severity by remember {mutableFloatStateOf(1f)}
    var notes by remember {mutableStateOf("")}
    var selectedDateText by remember { mutableStateOf("") }
    var selectedTimeText by remember { mutableStateOf("") }
    val patient by patientViewModel.getCurrentPatient().collectAsState(initial = null)
    var medicineAllergies by remember(patient?.medicineAllergies) { mutableStateOf(patient?.medicineAllergies.orEmpty()) }

    // Max words for notes
    val maxWords = 200

    // Error states
    var categoryError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var timeError by remember {mutableStateOf(false)}
    val severityError = severity !in 1f..10f

    //Dropdown options for categories
    val categories = listOf("Pain", "Nausea", "Dizziness", "Fatigue", "Headache", "Skin Reaction", "Other")

    Scaffold(
        snackbarHost = {SnackbarHost(hostState = snackbarHostState)},
        bottomBar = { MedTrackBottomBar(navController, currentRoute = "symptoms") }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            item{
                Text(
                    text = strings.symptomsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.LightBlue)
                )
            }

            //Symptoms Category Dropdown Menu
            item {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {expanded = !expanded}
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = {Text(strings.symptomsCategoryLabel)},
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded)},
                        isError = categoryError,
                        supportingText = {
                            if (categoryError) {
                                Text(
                                    text = strings.symptomsCategoryRequired,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )

                    // Validate if none is chosen
                    if (selectedCategory.isBlank()){
                        categoryError = true
                    }

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {expanded = false}
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {Text(category)},
                                onClick = {
                                    selectedCategory = category
                                    categoryError = false
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Shared patient allergy context used by Fact Checker and Personalized AI Tips.
            item {
                OutlinedTextField(
                    value = medicineAllergies,
                    onValueChange = { medicineAllergies = it },
                    label = { Text(strings.symptomsMedicineAllergiesLabel) },
                    supportingText = { Text(strings.symptomsMedicineAllergiesHint) },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        patientViewModel.updateMedicineAllergies(patientId, medicineAllergies.trim())
                        scope.launch { snackbarHostState.showSnackbar(strings.symptomsSavedMessage) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.LightBlue))
                ) {
                    Text(strings.symptomsMedicineAllergiesSave)
                }
            }

            //Severity Slider
            // Slider and label changes based on severity
            item{
                val severityInt = severity.toInt()
                val severityLabel = getSeverityLabel(severityInt, strings)
                val severityColor = getSeverityColor(severityInt)

                Text(
                    text = strings.symptomsSeverityLabel.format(severityInt, severityLabel),
                    color = severityColor,
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = severity,
                    onValueChange = { severity = it },
                    valueRange = 1f..10f,
                    colors = SliderDefaults.colors(
                        thumbColor = severityColor,
                        activeTrackColor = severityColor,
                        inactiveTrackColor = severityColor.copy(alpha = 0.3f)
                    ),
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                // Validate if the slider is in range
                if (severityError){
                    Text(
                        text = strings.symptomsSeverityRangeError,
                        color = MaterialTheme.colorScheme.error
                    )
                }

            }

            //Enter Notes
            // Has a limit of 200 characters
            item{
                OutlinedTextField(
                    value = notes,
                    onValueChange = {if (it.length <= maxWords) notes = it},
                    label = {Text(strings.symptomsNotesLabel) },
                    supportingText = {Text("${notes.length}/200")},
                    modifier = Modifier.fillMaxWidth()
                )
            }

            //Date and Time selection
            item{
                Text(strings.symptomsWhenOccurred, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // when date and time is selected, show them together
                if (selectedDateText.isNotBlank() && selectedTimeText.isNotBlank()){
                    Text(
                        text = "$selectedDateText $selectedTimeText",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Date picker
                Text(strings.symptomsSelectDate)
                Spacer(modifier = Modifier.height(8.dp))

                DatePickerFun(
                    onConfirm = {dateString ->
                        selectedDateText = dateString
                        dateError = false
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Time Picker
                Text(strings.symptomsSelectTime)
                Spacer(modifier = Modifier.height(8.dp))

                TimePickerFun(
                    onConfirm = {timeState ->
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, timeState.hour)
                        cal.set(Calendar.MINUTE, timeState.minute)

                        // Format the time
                        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                        selectedTimeText = formatter.format(cal.time)
                        timeError = false
                    }
                )

                // Show error if date and time is not selected
                if (selectedDateText.isBlank()){
                    dateError = true

                    Text(
                        text = strings.symptomsDateRequired,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (selectedTimeText.isBlank()){
                    timeError = true

                    Text(
                        text = strings.symptomsTimeRequired,
                        color = MaterialTheme.colorScheme.error
                    )
                }

            }

            //Save Button
            // Validates then saves, and refreshes the list
            item{
                Button(
                    onClick = {

                        if(!categoryError && !dateError && !timeError) {

                            // Only save if the user filled in the required parts
                            patientViewModel.updateMedicineAllergies(patientId, medicineAllergies.trim())

                            symptomViewModel.addSymptom(
                                category = selectedCategory,
                                severity = severity.toInt(),
                                notes = notes.trim(),
                                date = selectedDateText,
                                time = selectedTimeText
                            )

                            scope.launch {
                                snackbarHostState.showSnackbar(strings.symptomsSavedMessage)
                            }

                            selectedCategory = ""
                            severity = 1f
                            notes = ""
                            selectedDateText = ""
                            selectedTimeText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.LightBlue),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.symptomsSaveButton)
                }
            }

            //Symptom History
            item{
                Spacer(modifier = Modifier.height(20.dp))
                // Title
                Text(
                    text = strings.symptomsHistoryTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // If the database is empty, show a message. Otherwise, show cards.
            if (symptomsList.isEmpty()) {
                item{
                    Text(strings.symptomsNoneLogged)
                }
            }
            // Show all symptoms
            else {
                items(symptomsList) {symptom ->
                    SymptomCard(symptom) // Build a card for every symptom in the list
                }
            }
        }

    }

}

@Composable
fun SymptomCard(symptom: Symptom){
    val strings = LocalStrings.current
    val symptomSeverityColor = getSeverityColor(symptom.severity)
    val symptomSeverityLabel = getSeverityLabel(symptom.severity, strings)

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = symptomSeverityColor.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = symptom.category,
                fontWeight = FontWeight.Bold
            )

            Text(strings.symptomsDateTimeLabel.format(symptom.symptomDateTime))

            Text(
                text = strings.symptomsSeverityLabel.format(symptom.severity, symptomSeverityLabel),
                color = symptomSeverityColor
            )

            Text(strings.symptomsNotesPrefix.format(symptom.symptomNotes))
        }
    }
}
