package com.colleen.s36349879.medtrack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.genAI.GenAIViewModel
import com.colleen.s36349879.medtrack.data.medication.MedicationViewModel
import com.colleen.s36349879.medtrack.data.network.DrugResult
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel
import com.colleen.s36349879.medtrack.data.symptom.SymptomViewModel
import com.colleen.s36349879.medtrack.data.tip.TipViewModel
import com.colleen.s36349879.medtrack.ui.localization.LocalStrings


// A card that displays the FDA drug label information for a searched medication
@Composable
fun DrugResultCard(result: DrugResult) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                strings.medCoachFdaLabelInfo,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.LightBlue)
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // Each section of the drug label is displayed in its own expandable block
            ExpandableDrugDetail(
                label = strings.medCoachPurpose,
                // firstOrNull() gets the first item or null if the list is empty
                content = result.purpose?.firstOrNull()
            )

            ExpandableDrugDetail(
                label = strings.medCoachWarnings,
                content = result.warnings?.firstOrNull(),
                isWarning = true // Flags this section to display in red
            )

            ExpandableDrugDetail(
                label = strings.medCoachDosageAdmin,
                content = result.dosageAndAdministration?.firstOrNull()
            )

            ExpandableDrugDetail(
                label = strings.medCoachIndications,
                content = result.indicationsAndUsage?.firstOrNull()
            )
        }
    }
}

