package com.madt.calculator;

import java.io.Serializable;

/**
 * Model representing a completed calculation item in runtime history.
 */
public class HistoryItem implements Serializable {
    private final String expression;
    private final String result;

    public HistoryItem(String expression, String result) {
        this.expression = expression;
        this.result = result;
    }

    public String getExpression() {
        return expression;
    }

    public String getResult() {
        return result;
    }
}
