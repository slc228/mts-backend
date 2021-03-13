package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.FangAn;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FangAnServiceImpl implements FangAnService {

    @Autowired
    private FangAnDao fangAnDao;

    @Override
    public JSONArray findAllByUsername(String username){

        JSONArray jsonArray = new JSONArray();
        List<FangAn> fangAnList =   fangAnDao.findAllByUsername(username);
        for(FangAn fangAn : fangAnList){
            JSONObject object = new JSONObject();
            object.put("username", fangAn.getUsername());
            object.put("programmeName", fangAn.getProgrammeName());
            object.put("matchType", fangAn.getMatchType());
            object.put("regionKeyword", fangAn.getRegionKeyword());
            object.put("regionKeywordMatch", fangAn.getRegionKeywordMatch());
            object.put("roleKeyword", fangAn.getRoleKeyword());
            object.put("roleKeywordMatch", fangAn.getRoleKeywordMatch());
            object.put("eventKeyword", fangAn.getEventKeyword());
            object.put("eventKeywordMatch", fangAn.getEventKeywordMatch());
            object.put("enableAlert", fangAn.getEnableAlert());
            jsonArray.appendElement(object);
        }
        return  jsonArray;

    }

    @Override
    public JSONObject saveFangAn(String username,
                                 String programmeName,
                                 int matchType,
                                 String regionKeyword,
                                 int regionKeywordMatch,
                                 String roleKeyword,
                                 int roleKeywordMatch,
                                 String eventKeyword,
                                 int eventKeywordMatch,
                                 boolean enableAlert){
        JSONObject result = new JSONObject();
        result.put("saveFangAn", 0);
        Boolean ifExist = fangAnDao.existsByUsernameAndProgrammeName(username,programmeName);
        if(ifExist){
            result.put("saveFangAn", 0);
            result.put("方案名重复", 1);
            return result;
        }
        try {
            FangAn fangAn1 = new FangAn(username,programmeName,matchType,regionKeyword,regionKeywordMatch,roleKeyword,roleKeywordMatch,eventKeyword,eventKeywordMatch,enableAlert);
            fangAnDao.save(fangAn1);
            result.put("saveFangAn", 1);
            return result;
        }catch (Exception e){
            result.put("saveFangAn", 0);
        }
        return result;
    }
    @Override
    public JSONObject changeFangAn(long fid,
                                   String username,
                                   String programmeName,
                                   int matchType,
                                   String regionKeyword,
                                   int regionKeywordMatch,
                                   String roleKeyword,
                                   int roleKeywordMatch,
                                   String eventKeyword,
                                   int eventKeywordMatch,
                                   boolean enableAlert
    ){
        JSONObject result = new JSONObject();
        result.put("changeFangAn", 0);
        Boolean ifExist = fangAnDao.existsByUsernameAndProgrammeName(username,programmeName);
        if(ifExist){
            result.put("changeFangAn", 0);
            result.put("方案名重复", 1);
            return result;
        }
        try {
            FangAn oldFangAn = fangAnDao.findByFid(fid);
            if(!oldFangAn.getUsername().equals(username)){
                result.put("该方案不是你的",1);
                result.put("changeFangAn", 0);
                return result;
            }
            oldFangAn.setUsername(username);
            oldFangAn.setProgrammeName(programmeName);
            oldFangAn.setRegionKeyword(regionKeyword);
            oldFangAn.setRegionKeywordMatch(regionKeywordMatch);
            oldFangAn.setRoleKeyword(roleKeyword);
            oldFangAn.setRoleKeywordMatch(roleKeywordMatch);
            oldFangAn.setEventKeyword(eventKeyword);
            oldFangAn.setEventKeywordMatch(eventKeywordMatch);
            oldFangAn.setEnableAlert(enableAlert);
            fangAnDao.save(oldFangAn);
            result.put("changeFangAn", 1);
            return result;
        }catch (Exception e){
            result.put("changeFangAn", 0);
        }
        return result;
    }
    @Override
    public JSONObject delFangAn(String username,long fid){
        JSONObject result = new JSONObject();
        result.put("delFangAn", 0);

        try {
            FangAn fangAn = fangAnDao.findByFid(fid);
            if(!fangAn.getUsername().equals(username)){
                result.put("该方案不是你的",1);
                result.put("delFangAn", 0);
                return result;
            }
            fangAnDao.deleteByFid(fid);
            result.put("delFangAn", 1);
            return result;
        }catch (Exception e){
            result.put("delFangAn", 0);
        }
        return result;
    }

    @Override
    public JSONObject findFangAnByFid(String username,long fid){
        JSONObject result = new JSONObject();
        FangAn fangAn = fangAnDao.findByFid(fid);
        if(!fangAn.getUsername().equals(username)){
            result.put("该方案不是你的",1);
            result.put("findFangAn", 0);
            return result;
        }else {
            result.put("username", fangAn.getUsername());
            result.put("programmeName", fangAn.getProgrammeName());
            result.put("matchType", fangAn.getMatchType());
            result.put("regionKeyword", fangAn.getRegionKeyword());
            result.put("regionKeywordMatch", fangAn.getRegionKeywordMatch());
            result.put("roleKeyword", fangAn.getRoleKeyword());
            result.put("roleKeywordMatch", fangAn.getRoleKeywordMatch());
            result.put("eventKeyword", fangAn.getEventKeyword());
            result.put("eventKeywordMatch", fangAn.getEventKeywordMatch());
            result.put("enableAlert", fangAn.getEnableAlert());
            return result;
        }

    }



}
