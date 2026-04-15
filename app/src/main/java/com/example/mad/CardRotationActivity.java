package com.example.mad;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

/**
 * CardRotationActivity implements a spatial reasoning test.
 * It identifies if an image is a rotation or a mirrored flip.
 */
public class CardRotationActivity extends AppCompatActivity {
    private int score = 0;
    private int currentQuestion = 1;
    private final int totalQuestions = 10;
    private boolean isMirrored = false;
    private float lastRotation = -1f;

    private ImageView targetCard, optionCard;
    private TextView questionStatus;
    private Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_rotation);

        targetCard = findViewById(R.id.targetCard);
        optionCard = findViewById(R.id.optionCard);
        questionStatus = findViewById(R.id.questionStatus);

        Button btnSame = findViewById(R.id.btnSame);
        Button btnDifferent = findViewById(R.id.btnDifferent);

        loadNewQuestion();

        btnSame.setOnClickListener(v -> checkAnswer(true));
        btnDifferent.setOnClickListener(v -> checkAnswer(false));
    }

    private void loadNewQuestion() {
        if (currentQuestion > totalQuestions) {
            endTest();
            return;
        }

        questionStatus.setText("Question: " + currentQuestion + "/" + totalQuestions);

        Random random = new Random();
        isMirrored = random.nextBoolean();

        float newRotation;
        do {
            newRotation = (random.nextInt(7) + 1) * 45f;
        } while (newRotation == lastRotation);

        lastRotation = newRotation;

        if (isMirrored) {
            optionCard.setImageResource(R.drawable.card_mirrored);
        } else {
            optionCard.setImageResource(R.drawable.card_original);
        }

        optionCard.setRotation(newRotation);
        currentQuestion++;
    }

    private void checkAnswer(boolean userGuessedSame) {
        if (currentToast != null) {
            currentToast.cancel();
        }

        if (userGuessedSame == !isMirrored) {
            score++;
            currentToast = Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT);
        } else {
            currentToast = Toast.makeText(this, "Incorrect", Toast.LENGTH_SHORT);
        }
        currentToast.show();
        loadNewQuestion();
    }

    private void endTest() {
        if (currentToast != null) {
            currentToast.cancel();
        }
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("ROTATION_SCORE", score);
        intent.putExtra("TEST_TYPE", "ROTATION");
        startActivity(intent);
        finish();
    }
}