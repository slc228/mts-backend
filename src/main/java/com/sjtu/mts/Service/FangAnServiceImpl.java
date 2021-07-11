package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.FangAn;
import com.sjtu.mts.Repository.SwordFidRepository;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FangAnServiceImpl implements FangAnService {

    @Autowired
    private FangAnDao fangAnDao;
    @Autowired
    private SwordFidRepository swordFidRepository;

    @Override
    public JSONObject findAllByUsername(String username){

        JSONArray jsonArray = new JSONArray();
        List<FangAn> fangAnList =   fangAnDao.findAllByUsername(username);
        for(FangAn fangAn : fangAnList){
            JSONObject object = new JSONObject();
            object.put("fid", fangAn.getFid());
            object.put("username", fangAn.getUsername());
            object.put("programmeName", fangAn.getProgrammeName());
            object.put("matchType", fangAn.getMatchType());
            object.put("regionKeyword", fangAn.getRegionKeyword());
            object.put("regionKeywordMatch", fangAn.getRegionKeywordMatch());
            object.put("roleKeyword", fangAn.getRoleKeyword());
            object.put("roleKeywordMatch", fangAn.getRoleKeywordMatch());
            //object.put("eventKeyword", fangAn.getEventKeyword());
            object.put("eventKeywordMatch", fangAn.getEventKeywordMatch());
            object.put("enableAlert", fangAn.getEnableAlert());
            object.put("sensitiveWord", fangAn.getSensitiveword());
            JSONArray eventKeyword = new JSONArray();
            String event = fangAn.getEventKeyword();
            while(event.length()>0)
            {
                int tag=event.indexOf('+');
                eventKeyword.appendElement(event.substring(0,tag));
                event=event.substring(tag+1);
            }
            object.put("eventKeyword", eventKeyword);
            jsonArray.appendElement(object);
        }
        JSONObject object = new JSONObject();
        object.put("data",jsonArray);
        return  object;

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
                                 boolean enableAlert,
                                 String sensitiveWord){
        JSONObject result = new JSONObject();
        result.put("saveFangAn", 0);
        Boolean ifExist = fangAnDao.existsByUsernameAndProgrammeName(username,programmeName);
        if(ifExist){
            result.put("saveFangAn", 0);
            result.put("方案名重复", 1);
            return result;
        }
        try {
            FangAn fangAn1 = new FangAn(username,programmeName,matchType,regionKeyword,regionKeywordMatch,roleKeyword,roleKeywordMatch,eventKeyword,eventKeywordMatch,enableAlert,sensitiveWord);
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
                                   boolean enableAlert,
                                   String sensitiveWord
    ){
        JSONObject result = new JSONObject();
        result.put("changeFangAn", 0);
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
            oldFangAn.setSensitiveword(sensitiveWord);
            //fangAnDao.deleteByFid(fid);
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
            swordFidRepository.deleteByFid(fid);
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
            //result.put("eventKeyword", fangAn.getEventKeyword());
            result.put("eventKeywordMatch", fangAn.getEventKeywordMatch());
            result.put("enableAlert", fangAn.getEnableAlert());
            result.put("sensitiveWord", fangAn.getSensitiveword());
            JSONArray eventKeyword = new JSONArray();
            String event = fangAn.getEventKeyword();
            while(event.length()>0)
            {
                int tag=event.indexOf('+');
                eventKeyword.appendElement(event.substring(0,tag));
                event=event.substring(tag+1);
            }
            result.put("eventKeyword", eventKeyword);
            return result;
        }

    }



}
