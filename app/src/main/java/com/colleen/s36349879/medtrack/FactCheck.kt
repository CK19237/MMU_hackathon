package com.colleen.s36349879.medtrack

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.factcheck.FactCheckLanguage
import com.colleen.s36349879.medtrack.data.factcheck.FactCheckResult
import com.colleen.s36349879.medtrack.data.factcheck.FactCheckUiState
import com.colleen.s36349879.medtrack.data.factcheck.FactCheckViewModel
import com.colleen.s36349879.medtrack.data.factcheck.SourceRef
import com.colleen.s36349879.medtrack.data.factcheck.Verdict
import java.util.Locale

/**
 * Fact-Check tab: the primary demo screen. Paste/type or speak a health claim,
 * pick a language, and get back a color-coded verdict with sources. Checks the
 * local myth cache first (instant) before calling the live grounded API.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactCheckScreen(
    navController: NavHostController,
    viewModel: FactCheckViewModel
) {
    val context = LocalContext.current
    var claimText by remember { mutableStateOf("") }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    val uiState = viewModel.uiState

    // Voice-to-text: launches the system speech recognizer (no RECORD_AUDIO
    // permission needed since this delegates capture to the recognizer app,
    // which keeps the hackathon build simple for elderly/low-literacy users).
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                claimText = spoken
            }
        }
    }

    fun launchVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeForLanguage(viewModel.selectedLanguage))
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the health claim you want to check")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Voice input isn't available on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(60.dp),
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { navController.navigate("home") }) {
                            Icon(Icons.Filled.Home, contentDescription = "Go Home")
                        }
                        IconButton(onClick = { navController.navigate("symptoms") }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Symptoms")
                        }
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = { navController.navigate("med_coach") }) {
                            Icon(Icons.Filled.SupportAgent, contentDescription = "MedCoach")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Text(
                text = "Fact-Check",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.LightBlue)
            )
            Text(
                text = "Paste a forwarded message or type a health claim to verify it against trusted sources.",
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Language selector
            ExposedDropdownMenuBox(
                expanded = languageMenuExpanded,
                onExpandedChange = { languageMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = viewModel.selectedLanguage.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Language") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = { languageMenuExpanded = false }
                ) {
                    FactCheckLanguage.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang.displayName) },
                            onClick = {
                                viewModel.selectedLanguage = lang
                                languageMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = claimText,
                onValueChange = { claimText = it },
                label = { Text("Health claim") },
                placeholder = { Text("e.g. \"5G towers spread COVID-19\"") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { launchVoiceInput() }) {
                        Icon(Icons.Filled.Mic, contentDescription = "Voice input")
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.verifyClaim(claimText) },
                enabled = uiState !is FactCheckUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.LightBlue))
            ) {
                if (uiState is FactCheckUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Check this claim")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (uiState) {
                is FactCheckUiState.Success -> VerdictCard(result = uiState.result, onFlag = { viewModel.flagCurrentResult() })
                is FactCheckUiState.Error -> Text(text = uiState.message, color = Color.Red)
                else -> {}
            }
        }
    }
}

/** Color-coded verdict card: green = True, red = False, amber = Misleading, gray = Unverified. */
@Composable
fun VerdictCard(result: FactCheckResult, onFlag: () -> Unit) {
    var flagged by remember(result) { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val (badgeColor, label) = when (result.verdict) {
        Verdict.TRUE -> Color(0xFF2E7D32) to "TRUE"
        Verdict.FALSE -> Color(0xFFC62828) to "FALSE"
        Verdict.MISLEADING -> Color(0xFFF9A825) to "MISLEADING"
        Verdict.UNVERIFIED -> Color(0xFF757575) to "UNVERIFIED"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.10f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(badgeColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(text = label, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (result.fromCache) {
                    Text(text = "Instant match (local cache)", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = result.explanation)

            if (!result.officialEvidenceFound) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠ No reliable Malaysian government source was found for this claim — this result is based on general web sources instead.",
                    fontSize = 12.sp,
                    color = Color(0xFF8A6D00)
                )
            }

            if (result.sources.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Evidence Source${if (result.sources.size > 1) "s" else ""}:", fontWeight = FontWeight.Bold)
                result.sources.forEach { source: SourceRef ->
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text(text = source.name, fontSize = 13.sp)
                        if (!source.url.isNullOrBlank()) {
                            Text(
                                text = "View official source",
                                fontSize = 13.sp,
                                color = colorResource(R.color.LightBlue),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { uriHandler.openUri(source.url) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onFlag(); flagged = true },
                enabled = !flagged
            ) {
                Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (flagged) "Sent to doctor review" else "Flag this verdict")
            }
        }
    }
}

/** Maps a [FactCheckLanguage] to a BCP-47 locale tag for the speech recognizer intent. */
private fun localeForLanguage(language: FactCheckLanguage): String = when (language) {
    FactCheckLanguage.ENGLISH -> Locale.ENGLISH.toLanguageTag()
    FactCheckLanguage.BAHASA_MALAYSIA -> "ms-MY"
    FactCheckLanguage.CHINESE -> "zh-CN"
    FactCheckLanguage.TAMIL -> "ta-IN"
}
