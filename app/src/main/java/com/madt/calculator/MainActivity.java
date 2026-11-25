package com.madt.calculator;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_CALCULATOR_ENGINE = "key_calculator_engine";
    private static final String KEY_HISTORY_LIST = "key_history_list";
    private static final String KEY_HISTORY_VISIBLE = "key_history_visible";

    private TextView tvExpression;
    private TextView tvDisplay;

    // History panel components
    private RelativeLayout layoutHistoryPanel;
    private RecyclerView rvHistory;
    private TextView tvEmptyHistory;
    private ImageButton btnClearHistory;
    private ImageButton btnHistoryToggle;

    private CalculatorEngine calculatorEngine;
    private final ArrayList<HistoryItem> historyList = new ArrayList<>();
    private HistoryAdapter historyAdapter;

    // Interactive keypad buttons list for error state toggling
    private final List<Button> operationalButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupCalculatorEngine(savedInstanceState);
        setupHistoryPanel();
        setupListeners();
        setupBackNavigation();

        if (savedInstanceState != null) {
            boolean historyVisible = savedInstanceState.getBoolean(KEY_HISTORY_VISIBLE, false);
            layoutHistoryPanel.setVisibility(historyVisible ? View.VISIBLE : View.GONE);
        }

        updateUI();
    }

    private void initViews() {
        tvExpression = findViewById(R.id.tvExpression);
        tvDisplay = findViewById(R.id.tvDisplay);

        layoutHistoryPanel = findViewById(R.id.layoutHistoryPanel);
        rvHistory = findViewById(R.id.rvHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        btnHistoryToggle = findViewById(R.id.btnHistoryToggle);
    }

    private void setupCalculatorEngine(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_CALCULATOR_ENGINE)) {
            calculatorEngine = (CalculatorEngine) savedInstanceState.getSerializable(KEY_CALCULATOR_ENGINE);
        }

        if (calculatorEngine == null) {
            calculatorEngine = new CalculatorEngine();
        }

        calculatorEngine.setHistoryListener((expression, result) -> {
            historyList.add(0, new HistoryItem(expression, result));
            if (historyAdapter != null) {
                historyAdapter.notifyItemInserted(0);
                rvHistory.scrollToPosition(0);
            }
            updateHistoryEmptyState();
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_HISTORY_LIST)) {
            @SuppressWarnings("unchecked")
            ArrayList<HistoryItem> savedHistory = (ArrayList<HistoryItem>) savedInstanceState.getSerializable(KEY_HISTORY_LIST);
            if (savedHistory != null) {
                historyList.clear();
                historyList.addAll(savedHistory);
            }
        }
    }

    private void setupHistoryPanel() {
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(historyList, item -> {
            calculatorEngine.restoreFromHistory(item.getExpression(), item.getResult());
            updateUI();
            hideHistoryPanel();
        });
        rvHistory.setAdapter(historyAdapter);
        updateHistoryEmptyState();
    }

    private void setupListeners() {
        // Digits
        setDigitListener(R.id.btn0, "0");
        setDigitListener(R.id.btn1, "1");
        setDigitListener(R.id.btn2, "2");
        setDigitListener(R.id.btn3, "3");
        setDigitListener(R.id.btn4, "4");
        setDigitListener(R.id.btn5, "5");
        setDigitListener(R.id.btn6, "6");
        setDigitListener(R.id.btn7, "7");
        setDigitListener(R.id.btn8, "8");
        setDigitListener(R.id.btn9, "9");

        // Decimal & Negate
        Button btnDot = findViewById(R.id.btnDot);
        btnDot.setOnClickListener(v -> {
            calculatorEngine.inputDecimal();
            updateUI();
        });
        operationalButtons.add(btnDot);

        Button btnNegate = findViewById(R.id.btnNegate);
        btnNegate.setOnClickListener(v -> {
            calculatorEngine.inputNegate();
            updateUI();
        });
        operationalButtons.add(btnNegate);

        // Basic Operators
        setOperatorListener(R.id.btnAdd, getString(R.string.btn_add));
        setOperatorListener(R.id.btnSubtract, getString(R.string.btn_subtract));
        setOperatorListener(R.id.btnMultiply, getString(R.string.btn_multiply));
        setOperatorListener(R.id.btnDivide, getString(R.string.btn_divide));

        // Equals
        Button btnEquals = findViewById(R.id.btnEquals);
        btnEquals.setOnClickListener(v -> {
            calculatorEngine.equals();
            updateUI();
        });
        operationalButtons.add(btnEquals);

        // Unary functions
        Button btnSqrt = findViewById(R.id.btnSqrt);
        btnSqrt.setOnClickListener(v -> {
            calculatorEngine.squareRoot();
            updateUI();
        });
        operationalButtons.add(btnSqrt);

        Button btnSquare = findViewById(R.id.btnSquare);
        btnSquare.setOnClickListener(v -> {
            calculatorEngine.square();
            updateUI();
        });
        operationalButtons.add(btnSquare);

        Button btnReciprocal = findViewById(R.id.btnReciprocal);
        btnReciprocal.setOnClickListener(v -> {
            calculatorEngine.reciprocal();
            updateUI();
        });
        operationalButtons.add(btnReciprocal);

        Button btnPercent = findViewById(R.id.btnPercent);
        btnPercent.setOnClickListener(v -> {
            calculatorEngine.percentage();
            updateUI();
        });
        operationalButtons.add(btnPercent);

        // Editing
        Button btnBackspace = findViewById(R.id.btnBackspace);
        btnBackspace.setOnClickListener(v -> {
            calculatorEngine.inputBackspace();
            updateUI();
        });
        operationalButtons.add(btnBackspace);

        Button btnClearEntry = findViewById(R.id.btnClearEntry);
        btnClearEntry.setOnClickListener(v -> {
            calculatorEngine.clearEntry();
            updateUI();
        });

        Button btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> {
            calculatorEngine.clear();
            updateUI();
        });

        // History Actions
        btnHistoryToggle.setOnClickListener(v -> toggleHistoryPanel());

        btnClearHistory.setOnClickListener(v -> {
            historyList.clear();
            historyAdapter.notifyDataSetChanged();
            updateHistoryEmptyState();
        });

        // Header and Memory Stubs
        findViewById(R.id.btnMenu).setOnClickListener(v ->
                Toast.makeText(this, R.string.desc_menu, Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnKeepOnTop).setOnClickListener(v ->
                Toast.makeText(this, R.string.desc_keep_on_top, Toast.LENGTH_SHORT).show());
    }

    private void setDigitListener(int viewId, String digit) {
        Button btn = findViewById(viewId);
        btn.setOnClickListener(v -> {
            calculatorEngine.inputDigit(digit);
            updateUI();
        });
        operationalButtons.add(btn);
    }

    private void setOperatorListener(int viewId, String op) {
        Button btn = findViewById(viewId);
        btn.setOnClickListener(v -> {
            calculatorEngine.setOperator(op);
            updateUI();
        });
        operationalButtons.add(btn);
    }

    private void updateUI() {
        tvExpression.setText(calculatorEngine.getExpression());
        tvDisplay.setText(calculatorEngine.getCurrentInput());

        boolean isError = calculatorEngine.isError();
        for (Button btn : operationalButtons) {
            btn.setEnabled(!isError);
            btn.setAlpha(isError ? 0.4f : 1.0f);
        }
    }

    private void updateHistoryEmptyState() {
        boolean isEmpty = historyList.isEmpty();
        tvEmptyHistory.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        btnClearHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void toggleHistoryPanel() {
        if (layoutHistoryPanel.getVisibility() == View.VISIBLE) {
            hideHistoryPanel();
        } else {
            showHistoryPanel();
        }
    }

    private void showHistoryPanel() {
        layoutHistoryPanel.setVisibility(View.VISIBLE);
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        layoutHistoryPanel.startAnimation(slideIn);
    }

    private void hideHistoryPanel() {
        if (layoutHistoryPanel.getVisibility() == View.VISIBLE) {
            Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
            slideOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    layoutHistoryPanel.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            layoutHistoryPanel.startAnimation(slideOut);
        }
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (layoutHistoryPanel.getVisibility() == View.VISIBLE) {
                    hideHistoryPanel();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_CALCULATOR_ENGINE, calculatorEngine);
        outState.putSerializable(KEY_HISTORY_LIST, historyList);
        outState.putBoolean(KEY_HISTORY_VISIBLE, layoutHistoryPanel.getVisibility() == View.VISIBLE);
    }
}
