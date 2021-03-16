package com.sjtu.mts.Controller;

import com.sjtu.mts.Service.FangAnService;
import com.sjtu.mts.Service.UserService;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RequestMapping(path="/User")
@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private FangAnService fangAnService;

    @GetMapping(path="/allUsers")
    @ResponseBody
    public  JSONArray getAllUsers(HttpServletRequest request) {
        // This returns a JSON or XML with the users
        HttpSession session = request.getSession();
        String matter = (String) session.getAttribute("role");
        if("0".equals(matter)){
            return userService.getAllUsers();
        }else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("login", 0);
            JSONArray jsonArray = new JSONArray();
            jsonArray.appendElement(jsonObject);
            return jsonArray;
        }

    }
    @GetMapping(path="/allManagers")
    @ResponseBody
    public  JSONArray getAllManager(HttpServletRequest request) {
        // This returns a JSON or XML with the users
        HttpSession session = request.getSession();
        String matter = (String) session.getAttribute("role");
        if("0".equals(matter)){
            return userService.getAllManager();
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("login", 0);
            JSONArray jsonArray = new JSONArray();
            jsonArray.appendElement(jsonObject);
            return jsonArray;
        }
    }
    @PostMapping(path = "/register")
    @ResponseBody
    public JSONObject register(@RequestParam String username, @RequestParam String password, @RequestParam String phone,
                                @RequestParam String email) {
        return userService.registerUser(username, password, phone, email);
    }

    @PostMapping(path = "/registerManager")
    @ResponseBody
    public JSONObject registerManager(@RequestParam String username, @RequestParam String password, @RequestParam String phone,
                                      @RequestParam String email) {
        return userService.registerManager(username, password, phone, email);
    }

    @RequestMapping(path = "/login")
    @ResponseBody
    public JSONObject login(HttpServletRequest request, @RequestBody Map<String,String> userinfo) {
        JSONObject result = ("0".equals(userinfo.get("role"))) ? userService.loginManager(userinfo.get("username"), userinfo.get("password")) : userService.login(userinfo.get("username"), userinfo.get("password"),"1");
        if ("1".equals(result.getAsString("login"))) {
            HttpSession session = request.getSession();
            //System.out.println("此时的sessionid为：");
            System.out.println(session.getId());
            //System.out.println("此时的session attributions为：");
            //System.out.println(session.getAttributeNames());
            //System.out.println(session.isNew());
            String name = (String) session.getAttribute("username");
            if (StringUtils.isEmpty(name)) {
                session.setAttribute("username", userinfo.get("username"));
                if ("0".equals(result.getAsString("role"))) {
                    session.setAttribute("role", "0");
                } else {
                    session.setAttribute("role", "1");
                }
            } else if (!(name.equals(userinfo.get("username")))) {
                JSONObject err = new JSONObject();
                err.put("login", -1);
                return err;
            }
        }
        return result;
    }

    @RequestMapping(path = "/logout")
    @ResponseBody
    public JSONObject logout(HttpServletRequest request) {
        JSONObject result = new JSONObject();
        HttpSession session = request.getSession();
        String name = (String) session.getAttribute("username");
        if (StringUtils.isEmpty(name)) {
            result.put("logout", 0);
        } else {
            session.removeAttribute("username");
            session.removeAttribute("role");
            result.put("logout", 1);
        }
        return result;
    }

    @PostMapping(path = "/saveFangAn")
    @ResponseBody
    public JSONObject saveFangAn(@RequestBody Map<String,String> fangAnInfo ) {

        return fangAnService.saveFangAn(
                fangAnInfo.get("username"),
                fangAnInfo.get("programmeName"),
                Integer.parseInt(fangAnInfo.get("matchType")),
                fangAnInfo.get("regionKeyword"),
                Integer.parseInt(fangAnInfo.get("regionKeywordMatch")),
                fangAnInfo.get("roleKeyword"),
                Integer.parseInt(fangAnInfo.get("roleKeywordMatch")),
                fangAnInfo.get("eventKeyword"),
                Integer.parseInt(fangAnInfo.get("eventKeywordMatch")),
                Boolean.parseBoolean(fangAnInfo.get("enableAlert"))
                );
    }
    @PostMapping(path = "/changeFangAn")
    @ResponseBody
    public JSONObject changeFangAn(@RequestBody Map<String,String> fangAnInfo ) {
        return fangAnService.changeFangAn(
                Long.parseLong(fangAnInfo.get("fid")),
                fangAnInfo.get("username"),
                fangAnInfo.get("programmeName"),
                Integer.parseInt(fangAnInfo.get("matchType")),
                fangAnInfo.get("regionKeyword"),
                Integer.parseInt(fangAnInfo.get("regionKeywordMatch")),
                fangAnInfo.get("roleKeyword"),
                Integer.parseInt(fangAnInfo.get("roleKeywordMatch")),
                fangAnInfo.get("eventKeyword"),
                Integer.parseInt(fangAnInfo.get("eventKeywordMatch")),
                Boolean.parseBoolean(fangAnInfo.get("enableAlert"))
        );
    }
    @GetMapping(path = "/delFangAn")
    @ResponseBody
    public JSONObject delFangAn(@RequestParam("username") String username,
                                 @RequestParam("fid") long fid

                                ) {
        return fangAnService.delFangAn(username,fid);
    }
    @GetMapping(path = "/findFangAn")
    @ResponseBody
    public JSONArray findFangAnByusername(@RequestParam String username){
        return fangAnService.findAllByUsername(username);
    }
    @GetMapping(path = "/findFangAnByFid")
    @ResponseBody
    public JSONObject findFangAnByFid(@RequestParam("username") String username,
                                @RequestParam("fid") long fid

    ) {
        return fangAnService.findFangAnByFid(username,fid);
    }


}

