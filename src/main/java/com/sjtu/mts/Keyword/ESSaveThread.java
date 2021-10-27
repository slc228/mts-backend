package com.sjtu.mts.Keyword;

import com.sjtu.mts.Dao.ElasticSearchDao;
import com.sjtu.mts.Entity.YuQingElasticSearch;
import com.sjtu.mts.Query.ElasticSearchQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ESSaveThread extends Thread {
    private ElasticSearchQuery elasticSearchQuery;
    private long newfid;
    private final ElasticsearchOperations elasticsearchOperations;
    private ElasticSearchDao elasticSearchDao;

    public ESSaveThread(ElasticSearchQuery elasticSearchQuery, long newfid, ElasticsearchOperations elasticsearchOperations,ElasticSearchDao elasticSearchDao) {
        this.elasticSearchQuery = elasticSearchQuery;
        this.newfid=newfid;
        this.elasticsearchOperations = elasticsearchOperations;
        this.elasticSearchDao=elasticSearchDao;
    }

    public void run() {
        elasticSearchQuery.JoinFidQueryBuildersWithOutFid(newfid);
        elasticSearchQuery.SetBoolQuery();
        List<YuQingElasticSearch> yuQings=elasticSearchDao.findESByQuery(elasticSearchQuery);
        System.out.println(yuQings.size());
        List<YuQingElasticSearch> newYuqings=new ArrayList<>();
        for (YuQingElasticSearch yuQing:yuQings)
        {
            yuQing.setFid((int) newfid);
            yuQing.setId(UUID.randomUUID().toString());
            newYuqings.add(yuQing);
        }
        this.elasticsearchOperations.save(newYuqings,this.elasticsearchOperations.getIndexCoordinatesFor(YuQingElasticSearch.class));
        System.out.println("hhhhhere");
    }
}