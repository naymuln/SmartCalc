package com.bankasia.smartcalc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Main home/dashboard screen showing calculator options and recent calculations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App greeting/title
        GreetingSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Financial calculators section
        CalculatorsSection(navController)

        Spacer(modifier = Modifier.height(24.dp))

        // Recent calculations section
        RecentCalculationsSection()
    }
}

@Composable
private fun GreetingSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SmartCalc",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Professional Banking Calculators",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorsSection(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Financial Calculators",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // EMI Calculator Card
        CalculatorCard(
            icon = Icons.Filled.Schedule,
            title = "EMI Calculator",
            description = "Calculate monthly loan installment",
            onClick = { navController.navigate("emi") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Personal Loan DVR Calculator Card
        CalculatorCard(
            icon = Icons.Filled.AccountBalance,
            title = "Personal Loan DVR",
            description = "Calculate repayment capacity according to Bank Asia PPD",
            onClick = { navController.navigate("personal_loan_dvr") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Credit Card DVR Calculator Card
        CalculatorCard(
            icon = Icons.Filled.CreditCard,
            title = "Credit Card DVR",
            description = "Calculate credit-card repayment capacity according to Bank Asia PPG",
            onClick = { navController.navigate("credit_card_dvr") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // History Card
        CalculatorCard(
            icon = Icons.Filled.History,
            title = "Calculation History",
            description = "View and manage past calculations",
            onClick = { navController.navigate("history") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Info Card
        CalculatorCard(
            icon = Icons.Filled.Info,
            title = "About",
            description = "App info, methodology, and disclaimer",
            onClick = { navController.navigate("info") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentCalculationsSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Recent Calculations",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // TODO: Replace with actual history data
        RecentCalculationsPlaceholder()
    }
}

@Composable
private fun RecentCalculationsPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No recent calculations",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}