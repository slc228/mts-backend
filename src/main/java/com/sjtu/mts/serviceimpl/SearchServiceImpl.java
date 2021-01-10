package com.sjtu.mts.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.Repository.DataRepository;
import com.sjtu.mts.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import javax.management.Query;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchServiceImpl(ElasticsearchOperations elasticsearchOperations)
    {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public List<Data> Search(JSONObject jsonObject)
    {
        Criteria criteria = new Criteria();
        if (jsonObject.containsKey("cflag"))
        {
            criteria.subCriteria(new Criteria().and("cflag").is(jsonObject.getString("cflag")));
        }
        if (jsonObject.containsKey("publishedDay"))
        {
            criteria.subCriteria(new Criteria().and("publishedDay").is(jsonObject.getString("publishedDay")));
        }
        if (jsonObject.containsKey("resourse"))
        {
            criteria.subCriteria(new Criteria().and("resourse").is(jsonObject.getString("resourse")));
        }
        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);

        List<Data> result = new ArrayList<>();
        for (SearchHit<Data> hit : searchHits)
        {
            result.add(hit.getContent());
        }

        return result;
    }
}
