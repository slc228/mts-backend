package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.YuQing;
import com.sjtu.mts.Expression.ElasticSearchExpression;
import com.sjtu.mts.Query.ElasticSearchQuery;

import java.util.List;

public interface ElasticSearchDao {

    List<YuQing> findByExpression(ElasticSearchExpression expression);
    List<YuQing> findByQuery(ElasticSearchQuery query);
}
