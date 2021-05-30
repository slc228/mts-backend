package com.sjtu.mts.rpc;


import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class TextAlertRpc {

    @Autowired
    RestTemplate restTemplate;

    public String textAlert(List<String> fileContents){
        JSONArray json = new JSONArray();
        for (String file : fileContents){
            json.add(file);
        }
        HttpHeaders headers = new HttpHeaders();
        //定义请求参数类型，这里用json所以是MediaType.APPLICATION_JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        //RestTemplate带参传的时候要用HttpEntity<?>对象传递
        Map<String, Object> requestParam = new HashMap<String, Object>();
        requestParam.put("textList", json);
        HttpEntity entity = new HttpEntity(requestParam, headers);
        return  restTemplate.postForObject("http://text-alert/predict",entity,String.class);
        // return  restTemplate.postForObject("http://localhost:3337/predict",entity,String.class);
    }
}
