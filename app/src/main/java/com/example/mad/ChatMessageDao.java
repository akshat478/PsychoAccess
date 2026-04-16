package com.example.mad;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ChatMessageDao {
    @Insert
    void insert(ChatMessage message);

    @Query("SELECT * FROM chat_messages WHERE " +
           "(sender = :user1 AND receiver = :user2) OR " +
           "(sender = :user2 AND receiver = :user1) " +
           "ORDER BY timestamp ASC")
    List<ChatMessage> getChatHistory(String user1, String user2);

    @Query("SELECT DISTINCT sender FROM chat_messages WHERE receiver = :doctorName")
    List<String> getUsersWhoMessaged(String doctorName);

    @Query("SELECT DISTINCT CASE WHEN sender = :me THEN receiver ELSE sender END FROM chat_messages WHERE sender = :me OR receiver = :me")
    List<String> getAllChatPartners(String me);
}