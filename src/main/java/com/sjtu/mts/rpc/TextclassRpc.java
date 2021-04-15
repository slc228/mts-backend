package com.sjtu.mts.rpc;


import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service

public class TextclassRpc {

    @Autowired
    RestTemplate restTemplate;

    public String textclass(List<String> fileContents){
        JSONArray json = new JSONArray();
        for (String file : fileContents){
            json.add(file);
            System.out.println(file);
        }
        System.out.println(fileContents.size());
        return  restTemplate.getForObject("http://python-service/predict?textList={1}", String.class,json);
    }
    public String clustering(List<String> fileContents){
        JSONArray json = new JSONArray();
        JSONObject object = new JSONObject();
//        object.put("textList",fileContents);
//        json.add(object);
        for (String file : fileContents){
            json.add(file);
            System.out.println(file);

        }
        System.out.println(json);
        return  restTemplate.postForObject("http://clustering-service/kmeans?textList={1}",object,String.class,object);
    }
}
