package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.MonitorUrlDao;
import com.sjtu.mts.Entity.MonitorUrl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitorUrlServiceImpl implements MonitorUrlService{

    @Autowired // This means to get the bean called userRepository
    private MonitorUrlDao monitorUrlDao;

    @Override
    public JSONObject saveUrl(String name, String url, String create_by, String create_date, String update_by, String update_date, String remarks, char del_flag){
        JSONObject result = new JSONObject();
        result.put("saveUrl", 0);
        try {
            MonitorUrl monitorUrl = new MonitorUrl(name,url,create_by,create_date,update_by,update_date,remarks,del_flag);
            monitorUrlDao.save(monitorUrl);
            result.put("saveUrl", 1);
        }catch (Exception e) {
            result.put("saveUrl", 0);
        }
        return result;
    }
    @Override
    public JSONArray allUrl(){
        JSONArray jsonArray = new JSONArray();
        List<MonitorUrl> monitorUrlList = monitorUrlDao.findAll();
        for(MonitorUrl monitorUrl : monitorUrlList){
            JSONObject object = new JSONObject();
            object.put("id",monitorUrl.getId());
            object.put("name", monitorUrl.getName());
            object.put("url", monitorUrl.getUrl());
            object.put("create_by", monitorUrl.getCreate_by());
            object.put("create_date", monitorUrl.getCreate_date());
            object.put("update_by", monitorUrl.getUpdate_by());
            object.put("update_date", monitorUrl.getUpdate_date());
            object.put("remarks", monitorUrl.getRemarks());
            object.put("del_flag", monitorUrl.getDel_flag());
            jsonArray.appendElement(object);
        }
        return jsonArray;
    }
    @Override
    public JSONObject delUrl(long id){
        JSONObject result = new JSONObject();
        result.put("delUrl", 0);
        try {
            monitorUrlDao.deleteById(id);
            result.put("delUrl", 1);
        }catch (Exception e) {
            result.put("delUrl", 0);
        }
        return result;
    }

}
