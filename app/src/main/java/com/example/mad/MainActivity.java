package com.example.mad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private String currentUser;
    private String role;
    private TextToSpeech tts;
    private boolean isTtsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getSharedPreferences("MAD_PREFS", Context.MODE_PRIVATE);
        isTtsEnabled = sharedPref.getBoolean("IS_TTS_ENABLED", false);

        role = getIntent().getStringExtra("USER_ROLE");
        if (role == null) role = sharedPref.getString("USER_ROLE", null);
        currentUser = getIntent().getStringExtra("USERNAME");
        if (currentUser == null) currentUser = sharedPref.getString("USERNAME", null);

        tts = new TextToSpeech(this, this);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        MaterialCardView cardAdmin = findViewById(R.id.cardAdminPanel);
        MaterialCardView cardMessageDoctor = findViewById(R.id.cardMessageDoctor);
        MaterialCardView cardViewMessages = findViewById(R.id.cardViewMessages);
        MaterialCardView cardMyRecords = findViewById(R.id.cardMyRecords);
        MaterialCardView cardWcst = findViewById(R.id.cardWcst);
        MaterialCardView cardSopt = findViewById(R.id.cardSopt);
        
        ImageButton btnTtsToggle = findViewById(R.id.btnTtsToggle);
        updateTtsIcon(btnTtsToggle);

        tvWelcome.setText("Welcome, " + (currentUser != null ? currentUser : "User"));

        if ("ADMIN".equals(role)) {
            cardAdmin.setVisibility(View.VISIBLE);
        } else if ("USER".equals(role)) {
            cardMessageDoctor.setVisibility(View.VISIBLE);
            cardMyRecords.setVisibility(View.VISIBLE);
            cardWcst.setVisibility(View.VISIBLE);
            cardSopt.setVisibility(View.VISIBLE);
        } else if ("DOCTOR".equals(role)) {
            cardViewMessages.setVisibility(View.VISIBLE);
        }

        btnTtsToggle.setOnClickListener(v -> {
            isTtsEnabled = !isTtsEnabled;
            sharedPref.edit().putBoolean("IS_TTS_ENABLED", isTtsEnabled).apply();
            updateTtsIcon(btnTtsToggle);
            String msg = isTtsEnabled ? "Accessibility mode enabled." : "Accessibility mode disabled";
            speak(msg);
        });

        findViewById(R.id.btnCorsi).setOnClickListener(v -> startActivity(new Intent(this, CorsiTestActivity.class)));
        findViewById(R.id.btnRotation).setOnClickListener(v -> startActivity(new Intent(this, CardRotationActivity.class)));
        findViewById(R.id.btnWcst).setOnClickListener(v -> startActivity(new Intent(this, WcstInstructionsActivity.class)));
        findViewById(R.id.btnSopt).setOnClickListener(v -> startActivity(new Intent(this, SoptActivity.class)));
        findViewById(R.id.btnAdminPanel).setOnClickListener(v -> startActivity(new Intent(this, AdminActivity.class)));
        findViewById(R.id.btnMessageDoctor).setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorListActivity.class);
            intent.putExtra("USERNAME", currentUser);
            startActivity(intent);
        });
        findViewById(R.id.btnViewMessages).setOnClickListener(v -> {
            Intent intent = new Intent(this, MessageListActivity.class);
            intent.putExtra("USERNAME", currentUser);
            startActivity(intent);
        });
        findViewById(R.id.btnMyRecords).setOnClickListener(v -> startActivity(new Intent(this, MyRecordsActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sharedPref.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void updateTtsIcon(ImageButton btn) {
        btn.setImageResource(isTtsEnabled ? android.R.drawable.ic_lock_silent_mode : android.R.drawable.ic_lock_silent_mode_off);
    }

    @Override public void onInit(int status) { if (status == TextToSpeech.SUCCESS) tts.setLanguage(Locale.US); }
    private void speak(String text) { if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null); }
    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }
}