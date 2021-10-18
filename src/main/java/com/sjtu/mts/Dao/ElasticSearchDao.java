package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.YuQing;
import com.sjtu.mts.Expression.ElasticSearchExpression;
import com.sjtu.mts.Query.ElasticSearchQuery;
import com.sjtu.mts.Response.YuQingResponse;
import net.minidev.json.JSONArray;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.util.List;

public interface ElasticSearchDao {

    List<YuQing> findByExpression(ElasticSearchExpression expression);
    YuQingResponse findByQuery(ElasticSearchQuery query);
    public Long countByQuery(ElasticSearchQuery query);
    public JSONArray aggregateByResource(ElasticSearchQuery query);
    public List<Long> aggregateBySensitiveType(ElasticSearchQuery query);
    public Terms getAggregateByResource(ElasticSearchQuery query);
}
