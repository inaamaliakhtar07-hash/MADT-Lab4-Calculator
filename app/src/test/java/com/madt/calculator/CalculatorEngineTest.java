package com.madt.calculator;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for CalculatorEngine verifying all core operations, chained operations,
 * unary functions, edge cases, and serialization.
 */
public class CalculatorEngineTest {

    private CalculatorEngine engine;
    private List<HistoryItem> recordedHistory;

    @Before
    public void setUp() {
        engine = new CalculatorEngine();
        recordedHistory = new ArrayList<>();
        engine.setHistoryListener((expression, result) ->
                recordedHistory.add(new HistoryItem(expression, result)));
    }

    @Test
    public void testBasicAddition() {
        engine.inputDigit("1");
        engine.inputDigit("2");
        engine.setOperator("+");
        engine.inputDigit("8");
        engine.equals();

        assertEquals("20", engine.getCurrentInput());
        assertEquals("12 + 8 =", engine.getExpression());
        assertEquals(1, recordedHistory.size());
        assertEquals("12 + 8 =", recordedHistory.get(0).getExpression());
        assertEquals("20", recordedHistory.get(0).getResult());
    }

    @Test
    public void testFloatingPointPrecision() {
        // Verifies 0.1 + 0.2 = 0.3 without IEEE 754 precision glitch
        engine.inputDigit("0");
        engine.inputDecimal();
        engine.inputDigit("1");
        engine.setOperator("+");
        engine.inputDigit("0");
        engine.inputDecimal();
        engine.inputDigit("2");
        engine.equals();

        assertEquals("0.3", engine.getCurrentInput());
    }

    @Test
    public void testBasicSubtraction() {
        engine.inputDigit("2");
        engine.inputDigit("5");
        engine.setOperator("−");
        engine.inputDigit("7");
        engine.equals();

        assertEquals("18", engine.getCurrentInput());
    }

    @Test
    public void testBasicMultiplication() {
        engine.inputDigit("6");
        engine.setOperator("×");
        engine.inputDigit("7");
        engine.equals();

        assertEquals("42", engine.getCurrentInput());
    }

    @Test
    public void testBasicDivision() {
        engine.inputDigit("8");
        engine.inputDigit("4");
        engine.setOperator("÷");
        engine.inputDigit("4");
        engine.equals();

        assertEquals("21", engine.getCurrentInput());
    }

    @Test
    public void testDivisionByZero() {
        engine.inputDigit("5");
        engine.setOperator("÷");
        engine.inputDigit("0");
        engine.equals();

        assertTrue(engine.isError());
        assertEquals("Cannot divide by zero", engine.getCurrentInput());

        // Test clear resets error state
        engine.clear();
        assertFalse(engine.isError());
        assertEquals("0", engine.getCurrentInput());
    }

    @Test
    public void testSquareRoot() {
        engine.inputDigit("6");
        engine.inputDigit("4");
        engine.squareRoot();

        assertEquals("8", engine.getCurrentInput());
        assertEquals("√(64) =", engine.getExpression());
    }

    @Test
    public void testSquareRootNegative() {
        engine.inputDigit("9");
        engine.inputNegate();
        engine.squareRoot();

        assertTrue(engine.isError());
        assertEquals("Invalid input", engine.getCurrentInput());
    }

    @Test
    public void testSquare() {
        engine.inputDigit("1");
        engine.inputDigit("2");
        engine.square();

        assertEquals("144", engine.getCurrentInput());
        assertEquals("sqr(12) =", engine.getExpression());
    }

    @Test
    public void testReciprocal() {
        engine.inputDigit("8");
        engine.reciprocal();

        assertEquals("0.125", engine.getCurrentInput());
        assertEquals("1/(8) =", engine.getExpression());
    }

    @Test
    public void testReciprocalZero() {
        engine.inputDigit("0");
        engine.reciprocal();

        assertTrue(engine.isError());
        assertEquals("Cannot divide by zero", engine.getCurrentInput());
    }

    @Test
    public void testPercentageStandalone() {
        // Single percentage: 50% = 0.5
        engine.inputDigit("5");
        engine.inputDigit("0");
        engine.percentage();

        assertEquals("0.5", engine.getCurrentInput());
        assertEquals("0.5 =", engine.getExpression());
    }

    @Test
    public void testPercentageInAddition() {
        // Windows style: 200 + 10% = 200 + 20 = 220
        engine.inputDigit("2");
        engine.inputDigit("0");
        engine.inputDigit("0");
        engine.setOperator("+");
        engine.inputDigit("1");
        engine.inputDigit("0");
        engine.percentage();

        assertEquals("20", engine.getCurrentInput());
        assertEquals("200 + 20", engine.getExpression());

        engine.equals();
        assertEquals("220", engine.getCurrentInput());
        assertEquals("200 + 20 =", engine.getExpression());
    }

