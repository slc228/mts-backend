package com.sjtu.mts.rpc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.Entity.BriefWeiboUser;
import io.netty.handler.codec.json.JsonObjectDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeiboSpiderRpc {

    @Autowired
    RestTemplate restTemplate;

    public String crawlNewUserids(List<String> weibo_userids){
        JSONArray json = new JSONArray();
        for (String userid : weibo_userids){
            json.add(userid);
            //System.out.println(file);
        }
        HttpHeaders headers = new HttpHeaders();
        //定义请求参数类型，这里用json所以是MediaType.APPLICATION_JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        //RestTemplate带参传的时候要用HttpEntity<?>对象传递
        Map<String, Object> requestParam = new HashMap<String, Object>();
        requestParam.put("weibo_userids", json);
        HttpEntity entity = new HttpEntity(requestParam, headers);
//        System.out.println(entity);
        return  restTemplate.postForObject("http://weibo-spider-service/new_userid",entity,String.class);
    }

    public void searchBriefWeiboUser(String WeiboUserForSearch){
        HttpHeaders headers = new HttpHeaders();
        //定义请求参数类型，这里用json所以是MediaType.APPLICATION_JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        //RestTemplate带参传的时候要用HttpEntity<?>对象传递
        Map<String, Object> requestParam = new HashMap<String, Object>();
        requestParam.put("WeiboUserForSearch", WeiboUserForSearch);
        HttpEntity entity = new HttpEntity(requestParam, headers);
        String response = restTemplate.postForObject("http://weibo-spider-service/BriefWeiboUser",entity, String.class);
    }

//    public List<BriefWeiboUser> searchBriefWeiboUser(String WeiboUserForSearch){
//        HttpHeaders headers = new HttpHeaders();
//        //定义请求参数类型，这里用json所以是MediaType.APPLICATION_JSON
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        //RestTemplate带参传的时候要用HttpEntity<?>对象传递
//        Map<String, Object> requestParam = new HashMap<String, Object>();
//        requestParam.put("WeiboUserForSearch", WeiboUserForSearch);
//        HttpEntity entity = new HttpEntity(requestParam, headers);
////        System.out.println(entity);
//        String response = restTemplate.postForObject("http://weibo-spider-service/BriefWeiboUser",entity, String.class);
//        JSONObject parsed = JSONObject.parseObject(response);
//        JSONArray briefUsers = parsed.getJSONArray("data");
//        List<BriefWeiboUser> result = new ArrayList<>();
//        for (int i = 0; i < briefUsers.size(); i++) {
//            result.add(new BriefWeiboUser(
//                    briefUsers.getJSONObject(i).getString("uri"),
//                    briefUsers.getJSONObject(i).getString("nickname")
//            ));
//        }
//        return result;
//    }

}
