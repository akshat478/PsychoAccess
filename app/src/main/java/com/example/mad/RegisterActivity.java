package com.example.mad;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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
            boolean isVerified = role.equals("USER"); // Doctors need admin approval

            AppDatabase.databaseWriteExecutor.execute(() -> {
                // Check if user already exists
                User existing = AppDatabase.getDatabase(this).userDao().getUserByName(username);
                if (existing != null) {
                    runOnUiThread(() -> Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show());
                    return;
                }

                User newUser = new User(username, password, role, isVerified);
                AppDatabase.getDatabase(this).userDao().register(newUser);
                
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
    }
}