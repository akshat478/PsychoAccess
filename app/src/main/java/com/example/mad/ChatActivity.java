package com.example.mad;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private String doctorName;
    private String senderName;
    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private EditText etMessage;
    private boolean isDoctorView;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_new);

        doctorName = getIntent().getStringExtra("DOCTOR_NAME");
        senderName = getIntent().getStringExtra("SENDER_NAME");
        isDoctorView = getIntent().getBooleanExtra("IS_DOCTOR_VIEW", false);
        currentUsername = isDoctorView ? doctorName : senderName;

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        FloatingActionButton btnSend = findViewById(R.id.btnSend);

        if (isDoctorView) {
            ((TextView) findViewById(R.id.tvChatWith)).setText("Patient: " + senderName);
        } else {
            ((TextView) findViewById(R.id.tvChatWith)).setText("Chat with Dr. " + doctorName);
        }

        adapter = new ChatAdapter(new ArrayList<>());
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        // Check for report to send
        String reportData = getIntent().getStringExtra("REPORT_TO_SEND");
        if (reportData != null) {
            sendReportMessage(reportData);
        }

        loadChatHistory();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendReportMessage(String reportData) {
        ChatMessage msg = new ChatMessage(senderName, doctorName, reportData, System.currentTimeMillis(), true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(this).chatMessageDao().insert(msg);
            loadChatHistory();
        });
    }

    private void loadChatHistory() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<ChatMessage> history = AppDatabase.getDatabase(this).chatMessageDao()
                    .getChatHistory(senderName, doctorName);
            runOnUiThread(() -> {
                adapter.setMessages(history);
                if (history.size() > 0) {
                    rvChat.scrollToPosition(history.size() - 1);
                }
            });
        });
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        String actualSender = isDoctorView ? doctorName : senderName;
        String actualReceiver = isDoctorView ? senderName : doctorName;

        ChatMessage msg = new ChatMessage(actualSender, actualReceiver, content, System.currentTimeMillis(), false);
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(this).chatMessageDao().insert(msg);
            runOnUiThread(() -> {
                etMessage.setText("");
                loadChatHistory();
            });
        });
    }

    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_TEXT = 0;
        private static final int TYPE_REPORT = 1;
        private List<ChatMessage> messages;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        public ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        public void setMessages(List<ChatMessage> messages) {
            this.messages = messages;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isReport ? TYPE_REPORT : TYPE_TEXT;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_REPORT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_report, parent, false);
                return new ReportViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_text, parent, false);
                return new TextViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatMessage msg = messages.get(position);
            if (holder instanceof ReportViewHolder) {
                ((ReportViewHolder) holder).bind(msg);
            } else if (holder instanceof TextViewHolder) {
                ((TextViewHolder) holder).bind(msg);
            }
        }

        @Override
        public int getItemCount() { return messages.size(); }

        class TextViewHolder extends RecyclerView.ViewHolder {
            TextView tvMsg, tvTime;
            LinearLayout container;

            public TextViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMsg = itemView.findViewById(R.id.tvMessage);
                tvTime = itemView.findViewById(R.id.tvTime);
                container = itemView.findViewById(R.id.container);
            }

            void bind(ChatMessage msg) {
                tvMsg.setText(msg.content);
                tvTime.setText(timeFormat.format(new Date(msg.timestamp)));
                boolean isMe = msg.sender.equals(currentUsername);
                container.setGravity(isMe ? Gravity.END : Gravity.START);
                tvMsg.setBackgroundResource(isMe ? R.drawable.bg_bubble_me : R.drawable.bg_bubble_other);
                tvMsg.setTextColor(isMe ? Color.WHITE : Color.BLACK);
            }
        }

        class ReportViewHolder extends RecyclerView.ViewHolder {
            TextView tvType, tvScore, tvFeedback, tvTime;
            LinearLayout container;

            public ReportViewHolder(@NonNull View itemView) {
                super(itemView);
                tvType = itemView.findViewById(R.id.tvReportType);
                tvScore = itemView.findViewById(R.id.tvReportScore);
                tvFeedback = itemView.findViewById(R.id.tvReportFeedback);
                tvTime = itemView.findViewById(R.id.tvReportTime);
                container = itemView.findViewById(R.id.reportContainer);
            }

            void bind(ChatMessage msg) {
                String data = msg.content.replace("REPORT_DATA:", "");
                String[] parts = data.split("\\|");
                if (parts.length >= 3) {
                    tvType.setText(parts[0] + " VERIFIED REPORT");
                    tvScore.setText("Score: " + parts[1]);
                    tvFeedback.setText(parts[2]);
                }
                tvTime.setText(timeFormat.format(new Date(msg.timestamp)));
                boolean isMe = msg.sender.equals(currentUsername);
                container.setGravity(isMe ? Gravity.END : Gravity.START);
            }
        }
    }
}