//package com.sjtu.mts.rpc;
//
//import feign.form.spring.SpringFormEncoder;
//import org.springframework.beans.factory.ObjectFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
//import org.springframework.cloud.openfeign.support.SpringEncoder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import feign.Logger;
//import feign.codec.Encoder;
//
//@Configuration
//public class FeignConfigure {
//    @Bean
//    Logger.Level feignLoggerLevel() {
//        return Logger.Level.FULL;
//    }
//
//    @Autowired
//    private ObjectFactory<HttpMessageConverters> messageConverters;
//
//    @Bean
//    public Encoder feignFormEncoder() {
//        return new SpringFormEncoder(new SpringEncoder(messageConverters));
//    }
//}
