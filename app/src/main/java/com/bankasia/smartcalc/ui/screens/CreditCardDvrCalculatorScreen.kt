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
import com.bankasia.smartcalc.domain.calculator.CreditCardDvrCalculator
import com.bankasia.smartcalc.domain.calculator.CreditCardDvrCalculator.CreditCardCustomerType
import com.bankasia.smartcalc.domain.validation.CalculatorValidator
import com.bankasia.smartcalc.domain.validation.ValidationResult
import com.bankasia.smartcalc.ui.theme.formatCurrency
import java.math.BigDecimal
import java.math.RoundingMode

// Data classes for form state
private data class CCExistingLoanItem(
    val institution: String = "",
    val amount: String = ""
)

private data class CCCreditCardItem(
    val limit: String = "",
    val outstanding: String = ""
)

/**
 * Credit Card DVR Calculator Screen implementing Bank Asia Credit Card PPG methodology.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardDvrCalculatorScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Input state
    var monthlyNetSalary by remember { mutableStateOf("") }
    var customerType by remember { mutableStateOf(CreditCardCustomerType.Salaried) }
    var ownsOwnHouse by remember { mutableStateOf(false) }
    var hasHomeLoan by remember { mutableStateOf(false) }

    // Existing loans
    var existingLoanList by remember { mutableStateOf(listOf(CCExistingLoanItem())) }

    // Proposed credit card
    var proposedCardLimit by remember { mutableStateOf("") }

    // Existing credit cards
    var creditCardList by remember { mutableStateOf(listOf(CCCreditCardItem())) }

    // Results
    var result by remember { mutableStateOf<CreditCardDvrCalculator.Result?>(null) }
    var eligibilityMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCalculating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Credit Card DVR") },
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
            // Customer type
            CCCustomerTypeSection(
                customerType = customerType,
                onTypeSelected = { customerType = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Monthly net salary
            CCMonthlyNetSalarySection(
                monthlyNetSalary = monthlyNetSalary,
                onSalaryChanged = { monthlyNetSalary = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Existing loans
            CCExistingLoansSection(
                loanList = existingLoanList,
                onLoanListChanged = { existingLoanList = it },
                onAddLoan = { existingLoanList = existingLoanList + CCExistingLoanItem() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Proposed credit card
            CCProposedCardSection(
                proposedLimit = proposedCardLimit,
                onLimitChanged = { proposedCardLimit = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Existing credit cards
            CCCreditCardsSection(
                cardList = creditCardList,
                onCardListChanged = { creditCardList = it },
                onAddCard = { creditCardList = creditCardList + CCCreditCardItem() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // House ownership (for relaxation)
            CCHouseOwnershipSection(
                ownsOwnHouse = ownsOwnHouse,
                hasHomeLoan = hasHomeLoan,
                onOwnHouseChanged = { ownsOwnHouse = it },
                onHomeLoanChanged = { hasHomeLoan = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Check if DBR should be skipped for this customer type
            val skipMessage = CreditCardDvrCalculator.shouldSkipDbrCalculation(customerType)

            // Calculate button
            Button(
                onClick = {
                    // Validate and calculate
                    errorMessage = null
                    eligibilityMessage = null
                    isCalculating = true

                    try {
                        // Convert and validate inputs
                        val salaryBDT = if (monthlyNetSalary.isBlank()) null else BigDecimal(monthlyNetSalary.replace(",", ""))
                        val proposedLimitBDT = if (proposedCardLimit.isBlank()) BigDecimal.ZERO else BigDecimal(proposedCardLimit.replace(",", ""))

                        // Validate salary
                        val salaryValid = CalculatorValidator.validateNetSalary(salaryBDT)
                        val proposedLimitValid = CalculatorValidator.validateProposedCardLimit(proposedLimitBDT)

                        // Convert existing loans
                        val loanEmis = existingLoanList
                            .filter { it.amount.isNotBlank() }
                            .mapNotNull {
                                try {
                                    val amt = BigDecimal(it.amount.replace(",", ""))
                                    CreditCardDvrCalculator.ExistingLoanInput(it.institution, amt)
                                } catch (_: Exception) { null }
                            }

                        // Convert credit cards
                        val existingCards = creditCardList
                            .filter { it.limit.isNotBlank() || it.outstanding.isNotBlank() }
                            .mapNotNull {
                                try {
                                    val limit = if (it.limit.isNotBlank()) BigDecimal(it.limit.replace(",", "")) else BigDecimal.ZERO
                                    val outstanding = if (it.outstanding.isNotBlank()) BigDecimal(it.outstanding.replace(",", "")) else BigDecimal.ZERO
                                    CreditCardDvrCalculator.ExistingCardInput(limit, outstanding)
                                } catch (_: Exception) { null }
                            }

                        if (salaryValid is ValidationResult.Valid &&
                            proposedLimitValid is ValidationResult.Valid) {

                            // Calculate using the calculator
                            val input = CreditCardDvrCalculator.Input(
                                monthlyNetSalary = salaryBDT!!,
                                existingLoanEmis = loanEmis,
                                proposedCardLimit = proposedLimitBDT,
                                existingCards = existingCards
                            )

                            var calcResult = CreditCardDvrCalculator.calculateDbr(input)

                            // Apply businessman/landlord cap if applicable
                            val cappedDbr = CreditCardDvrCalculator.applyBusinessmanLandlordCap(
                                calcResult.dbrValue.toDouble(),
                                customerType
                            )

                            // Apply own house relaxation
                            val finalAllowedDvr = CreditCardDvrCalculator.applyOwnHouseRelaxation(
                                calcResult.maximumAllowedDvr,
                                ownsOwnHouse,
                                hasHomeLoan
                            )

                            // Check eligibility
                            val isEligible = cappedDbr <= finalAllowedDvr
                            eligibilityMessage = if (isEligible) "ELIGIBLE" else "NOT ELIGIBLE"

                            result = calcResult.copy(
                                maximumAllowedDvr = finalAllowedDvr,
                                isEligible = isEligible
                            )

                            isCalculating = false
                        } else {
                            // Show validation error
                            errorMessage = when {
                                salaryValid is ValidationResult.Invalid -> salaryValid.errorMessage
                                proposedLimitValid is ValidationResult.Invalid -> proposedLimitValid.errorMessage
                                else -> "Please check your inputs"
                            }
                            isCalculating = false
                        }
                    } catch (e: Exception) {
                        errorMessage = "Calculation error: ${e.message}"
                        isCalculating = false
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
                    Text("Calculate DVR")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Skip message for certain customer types
            skipMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Eligibility message
            eligibilityMessage?.let {
                Text(
                    text = it,
                    color = if (it == "ELIGIBLE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results
            result?.let { res ->
                CCDvrResultSection(res)
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
private fun CCCustomerTypeSection(
    customerType: CreditCardCustomerType,
    onTypeSelected: (CreditCardCustomerType) -> Unit
) {
    Text(
        text = "Customer Type",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Column {
        CreditCardCustomerType.values().forEach { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = customerType == type,
                    onClick = { onTypeSelected(type) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getCCCustomerTypeLabel(type),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun getCCCustomerTypeLabel(type: CreditCardCustomerType): String {
    return when (type) {
        CreditCardCustomerType.Salaried -> "Salaried"
        CreditCardCustomerType.Businessman -> "Businessman"
        CreditCardCustomerType.Landlord -> "Landlord"
        CreditCardCustomerType.Other -> "Other"
        CreditCardCustomerType.GridCustomer -> "Grid Customer"
        CreditCardCustomerType.BankAsiaStaffCard -> "Bank Asia Staff Card"
    }
}

@Composable
private fun CCMonthlyNetSalarySection(
    monthlyNetSalary: String,
    onSalaryChanged: (String) -> Unit
) {
    Text(
        text = "Monthly Net Salary (BDT)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        label = { Text("Take-home monthly salary after deductions") },
        value = monthlyNetSalary,
        onValueChange = { onSalaryChanged(it) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CCExistingLoansSection(
    loanList: List<CCExistingLoanItem>,
    onLoanListChanged: (List<CCExistingLoanItem>) -> Unit,
    onAddLoan: () -> Unit
) {
    Text(
        text = "Existing Installment Loans",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))

    Column {
        loanList.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Institution (optional)
                OutlinedTextField(
                    label = { Text("Bank (optional)") },
                    value = item.institution,
                    onValueChange = {
                        val updated = loanList.toMutableList()
                        updated[index] = item.copy(institution = it)
                        onLoanListChanged(updated)
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Monthly installment (required)
                OutlinedTextField(
                    label = { Text("Monthly EMI (BDT)") },
                    value = item.amount,
                    onValueChange = {
                        val updated = loanList.toMutableList()
                        updated[index] = item.copy(amount = it)
                        onLoanListChanged(updated)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            if (index < loanList.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            }
        }

        // Add loan button
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onAddLoan,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add loan"
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Loan")
        }
    }
}

@Composable
private fun CCProposedCardSection(
    proposedLimit: String,
    onLimitChanged: (String) -> Unit
) {
    Text(
        text = "Proposed Credit Card Limit (BDT)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        label = { Text("Amount being applied for") },
        value = proposedLimit,
        onValueChange = { onLimitChanged(it) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CCCreditCardsSection(
    cardList: List<CCCreditCardItem>,
    onCardListChanged: (List<CCCreditCardItem>) -> Unit,
    onAddCard: () -> Unit
) {
    Text(
        text = "Existing Credit Cards",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))

    Column {
        cardList.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Credit limit
                OutlinedTextField(
                    label = { Text("Credit Limit (BDT)") },
                    value = item.limit,
                    onValueChange = {
                        val updated = cardList.toMutableList()
                        updated[index] = item.copy(limit = it)
                        onCardListChanged(updated)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Average outstanding
                OutlinedTextField(
                    label = { Text("Avg Outstanding (BDT)") },
                    value = item.outstanding,
                    onValueChange = {
                        val updated = cardList.toMutableList()
                        updated[index] = item.copy(outstanding = it)
                        onCardListChanged(updated)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            if (index < cardList.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            }
        }

        // Add card button
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onAddCard,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add card"
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Credit Card")
        }
    }
}

@Composable
private fun CCHouseOwnershipSection(
    ownsOwnHouse: Boolean,
    hasHomeLoan: Boolean,
    onOwnHouseChanged: (Boolean) -> Unit,
    onHomeLoanChanged: (Boolean) -> Unit
) {
    Text(
        text = "House Ownership (for relaxation)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Own / family-owned house?",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = ownsOwnHouse,
            onCheckedChange = { onOwnHouseChanged(it) }
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Home loan on this house?",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = hasHomeLoan,
            onCheckedChange = { onHomeLoanChanged(it) }
        )
    }
    // Add info about relaxation
    if (ownsOwnHouse && !hasHomeLoan) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "(Up to 5% relaxation may apply)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun CCDvrResultSection(result: CreditCardDvrCalculator.Result) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // DBR percentage (main result)
            Text(
                text = "Credit Card DVR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${result.dbrValue.setScale(2, RoundingMode.HALF_UP)}%",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = if (result.isEligible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleSmall
                )
                val statusText = if (result.isEligible) "ELIGIBLE" else "NOT ELIGIBLE"
                val statusColor = if (result.isEligible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Breakdown section
            Text(
                text = "Calculation Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            CCBreakdownRow("Monthly Net Salary", formatCurrency(result.totalNetSalary))
            CCBreakdownRow("Existing Loan EMIs", formatCurrency(result.existingLoanEmis))
            CCBreakdownRow("5% Proposed Card Limit", formatCurrency(result.fivePercentProposedCardLimit))
            CCBreakdownRow("3% Existing Card Limits", formatCurrency(result.threePercentExistingCardLimits))
            CCBreakdownRow("5% Avg Outstanding", formatCurrency(result.fivePercentAverageOutstanding))
            CCBreakdownRow("Higher Card Liability", formatCurrency(result.higherExistingCardLiability))
            CCBreakdownRow("Total Monthly Obligation", formatCurrency(result.totalMonthlyObligations))
            Spacer(modifier = Modifier.height(8.dp))
            CCBreakdownRow("DVR", "${result.dbrValue.setScale(2, RoundingMode.HALF_UP)}%", isBold = true)
            CCBreakdownRow("Maximum Allowed DVR", "${result.maximumAllowedDvr}%", isBold = true)
        }
    }
}

@Composable
private fun CCBreakdownRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}