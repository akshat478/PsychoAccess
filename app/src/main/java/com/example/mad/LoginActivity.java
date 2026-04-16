package com.example.mad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.Executor;

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

        TextInputEditText etUsername = findViewById(R.id.etUsername);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnRegister = findViewById(R.id.tvRegister);
        MaterialButton btnBiometric = findViewById(R.id.btnBiometric);

        // Remove the separate biometric button as it's now part of the password flow
        btnBiometric.setVisibility(android.view.View.GONE);

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
                            // Password correct, now check for Biometric 2FA
                            checkBiometricEnforcement(user.username, user.role);
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

    private void checkBiometricEnforcement(String username, String role) {
        SharedPreferences sharedPref = getSharedPreferences("MAD_PREFS", Context.MODE_PRIVATE);
        boolean isBioEnabled = sharedPref.getBoolean("BIOMETRIC_ENABLED_" + username, false);
        
        if (isBioEnabled) {
            // Force biometric scan after password success
            startBiometricVerification(username, role);
        } else {
            // Proceed normally if not linked yet
            loginSuccess(username, role);
        }
    }

    private void startBiometricVerification(String username, String role) {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                loginSuccess(username, role);
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(LoginActivity.this, "Biometric verification failed: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(LoginActivity.this, "Fingerprint not recognized", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Security Verification")
                .setSubtitle("Scan fingerprint to confirm it is you")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void loginSuccess(String username, String role) {
        SharedPreferences sharedPref = getSharedPreferences("MAD_PREFS", Context.MODE_PRIVATE);
        sharedPref.edit().putString("USERNAME", username).putString("USER_ROLE", role).apply();
        goToMain(username, role);
    }

    private void goToMain(String username, String role) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_ROLE", role);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finish();
    }
}