package com.example.mad;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import java.util.Locale;
import java.util.Random;

public class CardRotationActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private int score = 0;
    private int currentQuestion = 1;
    private final int totalQuestions = 10;
    private boolean isMirrored = false;
    private float lastRotation = -1f;

    private ImageView targetCard, optionCard;
    private TextView questionStatus;
    private Toast currentToast;
    
    private TextToSpeech tts;
    private boolean isTtsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_rotation);
        applyImmersiveMode();
        
        targetCard = findViewById(R.id.targetCard);
        optionCard = findViewById(R.id.optionCard);
        questionStatus = findViewById(R.id.questionStatus);

        isTtsEnabled = getSharedPreferences("MAD_PREFS", MODE_PRIVATE).getBoolean("IS_TTS_ENABLED", false);
        if (isTtsEnabled) {
            tts = new TextToSpeech(this, this); // loadNewQuestion called in onInit
        } else {
            loadNewQuestion();
        }

        findViewById(R.id.btnSame).setOnClickListener(v -> checkAnswer(true));
        findViewById(R.id.btnDifferent).setOnClickListener(v -> checkAnswer(false));
    }

    private void applyImmersiveMode() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void loadNewQuestion() {
        if (currentQuestion > totalQuestions) {
            endTest();
            return;
        }

        questionStatus.setText("Question: " + currentQuestion + "/" + totalQuestions);
        
        // Speak instruction only on first question, but after TTS is ready
        if (currentQuestion == 1 && isTtsEnabled) {
            speak("Is the shape below a rotation or mirrored?");
        }

        Random random = new Random();
        isMirrored = random.nextBoolean();
        float newRotation;
        do { newRotation = (random.nextInt(7) + 1) * 45f; } while (newRotation == lastRotation);
        lastRotation = newRotation;

        optionCard.setImageResource(isMirrored ? R.drawable.card_mirrored : R.drawable.card_original);
        optionCard.setRotation(newRotation);
        currentQuestion++;
    }

    private void checkAnswer(boolean userGuessedSame) {
        if (currentToast != null) currentToast.cancel();

        if (userGuessedSame == !isMirrored) {
            score++;
            showInstantFeedback("Correct");
        } else {
            showInstantFeedback("Incorrect");
        }
        loadNewQuestion();
    }

    private void showInstantFeedback(String msg) {
        currentToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        currentToast.show();
        if (isTtsEnabled) speak(msg);
    }

    private void endTest() {
        if (currentToast != null) currentToast.cancel();
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("ROTATION_SCORE", score);
        intent.putExtra("TEST_TYPE", "ROTATION");
        startActivity(intent);
        finish();
    }

    @Override 
    public void onInit(int status) { 
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            new Handler().postDelayed(this::loadNewQuestion, 500);
        }
    }

    private void speak(String text) { 
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null); 
    }

    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }
}