package com.example.mad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WcstActivity extends AppCompatActivity {
    // Constants for WCST Logic
    private static final int TOTAL_CARDS_DECK = 128;
    private static final int CONSECUTIVE_SUCCESS_REQUIRED = 10;
    private static final int CATEGORIES_TO_COMPLETE = 6;

    // Sorting Rules State
    private enum SortingRule { COLOR, FORM, NUMBER }
    private final SortingRule[] ruleSequence = {
            SortingRule.COLOR, SortingRule.FORM, SortingRule.NUMBER,
            SortingRule.COLOR, SortingRule.FORM, SortingRule.NUMBER
    };
    
    private int currentRuleIndex = 0;
    private int consecutiveCorrectCount = 0;
    private int categoriesCompletedCount = 0;
    private int trialsAdministeredCount = 0;

    // Clinical Metrics Tracking
    private int totalCorrect = 0;
    private int totalErrors = 0;
    private int perseverativeErrors = 0;
    private int nonPerseverativeErrors = 0;
    private int conceptualLevelResponses = 0;
    private int failureToMaintainSet = 0;
    
    // Variables for tracking metrics
    private SortingRule previousRule = null;
    private int trialSinceRuleChange = 0;

    // Deck and Card State
    private List<WcstCard> deck = new ArrayList<>();
    private WcstCard activeSortingCard;
    private final WcstCard[] keyCards = new WcstCard[4];

    // UI Elements
    private final GridLayout[] keyCardContainers = new GridLayout[4];
    private GridLayout activeCardContainer;
    private TextView tvProgress;
    
    // Toast handling for instant updates
    private Toast feedbackToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wcst);

        tvProgress = findViewById(R.id.tvProgress);
        initializeKeyCards();
        generateRandomizedDeck();
        loadNextSortingCard();
    }

    private void initializeKeyCards() {
        keyCards[0] = new WcstCard(WcstCard.Shape.TRIANGLE, WcstCard.ColorName.RED, 1);
        keyCards[1] = new WcstCard(WcstCard.Shape.STAR, WcstCard.ColorName.GREEN, 2);
        keyCards[2] = new WcstCard(WcstCard.Shape.CROSS, WcstCard.ColorName.YELLOW, 3);
        keyCards[3] = new WcstCard(WcstCard.Shape.CIRCLE, WcstCard.ColorName.BLUE, 4);

        int[] keyCardIds = {R.id.keyCard1, R.id.keyCard2, R.id.keyCard3, R.id.keyCard4};
        for (int i = 0; i < 4; i++) {
            View container = findViewById(keyCardIds[i]);
            keyCardContainers[i] = container.findViewById(R.id.cardGridLayout);
            keyCards[i].render(this, keyCardContainers[i]);
            
            final int index = i;
            container.setOnClickListener(v -> onKeyCardSelected(index));
        }
        activeCardContainer = findViewById(R.id.activeCard).findViewById(R.id.cardGridLayout);
    }

    private void generateRandomizedDeck() {
        List<WcstCard> unique64 = new ArrayList<>();
        for (WcstCard.Shape s : WcstCard.Shape.values()) {
            for (WcstCard.ColorName c : WcstCard.ColorName.values()) {
                for (int q = 1; q <= 4; q++) {
                    unique64.add(new WcstCard(s, c, q));
                }
            }
        }
        deck.addAll(unique64);
        deck.addAll(unique64);
        Collections.shuffle(deck);
    }

    private void loadNextSortingCard() {
        if (trialsAdministeredCount >= TOTAL_CARDS_DECK || categoriesCompletedCount >= CATEGORIES_TO_COMPLETE) {
            finishTest();
            return;
        }
        activeSortingCard = deck.get(trialsAdministeredCount);
        activeSortingCard.render(this, activeCardContainer);
        tvProgress.setText("Card " + (trialsAdministeredCount + 1) + " of " + TOTAL_CARDS_DECK);
    }

    private void showFeedback(String message) {
        if (feedbackToast != null) {
            feedbackToast.cancel();
        }
        feedbackToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        feedbackToast.show();
    }

    private void onKeyCardSelected(int keyIndex) {
        WcstCard selectedKeyCard = keyCards[keyIndex];
        SortingRule activeRule = ruleSequence[currentRuleIndex];
        
        // 1. Determine correctness based on CURRENT rule
        boolean isCorrect = isMatch(activeSortingCard, selectedKeyCard, activeRule);
        
        // 2. Clinical Scoring Logic (Phase 3)
        trialsAdministeredCount++;
        trialSinceRuleChange++;
        
        if (isCorrect) {
            totalCorrect++;
            consecutiveCorrectCount++;
            
            if (consecutiveCorrectCount >= 3) {
                conceptualLevelResponses++;
            }
            
            showFeedback("Correct");

            if (consecutiveCorrectCount == CONSECUTIVE_SUCCESS_REQUIRED) {
                previousRule = activeRule;
                categoriesCompletedCount++;
                currentRuleIndex++;
                consecutiveCorrectCount = 0;
                trialSinceRuleChange = 0;
            }
        } else {
            totalErrors++;
            
            if (consecutiveCorrectCount >= 5 && consecutiveCorrectCount < 10) {
                failureToMaintainSet++;
            }
            
            if (previousRule != null && isMatch(activeSortingCard, selectedKeyCard, previousRule)) {
                perseverativeErrors++;
            } else {
                nonPerseverativeErrors++;
            }
            
            consecutiveCorrectCount = 0;
            showFeedback("Incorrect");
        }

        loadNextSortingCard();
    }

    private boolean isMatch(WcstCard card, WcstCard key, SortingRule rule) {
        switch (rule) {
            case COLOR:  return card.color == key.color;
            case FORM:   return card.shape == key.shape;
            case NUMBER: return card.quantity == key.quantity;
            default:     return false;
        }
    }

    private void finishTest() {
        try {
            JSONObject metrics = new JSONObject();
            metrics.put("test_type", "WCST");
            metrics.put("total_trials", trialsAdministeredCount);
            metrics.put("total_correct", totalCorrect);
            metrics.put("total_errors", totalErrors);
            metrics.put("perseverative_errors", perseverativeErrors);
            metrics.put("non_perseverative_errors", nonPerseverativeErrors);
            metrics.put("conceptual_level_responses", conceptualLevelResponses);
            metrics.put("failure_to_maintain_set", failureToMaintainSet);
            metrics.put("categories_completed", categoriesCompletedCount);

            Intent intent = new Intent(this, ReportActivity.class);
            intent.putExtra("TEST_TYPE", "WCST");
            intent.putExtra("DETAILED_METRICS", metrics.toString());
            startActivity(intent);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
        }
    }
}