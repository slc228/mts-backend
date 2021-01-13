package com.sjtu.mts.service;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.Entity.Data;

import java.util.Date;
import java.util.List;

public interface SearchService {

    public List<Data> Search(String keyword, String cflag, String startPublishedDay, String endPublishedDay, String fromType);
}
