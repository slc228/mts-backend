package com.sjtu.mts;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

@Configuration
public class RestClientConfig extends AbstractElasticsearchConfiguration {

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {

        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
//                .connectedTo("192.168.0.19:9200")
                .connectedTo("202.120.40.69:9200")
                .withBasicAuth("elastic","tang3501")
                .build();

        return RestClients.create(clientConfiguration).rest();
    }
}
