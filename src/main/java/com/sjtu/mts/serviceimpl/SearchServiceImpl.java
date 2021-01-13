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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchServiceImpl(ElasticsearchOperations elasticsearchOperations)
    {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public List<Data> Search(String keyword, String cflag, String startPublishedDay, String endPublishedDay, String fromType)
    {
        Criteria criteria = new Criteria();
        if (!keyword.isEmpty())
        {
            criteria.subCriteria(new Criteria().and("content").contains(keyword).or("title").contains(keyword));
        }
        if (!cflag.isEmpty())
        {
            criteria.subCriteria(new Criteria().and("cflag").is(cflag));
        }
        if (!startPublishedDay.isEmpty() && !endPublishedDay.isEmpty())
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date startDate = sdf.parse(startPublishedDay);
                Date endDate = sdf.parse(endPublishedDay);
                criteria.subCriteria(new Criteria().and("publishedDay").between(startDate, endDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (!fromType.isEmpty())
        {
            criteria.subCriteria(new Criteria().and("fromType").is(fromType));
        }
        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);
        System.out.println(this.elasticsearchOperations.count(query, Data.class));

        List<Data> result = new ArrayList<>();
        for (SearchHit<Data> hit : searchHits)
        {
            result.add(hit.getContent());
        }

        return result;
    }
}
