package com.colleen.s36349879.medtrack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.colleen.s36349879.medtrack.ui.localization.LocalStrings

/**
 * The one bottom navigation bar used by every primary patient screen (Home, Fact-Check,
 * Health Assistant, Symptoms, History, Settings, MedCoach).
 *
 * Before this component existed, each screen duplicated its own copy of this bar and
 * they'd drifted apart — Home's had 6 icons (missing History, which didn't exist yet)
 * while every other screen only had 4 (missing the Fact-Check and Health Assistant icons
 * entirely). This restores Home's full icon set as the one source of truth, adds
 * History to it, and applies it identically everywhere so position/height/padding/icon
 * style/selected-state can never drift apart again.
 *
 * @param currentRoute The nav route of the screen this bar is shown on, so its own icon
 *   can be tinted to indicate the selected tab (previously no screen did this at all).
 */
@Composable
fun MedTrackBottomBar(navController: NavHostController, currentRoute: String) {
    val strings = LocalStrings.current
    val selectedColor = colorResource(R.color.LightBlue)
    val unselectedColor = LocalContentColor.current

    data class NavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)

    val items = listOf(
        NavItem("home", Icons.Filled.Home, strings.navHome),
        NavItem("fact_check", Icons.Filled.FactCheck, strings.navFactCheck),
        NavItem("health_assistant", Icons.Filled.HealthAndSafety, strings.navHealthAssistant),
        NavItem("symptoms", Icons.AutoMirrored.Filled.List, strings.navSymptoms),
        NavItem("history", Icons.Filled.History, strings.navHistory),
        NavItem("settings", Icons.Filled.Settings, strings.navSettings),
        NavItem("med_coach", Icons.Filled.SupportAgent, strings.navMedCoach)
    )

    BottomAppBar(
        modifier = Modifier.height(60.dp),
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items.forEach { item ->
                    IconButton(onClick = {
                        if (item.route != currentRoute) navController.navigate(item.route)
                    }) {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = if (item.route == currentRoute) selectedColor else unselectedColor
                        )
                    }
                }
            }
        }
    )
}
