package com.madt.calculator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Core calculation engine implemented completely from scratch.
 * Uses high-precision BigDecimal arithmetic to prevent floating-point inaccuracy.
 * Implements Serializable for lifecycle preservation across configuration changes.
 */
public class CalculatorEngine implements Serializable {

    private static final long serialVersionUID = 1L;

    public interface HistoryListener {
        void onCalculationCompleted(String expression, String result);
    }

    private String currentInput = "0";
    private BigDecimal firstOperand = null;
    private String pendingOperator = null;
    private String expression = "";
    private String secondOperandExpr = null;

    private boolean isNewInput = true;
    private boolean operatorReplaceable = false;
    private boolean hasCalculated = false;

    // State preserved for repeating equals
    private BigDecimal lastSecondOperand = null;
    private String lastOperator = null;

    // Error states
    private boolean isError = false;
    private String errorMessage = null;

    private transient HistoryListener historyListener;

    public void setHistoryListener(HistoryListener listener) {
        this.historyListener = listener;
    }

    public String getCurrentInput() {
        return isError ? errorMessage : currentInput;
    }

    public String getExpression() {
        return expression;
    }

    public boolean isError() {
        return isError;
    }

    public void restoreFromHistory(String result) {
        restoreFromHistory(null, result);
    }

    public void restoreFromHistory(String expr, String result) {
        if (result == null || result.isEmpty()) return;
        clear();
        currentInput = result;
        expression = expr != null ? expr : "";
        isNewInput = true;
        hasCalculated = true;
    }

    public void inputDigit(String digit) {
        if (isError) return;

        if (hasCalculated) {
            expression = "";
            hasCalculated = false;
        }

        if (isNewInput || "0".equals(currentInput)) {
            currentInput = digit;
            isNewInput = false;
        } else {
            // Limit max digits length for usability
            if (currentInput.replace(".", "").replace("-", "").length() < 16) {
                currentInput += digit;
            }
        }
        operatorReplaceable = false;
    }

    public void inputDecimal() {
        if (isError) return;

        if (hasCalculated) {
            expression = "";
            hasCalculated = false;
            currentInput = "0.";
            isNewInput = false;
            operatorReplaceable = false;
            return;
        }

        if (isNewInput) {
            currentInput = "0.";
            isNewInput = false;
        } else if (!currentInput.contains(".")) {
            currentInput += ".";
        }
        operatorReplaceable = false;
    }

