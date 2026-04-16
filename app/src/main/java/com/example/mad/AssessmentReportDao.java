package com.example.mad;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface AssessmentReportDao {
    @Insert
    void insert(AssessmentReport report);

    @Query("SELECT * FROM assessment_reports WHERE userId = :userId ORDER BY timestamp DESC")
    List<AssessmentReport> getReportsByUserId(String userId);
}