package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.FangAn;
import com.sjtu.mts.Response.FangAnResponse;
import net.minidev.json.JSONObject;

import java.util.List;

public interface FangAnService {
    JSONObject findAllByUsername(String username);
    JSONObject saveFangAn(String username,
                          String programmeName,
                          int matchType,
                          String regionKeyword,
                          int regionKeywordMatch,
                          String roleKeyword,
                          int roleKeywordMatch,
                          String eventKeyword,
                          int eventKeywordMatch,
                          boolean enableAlert,
                          String sensitiveWord,
                          Integer priority
                          );
    JSONObject changeFangAn(long fid,
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
                          String sensitiveWord,
                          Integer priority
    );
    JSONObject delFangAn(String username,long fid);

    JSONObject findFangAnByFid(String username,long fid);

    JSONObject getAllFid();

    FangAnResponse getAllFangan(int pageID, int pageSize, String username);

}
