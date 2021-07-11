package com.sjtu.mts.Service;

import net.minidev.json.JSONObject;

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
                          String sensitiveWord
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
                          String sensitiveWord
    );
    JSONObject delFangAn(String username,long fid);

    JSONObject findFangAnByFid(String username,long fid);

}
