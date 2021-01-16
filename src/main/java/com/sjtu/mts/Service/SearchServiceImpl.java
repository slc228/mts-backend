package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.Response.CflagCountResponse;
import com.sjtu.mts.Response.DataResponse;
import com.sjtu.mts.Response.ResourceCountResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

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

    public DataResponse Search(String keyword, String cflag, String startPublishedDay, String endPublishedDay,
                               String fromType, int page, int pageSize, int timeOrder)
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
        if (timeOrder == 0) {
            query.setPageable(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "publishedDay")));
        }
        else {
            query.setPageable(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "publishedDay")));
        }
        SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);
        SearchPage<Data> searchPage = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
        long hitNumber = this.elasticsearchOperations.count(query, Data.class);

        List<Data> pageDataContent = new ArrayList<>();
        for (SearchHit<Data> hit : searchPage.getSearchHits())
        {
            pageDataContent.add(hit.getContent());
        }

        DataResponse result = new DataResponse();
        result.setHitNumber(hitNumber);
        result.setDataContent(pageDataContent);

        return result;
    }

    public ResourceCountResponse globalSearchResourceCount(String keyword, String startPublishedDay, String endPublishedDay) {
        List<Long> resultList = new ArrayList<>();
        for (int fromType = 1; fromType <= 7 ; fromType++) {
            Criteria criteria = new Criteria();
            if (!keyword.isEmpty())
            {
                criteria.subCriteria(new Criteria().and("content").contains(keyword).or("title").contains(keyword));
            }
            if (!startPublishedDay.isEmpty() && !endPublishedDay.isEmpty())
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date startDate = sdf.parse(startPublishedDay);
                    Date endDate = sdf.parse(endPublishedDay);
                    criteria.subCriteria(new Criteria().and("publishedDay").between(startDate, endDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            criteria.subCriteria(new Criteria().and("fromType").is(fromType));
            CriteriaQuery query = new CriteriaQuery(criteria);
            long searchHitCount = this.elasticsearchOperations.count(query, Data.class);
            resultList.add(searchHitCount);
        }
        ResourceCountResponse result = new ResourceCountResponse(resultList.get(0), resultList.get(1), resultList.get(2),
                resultList.get(3), resultList.get(4), resultList.get(5), resultList.get(6));
        return result;
    }

    public CflagCountResponse globalSearchCflagCount(String keyword, String startPublishedDay, String endPublishedDay) {
        List<Long> resultList = new ArrayList<>();
        for (int cflag = 1; cflag <= 2 ; cflag++) {
            Criteria criteria = new Criteria();
            if (!keyword.isEmpty())
            {
                criteria.subCriteria(new Criteria().and("content").contains(keyword).or("title").contains(keyword));
            }
            if (!startPublishedDay.isEmpty() && !endPublishedDay.isEmpty())
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date startDate = sdf.parse(startPublishedDay);
                    Date endDate = sdf.parse(endPublishedDay);
                    criteria.subCriteria(new Criteria().and("publishedDay").between(startDate, endDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            criteria.subCriteria(new Criteria().and("cflag").is(cflag));
            CriteriaQuery query = new CriteriaQuery(criteria);
            long searchHitCount = this.elasticsearchOperations.count(query, Data.class);
            resultList.add(searchHitCount);
        }
        CflagCountResponse result = new CflagCountResponse(resultList.get(0), resultList.get(1));
        return result;
    }
}
