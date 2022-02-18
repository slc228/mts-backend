package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.FangAnWarning;
import net.minidev.json.JSONObject;
import org.springframework.web.bind.annotation.RequestParam;

public interface FangAnWarningService {
    FangAnWarning getFangAnWarning(long fid);
    JSONObject modifyFangAnWarning(long fid,int warningSwitch,String words,int sensitiveAttribute,int similarArticle,
                                   String area, int sourceSite,int result,int involve,int matchingWay,int weiboType,
                                   int deWeight,int filtrate,String informationType,int warningType);
}
