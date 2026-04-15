package com.example.mad;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CorsiTestActivity extends AppCompatActivity {
    private List<View> blocks = new ArrayList<>();
    private List<Integer> sequence = new ArrayList<>();
    private List<Integer> userInputs = new ArrayList<>();
    private int currentLevel = 2; // Starting sequence length
    private boolean isUserTurn = false;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corsi); // [cite: 293, 336]

        statusText = findViewById(R.id.statusText);
        initializeBlocks();
        startNextRound();
    }

    private void initializeBlocks() {
        // Adding 9 blocks to match the assessment requirement
        int[] blockIds = {R.id.block1, R.id.block2, R.id.block3, R.id.block4,
                R.id.block5, R.id.block6, R.id.block7, R.id.block8, R.id.block9};

        for (int id : blockIds) {
            View v = findViewById(id);
            blocks.add(v);
            v.setOnClickListener(view -> handleTap(blocks.indexOf(view))); // [cite: 339, 631]
        }
    }

    private void startNextRound() {
        userInputs.clear();
        isUserTurn = false;
        statusText.setText("Watch the sequence...");
        generateSequence();

        // Delay sequence start so the child is prepared
        new Handler().postDelayed(this::playSequence, 1000);
    }

    private void generateSequence() {
        sequence.clear();
        Random random = new Random();
        for (int i = 0; i < currentLevel; i++) {
            sequence.add(random.nextInt(blocks.size()));
        }
    }

    private void playSequence() {
        Handler handler = new Handler();
        for (int i = 0; i < sequence.size(); i++) {
            int blockIndex = sequence.get(i);
            // Highlight blocks one by one
            handler.postDelayed(() -> highlightBlock(blockIndex), i * 1200);
        }

        // Enable user input after sequence finishes
        handler.postDelayed(() -> {
            isUserTurn = true;
            statusText.setText("Your turn! Reproduce the sequence.");
            Toast.makeText(this, "Go!", Toast.LENGTH_SHORT).show(); // [cite: 640]
        }, sequence.size() * 1200);
    }

    private void highlightBlock(int index) {
        View v = blocks.get(index);
        v.setBackgroundColor(Color.YELLOW); // "Active" state
        // Return to default state after 600ms
        new Handler().postDelayed(() -> v.setBackgroundResource(R.drawable.block_default), 600);
    }

    private void handleTap(int index) {
        if (!isUserTurn) return;

        highlightBlock(index);
        userInputs.add(index);

        // Check accuracy immediately
        if (userInputs.get(userInputs.size() - 1) != sequence.get(userInputs.size() - 1)) {
            endTest();
            return;
        }

        // Check if level is completed
        if (userInputs.size() == sequence.size()) {
            currentLevel++;
            statusText.setText("Correct! Level " + (currentLevel - 1) + " cleared.");
            new Handler().postDelayed(this::startNextRound, 1500);
        }
    }

    private void endTest() {
        isUserTurn = false;
        Intent intent = new Intent(this, ReportActivity.class);
        // This 'key' must be an exact string match for the receiver
        intent.putExtra("MAX_SPAN", currentLevel - 1);
        startActivity(intent);
        finish();
    }
}