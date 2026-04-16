package com.example.mad;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MessageListActivity extends AppCompatActivity {
    private List<String> userList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String doctorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        doctorName = getIntent().getStringExtra("USERNAME");
        ListView lv = findViewById(R.id.lvUserMessages);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        lv.setAdapter(adapter);

        loadUsers();

        lv.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("DOCTOR_NAME", doctorName);
            intent.putExtra("SENDER_NAME", userList.get(position));
            intent.putExtra("IS_DOCTOR_VIEW", true);
            startActivity(intent);
        });
    }

    private void loadUsers() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Get all unique users who have sent a message to this doctor
            List<String> users = AppDatabase.getDatabase(this).chatMessageDao().getAllChatPartners(doctorName);
            runOnUiThread(() -> {
                userList.clear();
                userList.addAll(users);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers(); // Refresh when returning to the screen
    }
}