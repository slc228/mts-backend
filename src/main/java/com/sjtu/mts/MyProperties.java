package com.sjtu.mts;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "my")
public class MyProperties {
    private final ElasticSearch elasticSearch = new ElasticSearch();

    public ElasticSearch getElasticSearch() {
        return elasticSearch;
    }

    public static class ElasticSearch {
        private String host;
        private String username;
        private String password;

        public String getHost(){
            return host;
        }
        public void setHost(String host){
            this.host = host;
        }

        public String getUsername(){
            return username;
        }
        public void setUsername(String username){
            this.username = username;
        }

        public String getPassword(){
            return password;
        }
        public void setPassword(String password){
            this.password = password;
        }
    }
}
