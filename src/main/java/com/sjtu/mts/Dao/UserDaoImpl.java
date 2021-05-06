package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.User;
import com.sjtu.mts.Repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepository;

    public UserDaoImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Boolean existByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    public void deleteByUsername(String username) {
        userRepository.deleteByUsername(username);
    }
    @Override
    public List<User> findAllByUsernameContains(String username){
        return userRepository.findAllByUsernameContains(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void changeUserState(String username){
        User user = userRepository.findByUsername(username);
        Integer state = user.getState();
        if (state == 0){
            user.setState(1);
        }
        if (state == 1){
            user.setState(0);
        }
        userRepository.save(user);
    }
}

