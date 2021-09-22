package com.sjtu.mts.rpc;

import com.sjtu.mts.Entity.YuQing;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class HBaseHandleRpc {

    @Autowired
    RestTemplate restTemplate;

    public YuQing GetYuqing(String yuqing_id)
    {
        HttpHeaders headers = new HttpHeaders();
        //定义请求参数类型，这里用json所以是MediaType.APPLICATION_JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        //RestTemplate带参传的时候要用HttpEntity<?>对象传递
        Map<String, Object> requestParam = new HashMap<String, Object>();
        requestParam.put("yuqing_id", yuqing_id);
        HttpEntity entity = new HttpEntity(requestParam, headers);
//        System.out.println(entity);
        String result = restTemplate.postForObject("http://hbase-scan-service/get_row",entity,String.class);
        JSONObject parsed = (JSONObject) JSONValue.parse(result);
        YuQing yuqing = new YuQing(parsed);
        return yuqing;
    }

}
