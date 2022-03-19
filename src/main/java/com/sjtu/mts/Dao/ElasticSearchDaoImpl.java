package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.YuQing;
import com.sjtu.mts.Entity.YuQingElasticSearch;
import com.sjtu.mts.Expression.ElasticSearchExpression;
import com.sjtu.mts.Query.ElasticSearchQuery;
import com.sjtu.mts.Response.YuQingResponse;
import com.sjtu.mts.rpc.HBaseHandleRpc;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
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
    public YuQingResponse findByQuery(ElasticSearchQuery query) {
        YuQingResponse response = new YuQingResponse();
        List<YuQing> result = new ArrayList<>();
        NativeSearchQuery query1= query.GetQuery();
        query1.setTrackTotalHits(true);
        query1.setMaxResults(20000);
        SearchHits<YuQingElasticSearch> searchHits = this.elasticsearchOperations.search(
                query1,
                YuQingElasticSearch.class
        );
        long hitNumber=searchHits.getTotalHits();
        System.out.println("hitNumber");
        System.out.println(hitNumber);
        System.out.println(searchHits.getSearchHits().size());
        response.setHitNumber(hitNumber);
        for (SearchHit<YuQingElasticSearch> hit : searchHits.getSearchHits())
        {
            String yuQingUrl = hit.getContent().getYuqing_url();
            YuQing yuqing = hBaseHandleRpc.GetYuqing(yuQingUrl);
            result.add(yuqing);
        }

        response.setYuQingContent(result);
        return response;
    }

    @Override
    public List<YuQingElasticSearch> findESByQuery(ElasticSearchQuery query) {
        List<YuQingElasticSearch> ret = new ArrayList<>();
        SearchHits<YuQingElasticSearch> searchHits = this.elasticsearchOperations.search(
                query.GetQuery(),
                YuQingElasticSearch.class
        );
        for (SearchHit<YuQingElasticSearch> hit : searchHits.getSearchHits())
        {
            ret.add(hit.getContent());
        }
        return ret;
    }

    @Override
    public Long countByQuery(ElasticSearchQuery query) {
        Long ret = this.elasticsearchOperations.count(
                query.GetQuery(),
                YuQingElasticSearch.class
        );

        return ret;
    }

    @Override
    public JSONArray aggregateByResource(ElasticSearchQuery query)
    {
        NativeSearchQuery nativeSearchQuery=query.GetQuery();
        nativeSearchQuery.setMaxResults(0);
        Aggregations aggregations=this.elasticsearchOperations.search(nativeSearchQuery,YuQingElasticSearch.class).getAggregations();
        Terms aggregation = aggregations.get("resource_count");

        JSONArray ret=new JSONArray();
        for (Terms.Bucket bucket : aggregation.getBuckets()) {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("name",bucket.getKey());
            jsonObject.put("label",bucket.getKey());
            jsonObject.put("value",bucket.getDocCount());
            ret.appendElement(jsonObject);
        }

        return ret;
    }

    @Override
    public List<Long> aggregateBySensitiveType(ElasticSearchQuery query)
    {
        List<Long> resultList = new ArrayList<>();
        for (int i = 0; i <= 1 ; i++) {
            resultList.add((long)0);
        }

        NativeSearchQuery nativeSearchQuery=query.GetQuery();
        nativeSearchQuery.setMaxResults(0);
        Aggregations aggregations=this.elasticsearchOperations.search(nativeSearchQuery,YuQingElasticSearch.class).getAggregations();
        Terms aggregation = aggregations.get("sensitiveType_count");

        for (Terms.Bucket bucket : aggregation.getBuckets()) {
            if (bucket.getKeyAsString().startsWith("政治敏感"))
            {
                resultList.set(1,bucket.getDocCount()+resultList.get(1));
            }
            else
            {
                resultList.set(0,bucket.getDocCount()+resultList.get(0));
            }
        }

        return resultList;
    }

    @Override
    public List<Long> aggregateSixTypeBySensitiveType(ElasticSearchQuery query) {
        List<Long> resultList = new ArrayList<>();
        for (int i = 0; i <= 4 ; i++) {
            resultList.add((long)0);
        }

        NativeSearchQuery nativeSearchQuery=query.GetQuery();
        nativeSearchQuery.setMaxResults(0);
        Aggregations aggregations=this.elasticsearchOperations.search(nativeSearchQuery,YuQingElasticSearch.class).getAggregations();
        Terms aggregation = aggregations.get("sensitiveType_count");

        for (Terms.Bucket bucket : aggregation.getBuckets()) {
            if (bucket.getKeyAsString().startsWith("正常信息"))
            {
                resultList.set(0,bucket.getDocCount());
            }
            if (bucket.getKeyAsString().startsWith("政治敏感"))
            {
                resultList.set(1,bucket.getDocCount());
            }
            if (bucket.getKeyAsString().startsWith("低俗信息"))
            {
                resultList.set(2,bucket.getDocCount());
            }
            if (bucket.getKeyAsString().startsWith("广告营销"))
            {
                resultList.set(3,bucket.getDocCount());
            }
            if (bucket.getKeyAsString().startsWith("人身攻击"))
            {
                resultList.set(4,bucket.getDocCount());
            }
        }

        return resultList;
    }

    @Override
    public Terms getAggregateByResource(ElasticSearchQuery query)
    {
        NativeSearchQuery nativeSearchQuery=query.GetQuery();
        nativeSearchQuery.setMaxResults(0);
        Aggregations aggregations=this.elasticsearchOperations.search(nativeSearchQuery,YuQingElasticSearch.class).getAggregations();
        Terms aggregation = aggregations.get("resource_count");

        return aggregation;
    }

}
