package com.sjtu.mts.rpc;


import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service

public class TextclassRpc {

    @Autowired
    RestTemplate restTemplate;

    public String sayHello(){
        return  restTemplate.getForObject("http://python-service/test?name=zhangtaifeng",String.class);
    }

    public String textclass(List<String> fileContents){
        JSONArray json = new JSONArray();
        for (String file : fileContents){
            json.add(file);
            System.out.println(file);
        }
        return  restTemplate.getForObject("http://python-service/predict?textList={1}", String.class,json);
    }
}
