package com.example.mad;

import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        TextView reportTitle = findViewById(R.id.reportTitle);
        TextView metricLabel = findViewById(R.id.metricLabel);
        TextView scoreValue = findViewById(R.id.scoreValue);
        TextView feedbackText = findViewById(R.id.feedbackText);
        View wcstCard = findViewById(R.id.wcstCard);
        TableLayout wcstTable = findViewById(R.id.wcstTable);

        String testType = getIntent().getStringExtra("TEST_TYPE");
        String detailedMetrics = getIntent().getStringExtra("DETAILED_METRICS");
        String finalScore = "0";
        String feedback = "";

        if ("WCST".equals(testType)) {
            reportTitle.setText("WCST Clinical Report");
            wcstCard.setVisibility(View.VISIBLE);
            
            try {
                JSONObject json = new JSONObject(detailedMetrics);
                finalScore = json.getString("categories_completed") + "/6 Categories";
                feedback = "Clinical assessment complete.";
                
                // Dynamically fill the WCST table
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (key.equals("test_type")) continue;
                    
                    TableRow row = new TableRow(this);
                    row.setPadding(0, 8, 0, 8);
                    
                    TextView label = new TextView(this);
                    label.setText(key.replace("_", " ").toUpperCase());
                    label.setTextSize(14f);
                    
                    TextView value = new TextView(this);
                    value.setText(json.getString(key));
                    value.setTextSize(14f);
                    value.setGravity(android.view.Gravity.END);
                    value.setTypeface(null, android.graphics.Typeface.BOLD);
                    
                    row.addView(label);
                    row.addView(value);
                    wcstTable.addView(row);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ("ROTATION".equals(testType)) {
            int score = getIntent().getIntExtra("ROTATION_SCORE", 0);
            reportTitle.setText("Rotation Test Report");
            metricLabel.setText("Total Score");
            finalScore = score + "/10";
            feedback = score >= 7 ? "Great spatial visualization!" : "Assessment complete. Good effort!";
        } else {
            testType = "CORSI";
            int maxSpan = getIntent().getIntExtra("MAX_SPAN", 0);
            reportTitle.setText("Corsi Test Report");
            metricLabel.setText("Max Span");
            finalScore = String.valueOf(maxSpan);
            feedback = maxSpan >= 5 ? "Excellent spatial memory!" : "Test completed. Good effort!";
        }

        scoreValue.setText(finalScore);
        feedbackText.setText(feedback);

        saveReportToDatabase(testType, finalScore, feedback, detailedMetrics);

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void saveReportToDatabase(String type, String score, String feedback, String detailed) {
        SharedPreferences sharedPref = getSharedPreferences("MAD_PREFS", MODE_PRIVATE);
        String currentUsername = sharedPref.getString("USERNAME", "Unknown");

        AssessmentReport report = new AssessmentReport(currentUsername, type, score, feedback, System.currentTimeMillis());
        report.detailedMetrics = detailed;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(this).assessmentReportDao().insert(report);
        });
    }
}