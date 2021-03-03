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
    public JSONObject saveFangAn(String username,String fangAnname,String keyword,int kisAnd,String fromType,String area,int aisAnd){
        JSONObject result = new JSONObject();
        result.put("saveFangAn", 0);
        Boolean ifExist = fangAnDao.existsByUsernameAndFangAnname(username,fangAnname);
        if(ifExist){
            result.put("saveFangAn", 0);
            result.put("方案名重复", 1);
            return result;
        }
        try {
            FangAn fangAn1 = new FangAn(username,fangAnname,keyword,kisAnd,fromType,area,aisAnd);
            fangAnDao.save(fangAn1);
            result.put("saveFangAn", 1);
            return result;
        }catch (Exception e){
            result.put("saveFangAn", 0);
        }
        return result;
    }



}
