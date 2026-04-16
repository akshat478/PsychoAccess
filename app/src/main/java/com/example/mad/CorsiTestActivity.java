package com.example.mad;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CorsiTestActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private List<View> blocks = new ArrayList<>();
    private List<Integer> sequence = new ArrayList<>();
    private List<Integer> userInputs = new ArrayList<>();
    private int currentLevel = 2; 
    private boolean isUserTurn = false;
    private TextView statusText;
    private TextToSpeech tts;
    private boolean isTtsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corsi); 
        applyImmersiveMode();
        
        statusText = findViewById(R.id.statusText);
        initializeBlocks();

        isTtsEnabled = getSharedPreferences("MAD_PREFS", MODE_PRIVATE).getBoolean("IS_TTS_ENABLED", false);
        if (isTtsEnabled) {
            tts = new TextToSpeech(this, this); // startNextRound will be called in onInit
        } else {
            startNextRound();
        }
    }

    private void applyImmersiveMode() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void initializeBlocks() {
        int[] blockIds = {R.id.block1, R.id.block2, R.id.block3, R.id.block4,
                R.id.block5, R.id.block6, R.id.block7, R.id.block8, R.id.block9};
        for (int id : blockIds) {
            View v = findViewById(id);
            if (v != null) {
                blocks.add(v);
                v.setOnClickListener(view -> handleTap(blocks.indexOf(view)));
            }
        }
    }

    private void startNextRound() {
        userInputs.clear();
        isUserTurn = false;
        String msg = "Watch the sequence";
        statusText.setText(msg);
        if (isTtsEnabled) speak(msg);
        generateSequence();
        new Handler().postDelayed(this::playSequence, 2000);
    }

    private void generateSequence() {
        sequence.clear();
        Random random = new Random();
        for (int i = 0; i < currentLevel; i++) sequence.add(random.nextInt(blocks.size()));
    }

    private void playSequence() {
        Handler handler = new Handler();
        for (int i = 0; i < sequence.size(); i++) {
            int blockIndex = sequence.get(i);
            handler.postDelayed(() -> highlightBlock(blockIndex), i * 1200);
        }
        handler.postDelayed(() -> {
            isUserTurn = true;
            String msg = "Your turn! Reproduce the sequence.";
            statusText.setText(msg);
            if (isTtsEnabled) speak(msg);
        }, sequence.size() * 1200);
    }

    private void highlightBlock(int index) {
        View v = blocks.get(index);
        v.setBackgroundColor(Color.YELLOW); 
        new Handler().postDelayed(() -> v.setBackgroundResource(R.drawable.block_default), 600);
    }

    private void handleTap(int index) {
        if (!isUserTurn) return;
        highlightBlock(index);
        userInputs.add(index);

        if (userInputs.get(userInputs.size() - 1) != sequence.get(userInputs.size() - 1)) {
            if (isTtsEnabled) speak("Incorrect. Test ending.");
            endTest();
            return;
        }

        if (userInputs.size() == sequence.size()) {
            currentLevel++;
            if (isTtsEnabled) speak("Correct");
            statusText.setText("Correct!");
            new Handler().postDelayed(this::startNextRound, 1500);
        }
    }

    private void endTest() {
        isUserTurn = false;
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("TEST_TYPE", "CORSI");
        intent.putExtra("MAX_SPAN", currentLevel - 1);
        startActivity(intent);
        finish();
    }

    @Override 
    public void onInit(int status) { 
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            new Handler().postDelayed(this::startNextRound, 500); // Small delay for stability
        }
    }

    private void speak(String text) { 
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null); 
    }

    @Override 
    protected void onDestroy() { 
        if (tts != null) { tts.stop(); tts.shutdown(); } 
        super.onDestroy(); 
    }
}