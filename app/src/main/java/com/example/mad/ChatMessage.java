package com.example.mad;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String sender;
    public String receiver;
    public String content;
    public long timestamp;

    // Required for Firebase
    public ChatMessage() {}

    public ChatMessage(String sender, String receiver, String content, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
    }
}