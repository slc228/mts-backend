package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.User;

import java.util.List;

public interface UserDao {

    List<User> getAllUsers();
    /**
     * save user
     *
     * @param user user
     * @return user saved
     */
    User save(User user);

    /**
     * whether exists username in database
     *
     * @param username username
     * @return whether exists username
     */
    Boolean existByUsername(String username);

    /**
     * whether exists phone in database
     *
     * @param phone phone
     * @return whether exists phone
     */
    Boolean existsByPhone(String phone);

    /**
     * find user by username
     *
     * @param username username
     * @return user found
     */
    User findByUsername(String username);

    /**
     * find user by phone
     *
     * @param phone phone
     * @return user found
     */
    User findByPhone(String phone);

    /**
     * delete data from database by username
     *
     * @param username username
     */
    void deleteByUsername(String username);
    /**
     * find all user information contains the username
     * @param username username
     * @return all user that contains the username
     */
    List<User> findAllByUsernameContains(String username);

    void changeUserState(String username);

}
