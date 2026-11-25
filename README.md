# MADT Lab #4 — Calculator App

An Android Standard Calculator application designed to replicate the look, feel, and functionality of the **Windows Standard Calculator**. Developed in **Java** and **XML** for Android Studio, featuring scratch-built calculation logic, complete runtime history, responsive layout, and resource-based localization.

---

## 1. Project Description & Purpose

This project is developed as part of **MADT Lab #4 (Mobile Application Development Technologies)**. The primary objective is to create a complete, reliable, and user-friendly mobile calculator adhering strictly to standard calculator workflows and the Windows Standard Calculator visual design.

Key goals achieved in this project:
- Replicating the Windows 10/11 Fluent-inspired calculator user interface.
- Implementing an arithmetic calculation engine from scratch using high-precision decimal math.
- Maintaining an interactive runtime history panel with tap-to-recall capabilities.
- Managing Android activity lifecycles and preserving state during configuration changes (e.g. screen rotation).
- Supporting clean internationalization by abstracting all UI text into Android resource files.

---

## 2. Technologies Used

- **Language**: Java 8 (Pure standard library `java.math.BigDecimal`, zero third-party calculation packages)
- **UI Framework**: Android XML Layouts, Material Components (`Theme.MaterialComponents.Light.NoActionBar`), CoordinatorLayout, and RecyclerView
- **IDE**: Android Studio (Ladybug / Koala / Hedgehog compatible)
- **Build System**: Gradle 8.4 with Kotlin DSL (`build.gradle.kts`), Android Gradle Plugin 8.3.2
- **Compatibility**:
  - Minimum SDK: API 24 (Android 7.0 Nougat)
  - Target / Compile SDK: API 34 (Android 14)
- **Testing**: JUnit 4 unit test suite (`CalculatorEngineTest.java`)

---

## 3. Application Features

### Core Arithmetic Operations
- **Addition (`+`)**: Computes the sum of operands.
- **Subtraction (`−`)**: Computes the difference.
- **Multiplication (`×`)**: Computes the product.
- **Division (`÷`)**: Computes the quotient with precision handling and division-by-zero detection.
- **Chained Evaluation**: Evaluates intermediate expressions sequentially (e.g., `5 + 3 × 2 = 16`), matching Windows Standard Calculator behavior.
- **Repeating Equals**: Pressing `=` consecutively reapplies the last operator and second operand (e.g., `5 + 3 = 8`, `=` results in `11`, `=` results in `14`).

### Advanced & Unary Operations
- **Square Root (`²√x`)**: Computes $\sqrt{x}$ for non-negative values; displays formatted expression (e.g., `√(64) = 8`). Flags `"Invalid input"` for negative numbers.
- **Squaring (`x²`)**: Computes $x^2$; displays formatted expression (e.g., `sqr(12) = 144`).
- **Reciprocal (`1/x`)**: Computes $\frac{1}{x}$; displays formatted expression (e.g., `1/(8) = 0.125`). Flags `"Cannot divide by zero"` when $x = 0$.
- **Percentage (`%`)**:
  - Standalone: Divides current operand by 100 (e.g., `50% = 0.5`).
  - In additive operations: Calculates relative percentage (e.g., `200 + 10% = 220`).
  - In multiplicative operations: Calculates scale percentage (e.g., `200 × 10% = 20`).

### Editing & Control Operations
- **Backspace (`⌫`)**: Deletes the last entered digit or reverts to `0`.
- **Clear Entry (`CE`)**: Clears the current input without discarding pending operations or the first operand.
- **Clear All (`C`)**: Completely resets the calculation engine, displays, and any error states.
- **Sign Change (`+/-`)**: Toggles the sign of the current operand between positive and negative.

### Runtime Calculation History
- **Slide-in Overlay**: Accessible via the History clock icon at the top right of the application header.
- **Expression & Result Log**: Displays completed operations showing the full mathematical expression and result.
- **Tap-to-Recall**: Tapping any historical record restores both its formula expression and calculated result to the active display.
- **Clear History**: Trash-can button at the bottom right clears runtime history with an empty state placeholder (`"There's no history yet"`).
- **Lifecycle Persistence**: Full history and calculation state survive device rotation and configuration changes.

