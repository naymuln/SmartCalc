# 📱 SmartCalc — Modern Banking & Financial Eligibility Suite

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_1.9.22-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose_BOM-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material_3-blue?style=flat)](https://m3.material.io/)
[![Hilt](https://img.shields.io/badge/DI-Dagger_Hilt-orange?style=flat)](https://dagger.dev/hilt/)
[![Room](https://img.shields.io/badge/Storage-Room_DB-green?style=flat)](https://developer.android.com/training/data-storage/room)
[![Min SDK](https://img.shields.io/badge/Min_SDK-26_(Android_8.0)-brightgreen?style=flat)](https://developer.android.com/)
[![Target SDK](https://img.shields.io/badge/Target_SDK-34_(Android_14)-blue?style=flat)](https://developer.android.com/)

**SmartCalc** is a financial assessment and retail banking calculation app built with modern Android engineering standards (**Jetpack Compose**, **Clean Architecture**, **MVI/MVVM**, and **Room DB**).

It simplifies loan calculations and credit underwriting assessments according to modern retail banking standards, specifically implementing **Bank Asia PLC** policy guidelines for Debt Burden Ratio (DBR) and Debt Volume Ratio (DVR) capacity assessment.

---

## ✨ Features

### 1. 📉 Reducing-Balance EMI Calculator
- High-precision loan amortization based on standard financial compounding:
  $$\text{EMI} = \frac{P \times r \times (1+r)^n}{(1+r)^n - 1}$$
- Supports flexible loan tenures (months or years).
- Handles edge cases seamlessly (including 0% interest and high-precision `BigDecimal` arithmetic).
- Detailed breakdown of **Principal**, **Total Interest Payable**, and **Total Repayment**.

### 2. 🏦 Personal Loan DVR Assessment (PPD-2026 Guidelines)
- Fast capacity-to-pay and disposable income calculation.
- Factoring in monthly gross/net income, statutory deductions, existing loan obligations, and living costs.
- Calculates allowable Debt Volume Ratio (DVR %), maximum allowable EMI, and maximum eligible loan disbursement amount.

### 3. 💳 Credit Card DVR / DBR Assessment (PPG Feb 2025 Guidelines)
- Evaluates credit card limit eligibility and minimum income benchmarks.
- Automatic DBR check against existing credit card limits, personal commitments, and regulatory caps.
- Instant eligibility verdict with status indicators (Eligible, Conditional, or Exceeds Threshold).

### 4. 📜 Local History & Storage
- Persistent calculation history powered by **Room Database** and **Kotlin Coroutines / Flow**.
- Save, review, and clear past computations anytime with zero network latency.

### 5. 🎨 Modern Material 3 UI / UX
- Native **Jetpack Compose** UI with dynamic theming.
- Full Light and Dark mode support with banking-grade color palettes.
- Adaptive typography, input formatting, and responsive error handling.

---

## 🏗️ Architecture & Tech Stack

The project adheres to Google's official Android Architecture Guidelines and **Clean Architecture** principles:

```
SmartCalc
├── domain/            # Business Logic & Pure Kotlin rules (No Android dependencies)
│   ├── calculator/    # EmiCalculator, PersonalLoanDvrCalculator, CreditCardDvrCalculator
│   ├── ppg/           # Credit Card PPG & Personal Loan PPD rule engines
│   └── validation/    # Input validation and business constraint checks
│
├── data/              # Data persistence and repositories
│   ├── local/         # Room Database, DAOs, Entity models
│   └── repository/    # History repository implementations
│
├── di/                # Dagger-Hilt Dependency Injection modules
│
└── ui/                # UI Layer with Jetpack Compose
    ├── screens/       # HomeScreen, EmiScreen, DvrScreens, HistoryScreen, InfoScreen
    └── theme/         # Color palettes, Material 3 Typography, Dynamic Theme
```

### Key Libraries & Components:
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3.
- **Dependency Injection:** [Dagger Hilt](https://dagger.dev/hilt/) for scalable dependency graphs.
- **Local Persistence:** [Room](https://developer.android.com/training/data-storage/room) (SQLite ORM) with KSP/KAPT.
- **Asynchronous Execution:** Kotlin Coroutines & `StateFlow`.
- **Navigation:** Jetpack Navigation Compose.
- **Testing:** JUnit, Kotlinx Coroutines Test, Mocking.

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio Iguana | 2023.2.1** or newer.
- **JDK 17** (configured in Gradle settings).
- Android device or emulator running **API 26 (Android 8.0 Oreo)** or higher.

### Installation & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/SmartCalc.git
   cd SmartCalc
   ```

2. Open the project in **Android Studio**.

3. Let Gradle sync and download dependencies.

4. Build and run the app:
   - Run on an attached device or emulator directly via the Android Studio **Run** button (Shift + F10).
   - Or build via command line:
     ```bash
     # Windows
     .\gradlew.bat assembleDebug

     # macOS / Linux
     ./gradlew assembleDebug
     ```

### Running Unit Tests
```bash
# Windows
.\gradlew.bat test

# macOS / Linux
./gradlew test
```

---

## 📖 Calculation Methodology

| Calculator | Reference Standard | Key Metrics |
| :--- | :--- | :--- |
| **EMI Calculator** | Standard Reducing-Balance Formula | Monthly Installment, Total Interest, Total Payable |
| **Personal Loan DVR** | Bank Asia Personal Loan PPD-2026 | Capacity-to-pay, Maximum Disbursable Amount, Allowable DBR |
| **Credit Card DVR** | Bank Asia Credit Card PPG Feb 2025 | DBR Threshold, Recommended Card Limit, Obligation Ratio |

---

## ⚖️ Disclaimer
*SmartCalc is designed as a calculation aid and pre-assessment tool. Final underwriting approvals, interest rates, and loan disbursals remain subject to the official policies, circulars, and competent authority of Bank Asia PLC and applicable central bank regulations.*

---

## 👨‍💻 Author & Maintainer

**Naymul Nahin**  
- X / Twitter: [@naymuln_](https://twitter.com/naymuln_)
- GitHub: [@naymuln](https://github.com/naymuln)

---

## 📄 License
This project is open-source under the [MIT License](LICENSE) (or proprietary to author as designated).
