package com.example.mad;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SoptActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private List<Integer> imageIds = new ArrayList<>();
    private Set<Integer> clickedIds = new HashSet<>();
    private SoptAdapter adapter;
    private TextView tvStatus;
    private int score = 0;
    
    private TextToSpeech tts;
    private boolean isTtsEnabled;
    private Toast feedbackToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sopt);
        applyImmersiveMode();

        tvStatus = findViewById(R.id.tvStatus);
        RecyclerView rvGrid = findViewById(R.id.rvSoptGrid);

        // Phase 1: Initialize 8 distinct built-in icons
        imageIds.add(android.R.drawable.ic_menu_camera);
        imageIds.add(android.R.drawable.ic_menu_call);
        imageIds.add(android.R.drawable.ic_menu_gallery);
        imageIds.add(android.R.drawable.ic_menu_manage);
        imageIds.add(android.R.drawable.ic_menu_save);
        imageIds.add(android.R.drawable.ic_menu_share);
        imageIds.add(android.R.drawable.ic_menu_search);
        imageIds.add(android.R.drawable.ic_menu_view);

        Collections.shuffle(imageIds);

        // Phase 2: Setup Adapter
        adapter = new SoptAdapter(imageIds, this::handleCardClick);
        rvGrid.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        rvGrid.setAdapter(adapter);

        isTtsEnabled = getSharedPreferences("MAD_PREFS", MODE_PRIVATE).getBoolean("IS_TTS_ENABLED", false);
        if (isTtsEnabled) {
            tts = new TextToSpeech(this, this);
        } else {
            speakInstructions();
        }
    }

    private void applyImmersiveMode() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void speakInstructions() {
        if (isTtsEnabled) {
            speak("Click a new image you haven't selected yet.");
        }
    }

    private void handleCardClick(int imageId) {
        if (clickedIds.contains(imageId)) {
            showInstantFeedback("Incorrect");
            endTest();
        } else {
            clickedIds.add(imageId);
            score++;
            tvStatus.setText("Score: " + score + "/8");
            showInstantFeedback("Correct");

            if (score == 8) {
                endTest();
            } else {
                Collections.shuffle(imageIds);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void showInstantFeedback(String msg) {
        if (feedbackToast != null) feedbackToast.cancel();
        feedbackToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        feedbackToast.show();
        if (isTtsEnabled) speak(msg);
    }

    private void endTest() {
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("TEST_TYPE", "SOPT");
        intent.putExtra("MAX_SPAN", score);
        startActivity(intent);
        finish();
    }

    @Override 
    public void onInit(int status) { 
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            new Handler().postDelayed(this::speakInstructions, 500);
        }
    }

    private void speak(String text) { 
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null); 
    }

    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }
}