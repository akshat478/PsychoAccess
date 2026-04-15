package com.example.mad;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String username;
    public String password;
    public String role; // "USER", "DOCTOR", "ADMIN"
    public boolean isVerified; // Only for DOCTORs

    // Required for Firebase
    public User() {}

    public User(String username, String password, String role, boolean isVerified) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.isVerified = isVerified;
    }
}