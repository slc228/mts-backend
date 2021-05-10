package com.sjtu.mts.Service;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;


public interface MonitorUrlService {

    JSONObject saveUrl(String name,String url,String create_by,String create_date,String update_by,String update_date,String remarks,char del_flag);
    JSONArray allUrl();
    JSONObject delUrl(long id);
}
