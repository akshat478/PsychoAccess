package com.example.mad;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.Executor;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextInputEditText etUsername = findViewById(R.id.etRegUsername);
        TextInputEditText etPassword = findViewById(R.id.etRegPassword);
        RadioGroup rgRole = findViewById(R.id.rgRole);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            int checkedId = rgRole.getCheckedRadioButtonId();

            if (username.isEmpty() || password.isEmpty() || checkedId == -1) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String roleText = ((RadioButton) findViewById(checkedId)).getText().toString();
            String role = roleText.equalsIgnoreCase("Doctor") ? "DOCTOR" : "USER";
            boolean isVerified = role.equals("USER"); 

            AppDatabase.databaseWriteExecutor.execute(() -> {
                User existing = AppDatabase.getDatabase(this).userDao().getUserByName(username);
                if (existing != null) {
                    runOnUiThread(() -> Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show());
                    return;
                }

                User newUser = new User(username, password, role, isVerified);
                AppDatabase.getDatabase(this).userDao().register(newUser);
                
                runOnUiThread(() -> promptBiometricSetup(username));
            });
        });
    }

    private void promptBiometricSetup(String username) {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(RegisterActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                SharedPreferences sharedPref = getSharedPreferences("MAD_PREFS", Context.MODE_PRIVATE);
                sharedPref.edit().putBoolean("BIOMETRIC_ENABLED_" + username, true).apply();
                Toast.makeText(RegisterActivity.this, "Biometrics linked!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(RegisterActivity.this, "Registration successful (without biometrics)", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Link Fingerprint")
                .setSubtitle("Use your fingerprint for faster login next time")
                .setNegativeButtonText("Skip")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}