    public void inputBackspace() {
        if (isError || isNewInput || hasCalculated || operatorReplaceable) {
            return;
        }

        if (currentInput.length() > 1) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            if ("-".equals(currentInput)) {
                currentInput = "0";
                isNewInput = true;
            }
        } else {
            currentInput = "0";
            isNewInput = true;
        }
    }

    public void inputNegate() {
        if (isError || "0".equals(currentInput)) return;

        if (currentInput.startsWith("-")) {
            currentInput = currentInput.substring(1);
        } else {
            currentInput = "-" + currentInput;
        }

        if (hasCalculated) {
            hasCalculated = false;
            expression = "";
        }
        operatorReplaceable = false;
    }

    public void clearEntry() {
        if (isError) {
            clear();
            return;
        }
        currentInput = "0";
        isNewInput = true;
        if (secondOperandExpr != null) {
            secondOperandExpr = null;
            if (pendingOperator != null && firstOperand != null) {
                expression = formatNumber(firstOperand) + " " + pendingOperator;
            }
        }
    }

    public void clear() {
        currentInput = "0";
        firstOperand = null;
        pendingOperator = null;
        expression = "";
        secondOperandExpr = null;
        isNewInput = true;
        operatorReplaceable = false;
        hasCalculated = false;
        lastSecondOperand = null;
        lastOperator = null;
        isError = false;
        errorMessage = null;
    }

    public void setOperator(String op) {
        if (isError) return;

        BigDecimal currentNum = parseCurrent();
        if (currentNum == null) return;

        if (pendingOperator != null && !operatorReplaceable) {
            // Intermediate sequential evaluation (e.g. 5 + 3 * 2)
            BigDecimal intermediate = executeOperation(firstOperand, currentNum, pendingOperator);
            if (isError) return;
            firstOperand = intermediate;
            currentInput = formatNumber(intermediate);
        } else {
            firstOperand = currentNum;
        }

        pendingOperator = op;
        expression = formatNumber(firstOperand) + " " + op;
        secondOperandExpr = null;
        isNewInput = true;
        operatorReplaceable = true;
        hasCalculated = false;
    }

    public void equals() {
        if (isError) return;

        BigDecimal currentNum = parseCurrent();
        if (currentNum == null) return;

        if (pendingOperator != null) {
            BigDecimal second = currentNum;
            BigDecimal result = executeOperation(firstOperand, second, pendingOperator);
            if (isError) return;

            String secondStr = secondOperandExpr != null ? secondOperandExpr : formatNumber(second);
            String completedExpr = formatNumber(firstOperand) + " " + pendingOperator + " " + secondStr + " =";
            String resultStr = formatNumber(result);

            expression = completedExpr;
            currentInput = resultStr;
            lastOperator = pendingOperator;
            lastSecondOperand = second;
            firstOperand = result;
            pendingOperator = null;
            secondOperandExpr = null;
            hasCalculated = true;
            isNewInput = true;
            operatorReplaceable = false;

            notifyHistory(completedExpr, resultStr);

        } else if (hasCalculated && lastOperator != null && lastSecondOperand != null) {
            // Repeat last operation on consecutive equals
            BigDecimal first = currentNum;
            BigDecimal result = executeOperation(first, lastSecondOperand, lastOperator);
            if (isError) return;

            String completedExpr = formatNumber(first) + " " + lastOperator + " " + formatNumber(lastSecondOperand) + " =";
            String resultStr = formatNumber(result);

            expression = completedExpr;
            currentInput = resultStr;
            firstOperand = result;
            isNewInput = true;
            operatorReplaceable = false;

            notifyHistory(completedExpr, resultStr);
        } else {
            expression = formatNumber(currentNum) + " =";
            hasCalculated = true;
            isNewInput = true;
            operatorReplaceable = false;
        }
    }

    public void squareRoot() {
        if (isError) return;

        BigDecimal val = parseCurrent();
        if (val == null) return;

        if (val.compareTo(BigDecimal.ZERO) < 0) {
            setError("Invalid input");
            return;
        }

        double sqrtResult = Math.sqrt(val.doubleValue());
        BigDecimal result = BigDecimal.valueOf(sqrtResult);
        String formatted = formatNumber(result);
        String unaryExpr = secondOperandExpr != null ? "√(" + secondOperandExpr + ")" : "√(" + formatNumber(val) + ")";

        if (pendingOperator != null && firstOperand != null) {
            secondOperandExpr = unaryExpr;
            expression = formatNumber(firstOperand) + " " + pendingOperator + " " + secondOperandExpr;
        } else {
            expression = unaryExpr + " =";
            notifyHistory(expression, formatted);
            hasCalculated = true;
        }

        currentInput = formatted;
        isNewInput = true;
        operatorReplaceable = false;
    }

    public void square() {
        if (isError) return;

        BigDecimal val = parseCurrent();
        if (val == null) return;

        BigDecimal result = val.multiply(val);
        String formatted = formatNumber(result);
        String unaryExpr = secondOperandExpr != null ? "sqr(" + secondOperandExpr + ")" : "sqr(" + formatNumber(val) + ")";

        if (pendingOperator != null && firstOperand != null) {
            secondOperandExpr = unaryExpr;
            expression = formatNumber(firstOperand) + " " + pendingOperator + " " + secondOperandExpr;
        } else {
            expression = unaryExpr + " =";
            notifyHistory(expression, formatted);
            hasCalculated = true;
        }

        currentInput = formatted;
        isNewInput = true;
        operatorReplaceable = false;
    }

    public void reciprocal() {
        if (isError) return;

        BigDecimal val = parseCurrent();
        if (val == null) return;

        if (val.compareTo(BigDecimal.ZERO) == 0) {
            setError("Cannot divide by zero");
            return;
        }

        BigDecimal result = BigDecimal.ONE.divide(val, 16, RoundingMode.HALF_UP);
        String formatted = formatNumber(result);
        String unaryExpr = secondOperandExpr != null ? "1/(" + secondOperandExpr + ")" : "1/(" + formatNumber(val) + ")";

        if (pendingOperator != null && firstOperand != null) {
            secondOperandExpr = unaryExpr;
            expression = formatNumber(firstOperand) + " " + pendingOperator + " " + secondOperandExpr;
        } else {
            expression = unaryExpr + " =";
            notifyHistory(expression, formatted);
            hasCalculated = true;
        }

        currentInput = formatted;
        isNewInput = true;
        operatorReplaceable = false;
    }

    public void percentage() {
        if (isError) return;

        BigDecimal currentNum = parseCurrent();
        if (currentNum == null) return;

        if (pendingOperator != null && firstOperand != null) {
            BigDecimal percentVal;
            if ("+".equals(pendingOperator) || "−".equals(pendingOperator) || "-".equals(pendingOperator)) {
                // Windows Calculator: 200 + 10% calculates 10% of 200 (= 20)
                percentVal = firstOperand.multiply(currentNum)
                        .divide(new BigDecimal("100"), 16, RoundingMode.HALF_UP);
            } else {
                // For multiplication & division: 200 * 10% calculates (10 / 100 = 0.1)
                percentVal = currentNum.divide(new BigDecimal("100"), 16, RoundingMode.HALF_UP);
            }
            secondOperandExpr = formatNumber(percentVal);
            currentInput = secondOperandExpr;
            expression = formatNumber(firstOperand) + " " + pendingOperator + " " + secondOperandExpr;
        } else {
            BigDecimal result = currentNum.divide(new BigDecimal("100"), 16, RoundingMode.HALF_UP);
            String formatted = formatNumber(result);
            expression = formatted + " =";
            notifyHistory(expression, formatted);
            currentInput = formatted;
            hasCalculated = true;
        }
        isNewInput = true;
        operatorReplaceable = false;
    }

    private BigDecimal executeOperation(BigDecimal a, BigDecimal b, String op) {
        if (a == null || b == null || op == null) return BigDecimal.ZERO;

        try {
            switch (op) {
                case "+":
                    return a.add(b);
                case "−":
                case "-":
                    return a.subtract(b);
                case "×":
                case "*":
                    return a.multiply(b);
                case "÷":
                case "/":
                    if (b.compareTo(BigDecimal.ZERO) == 0) {
                        setError("Cannot divide by zero");
                        return BigDecimal.ZERO;
                    }
                    return a.divide(b, 16, RoundingMode.HALF_UP);
                default:
                    return b;
            }
        } catch (ArithmeticException e) {
            setError("Overflow");
            return BigDecimal.ZERO;
        }
    }

    private void setError(String message) {
        isError = true;
        errorMessage = message;
    }

    private BigDecimal parseCurrent() {
        try {
            String sanitized = currentInput.trim();
            if (sanitized.endsWith(".")) {
                sanitized = sanitized.substring(0, sanitized.length() - 1);
            }
            if (sanitized.isEmpty() || "-".equals(sanitized) || "-0".equals(sanitized)) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(sanitized);
        } catch (Exception e) {
            setError("Invalid input");
            return null;
        }
    }

    private String formatNumber(BigDecimal value) {
        if (value == null) return "0";

        BigDecimal stripped = value.stripTrailingZeros();
        String plain = stripped.toPlainString();

        // Avoid "-0"
        if ("-0".equals(plain) || "-0.0".equals(plain) || plain.isEmpty()) {
            return "0";
        }
        return plain;
    }

    private void notifyHistory(String expr, String res) {
        if (historyListener != null) {
            historyListener.onCalculationCompleted(expr, res);
        }
    }
}
