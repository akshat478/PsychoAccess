package com.example.mad;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private List<User> unverifiedDoctors = new ArrayList<>();
    private List<String> displayNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        ListView lvDoctors = findViewById(R.id.lvDoctors);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayNames);
        lvDoctors.setAdapter(adapter);

        loadUnverifiedDoctors();

        lvDoctors.setOnItemClickListener((parent, view, position, id) -> {
            User doctor = unverifiedDoctors.get(position);
            verifyDoctor(doctor);
        });
    }

    private void loadUnverifiedDoctors() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<User> list = AppDatabase.getDatabase(this).userDao().getUnverifiedDoctors();
            runOnUiThread(() -> {
                unverifiedDoctors.clear();
                displayNames.clear();
                unverifiedDoctors.addAll(list);
                for (User u : list) {
                    displayNames.add("Dr. " + u.username + " (Tap to Verify)");
                }
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void verifyDoctor(User doctor) {
        doctor.isVerified = true;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(this).userDao().verifyDoctor(doctor);
            runOnUiThread(() -> {
                Toast.makeText(AdminActivity.this, "Doctor Verified!", Toast.LENGTH_SHORT).show();
                loadUnverifiedDoctors();
            });
        });
    }
}