package com.example.mad;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatActivity extends AppCompatActivity {
    private String doctorName;
    private String senderName;
    private TextView tvChatLogs;
    private EditText etMessage;
    private NestedScrollView scrollView;
    private boolean isDoctorView;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mSocket = SocketHandler.getSocket();

        doctorName = getIntent().getStringExtra("DOCTOR_NAME");
        senderName = getIntent().getStringExtra("SENDER_NAME");
        isDoctorView = getIntent().getBooleanExtra("IS_DOCTOR_VIEW", false);

        tvChatLogs = findViewById(R.id.tvChatLogs);
        etMessage = findViewById(R.id.etMessage);
        scrollView = findViewById(R.id.scrollView);
        FloatingActionButton btnSend = findViewById(R.id.btnSend);

        if (isDoctorView) {
            ((TextView) findViewById(R.id.tvChatWith)).setText("Patient: " + senderName);
        } else {
            ((TextView) findViewById(R.id.tvChatWith)).setText("Chat with Dr. " + doctorName);
        }

        // 1. Load Chat History from Local Database
        loadChatHistory();

        // 2. Listen for incoming messages
        listenForMessages();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadChatHistory() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<ChatMessage> history = AppDatabase.getDatabase(this).chatMessageDao()
                    .getChatHistory(senderName, doctorName);
            
            runOnUiThread(() -> {
                tvChatLogs.setText("");
                for (ChatMessage msg : history) {
                    String displaySender = msg.sender.equals(isDoctorView ? doctorName : senderName) ? "Me" : msg.sender;
                    tvChatLogs.append("\n" + displaySender + ": " + msg.content);
                }
                scrollToBottom();
            });
        });
    }

    private void listenForMessages() {
        if (mSocket == null) return;

        mSocket.on("receive_message", args -> runOnUiThread(() -> {
            JSONObject data = (JSONObject) args[0];
            try {
                String sender = data.getString("sender");
                String receiver = data.getString("receiver");
                String content = data.getString("content");

                // Only show if it's for this conversation
                if ((sender.equals(doctorName) && receiver.equals(senderName)) ||
                    (sender.equals(senderName) && receiver.equals(doctorName))) {
                    
                    // Save received message to DB
                    saveMessageToDb(sender, receiver, content);
                    
                    String displaySender = sender.equals(isDoctorView ? doctorName : senderName) ? "Me" : sender;
                    tvChatLogs.append("\n" + displaySender + ": " + content);
                    scrollToBottom();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }));
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        String actualSender = isDoctorView ? doctorName : senderName;
        String actualReceiver = isDoctorView ? senderName : doctorName;

        // 1. Save to Local DB
        saveMessageToDb(actualSender, actualReceiver, content);

        // 2. Emit to Socket Server
        if (mSocket != null && mSocket.connected()) {
            JSONObject messageData = new JSONObject();
            try {
                messageData.put("sender", actualSender);
                messageData.put("receiver", actualReceiver);
                messageData.put("content", content);
                messageData.put("timestamp", System.currentTimeMillis());
                mSocket.emit("send_message", messageData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 3. Update UI
        tvChatLogs.append("\nMe: " + content);
        etMessage.setText("");
        scrollToBottom();
    }

    private void saveMessageToDb(String sender, String receiver, String content) {
        ChatMessage msg = new ChatMessage(sender, receiver, content, System.currentTimeMillis());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(this).chatMessageDao().insert(msg);
        });
    }

    private void scrollToBottom() {
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                scrollView.fullScroll(NestedScrollView.FOCUS_DOWN), 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.off("receive_message");
        }
    }
}