package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.ManagerDao;
import com.sjtu.mts.Dao.UserDao;
import com.sjtu.mts.Dao.UserRightsDao;
import com.sjtu.mts.Entity.Manager;
import com.sjtu.mts.Entity.User;
import com.sjtu.mts.Entity.UserRights;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImp implements UserService {

    @Autowired // This means to get the bean called userRepository
    private UserDao userDao;

    @Autowired
    private UserRightsDao userRightsDao;

    @Autowired
    private ManagerDao managerDao;

    @Override
    public JSONArray getAllUsers() {
        JSONArray jsonArray = new JSONArray();
        List<User> userList = userDao.getAllUsers();
        System.out.println("hereUser");
        System.out.println(userList.size());
        for(User user : userList){
            if (!user.getRole().equals("systemAdmin"))
            {
                JSONObject object = new JSONObject();
                object.put("username", user.getUsername());
                object.put("phone", user.getPhone());
                object.put("email", user.getEmail());
                object.put("status", user.getState());
                object.put("role", user.getRole());
                object.put("project_num", user.getProjectNum());
                object.put("valid_date", user.getValidDate());
                object.put("eventLimiter",user.getEventLimiter());
                object.put("sensitiveLimiter",user.getSensitiveLimiter());
                object.put("userRights",userRightsDao.findByUsername(user.getUsername()));
                jsonArray.appendElement(object);
            }
        }
        return jsonArray;
    }

    @Override
    public JSONArray getAllManager()
    {
        JSONArray jsonArray = new JSONArray();
        List<Manager> managerList = managerDao.getAllManager();
        for(Manager manager : managerList){
            JSONObject object = new JSONObject();
            object.put("username", manager.getUsername());
            object.put("phone", manager.getPhone());
            object.put("email", manager.getEmail());
            object.put("status", manager.getState());
            object.put("role", manager.getRole());
            object.put("project_num", manager.getProjectNum());
            object.put("valid_date", manager.getValidDate());
            jsonArray.appendElement(object);
        }
        return jsonArray;}

    @Override
    public String findUsernameByPhone(String phone) {
        return userDao.findByPhone(phone).getUsername();
    }

    @Override
    public JSONObject usernameAvailable(String username) {
        JSONObject result = new JSONObject();
        Boolean flag = userDao.existByUsername(username);
        result.put("available", (flag) ? 0 : 1);
        return result;
    }

    @Override
    public JSONObject usernameAvailableManager(String username) {
        JSONObject result = new JSONObject();
        Boolean flag = managerDao.existByUsername(username);
        result.put("available", (flag) ? 0 : 1);
        return result;
    }

    @Override
    public JSONObject phoneAvailable(String phone) {
        JSONObject result = new JSONObject();
        Boolean flag = userDao.existsByPhone(phone);
        result.put("available", (flag) ? 0 : 1);
        return result;
    }

    @Override
    public JSONObject phoneAvailableManager(String phone) {
        JSONObject result = new JSONObject();
        Boolean flag = managerDao.existsByPhone(phone);
        result.put("available", (flag) ? 0 : 1);
        return result;
    }


    @Override
    public JSONObject registerUser(String username, String password, String phone, String email, String role) {
        JSONObject result = new JSONObject();
        result.put("register", 0);
        String available = "available";
        System.out.println("registerTest");
        if ((int) usernameAvailable(username).get(available) != 1) {
            return result;
        }
        try {
            UserRights userRights=new UserRights(username,role);
            System.out.println("registerTest");
            userDao.InsertUser(username, password, phone, email, 0, "2099",role,1,"","");
            userRightsDao.InsertUserRights(userRights.getUsername(),userRights.getDataScreen(),userRights.getSchemeConfiguration(),
                    userRights.getGlobalSearch(),userRights.getAnalysis(),userRights.getWarning(),userRights.getBriefing(),
                    userRights.getUserRole(),userRights.getSensitiveWords());
            result.put("register", 1);
            System.out.println("registerTest");
            return result;
        } catch (Exception e) {
            result.put("register", 0);
        }
        return result;
    }

    @Override
    public JSONObject registerManager(String username, String password, String phone, String email) {
        JSONObject result = new JSONObject();
        result.put("register", 0);
        String available = "available";
        if ((int) usernameAvailableManager(username).get(available) != 1 ||(int) usernameAvailable(username).get(available) != 1 ) {
            return result;
        }
        try {
            Manager manager = new Manager(username, password, phone, email, 0, "2099",0,1);
            managerDao.save(manager);
            User user = new User(username, password, phone, email, 0, "2099","systemAdmin",1,"","");
            userDao.InsertUser(username, password, phone, email, 0, "2099","systemAdmin",1,"","");
            result.put("register", 1);
            return result;
        } catch (Exception e) {
            result.put("register", 0);
        }
        return result;
    }

    @Override
    public JSONObject login(String username, String password) {
        JSONObject result = new JSONObject();
        result.put("login", 0);
        if (!userDao.existByUsername(username)) {
            return result;
        } else {
            User user = userDao.findByUsername(username);
            System.out.println(user.getUsername());
            System.out.println(user.getPassword());
            if (!user.getPassword().equals(password)) {
                return result;
            } else {
                result.put("role", user.getRole());
                result.put("username", user.getUsername());
                result.put("phone", user.getPhone());
                result.put("email", user.getEmail());
                result.put("eventLimiter",user.getEventLimiter());
                result.put("sensitiveLimiter",user.getSensitiveLimiter());
                result.put("userRights",userRightsDao.findByUsername(username));
                result.put("login", 1);
                return result;
            }
        }
    }


    @Override
    public JSONObject loginManager(String username, String password) {
        JSONObject result = new JSONObject();
        result.put("login", 0);
        if (!managerDao.existByUsername(username)) {
            return result;
        } else {
            Manager manager = managerDao.findByUsername(username);
            if (!manager.getPassword().equals(password)) {
                return result;
            } else {
                result.put("login", 1);
                result.put("username", manager.getUsername());
                result.put("phone", manager.getPhone());
                result.put("email", manager.getEmail());
                result.put("role",manager.getRole().toString());
                return result;
            }
        }
    }
    @Override
    public JSONObject changeUserState(String username){
        JSONObject result = new JSONObject();
        result.put("changeUserState",0);
        try {
            userDao.changeUserState(username);
            result.put("changeUserState",1);
            return result;
        }catch (Exception e){
            System.out.println(e);
        }
        return result;
    }

    @Override
    public JSONObject changeUserJurisdiction(String username,String type,boolean checked)
    {
        JSONObject ret=new JSONObject();
        ret.put("changeUserJurisdiction",1);
        UserRights userRights=userRightsDao.findByUsername(username);
        if (type.equals("dataScreen")){
            userRights.setDataScreen(checked);
        }
        if (type.equals("schemeConfiguration")){
            userRights.setSchemeConfiguration(checked);
        }
        if (type.equals("globalSearch")){
            userRights.setGlobalSearch(checked);
        }
        if (type.equals("analysis")){
            userRights.setAnalysis(checked);
        }
        if (type.equals("warning")){
            userRights.setWarning(checked);
        }
        if (type.equals("briefing")){
            userRights.setBriefing(checked);
        }
        userRightsDao.UpdateUserRights(userRights.getUsername(),userRights.getDataScreen(),userRights.getSchemeConfiguration(),
                userRights.getGlobalSearch(),userRights.getAnalysis(),userRights.getWarning(),userRights.getBriefing(),
                userRights.getUserRole(),userRights.getSensitiveWords());
        return ret;
    }

    @Override
    public JSONObject changeUserEventLimiter(String username,String eventLimiter)
    {
        JSONObject ret=new JSONObject();
        ret.put("changeUserEventLimiter",1);
        User user = userDao.findByUsername(username);
        user.setEventLimiter(eventLimiter);
        userDao.UpdateUserByUsername(user.getUsername(),user.getPassword(),user.getPhone(),user.getEmail(),user.getProjectNum(),
                user.getValidDate(),user.getRole(),user.getState(),user.getEventLimiter(),user.getSensitiveLimiter());
        return ret;
    }

    @Override
    public JSONObject changeUserSensitiveLimiter(String username,String sensitiveLimiter)
    {
        JSONObject ret=new JSONObject();
        ret.put("changeUserSensitiveLimiter",1);
        User user = userDao.findByUsername(username);
        user.setSensitiveLimiter(sensitiveLimiter);
        userDao.UpdateUserByUsername(user.getUsername(),user.getPassword(),user.getPhone(),user.getEmail(),user.getProjectNum(),
                user.getValidDate(),user.getRole(),user.getState(),user.getEventLimiter(),user.getSensitiveLimiter());
        return ret;
    }
}

