package com.sjtu.mts.Controller;

import com.sjtu.mts.Service.FangAnService;
import com.sjtu.mts.Service.MonitorUrlService;
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
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private FangAnService fangAnService;
    @Autowired
    private MonitorUrlService monitorUrlService;


    @GetMapping(path="/allUsers")
    @ResponseBody
    public  JSONArray getAllUsers(HttpServletRequest request) {
        // This returns a JSON or XML with the users
        HttpSession session = request.getSession();
        String matter = (String) session.getAttribute("role");
        /* if("0".equals(matter)){
            return userService.getAllUsers();
        }else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("login", 0);
            JSONArray jsonArray = new JSONArray();
            jsonArray.appendElement(jsonObject);
            return jsonArray;
        } */
        return userService.getAllUsers();
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
    public JSONObject register(@RequestBody Map<String,String> registerinfo ) {
        return userService.registerUser(registerinfo.get("username"), registerinfo.get("password"), registerinfo.get("phone"), registerinfo.get("email"));
    }

    @PostMapping(path = "/registerManager")
    @ResponseBody
    public JSONObject registerManager(@RequestBody Map<String,String> registerinfo ) {
        return userService.registerManager(registerinfo.get("username"), registerinfo.get("password"), registerinfo.get("phone"), registerinfo.get("email"));
    }

    @RequestMapping(path = "/login")
    @ResponseBody
    public JSONObject login(HttpServletRequest request, @RequestBody Map<String,String> userinfo) {
        System.out.println("test login");
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
    @RequestMapping(path = "/changeUserState")
    @ResponseBody
    public JSONObject changeUserStatus(@RequestParam("username")String username){
        return userService.changeUserState(username);
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
        String event = fangAnInfo.get("eventKeyword");
        String eventKeyword="";
        event=event.substring(1,event.length()-1);
        event=event+",";
        while(event.length()>0)
        {
            int tag=event.indexOf(',');
            eventKeyword=eventKeyword+event.substring(1,tag-1)+"+";
            event=event.substring(tag+1);
        }
        return fangAnService.saveFangAn(
                fangAnInfo.get("username"),
                fangAnInfo.get("programmeName"),
                Integer.parseInt(fangAnInfo.get("matchType")),
                fangAnInfo.get("regionKeyword"),
                Integer.parseInt(fangAnInfo.get("regionKeywordMatch")),
                fangAnInfo.get("roleKeyword"),
                Integer.parseInt(fangAnInfo.get("roleKeywordMatch")),
                eventKeyword,
                Integer.parseInt(fangAnInfo.get("eventKeywordMatch")),
                Boolean.parseBoolean(fangAnInfo.get("enableAlert")),
                fangAnInfo.get("sensitiveWord")
                );
    }
    @PostMapping(path = "/changeFangAn")
    @ResponseBody
    public JSONObject changeFangAn(@RequestBody Map<String,String> fangAnInfo ) {
        String event = fangAnInfo.get("eventKeyword");
        String eventKeyword="";
        event=event.substring(1,event.length()-1);
        if (event.length()!=0)
        {
            event=event+",";
            while(event.length()>0)
            {
                int tag=event.indexOf(',');
                eventKeyword=eventKeyword+event.substring(1,tag-1)+"+";
                event=event.substring(tag+1);
            }
        }

        return fangAnService.changeFangAn(
                Long.parseLong(fangAnInfo.get("fid")),
                fangAnInfo.get("username"),
                fangAnInfo.get("programmeName"),
                Integer.parseInt(fangAnInfo.get("matchType")),
                fangAnInfo.get("regionKeyword"),
                Integer.parseInt(fangAnInfo.get("regionKeywordMatch")),
                fangAnInfo.get("roleKeyword"),
                Integer.parseInt(fangAnInfo.get("roleKeywordMatch")),
                eventKeyword,
                Integer.parseInt(fangAnInfo.get("eventKeywordMatch")),
                Boolean.parseBoolean(fangAnInfo.get("enableAlert")),
                fangAnInfo.get("sensitiveWord")
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
    public JSONObject findFangAnByusername(@RequestParam String username){
        return fangAnService.findAllByUsername(username);
    }
    @GetMapping(path = "/findFangAnByFid")
    @ResponseBody
    public JSONObject findFangAnByFid(@RequestParam("username") String username,
                                @RequestParam("fid") long fid

    ) {
        return fangAnService.findFangAnByFid(username,fid);
    }
    @GetMapping(path = "/findAllUrl")
    @ResponseBody
    public JSONArray findAllUrl() {
        return monitorUrlService.allUrl();
    }
    @GetMapping(path = "/delUrl")
    @ResponseBody
    public JSONObject dellUrl(@RequestParam("id") long id) {
        return monitorUrlService.delUrl(id);
    }
    @PostMapping(path = "/addUrl")
    @ResponseBody
    public JSONObject addUrl(@RequestBody Map<String,String> urlInfo){
        return monitorUrlService.saveUrl(
                urlInfo.get("name"),
                urlInfo.get("url"),
                urlInfo.get("create_by"),
                urlInfo.get("create_date"),
                urlInfo.get("update_by"),
                urlInfo.get("update_date"),
                urlInfo.get("remarks"),
                urlInfo.get("del_flag").charAt(0)
        );
    }

}

