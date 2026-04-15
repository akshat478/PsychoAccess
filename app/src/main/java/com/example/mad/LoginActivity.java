package com.example.mad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences sharedPref = getSharedPreferences("MAD_PREFS", Context.MODE_PRIVATE);
        String savedUser = sharedPref.getString("USERNAME", null);
        String savedRole = sharedPref.getString("USER_ROLE", null);
        
        if (savedUser != null && savedRole != null) {
            goToMain(savedUser, savedRole);
            return;
        }

        setContentView(R.layout.activity_login);

        // Re-initialize Sockets
        String serverIp = "192.168.1.3"; 
        SocketHandler.setSocket(serverIp);
        SocketHandler.establishConnection();

        TextInputEditText etUsername = findViewById(R.id.etUsername);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                User user = AppDatabase.getDatabase(this).userDao().login(username, password);
                runOnUiThread(() -> {
                    if (user != null) {
                        if (user.role.equals("DOCTOR") && !user.isVerified) {
                            Toast.makeText(this, "Wait for Admin Verification", Toast.LENGTH_LONG).show();
                        } else {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("USERNAME", user.username);
                            editor.putString("USER_ROLE", user.role);
                            editor.apply();
                            goToMain(user.username, user.role);
                        }
                    } else {
                        Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        btnRegister.setOnClickListener(v -> 
            startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void goToMain(String username, String role) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_ROLE", role);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finish();
    }
}