package com.sjtu.mts.service;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.Entity.Data;

import java.util.List;

public interface SearchService {

    public List<Data> Search(JSONObject jsonObject);
}
