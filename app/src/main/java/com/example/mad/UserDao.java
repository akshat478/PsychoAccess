package com.example.mad;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void register(User user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByName(String username);

    @Query("SELECT * FROM users WHERE role = 'DOCTOR' AND isVerified = 0")
    List<User> getUnverifiedDoctors();

    @Query("SELECT * FROM users WHERE role = 'DOCTOR' AND isVerified = 1")
    List<User> getVerifiedDoctors();

    @Update
    void verifyDoctor(User doctor);

    @Query("UPDATE users SET isVerified = :verified WHERE username = :username")
    void updateVerificationStatus(String username, boolean verified);
}