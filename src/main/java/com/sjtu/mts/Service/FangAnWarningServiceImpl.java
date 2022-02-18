package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.FangAnWarningDao;
import com.sjtu.mts.Entity.FangAnWarning;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FangAnWarningServiceImpl implements FangAnWarningService {
    @Autowired
    private FangAnWarningDao fangAnWarningDao;


    @Override
    public FangAnWarning getFangAnWarning(long fid) {
        FangAnWarning ret;
        if (fangAnWarningDao.existsByFid(fid))
        {
            ret=fangAnWarningDao.findAllByFid(fid).get(0);
        }
        else{
            FangAnWarning fangAnWarning=new FangAnWarning(fid);
            fangAnWarningDao.save(fangAnWarning);
            ret=fangAnWarning;
        }
        return ret;
    }

    @Override
    public JSONObject modifyFangAnWarning(long fid, int warningSwitch, String words, int sensitiveAttribute,
                                          int similarArticle, String area, int sourceSite, int result, int involve,
                                          int matchingWay, int weiboType, int deWeight, int filtrate,
                                          String informationType, int warningType) {
        JSONObject ret=new JSONObject();
        if (fangAnWarningDao.existsByFid(fid)) {
            FangAnWarning fangAnWarning=fangAnWarningDao.findAllByFid(fid).get(0);
            fangAnWarning.setWarningSwitch(warningSwitch);
            fangAnWarning.setWords(words);
            fangAnWarning.setSensitiveAttribute(sensitiveAttribute);
            fangAnWarning.setSimilarArticle(similarArticle);
            fangAnWarning.setArea(area);
            fangAnWarning.setSourceSite(sourceSite);
            fangAnWarning.setResult(result);
            fangAnWarning.setInvolve(involve);
            fangAnWarning.setMatchingWay(matchingWay);
            fangAnWarning.setWeiboType(weiboType);
            fangAnWarning.setDeWeight(deWeight);
            fangAnWarning.setFiltrate(filtrate);
            fangAnWarning.setInformationType(informationType);
            fangAnWarning.setWarningType(warningType);
            System.out.println(warningSwitch);
            fangAnWarningDao.save(fangAnWarning);
            ret.appendField("modifyWarning",1);
        }
        else {
            ret.appendField("modifyWarning",0);
        }
        return ret;
    }
}
