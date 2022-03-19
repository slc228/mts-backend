package com.sjtu.mts.Service;

import com.alibaba.fastjson.JSONObject;

public interface CloudService {

    public JSONObject getWordCloudByFid(long fid);
}
