package com.sjtu.mts.rpc;

import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeiboSpiderRpc {

    @Autowired
    RestTemplate restTemplate;

    public String multiDocumentsSummary(String weibo_userid){
        HttpHeaders headers = new HttpHeaders();
        //定义请求参数类型，这里用json所以是MediaType.APPLICATION_JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        //RestTemplate带参传的时候要用HttpEntity<?>对象传递
        Map<String, Object> requestParam = new HashMap<String, Object>();
        requestParam.put("weibo_userid", weibo_userid);
        HttpEntity entity = new HttpEntity(requestParam, headers);
//        System.out.println(entity);
        return  restTemplate.postForObject("http://weibo-spider-service/new_userid",entity,String.class);
    }

}
