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
import com.bankasia.smartcalc.domain.calculator.PersonalLoanDvrCalculator
import com.bankasia.smartcalc.domain.calculator.PersonalLoanDvrCalculator.CustomerCategory
import com.bankasia.smartcalc.domain.validation.CalculatorValidator
import com.bankasia.smartcalc.domain.validation.ValidationResult
import com.bankasia.smartcalc.ui.theme.formatCurrency
import java.math.BigDecimal
import java.math.RoundingMode

// Data classes for form state
private data class PLExistingLoanItem(
    val institution: String = "",
    val outstanding: String = "",
    val amount: String = "",
    val include: Boolean = true
)

private data class PLCreditCardItem(
    val limit: String = "",
    val outstanding: String = ""
)

/**
 * Personal Loan DVR Calculator Screen implementing Bank Asia PPD-2026 methodology.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalLoanDvrCalculatorScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Input state
    var monthlyIncome by remember { mutableStateOf("") }
    var customerCategory by remember { mutableStateOf(CustomerCategory.CategoryAB) }
    var ownsOwnHouse by remember { mutableStateOf(false) }
    var hasHomeLoan by remember { mutableStateOf(false) }

    // Existing loans
    var existingLoanList by remember { mutableStateOf(listOf(PLExistingLoanItem())) }
    var ccAverageInterest by remember { mutableStateOf("") }

    // Credit cards
    var creditCardList by remember { mutableStateOf(listOf(PLCreditCardItem())) }

    // Proposed loan
    var proposedLoanAmount by remember { mutableStateOf("") }
    var proposedLoanRate by remember { mutableStateOf("") }
    var proposedLoanTenure by remember { mutableStateOf("") }
    var proposedLoanTenureUnit by remember { mutableStateOf(EmiCalculator.TenureUnit.YEARS) }

    // Results
    var result by remember { mutableStateOf<PersonalLoanDvrCalculator.DvrResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCalculating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Personal Loan DVR") },
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
            // Applicant category
            PLApplicantCategorySection(
                customerCategory = customerCategory,
                onCategorySelected = { customerCategory = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Monthly income
            PLIncomeSection(
                monthlyIncome = monthlyIncome,
                onIncomeChanged = { monthlyIncome = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Existing loans
            PLExistingLoansSection(
                loanList = existingLoanList,
                onLoanListChanged = { existingLoanList = it },
                onAddLoan = { existingLoanList = existingLoanList + PLExistingLoanItem() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CC/OD average interest
            PLCcOdInterestSection(
                averageInterest = ccAverageInterest,
                onInterestChanged = { ccAverageInterest = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Credit cards
            PLCreditCardsSection(
                cardList = creditCardList,
                onCardListChanged = { creditCardList = it },
                onAddCard = { creditCardList = creditCardList + PLCreditCardItem() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // House ownership
            PLHouseOwnershipSection(
                ownsOwnHouse = ownsOwnHouse,
                hasHomeLoan = hasHomeLoan,
                onOwnHouseChanged = { ownsOwnHouse = it },
                onHomeLoanChanged = { hasHomeLoan = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Proposed personal loan
            PLProposedLoanSection(
                loanAmount = proposedLoanAmount,
                interestRate = proposedLoanRate,
                tenure = proposedLoanTenure,
                tenureUnit = proposedLoanTenureUnit,
                onAmountChanged = { proposedLoanAmount = it },
                onRateChanged = { proposedLoanRate = it },
                onTenureChanged = { proposedLoanTenure = it },
                onTenureUnitChanged = { proposedLoanTenureUnit = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Calculate button
            Button(
                onClick = {
                    // Validate and calculate
                    errorMessage = null
                    isCalculating = true

                    try {
                        // Convert and validate inputs
                        val incomeBDT = if (monthlyIncome.isBlank()) null else BigDecimal(monthlyIncome.replace(",", ""))
                        val ccInterestBDT = if (ccAverageInterest.isBlank()) BigDecimal.ZERO else BigDecimal(ccAverageInterest.replace(",", ""))
                        val loanAmtBDT = if (proposedLoanAmount.isBlank()) null else BigDecimal(proposedLoanAmount.replace(",", ""))
                        val loanRateBDT = if (proposedLoanRate.isBlank()) null else BigDecimal(proposedLoanRate)
                        val loanTenureInt = if (proposedLoanTenure.isBlank()) null else proposedLoanTenure.toIntOrNull()

                        // Validate income
                        val incomeValid = CalculatorValidator.validateMonthlyIncome(incomeBDT)
                        val loanAmtValid = if (loanAmtBDT == null) ValidationResult.Invalid("Loan amount is required") else CalculatorValidator.validatePrincipal(loanAmtBDT)
                        val loanRateValid = if (loanRateBDT == null) ValidationResult.Invalid("Interest rate is required") else CalculatorValidator.validateInterestRate(loanRateBDT)
                        val loanTenureValid = if (loanTenureInt == null) ValidationResult.Invalid("Tenure is required") else CalculatorValidator.validateTenure(loanTenureInt)

                        if (incomeValid is ValidationResult.Valid &&
                            loanAmtValid is ValidationResult.Valid &&
                            loanRateValid is ValidationResult.Valid &&
                            loanTenureValid is ValidationResult.Valid) {

                            // Calculate proposed loan EMI
                            val loanTenureMonths = if (proposedLoanTenureUnit == EmiCalculator.TenureUnit.YEARS) {
                                loanTenureInt!! * 12
                            } else {
                                loanTenureInt!!
                            }
                            val proposedEmi = PersonalLoanDvrCalculator.calculateProposedLoanEmi(
                                principal = loanAmtBDT!!,
                                annualInterestRate = loanRateBDT!!,
                                tenureMonths = loanTenureMonths
                            )

                            // Convert existing loans
                            val existingEmis = existingLoanList
                                .filter { it.amount.isNotBlank() && it.include }
                                .mapNotNull {
                                    try { BigDecimal(it.amount.replace(",", "")) } catch (_: Exception) { null }
                                }
                                .filter { it > BigDecimal.ZERO }

                            // Calculate credit card liability
                            val totalCreditLimit = creditCardList
                                .filter { it.limit.isNotBlank() }
                                .mapNotNull {
                                    try { BigDecimal(it.limit.replace(",", "")) } catch (_: Exception) { null }
                                }
                                .fold(BigDecimal.ZERO) { acc, v -> acc.add(v) }

                            // Calculate DVR
                            result = PersonalLoanDvrCalculator.calculateDvr(
                                monthlyIncome = incomeBDT!!,
                                existingLoanEmis = existingEmis,
                                ccAverageMonthlyInterest = ccInterestBDT,
                                totalCreditCardLimit = totalCreditLimit,
                                proposedLoanEmi = proposedEmi,
                                ownsOwnHouse = ownsOwnHouse,
                                hasHomeLoan = hasHomeLoan,
                                customerCategory = customerCategory
                            )

                            isCalculating = false
                        } else {
                            // Show validation error
                            errorMessage = when {
                                incomeValid is ValidationResult.Invalid -> incomeValid.errorMessage
                                loanAmtValid is ValidationResult.Invalid -> loanAmtValid.errorMessage
                                loanRateValid is ValidationResult.Invalid -> loanRateValid.errorMessage
                                loanTenureValid is ValidationResult.Invalid -> loanTenureValid.errorMessage
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

            // Results
            result?.let { res ->
                PLDvrResultSection(res)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PLApplicantCategorySection(
    customerCategory: CustomerCategory,
    onCategorySelected: (CustomerCategory) -> Unit
) {
    Text(
        text = "Applicant Category",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = customerCategory == CustomerCategory.CategoryAB,
            onClick = { onCategorySelected(CustomerCategory.CategoryAB) },
            label = { Text("Category A/B") }
        )
        FilterChip(
            selected = customerCategory == CustomerCategory.Employee,
            onClick = { onCategorySelected(CustomerCategory.Employee) },
            label = { Text("Bank Asia Employee") }
        )
    }
}

@Composable
private fun PLIncomeSection(
    monthlyIncome: String,
    onIncomeChanged: (String) -> Unit
) {
    Text(
        text = "Monthly Income (BDT)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        label = { Text("Considered monthly income") },
        value = monthlyIncome,
        onValueChange = { onIncomeChanged(it) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PLExistingLoansSection(
    loanList: List<PLExistingLoanItem>,
    onLoanListChanged: (List<PLExistingLoanItem>) -> Unit,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    label = { Text("Monthly EMI (BDT)") },
                    value = item.amount,
                    onValueChange = {
                        val updated = loanList.toMutableList()
                        updated[index] = item.copy(amount = it)
                        onLoanListChanged(updated)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    label = { Text("Bank/Institution (optional)") },
                    value = item.institution,
                    onValueChange = {
                        val updated = loanList.toMutableList()
                        updated[index] = item.copy(institution = it)
                        onLoanListChanged(updated)
                    },
                    modifier = Modifier.fillMaxWidth()
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
private fun PLCcOdInterestSection(
    averageInterest: String,
    onInterestChanged: (String) -> Unit
) {
    Text(
        text = "CC/OD Average Monthly Interest (BDT)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        label = { Text("Average monthly interest from CC/OD accounts") },
        value = averageInterest,
        onValueChange = { onInterestChanged(it) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PLCreditCardsSection(
    cardList: List<PLCreditCardItem>,
    onCardListChanged: (List<PLCreditCardItem>) -> Unit,
    onAddCard: () -> Unit
) {
    Text(
        text = "Credit Cards",
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
private fun PLHouseOwnershipSection(
    ownsOwnHouse: Boolean,
    hasHomeLoan: Boolean,
    onOwnHouseChanged: (Boolean) -> Unit,
    onHomeLoanChanged: (Boolean) -> Unit
) {
    Text(
        text = "House Ownership",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Own house / father's house?",
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
    // Add info about restriction
    if (ownsOwnHouse && hasHomeLoan) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "(+10% relaxation not applicable with home loan)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
    if (ownsOwnHouse && !hasHomeLoan) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "(+10% DVR relaxation will apply)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PLProposedLoanSection(
    loanAmount: String,
    interestRate: String,
    tenure: String,
    tenureUnit: EmiCalculator.TenureUnit,
    onAmountChanged: (String) -> Unit,
    onRateChanged: (String) -> Unit,
    onTenureChanged: (String) -> Unit,
    onTenureUnitChanged: (EmiCalculator.TenureUnit) -> Unit
) {
    Text(
        text = "Proposed Personal Loan",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))

    // Loan amount
    OutlinedTextField(
        label = { Text("Loan Amount (BDT)") },
        value = loanAmount,
        onValueChange = { onAmountChanged(it) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Interest rate
    OutlinedTextField(
        label = { Text("Annual Interest Rate (%)") },
        value = interestRate,
        onValueChange = { onRateChanged(it) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Tenure
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            label = { Text("Tenure") },
            value = tenure,
            onValueChange = { onTenureChanged(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilterChip(
            selected = tenureUnit == EmiCalculator.TenureUnit.YEARS,
            onClick = { onTenureUnitChanged(EmiCalculator.TenureUnit.YEARS) },
            label = { Text("Years") }
        )
        Spacer(modifier = Modifier.width(4.dp))
        FilterChip(
            selected = tenureUnit == EmiCalculator.TenureUnit.MONTHS,
            onClick = { onTenureUnitChanged(EmiCalculator.TenureUnit.MONTHS) },
            label = { Text("Months") }
        )
    }
}

@Composable
private fun PLDvrResultSection(result: PersonalLoanDvrCalculator.DvrResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // DVR percentage (main result)
            Text(
                text = "Personal Loan DVR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${result.dvrValue.setScale(2, RoundingMode.HALF_UP)}%",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = if (result.dvrValue.toDouble() <= result.finalPermittedDvr)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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
                val isEligible = result.dvrValue.toDouble() <= result.finalPermittedDvr
                val statusText = if (isEligible) "ELIGIBLE" else "NOT ELIGIBLE"
                val statusColor = if (isEligible)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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

            val existingEmisTotal = result.existingLoanEmis.fold(BigDecimal.ZERO) { acc, v -> acc.add(v) }

            PLBreakdownRow("Considered Income", formatCurrency(result.totalConsideredIncome))
            PLBreakdownRow("Existing Loan EMIs", formatCurrency(existingEmisTotal))
            PLBreakdownRow("CC/OD Interest", formatCurrency(result.averageMonthlyInterest))
            PLBreakdownRow("3% Credit Card Liability", formatCurrency(result.creditCardThreePercentLiability))
            PLBreakdownRow("Proposed Loan EMI", formatCurrency(result.proposedLoanEmi))
            PLBreakdownRow("Total Monthly Obligation", formatCurrency(result.totalMonthlyObligations))
            Spacer(modifier = Modifier.height(8.dp))
            PLBreakdownRow("DVR", "${result.dvrValue.setScale(2, RoundingMode.HALF_UP)}%", isBold = true)
            PLBreakdownRow("Maximum Permitted DVR", "${result.finalPermittedDvr}%", isBold = true)

            val remainingCapacity = result.totalConsideredIncome.multiply(
                BigDecimal.valueOf(result.finalPermittedDvr / 100)
            ).subtract(result.totalMonthlyObligations)
            PLBreakdownRow("Remaining Capacity", formatCurrency(remainingCapacity))

            // Add explanation for own house relaxation if applicable
            if (result.permittedDvr != result.finalPermittedDvr) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "+10% own house relaxation applied",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PLBreakdownRow(label: String, value: String, isBold: Boolean = false) {
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