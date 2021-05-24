package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.Cluster;
import net.minidev.json.JSONArray;

import java.util.List;

/*
* @author：HZT
* 研判预警*/
public interface TextAlertService {
    com.alibaba.fastjson.JSONObject textAlert(List<String> textList);
}
