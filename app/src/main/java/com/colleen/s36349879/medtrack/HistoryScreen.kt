package com.colleen.s36349879.medtrack

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.data.factcheck.FactCheckHistory
import com.colleen.s36349879.medtrack.data.factcheck.FactCheckViewModel
import com.colleen.s36349879.medtrack.data.factcheck.Verdict
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel
import com.colleen.s36349879.medtrack.ui.localization.LocalStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * History tab: every past fact-check submission for the logged-in patient, newest
 * first. Tapping a card expands it in place to show the full original message,
 * response, and sources — this is the "detail view" from the spec, done as an
 * expandable card rather than a new nav route so the existing navigation
 * architecture doesn't need to change.
 *
 * Reads from [FactCheckViewModel.getHistory], the same records
 * [FactCheckViewModel.verifyClaim] already writes at the end of its existing
 * verification flow — this screen never re-runs or duplicates fact-checking.
 */
@Composable
fun HistoryScreen(
    navController: NavHostController,
    factCheckViewModel: FactCheckViewModel,
    patientViewModel: PatientViewModel
) {
    val strings = LocalStrings.current
    val patientId = patientViewModel.getLoggedInPatientId()
    val history by factCheckViewModel.getHistory(patientId).collectAsState(initial = emptyList())

    Scaffold(
        bottomBar = { MedTrackBottomBar(navController, currentRoute = "history") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Text(
                text = strings.historyTitle,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                color = colorResource(R.color.LightBlue)
            )
            Text(
                text = strings.historySubtitle,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                color = Color.Gray
            )

            if (history.isEmpty()) {
                Text(text = strings.historyEmpty, color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(history, key = { it.id }) { entry ->
                        HistoryCard(entry)
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(entry: FactCheckHistory) {
    val strings = LocalStrings.current
    var expanded by rememberSaveable(entry.id) { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val date = Date(entry.checkedAtEpochMillis)

    // FactCheckHistory.verdict stores Verdict.name (e.g. "TRUE") directly, written by
    // FactCheckViewModel.recordHistory — not the lowercase API string, so parse with valueOf.
    val verdict = runCatching { Verdict.valueOf(entry.verdict) }.getOrDefault(Verdict.UNVERIFIED)
    val (badgeColor, verdictLabel) = when (verdict) {
        Verdict.TRUE -> Color(0xFF2E7D32) to strings.verdictTrue
        Verdict.FALSE -> Color(0xFFC62828) to strings.verdictFalse
        Verdict.MISLEADING -> Color(0xFFF9A825) to strings.verdictMisleading
        Verdict.UNVERIFIED -> Color(0xFF757575) to strings.verdictUnverified
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .background(badgeColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(text = verdictLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Text(text = dateFormatter.format(date), color = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = timeFormatter.format(date), color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = entry.title, fontWeight = FontWeight.Bold)

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = strings.historyOriginalMessageLabel, fontWeight = FontWeight.Bold)
                Text(text = entry.originalMessage)

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = strings.historyResponseLabel, fontWeight = FontWeight.Bold)
                Text(text = entry.explanation)

                val sourceNames = entry.sourceNames.split(";").filter { it.isNotBlank() }
                if (sourceNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = strings.historySourcesLabel, fontWeight = FontWeight.Bold)
                    sourceNames.forEach { name -> Text(text = "• $name") }
                }
            }
        }
    }
}
