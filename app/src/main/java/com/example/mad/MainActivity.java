package com.example.mad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {
    private String currentUser;
    private String role;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSocket = SocketHandler.getSocket();

        role = getIntent().getStringExtra("USER_ROLE");
        currentUser = getIntent().getStringExtra("USERNAME");

        if ("DOCTOR".equals(role) && currentUser != null) {
            mSocket.emit("doctor_online", currentUser);
        }

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        MaterialCardView cardAdmin = findViewById(R.id.cardAdminPanel);
        MaterialCardView cardMessageDoctor = findViewById(R.id.cardMessageDoctor);
        MaterialCardView cardViewMessages = findViewById(R.id.cardViewMessages);
        
        Button btnAdmin = findViewById(R.id.btnAdminPanel);
        Button btnCorsi = findViewById(R.id.btnCorsi);
        Button btnRotation = findViewById(R.id.btnRotation);
        Button btnMessageDoctor = findViewById(R.id.btnMessageDoctor);
        Button btnViewMessages = findViewById(R.id.btnViewMessages);
        ImageButton btnLogout = findViewById(R.id.btnLogout);

        tvWelcome.setText("Welcome, " + (currentUser != null ? currentUser : "User"));

        // Ensure the correct panel is visible based on role
        if ("ADMIN".equals(role)) {
            cardAdmin.setVisibility(View.VISIBLE);
        } else if ("USER".equals(role)) {
            cardMessageDoctor.setVisibility(View.VISIBLE);
        } else if ("DOCTOR".equals(role)) {
            cardViewMessages.setVisibility(View.VISIBLE);
        }

        btnCorsi.setOnClickListener(v -> startActivity(new Intent(this, CorsiTestActivity.class)));
        btnRotation.setOnClickListener(v -> startActivity(new Intent(this, CardRotationActivity.class)));
        btnAdmin.setOnClickListener(v -> startActivity(new Intent(this, AdminActivity.class)));

        btnMessageDoctor.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorListActivity.class);
            intent.putExtra("USERNAME", currentUser);
            startActivity(intent);
        });

        btnViewMessages.setOnClickListener(v -> {
            Intent intent = new Intent(this, MessageListActivity.class);
            intent.putExtra("USERNAME", currentUser);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            if (mSocket != null) mSocket.emit("user_logout", currentUser);
            SharedPreferences sharedPref = getSharedPreferences("MAD_PREFS", Context.MODE_PRIVATE);
            sharedPref.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ("DOCTOR".equals(role) && currentUser != null && mSocket != null) {
            mSocket.emit("doctor_offline", currentUser);
        }
    }
}