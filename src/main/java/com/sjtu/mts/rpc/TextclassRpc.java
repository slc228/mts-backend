package com.sjtu.mts.rpc;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service

public class TextclassRpc {

    @Autowired
    RestTemplate restTemplate;

    public String sayHello(){
        return  restTemplate.getForObject("http://python-service/test?name=zhangtaifeng",String.class);
    }
}
