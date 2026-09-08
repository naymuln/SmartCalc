package com.bankasia.smartcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bankasia.smartcalc.data.local.ThemePreferences
import com.bankasia.smartcalc.ui.screens.*
import com.bankasia.smartcalc.ui.theme.SmartCalcTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by ThemePreferences.themeMode.collectAsState()
            val isDark = when (themeMode) {
                ThemePreferences.MODE_LIGHT -> false
                ThemePreferences.MODE_DARK -> true
                else -> isSystemInDarkTheme()
            }

            SmartCalcTheme(darkTheme = isDark) {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        composable("home") {
                            HomeScreen(navController = navController)
                        }
                        composable("emi") {
                            EmiCalculatorScreen(navController = navController)
                        }
                        composable("personal_loan_dvr") {
                            PersonalLoanDvrCalculatorScreen(navController = navController)
                        }
                        composable("credit_card_dvr") {
                            CreditCardDvrCalculatorScreen(navController = navController)
                        }
                        composable("history") {
                            HistoryScreen(navController = navController)
                        }
                        composable("info") {
                            InfoScreen()
                        }
                        composable("settings") {
                            SettingsScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}