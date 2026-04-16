package com.example.mad;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "assessment_reports")
public class AssessmentReport {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String userId;
    public String testType;
    public String score;
    public String feedback;
    public long timestamp;
    public boolean isShared;

    public AssessmentReport(String userId, String testType, String score, String feedback, long timestamp) {
        this.userId = userId;
        this.testType = testType;
        this.score = score;
        this.feedback = feedback;
        this.timestamp = timestamp;
        this.isShared = false;
    }
}