package com.example.mad;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WcstActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int TOTAL_CARDS_DECK = 128;
    private static final int STREAK_REQUIRED = 10;
    private static final int CATEGORIES_MAX = 6;

    private enum Rule { COLOR, FORM, NUMBER }
    private int currentRuleIndex = 0; 
    private int currentStreak = 0;
    private int categoriesCompleted = 0;
    private int trialsCount = 0;

    private int totalCorrect = 0;
    private int perseverativeErrors = 0;
    private int nonPerseverativeErrors = 0;
    private Rule lastRule = null; 

    private List<WcstCard> deck = new ArrayList<>();
    private WcstCard activeCard;
    private final WcstCard[] keyCards = new WcstCard[4];

    private GridLayout activeCardContainer;
    private TextView tvProgress;
    private TextToSpeech tts;
    private boolean isTtsEnabled;
    private Toast feedbackToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wcst);
        applyImmersiveMode();

        tvProgress = findViewById(R.id.tvProgress);
        initializeKeyCards();
        generateDeck();

        isTtsEnabled = getSharedPreferences("MAD_PREFS", MODE_PRIVATE).getBoolean("IS_TTS_ENABLED", false);
        if (isTtsEnabled) {
            tts = new TextToSpeech(this, this);
        } else {
            loadNextCard();
        }
    }

    private void applyImmersiveMode() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void initializeKeyCards() {
        keyCards[0] = new WcstCard(WcstCard.Shape.TRIANGLE, WcstCard.ColorName.RED, 1);
        keyCards[1] = new WcstCard(WcstCard.Shape.STAR, WcstCard.ColorName.GREEN, 2);
        keyCards[2] = new WcstCard(WcstCard.Shape.CROSS, WcstCard.ColorName.YELLOW, 3);
        keyCards[3] = new WcstCard(WcstCard.Shape.CIRCLE, WcstCard.ColorName.BLUE, 4);

        int[] ids = {R.id.keyCard1, R.id.keyCard2, R.id.keyCard3, R.id.keyCard4};
        for (int i = 0; i < 4; i++) {
            View cardView = findViewById(ids[i]);
            keyCards[i].render(this, (GridLayout) cardView.findViewById(R.id.cardGridLayout));
            final int index = i;
            cardView.setOnClickListener(v -> handleSortSelection(index));
        }
        activeCardContainer = findViewById(R.id.activeCard).findViewById(R.id.cardGridLayout);
    }

    private void generateDeck() {
        List<WcstCard> unique64 = new ArrayList<>();
        for (WcstCard.Shape s : WcstCard.Shape.values()) {
            for (WcstCard.ColorName c : WcstCard.ColorName.values()) {
                for (int q = 1; q <= 4; q++) unique64.add(new WcstCard(s, c, q));
            }
        }
        deck.addAll(unique64); deck.addAll(unique64);
        Collections.shuffle(deck);
    }

    private void loadNextCard() {
        if (trialsCount >= TOTAL_CARDS_DECK || categoriesCompleted >= CATEGORIES_MAX) {
            finishTest();
            return;
        }
        
        if (trialsCount == 0 && isTtsEnabled) {
            speak("Select a matching card. The card to sort is shown below.");
        }
        
        activeCard = deck.get(trialsCount);
        activeCard.render(this, activeCardContainer);
        tvProgress.setText("Card " + (trialsCount + 1) + " of " + TOTAL_CARDS_DECK);
    }

    private void handleSortSelection(int keyIndex) {
        WcstCard targetKey = keyCards[keyIndex];
        Rule activeRule = Rule.values()[currentRuleIndex % 3]; 
        
        boolean isCorrect = checkMatch(activeCard, targetKey, activeRule);
        trialsCount++;

        if (isCorrect) {
            totalCorrect++;
            currentStreak++;
            showInstantFeedback("Correct");

            if (currentStreak == STREAK_REQUIRED) {
                lastRule = activeRule; 
                categoriesCompleted++;
                currentRuleIndex++; 
                currentStreak = 0;   
            }
        } else {
            if (lastRule != null && checkMatch(activeCard, targetKey, lastRule)) {
                perseverativeErrors++;
            } else {
                nonPerseverativeErrors++;
            }
            currentStreak = 0;
            showInstantFeedback("Incorrect");
        }
        loadNextCard();
    }

    private boolean checkMatch(WcstCard card, WcstCard key, Rule rule) {
        switch (rule) {
            case COLOR:  return card.color == key.color;
            case FORM:   return card.shape == key.shape;
            case NUMBER: return card.quantity == key.quantity;
            default:     return false;
        }
    }

    private void showInstantFeedback(String msg) {
        if (feedbackToast != null) feedbackToast.cancel();
        feedbackToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        feedbackToast.show();
        if (isTtsEnabled) speak(msg);
    }

    private void finishTest() {
        try {
            JSONObject m = new JSONObject();
            m.put("categories_completed", categoriesCompleted);
            m.put("total_trials", trialsCount);
            m.put("total_correct", totalCorrect);
            m.put("perseverative_errors", perseverativeErrors);
            m.put("non_perseverative_errors", nonPerseverativeErrors);

            Intent intent = new Intent(this, ReportActivity.class);
            intent.putExtra("TEST_TYPE", "WCST");
            intent.putExtra("DETAILED_METRICS", m.toString());
            startActivity(intent);
            finish();
        } catch (JSONException e) { finish(); }
    }

    @Override 
    public void onInit(int status) { 
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            new Handler().postDelayed(this::loadNextCard, 500);
        }
    }

    private void speak(String text) { 
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null); 
    }

    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }
}