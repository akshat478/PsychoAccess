package com.example.mad;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.util.Locale;

public class WcstInstructionsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private boolean isTtsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wcst_instructions);

        isTtsEnabled = getSharedPreferences("MAD_PREFS", MODE_PRIVATE).getBoolean("IS_TTS_ENABLED", false);
        if (isTtsEnabled) {
            tts = new TextToSpeech(this, this);
        }

        MaterialButton btnStart = findViewById(R.id.btnStartWcstGame);
        btnStart.setOnClickListener(v -> {
            startActivity(new Intent(this, WcstActivity.class));
            finish();
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && isTtsEnabled) {
            tts.setLanguage(Locale.US);
            String instructions = "In this test, you must sort cards from the deck into categories. " +
                    "The sorting rule is hidden. Use the feedback of Correct or Incorrect to figure it out. " +
                    "The rule may change without warning.";
            tts.speak(instructions, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}