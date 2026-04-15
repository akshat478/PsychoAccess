package com.example.mad;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        TextView reportTitle = findViewById(R.id.reportTitle);
        TextView metricLabel = findViewById(R.id.metricLabel);
        TextView scoreValue = findViewById(R.id.scoreValue);
        TextView feedbackText = findViewById(R.id.feedbackText);

        String testType = getIntent().getStringExtra("TEST_TYPE");

        if ("ROTATION".equals(testType)) {
            int score = getIntent().getIntExtra("ROTATION_SCORE", 0);
            reportTitle.setText("Rotation Test Report");
            metricLabel.setText("Total Score");
            scoreValue.setText(score + "/10");
            feedbackText.setText(score >= 7 ? "Great spatial visualization!" : "Assessment complete. Good effort!");
        } else {
            // Default to Corsi
            int maxSpan = getIntent().getIntExtra("MAX_SPAN", 0);
            reportTitle.setText("Corsi Test Report");
            metricLabel.setText("Max Span");
            scoreValue.setText(String.valueOf(maxSpan));
            feedbackText.setText(maxSpan >= 5 ? "Excellent spatial memory!" : "Test completed. Good effort!");
        }

        Button btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ReportActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}