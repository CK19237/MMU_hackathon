package com.colleen.s36349879.medtrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.doctorreview.DoctorReviewViewModel
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedItem
import com.colleen.s36349879.medtrack.data.doctorreview.FlaggedSourceTab
import com.colleen.s36349879.medtrack.data.doctorreview.ReviewStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clinician-only screen listing flagged verdicts/responses from the
 * Fact-Check and Health Assistant tabs. This is a pure async review queue —
 * nothing here blocks or delays what patients already saw; it exists purely
 * so a clinician can later confirm or correct an AI response.
 *
 * Reached from the Clinician Dashboard, so it inherits that flow's existing
 * clinician-password gate rather than adding a second auth mechanism.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorReviewScreen(
    navController: NavHostController,
    viewModel: DoctorReviewViewModel
) {
    val items by viewModel.allFlaggedItems.collectAsState(initial = emptyList())
    var noteDraft by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor Review Queue") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No flagged items yet.", color = Color.Gray)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { it.id }) { item: FlaggedItem ->
                FlaggedItemCard(
                    item = item,
                    note = noteDraft[item.id].orEmpty(),
                    onNoteChange = { noteDraft = noteDraft + (item.id to it) },
                    onMarkReviewed = { viewModel.markReviewed(item.id, noteDraft[item.id]) },
                    onMarkCorrected = { viewModel.markCorrected(item.id, noteDraft[item.id].orEmpty()) }
                )
            }
        }
    }
}

@Composable
private fun FlaggedItemCard(
    item: FlaggedItem,
    note: String,
    onNoteChange: (String) -> Unit,
    onMarkReviewed: () -> Unit,
    onMarkCorrected: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()) }
    val tabLabel = when (item.sourceTab) {
        FlaggedSourceTab.FACT_CHECK -> "Fact-Check"
        FlaggedSourceTab.HEALTH_ASSISTANT -> "Health Assistant"
    }
    val statusColor = when (item.status) {
        ReviewStatus.PENDING -> Color(0xFFF9A825)
        ReviewStatus.REVIEWED -> Color(0xFF2E7D32)
        ReviewStatus.CORRECTED -> Color(0xFF1565C0)
    }

    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = tabLabel, fontWeight = FontWeight.Bold)
                Text(text = item.status.name, color = statusColor, fontWeight = FontWeight.Bold)
            }
            Text(text = dateFormat.format(Date(item.flaggedAtEpochMillis)), color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Input:", fontWeight = FontWeight.Bold)
            Text(text = item.originalInput)

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "AI output:", fontWeight = FontWeight.Bold)
            Text(text = item.aiOutput)

            if (item.status == ReviewStatus.PENDING) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text("Clinician note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = onMarkReviewed, modifier = Modifier.weight(1f)) {
                        Text("Mark reviewed")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onMarkCorrected, modifier = Modifier.weight(1f)) {
                        Text("Mark corrected")
                    }
                }
            } else if (!item.clinicianNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Clinician note: ${item.clinicianNote}", color = Color.DarkGray)
            }
        }
    }
}
