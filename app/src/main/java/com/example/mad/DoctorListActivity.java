package com.example.mad;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class DoctorListActivity extends AppCompatActivity {
    private List<User> doctors = new ArrayList<>();
    private List<String> doctorNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String currentUser;
    private String reportToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        currentUser = getIntent().getStringExtra("USERNAME");
        reportToSend = getIntent().getStringExtra("REPORT_TO_SEND");
        
        ListView lv = findViewById(R.id.lvDoctorsList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, doctorNames);
        lv.setAdapter(adapter);

        loadVerifiedDoctors();

        lv.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("DOCTOR_NAME", doctors.get(position).username);
            intent.putExtra("SENDER_NAME", currentUser);
            intent.putExtra("IS_DOCTOR_VIEW", false);
            if (reportToSend != null) {
                intent.putExtra("REPORT_TO_SEND", reportToSend);
            }
            startActivity(intent);
            finish(); // Go back after selecting doctor
        });
    }

    private void loadVerifiedDoctors() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<User> list = AppDatabase.getDatabase(this).userDao().getVerifiedDoctors();
            runOnUiThread(() -> {
                doctors.clear();
                doctorNames.clear();
                doctors.addAll(list);
                for (User u : list) {
                    doctorNames.add("Dr. " + u.username);
                }
                adapter.notifyDataSetChanged();
            });
        });
    }
}