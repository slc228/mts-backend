package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.Entity.YuQing;
import com.sjtu.mts.Entity.YuQingElasticSearch;
import com.sjtu.mts.Expression.ElasticSearchExpression;
import com.sjtu.mts.Query.ElasticSearchQuery;
import com.sjtu.mts.rpc.HBaseHandleRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ElasticSearchDaoImpl implements ElasticSearchDao {

    @Autowired
    private HBaseHandleRpc hBaseHandleRpc;

    private final ElasticsearchOperations elasticsearchOperations;

    public ElasticSearchDaoImpl(ElasticsearchOperations elasticsearchOperations)
    {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public List<YuQing> findByExpression(ElasticSearchExpression expression) {
        List<YuQing> result = new ArrayList<>();
        SearchHits<YuQingElasticSearch> searchHits = this.elasticsearchOperations.search(
            expression.GetQuery(),
            YuQingElasticSearch.class
        );

        for (SearchHit<YuQingElasticSearch> hit : searchHits.getSearchHits())
        {
            String yuQingUrl = hit.getContent().getYuqing_url();
            YuQing yuqing = hBaseHandleRpc.GetYuqing(yuQingUrl);
            result.add(yuqing);
        }
        return result;
    }

    @Override
    public List<YuQing> findByQuery(ElasticSearchQuery query) {
        List<YuQing> result = new ArrayList<>();
        SearchHits<YuQingElasticSearch> searchHits = this.elasticsearchOperations.search(
                query.GetQuery(),
                YuQingElasticSearch.class
        );

        for (SearchHit<YuQingElasticSearch> hit : searchHits.getSearchHits())
        {
            String yuQingUrl = hit.getContent().getYuqing_url();
            System.out.println(yuQingUrl);
            YuQing yuqing = hBaseHandleRpc.GetYuqing(yuQingUrl);
            System.out.println(yuqing);
            result.add(yuqing);
        }
        return result;
    }

}