    @Test
    public void testPercentageInMultiplication() {
        // Windows style: 200 × 10% = 200 × 0.1 = 20
        engine.inputDigit("2");
        engine.inputDigit("0");
        engine.inputDigit("0");
        engine.setOperator("×");
        engine.inputDigit("1");
        engine.inputDigit("0");
        engine.percentage();

        assertEquals("0.1", engine.getCurrentInput());
        assertEquals("200 × 0.1", engine.getExpression());

        engine.equals();
        assertEquals("20", engine.getCurrentInput());
    }

    @Test
    public void testOperatorReplacement() {
        // User enters 5 +, changes mind and presses × 4 = 20
        engine.inputDigit("5");
        engine.setOperator("+");
        assertEquals("5 +", engine.getExpression());

        engine.setOperator("×");
        assertEquals("5 ×", engine.getExpression());

        engine.inputDigit("4");
        engine.equals();

        assertEquals("20", engine.getCurrentInput());
        assertEquals("5 × 4 =", engine.getExpression());
    }

    @Test
    public void testUnaryWithinBinaryExpression() {
        // 9 + 16 sqrt = 13 (expression: 9 + √(16) =)
        engine.inputDigit("9");
        engine.setOperator("+");
        engine.inputDigit("1");
        engine.inputDigit("6");
        engine.squareRoot();

        assertEquals("4", engine.getCurrentInput());
        assertEquals("9 + √(16)", engine.getExpression());

        engine.equals();
        assertEquals("13", engine.getCurrentInput());
        assertEquals("9 + √(16) =", engine.getExpression());
        assertEquals(1, recordedHistory.size());
        assertEquals("9 + √(16) =", recordedHistory.get(0).getExpression());
        assertEquals("13", recordedHistory.get(0).getResult());
    }

    @Test
    public void testChainedOperationsWithUnary() {
        // 9 + 16 sqrt + 2 = 15
        engine.inputDigit("9");
        engine.setOperator("+");
        engine.inputDigit("1");
        engine.inputDigit("6");
        engine.squareRoot();

        assertEquals("4", engine.getCurrentInput());
        assertEquals("9 + √(16)", engine.getExpression());

        engine.setOperator("+");
        assertEquals("13", engine.getCurrentInput());
        assertEquals("13 +", engine.getExpression());

        engine.inputDigit("2");
        engine.equals();

        assertEquals("15", engine.getCurrentInput());
        assertEquals("13 + 2 =", engine.getExpression());
    }

    @Test
    public void testBackspaceAndNegate() {
        engine.inputDigit("1");
        engine.inputDigit("2");
        engine.inputDigit("3");
        engine.inputBackspace();
        assertEquals("12", engine.getCurrentInput());

        engine.inputNegate();
        assertEquals("-12", engine.getCurrentInput());

        engine.inputNegate();
        assertEquals("12", engine.getCurrentInput());
    }

    @Test
    public void testRepeatingEquals() {
        engine.inputDigit("5");
        engine.setOperator("+");
        engine.inputDigit("3");
        engine.equals(); // 8
        assertEquals("8", engine.getCurrentInput());

        engine.equals(); // 8 + 3 = 11
        assertEquals("11", engine.getCurrentInput());

        engine.equals(); // 11 + 3 = 14
        assertEquals("14", engine.getCurrentInput());
    }

    @Test
    public void testHistoryRestore() {
        engine.restoreFromHistory("12 + 8 =", "20");
        assertEquals("20", engine.getCurrentInput());
        assertEquals("12 + 8 =", engine.getExpression());

        // Subsequent digit starts a new number
        engine.inputDigit("7");
        assertEquals("7", engine.getCurrentInput());
        assertEquals("", engine.getExpression());
    }

    @Test
    public void testEngineSerialization() throws Exception {
        engine.inputDigit("4");
        engine.inputDigit("2");
        engine.setOperator("+");
        engine.inputDigit("8");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(engine);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        CalculatorEngine restored = (CalculatorEngine) ois.readObject();
        ois.close();

        assertNotNull(restored);
        assertEquals("8", restored.getCurrentInput());
        assertEquals("42 +", restored.getExpression());

        restored.equals();
        assertEquals("50", restored.getCurrentInput());
        assertEquals("42 + 8 =", restored.getExpression());
    }
}
