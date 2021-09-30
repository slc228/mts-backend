package com.sjtu.mts.Expression;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ElasticSearchExpression {

    private final Criteria mainCriteria;
    private int page;
    private int pageSize;
    private int timeOrder;

    public ElasticSearchExpression()
    {
        mainCriteria = new Criteria();
        this.page = 0;
        this.pageSize = 20;
        this.timeOrder = -2;
    }

    public ElasticSearchExpression JoinTitleAndContentCriteria(String keywords)
    {
        String[] searchSplitArray1 = keywords.trim().split("\\s+");
        List<String> searchSplitArray = Arrays.asList(searchSplitArray1);
        return this.JoinTitleAndContentCriteria(searchSplitArray);
    }

    public ElasticSearchExpression JoinTitleAndContentCriteria(List<String> searchSplitArray)
    {
        mainCriteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
        return this;
    }

    public ElasticSearchExpression JoinPublishedDayCriteria(String startDay, String endDay)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date startDate = sdf.parse(startDay);
            Date endDate = sdf.parse(endDay);
            mainCriteria.subCriteria(new Criteria().and("publishedDay").between(startDate, endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return this;
    }

    public ElasticSearchExpression JoinFidCriteria(int fid)
    {
        mainCriteria.subCriteria(new Criteria("fid").is(fid));
        return this;
    }

    public ElasticSearchExpression JoinKeywordCriteria(String keyword)
    {
        mainCriteria.subCriteria(new Criteria("keyword").is(keyword));
        return this;
    }

    public ElasticSearchExpression JoinSenderCriteria(String sender)
    {
        mainCriteria.subCriteria(new Criteria("sender").is(sender));
        return this;
    }

    public ElasticSearchExpression JoinResourceCriteria(String resource)
    {
        mainCriteria.subCriteria(new Criteria("resource").is(resource));
        return this;
    }

    public ElasticSearchExpression JoinTagCriteria(String tag)
    {
        mainCriteria.subCriteria(new Criteria("tag").is(tag));
        return this;
    }

    public ElasticSearchExpression JoinEmotionCriteria(String emotion)
    {
        mainCriteria.subCriteria(new Criteria("emotion").is(emotion));
        return this;
    }

    public ElasticSearchExpression JoinSensitiveTypeCriteria(String sensitiveType)
    {
        mainCriteria.subCriteria(new Criteria("sensitiveType").is(sensitiveType));
        return this;
    }

    public void Finished()
    {

    }

    public void SetPageParameter(int page, int pageSize, int timeOrder)
    {
        this.page = page; this.pageSize= pageSize; this.timeOrder = timeOrder;
    }

    public CriteriaQuery GetQuery()
    {
        CriteriaQuery query = new CriteriaQuery(mainCriteria);
        if (timeOrder == -2)  // timeOrder没有初始化，不做分页
            return query;
        if (timeOrder == 0) {
            query.setPageable(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "publishedDay")));
        }
        else {
            query.setPageable(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "publishedDay")));
        }
        return query;
    }

}
