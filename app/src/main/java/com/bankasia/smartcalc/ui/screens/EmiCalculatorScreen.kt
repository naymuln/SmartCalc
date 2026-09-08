package com.bankasia.smartcalc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bankasia.smartcalc.domain.calculator.EmiCalculator
import com.bankasia.smartcalc.domain.validation.CalculatorValidator
import com.bankasia.smartcalc.domain.validation.ValidationResult
import com.bankasia.smartcalc.ui.theme.formatCurrency
import java.math.BigDecimal

/**
 * EMI Calculator Screen for calculating personal loan monthly installments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiCalculatorScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var principal by remember { mutableStateOf("") }
    var annualInterestRate by remember { mutableStateOf("") }
    var tenure by remember { mutableStateOf("") }
    var tenureUnit by remember { mutableStateOf(EmiCalculator.TenureUnit.YEARS) }
    var result by remember { mutableStateOf<EmiCalculator.Result?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCalculating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("EMI Calculator") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Input fields
            Column {
                OutlinedTextField(
                    label = { Text("Amount") },
                    placeholder = { Text("e.g., 10,00,000") },
                    value = principal,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' || it == ',' }) {
                            principal = input.replace(",", "")
                        }
                    },
                    visualTransformation = com.bankasia.smartcalc.ui.util.IndianNumberVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMessage != null && (errorMessage!!.contains("Loan amount") || errorMessage!!.contains("Amount")),
                    supportingText = {
                        if (errorMessage != null && (errorMessage!!.contains("Loan amount") || errorMessage!!.contains("Amount"))) {
                            Text(errorMessage!!)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        label = { Text("Interest Rate") },
                        placeholder = { Text("e.g., 9.5") },
                        value = annualInterestRate,
                        onValueChange = { annualInterestRate = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = errorMessage != null && errorMessage!!.contains("Interest rate"),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        label = { Text("Tenure") },
                        placeholder = { Text("e.g., 5") },
                        value = tenure,
                        onValueChange = { tenure = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorMessage != null && errorMessage!!.contains("Tenure"),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tenure unit selection - larger and easily noticeable buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (tenureUnit == EmiCalculator.TenureUnit.YEARS) {
                        Button(
                            onClick = { tenureUnit = EmiCalculator.TenureUnit.YEARS },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Text("Years", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { tenureUnit = EmiCalculator.TenureUnit.YEARS },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Text("Years")
                        }
                    }

                    if (tenureUnit == EmiCalculator.TenureUnit.MONTHS) {
                        Button(
                            onClick = { tenureUnit = EmiCalculator.TenureUnit.MONTHS },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Text("Months", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { tenureUnit = EmiCalculator.TenureUnit.MONTHS },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Text("Months")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Calculate button
            Button(
                onClick = {
                    // Validate inputs
                    val principalValid = CalculatorValidator.validatePrincipal(
                        if (principal.isBlank()) null else try { BigDecimal(principal.replace(",", "")) } catch (e: Exception) { null }
                    )
                    val rateValid = CalculatorValidator.validateInterestRate(
                        if (annualInterestRate.isBlank()) null else try { BigDecimal(annualInterestRate) } catch (e: Exception) { null }
                    )
                    val tenureValid = CalculatorValidator.validateTenure(
                        if (tenure.isBlank()) null else tenure.toIntOrNull()
                    )

                    if (principalValid is ValidationResult.Valid &&
                        rateValid is ValidationResult.Valid &&
                        tenureValid is ValidationResult.Valid) {

                        errorMessage = null
                        isCalculating = true

                        try {
                            // Perform calculation
                            val p = BigDecimal(principal.replace(",", ""))
                            val r = BigDecimal(annualInterestRate)
                            val t = if (tenureUnit == EmiCalculator.TenureUnit.YEARS) {
                                tenure.toInt() * 12
                            } else {
                                tenure.toInt()
                            }

                            result = EmiCalculator.calculate(
                                EmiCalculator.Input(
                                    principal = p,
                                    annualInterestRate = r,
                                    tenure = t,
                                    tenureUnit = EmiCalculator.TenureUnit.MONTHS // internal is months
                                )
                            )
                        } catch (e: Exception) {
                            errorMessage = "Calculation error: ${e.message}"
                        }

                        isCalculating = false
                    } else {
                        // Show first error found
                        errorMessage = when {
                            principalValid is ValidationResult.Invalid -> principalValid.errorMessage
                            rateValid is ValidationResult.Invalid -> rateValid.errorMessage
                            tenureValid is ValidationResult.Invalid -> tenureValid.errorMessage
                            else -> "Please check your inputs"
                        }
                    }
                },
                enabled = !isCalculating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isCalculating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Calculate")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Results
            result?.let { res ->
                EmiResultSection(res)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun EmiResultSection(result: EmiCalculator.Result) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Calculation Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Monthly EMI (most important result)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = "Monthly EMI",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.monthlyEmiDisplay,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // Other results
            EmiResultRow("Principal Amount", formatCurrency(result.principal))
            EmiResultRow("Total Interest", formatCurrency(result.totalInterest))
            EmiResultRow("Total Repayment", formatCurrency(result.totalRepayment))
        }
    }
}

@Composable
private fun EmiResultRow(label: String, value: String) {
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
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}