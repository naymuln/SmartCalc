package com.bankasia.smartcalc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bankasia.smartcalc.data.local.ThemePreferences

/**
 * Settings screen for theme selection and app configuration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val themeSelection by ThemePreferences.themeMode.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Theme selection
            SectionTitle(text = "Appearance")
            ThemeSelection(
                currentSelection = themeSelection,
                onSelectionChanged = { ThemePreferences.setThemeMode(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App info
            SectionTitle(text = "App Information")
            AppInfoSection()

            Spacer(modifier = Modifier.height(24.dp))

            // About section
            SectionTitle(text = "More Information")
            AboutSection(navController)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun ThemeSelection(
    currentSelection: String,
    onSelectionChanged: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose app theme appearance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            ThemeRadioButton(
                selected = currentSelection == ThemePreferences.MODE_LIGHT,
                onClick = { onSelectionChanged(ThemePreferences.MODE_LIGHT) },
                label = "Light"
            )
            ThemeRadioButton(
                selected = currentSelection == ThemePreferences.MODE_DARK,
                onClick = { onSelectionChanged(ThemePreferences.MODE_DARK) },
                label = "Dark"
            )
            ThemeRadioButton(
                selected = currentSelection == ThemePreferences.MODE_SYSTEM,
                onClick = { onSelectionChanged(ThemePreferences.MODE_SYSTEM) },
                label = "System Default"
            )
        }
    }
}

@Composable
private fun ThemeRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AppInfoSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        SettingsInfoRow("Package Name", "com.bankasia.smartcalc")
        SettingsInfoRow("Min SDK", "26 (Android 8.0)")
        SettingsInfoRow("Target SDK", "34 (Android 14)")
        SettingsInfoRow("Compile SDK", "34 (Android 14)")
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AboutSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(
            onClick = { navController.navigate("info") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("About / Info")
        }
    }
}