// A reusable component that shows a drug label section with a "Read More" toggle
// if the content is long. isWarning makes the label appear in red.
@Composable
fun ExpandableDrugDetail(label: String, content: String?, isWarning: Boolean = false) {
    val strings = LocalStrings.current

    // Only render this section if there is actual content to show
    if (!content.isNullOrBlank()) {

        // Tracks whether the full text is expanded or collapsed
        var isExpanded by remember { mutableStateOf(false) }

        Column(modifier = Modifier.padding(vertical = 8.dp)) {

            // Section label
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                color = if (isWarning) Color.Red else colorResource(R.color.LightBlue),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Show the full text if expanded, or limit to 3 lines if collapsed
            // TextOverflow.Ellipsis adds "..." at the end when the text is cut off
            Text(
                text = content,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                // Limit to 3 lines if not expanded
                maxLines = if (isExpanded) Int.MAX_VALUE else 3, // Int.MAX_VALUE means no line limit
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            // Only show the Read More / Show Less button if the content is long enough to need it
            if (content.length > 100) {
                TextButton(
                    onClick = { isExpanded = !isExpanded }, // Toggle between expanded and collapsed
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = if (isExpanded) strings.showLess else strings.readMore, // Label changes based on state
                        fontSize = 12.sp,
                        color = colorResource(R.color.LightBlue),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
        }
    }
}

// The MedCoach screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedCoachScreen(
    navController: NavHostController,
    medicationViewModel: MedicationViewModel,
    genAiViewModel: GenAIViewModel,
    tipViewModel: TipViewModel,
    symptomViewModel: SymptomViewModel,
    patientViewModel: PatientViewModel
){
    val strings = LocalStrings.current

    // The text the user types in the search field
    var searchQuery by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }

    // Controls whether the tip history dialog is shown
    var showHistory by remember { mutableStateOf(false) }

    val patientId = patientViewModel.getLoggedInPatientId()

    // Load the patient's AI tip history as a reactive stream
    val tipHistory by tipViewModel.getHistory(patientId).collectAsState(initial = emptyList())

    // Load the patient's symptoms and extract just the category names for the AI prompt
    val mySymptoms by symptomViewModel.getSymptomsForPatient(patientId).collectAsState(initial = emptyList())
    val symptomCategory = mySymptoms.map { it.category }

    // Load the patient's medication names to show in the search dropdown and pass to the AI
    val myMeds by medicationViewModel.getMyMedicationNames(patientId).collectAsState(initial = emptyList())
    val patient by patientViewModel.getCurrentPatient().collectAsState(initial = null)
    val allergies = remember(patient?.medicineAllergies) {
        patient?.medicineAllergies.orEmpty().split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
    val intakeAllergyMatches = remember(myMeds, allergies) {
        myMeds.filter { med -> allergies.any { allergy -> med.equals(allergy, ignoreCase = true) } }
    }

    Scaffold(
        bottomBar = { MedTrackBottomBar(navController, currentRoute = "med_coach") }
    ){ innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Screen title and subtitle
            item{
                Column{
                    Text(
                        text = strings.medCoachTitle,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.LightBlue)
                    )

                    Text(
                        text = strings.medCoachSubtitle,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // Drug search section with a dropdown showing the patient's own medications
            item{
                Text (
                    text = strings.medCoachSearchLabel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )

                // ExposedDropdownMenuBox combines a text field with a dropdown list
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {expanded = !expanded},
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            // Only show the dropdown if the user has typed something AND has medications
                            expanded = it.isNotEmpty() && myMeds.isNotEmpty()
                                        },
                        label = {Text(strings.medCoachSearchPlaceholder)},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
                        trailingIcon = {
                            IconButton(onClick = {
                                medicationViewModel.fetchDrugDetails(searchQuery)
                                expanded = false
                            }){
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                        // Also trigger the search when the user presses the Search key on the keyboard
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            medicationViewModel.fetchDrugDetails(searchQuery)
                            expanded = false
                        })
                    )

                    // Show the patient's own medications as quick-select dropdown options
                    if (myMeds.isNotEmpty()){
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {expanded = false}
                        ) {

                            // Create one dropdown item per medication name
                            myMeds.forEach{ medName ->
                                DropdownMenuItem(
                                    text = {Text(medName)},
                                    onClick = {
                                        searchQuery = medName
                                        expanded = false
                                        medicationViewModel.fetchDrugDetails(medName)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Drug search results section
            item {
                // Show a loading spinner while the API call is in progress
                if (medicationViewModel.isSearching) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorResource(R.color.LightBlue))
                    }
                }

                // Show an error message if the search failed or returned no result
                // ?.let only runs if apiError is not null
                medicationViewModel.apiError?.let { error ->
                    Text(error, color = Color.Red, fontSize = 14.sp)
                }

                // Show the drug result card if a successful result was returned
                medicationViewModel.drugDetails?.let { result ->
                    DrugResultCard(result)
                }
            }

            // High-priority patient safety flag when an intake record exactly matches a recorded allergy.
            if (intakeAllergyMatches.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE5E5))
                    ) {
                        Text(
                            text = strings.medCoachAllergyWarning,
                            color = Color(0xFF9B0000),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            // AI tip section
            item{

                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

                Spacer(Modifier.height(8.dp))

                Column()
                {
                    // Button to request a personalized AI tip
                    // Passes the patient's medications and symptoms to the AI for context
                    Button(
                        onClick = {genAiViewModel.generateMedicationTip (
                            medications = myMeds,
                            symptoms = symptomCategory,
                            allergies = allergies
                        ) {tip ->
                            // Callback that runs when the AI finishes generating the tip
                            // Save the tip to the database so it appears in history
                            tipViewModel.saveGeneratedTip(patientId, tip) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.LightBlue)),
                        enabled = !genAiViewModel.isLoading // Grey out the button while the AI is working
                    ){
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.medCoachGetTip)
                    }

                    // Show a loading spinner and message while the AI generates the tip
                    if (genAiViewModel.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = colorResource(R.color.LightBlue))
                                Spacer(Modifier.height(8.dp))
                                Text(strings.medCoachThinking, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }

                    // Show an error message if the AI request failed
                    genAiViewModel.errorMessage?.let { error ->
                        Text(error, color = Color.Red, fontSize = 13.sp)
                    }

                    // Show the generated tip in a blue card once it is available
                    genAiViewModel.aiTip?.let { tip ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Text(tip, modifier = Modifier.padding(16.dp), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Button to open the tip history dialog
                    TextButton(
                        onClick = { showHistory = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(strings.medCoachViewHistory, color = colorResource(R.color.LightBlue))
                    }
                }
            }
        }
    }

    // Tip history dialog that is shown as a popup when the user taps "View History"
    // It sits outside the Scaffold so it appears on top of everything
    if (showHistory) {
        AlertDialog(
            onDismissRequest = { showHistory = false }, // Close the dialog if the user taps outside it
            title = { Text(strings.medCoachHistoryTitle) },
            text = {
                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    if (tipHistory.isEmpty()) {
                        Text(strings.medCoachNoHistory)
                    } else {
                        // LazyColumn inside the dialog to handle long tip history
                        LazyColumn {
                            items(tipHistory) { tip ->
                                Text("• ${tip.tipContent}", modifier = Modifier.padding(vertical = 6.dp), fontSize = 13.sp)
                                HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showHistory = false }) { Text(strings.close) } }
        )
    }

}

