package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.User;
import com.sjtu.mts.Repository.UserRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

@Repository
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepository;

    public UserDaoImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void InsertUser(String username, String password, String phone, String email, int projectNum,
                           String validDate, String role, int state, String eventLimiter, String sensitiveLimiter)
    {
        userRepository.InsertUser(username,password,phone,email,projectNum,validDate,role,state,eventLimiter,sensitiveLimiter);
    }

    @Override
    public void UpdateUserByUsername(String username, String password, String phone, String email, int projectNum,
                                     String validDate, String role, int state, String eventLimiter, String sensitiveLimiter)
    {
        userRepository.UpdateUserByUsername(username,password,phone,email,projectNum,validDate,role,state,eventLimiter,sensitiveLimiter);
    }

    @Override
    public Boolean existByUsername(String username) {
        return userRepository.ExistsUserByUsername(username).equals(BigInteger.ONE);
    }

    @Override
    public Boolean existsByPhone(String phone) {
        return userRepository.ExistsUserByPhone(phone).equals(BigInteger.ONE);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.SelectUserByUsername(username);
    }

    @Override
    public User findByPhone(String phone) {
        return userRepository.SelectUserByPhone(phone);
    }

    @Override
    public void deleteByUsername(String username) {
        userRepository.DeleteUserByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.SelectUser();
    }

    @Override
    public void changeUserState(String username){
        User user = userRepository.SelectUserByUsername(username);
        Integer state = user.getState();
        if (state == 0){
            user.setState(1);
        }
        if (state == 1){
            user.setState(0);
        }
        userRepository.InsertUser(user.getUsername(),user.getPassword(),user.getPhone(),user.getEmail(),user.getProjectNum(),
                                    user.getValidDate(),user.getRole(),user.getState(),user.getEventLimiter(),user.getSensitiveLimiter());
    }
}