### Error Handling
- **Division by Zero**: Safeguarded in both standard division (`÷ 0`) and reciprocal (`1/0`), displaying `"Cannot divide by zero"`.
- **Invalid Roots**: Safeguarded against negative roots (e.g., `√(-9)`), displaying `"Invalid input"`.
- **Operational Lock**: Operational keypad buttons are disabled when an error occurs until the user resets via `C` or `CE`.

---

## 4. Calculation Logic Implemented From Scratch

In strict compliance with assignment specifications, **no external third-party math parsers or calculation libraries** (such as `exp4j`, `evalEx`, or `rhino`) are used:

1. **Precision Decimal Arithmetic**: Built exclusively upon standard Java `java.math.BigDecimal` and `java.math.RoundingMode.HALF_UP`. This guarantees floating-point precision without IEEE-754 binary rounding artifacts (e.g., $0.1 + 0.2 = 0.3$ exactly, never $0.30000000000000004$).
2. **Immediate Execution State Machine**: Encapsulated in [`CalculatorEngine.java`](file:///c:/Users/aliin/Downloads/MADT_Lab4_Calculator_Viber/app/src/main/java/com/madt/calculator/CalculatorEngine.java). The engine maintains:
   - Primary operand (`firstOperand`)
   - Active operator (`pendingOperator`)
   - Current display string (`currentInput`)
   - Unary second operand expression tracker (`secondOperandExpr`)
   - Input and operator-replaceable flags (`isNewInput`, `operatorReplaceable`)
   - Repeating equals state (`lastSecondOperand`, `lastOperator`)
3. **Architecture Isolation**: The calculation logic is fully decoupled from the Android framework, making it lightweight, testable, and verified by standard JUnit tests.

---

## 5. Localization & Resource Centralization

To ensure global readiness and maintainability:
- **Zero Hardcoded Strings**: All button labels, mathematical symbols, header titles, accessibility descriptions, and error notifications are defined in [`strings.xml`](file:///c:/Users/aliin/Downloads/MADT_Lab4_Calculator_Viber/app/src/main/res/values/strings.xml).
- **Unicode Support**: Accurate mathematical symbols (`−`, `×`, `÷`, `²√x`, `x²`, `1/x`) are standardized as XML entities to ensure identical cross-locale rendering.
- **Accessibility**: All icon buttons (`btnMenu`, `btnKeepOnTop`, `btnHistoryToggle`, `btnClearHistory`, `btnBackspace`) include localized `android:contentDescription` attributes for Android TalkBack.

---

## 6. Project Structure

```
MADT_Lab4_Calculator_Viber/
├── build.gradle.kts                   # Top-level Gradle build configuration
├── settings.gradle.kts                # Project name and repository settings
├── gradle.properties                 # JVM arguments & AndroidX flags
├── gradlew.bat                        # Gradle wrapper script for Windows
├── .gitignore                         # Git exclusion rules
├── README.md                          # Comprehensive project documentation
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties  # Gradle 8.4 wrapper distribution
└── app/
    ├── build.gradle.kts               # Application module build script (API 34 / 24)
    ├── proguard-rules.pro             # Proguard release optimization rules
    └── src/
        ├── test/java/com/madt/calculator/
        │   └── CalculatorEngineTest.java   # JUnit 4 unit tests for calculation engine
        └── main/
            ├── AndroidManifest.xml    # App manifest, theme & launcher activity
            ├── java/com/madt/calculator/
            │   ├── MainActivity.java       # Activity controller, animations & lifecycle state
            │   ├── CalculatorEngine.java   # Scratch calculation engine (BigDecimal)
            │   ├── HistoryItem.java        # History data model
            │   └── HistoryAdapter.java     # RecyclerView adapter with tap-to-recall
            └── res/
                ├── anim/
                │   ├── slide_in_right.xml  # History panel entry animation
                │   └── slide_out_right.xml # History panel exit animation
                ├── drawable/
                │   ├── ic_backspace.xml    # Vector drawable for backspace
                │   ├── ic_clear_history.xml# Vector drawable for clear history trash can
                │   ├── ic_history.xml      # Vector drawable for history toggle clock
                │   ├── ic_keep_on_top.xml  # Vector drawable for keep-on-top icon
                │   ├── ic_menu.xml         # Vector drawable for hamburger menu icon
                │   ├── ic_launcher_background.xml
                │   └── ic_launcher_foreground.xml
                ├── layout/
                │   ├── activity_main.xml   # 6x4 Windows Standard Calculator layout
                │   └── item_history.xml    # History item layout
                ├── values/
                │   ├── colors.xml          # Windows Fluent palette (accent blue #0067C0)
                │   ├── dimens.xml          # Standard dimensions and text sizes
                │   ├── strings.xml         # Centralized localized strings
                │   └── themes.xml          # Material Unelevated styles for keys
                └── mipmap-anydpi-v26/
                    └── ic_launcher.xml     # Adaptive app launcher icon
```

---

## 7. Instructions for Installing & Running

### Prerequisites
- **Android Studio**: Iguana, Jellyfish, Koala, Ladybug, or newer.
- **Java Development Kit (JDK)**: JDK 17 (bundled with Android Studio).
- **Android Device or Emulator**: Android 7.0 (API level 24) or higher.

### Step-by-Step Setup
1. **Open the Project in Android Studio**:
   - Launch Android Studio.
   - Click **Open** and select the folder `MADT_Lab4_Calculator_Viber`.
   - Wait for Gradle to finish indexing and syncing dependencies.
2. **Build the Application**:
   - In Android Studio, select **Build > Make Project** (or press `Ctrl + F9`).
   - Verify that the build finishes with zero errors.
3. **Run the Application**:
   - Connect a physical Android device (with USB debugging enabled) or start an Android Virtual Device (AVD) emulator.
   - Click the green **Run** button (or press `Shift + F10`).
   - The Calculator application will launch on your device/emulator.
4. **Execute Unit Tests**:
   - Right-click `app/src/test/java/com/madt/calculator/CalculatorEngineTest.java` in the project tree.
   - Select **Run 'CalculatorEngineTest'** to verify all arithmetic, edge case, and serialization tests.

---

## 8. Instructions for Using the Calculator

| Action | Description |
| :--- | :--- |
| **Entering Numbers** | Tap digits `0`–`9` and `.` for decimals. Numbers will format cleanly in the primary display. |
| **Basic Arithmetic** | Enter first number, tap operator (`+`, `−`, `×`, `÷`), enter second number, and tap `=`. |
| **Chained Calculations** | Enter `5 + 3 × 2 =`. The calculator evaluates intermediate steps sequentially (`5 + 3 = 8`, then `8 × 2 = 16`). |
| **Unary Operations** | Enter a number and tap `²√x`, `x²`, `1/x`, or `%`. The expression updates dynamically (e.g., `9 + √(16)`). |
| **Repeating Equals** | After computing a result (e.g., `5 + 3 = 8`), tapping `=` again computes `8 + 3 = 11`, then `14`, etc. |
| **Correcting Input** | Tap `⌫` to erase the last digit, `CE` to clear the current entry, or `C` to reset the entire calculator. |
| **Changing Sign** | Tap `+/-` to negate the active number. |
| **Viewing History** | Tap the clock icon in the top header to slide open the history panel. |
| **Recalling History** | Tap any calculation in the history panel to reload its formula and result into the calculator. |
| **Clearing History** | Tap the trash icon at the bottom right of the history panel. |

---

## 9. Acknowledgement

This project was developed for **MADT Lab #4 — Calculator App** in the Mobile Application Development Technologies coursework. All code generation, architectural refinement, code reviews, and testing were conducted in accordance with laboratory guidelines.
