package com.sjtu.mts.Service;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public interface UserService {

    JSONArray getAllUsers();
    JSONArray getAllManager();
    /**
     * find username by phone
     *
     * @param phone user's phone
     * @return username
     */
    String findUsernameByPhone(String phone);

    /**
     * whether username typed by user available
     *
     * @param username username typed by user
     * @return whether username available
     */
    JSONObject usernameAvailable(String username);

    /**
     * whether username typed by user available
     *
     * @param username username typed by user
     * @return whether username available in manager
     */
    JSONObject usernameAvailableManager(String username);

    /**
     * whether phone typed by user available
     *
     * @param phone phone typed by user
     * @return whether phone available
     */
    JSONObject phoneAvailable(String phone);

    /**
     * whether phone typed by user available
     *
     * @param phone phone typed by user
     * @return whether phone available in mabager
     */
    JSONObject phoneAvailableManager(String phone);

    /**
     * register into database as a user
     *
     * @param username    username
     * @param password    password
     * @param phone       phone
     * @param email       email
     * @return whether register successfully 1:success, 0:fail
     */
    JSONObject registerUser(String username, String password, String phone, String email);
    /**
     * register into database as a manager
     *
     * @param username    username
     * @param password    password
     * @param phone       phone
     * @param email       email
     * @return whether register successfully 1:success, 0:fail
     */
    JSONObject registerManager(String username, String password, String phone, String email);

    /**
     * login with username and password
     *
     * @param username username
     * @param password password
     * @return whether login successfully 1:success, 0:fail
     */
    JSONObject login(String username, String password,String role);



    /**
     * login with username and password
     *
     * @param username username
     * @param password password
     * @return whether login successfully 1:success, 0:fail
     */
    JSONObject loginManager(String username, String password);


    JSONObject changeUserState(String username);
}
