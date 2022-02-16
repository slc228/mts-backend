package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.HashMap;
import java.util.List;

public interface UserDao {
    List<User> getAllUsers();
    void InsertUser(String username, String password, String phone, String email, int projectNum,
                    String validDate, String role, int state, String eventLimiter, String sensitiveLimiter);
    void UpdateUserByUsername(String username, String password, String phone, String email, int projectNum,
                              String validDate, String role, int state, String eventLimiter, String sensitiveLimiter);
    Boolean existByUsername(String username);
    Boolean existsByPhone(String phone);
    User findByUsername(String username);
    User findByPhone(String phone);
    void deleteByUsername(String username);
    void changeUserState(String username);
}
