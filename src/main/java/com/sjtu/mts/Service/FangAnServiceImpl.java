package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.FangAn;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FangAnServiceImpl implements FangAnService {

    @Autowired
    private FangAnDao fangAnDao;

    @Override
    public List<FangAn> findAllByUsername(String username){

        return  fangAnDao.findAllByUsername(username);
    }

    @Override
    public JSONObject saveFangAn(String username, String fangAnname,String guanjianci,String laiyuan,String diyu){
        JSONObject result = new JSONObject();
        result.put("saveFangAn", 0);
        try {
            FangAn fangAn1 = new FangAn(username,fangAnname,guanjianci,laiyuan,diyu);
            fangAnDao.save(fangAn1);
            result.put("saveFangAn", 1);
            return result;
        }catch (Exception e){
            result.put("saveFangAn", 0);
        }
        return result;
    }

    @Override
    public List<FangAn> findByFangAnName(String fangAnName){
        return fangAnDao.findByFangAnName(fangAnName);
    }


}
