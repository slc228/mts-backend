package com.sjtu.mts.Query;

import com.sjtu.mts.Expression.ElasticSearchExpression;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ElasticSearchQuery {
    private final BoolQueryBuilder boolQueryBuilder;
    private int page;
    private int pageSize;
    private int timeOrder;

    public ElasticSearchQuery() {
        boolQueryBuilder= new BoolQueryBuilder();
        this.page = 0;
        this.pageSize = 10;
        this.timeOrder = -2;
    }

    public ElasticSearchQuery JoinTitleAndContentQueryBuilders(String keyword)
    {
        BoolQueryBuilder keywordsBoolQueryBuilder=new BoolQueryBuilder();
        String[] searchSplitArray1 = keyword.trim().split("\\s+");
        List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
        BoolQueryBuilder contentBoolQueryBuilder=new BoolQueryBuilder();
        BoolQueryBuilder titleBoolQueryBuilder=new BoolQueryBuilder();
        for (String searchString : searchSplitArray) {
            contentBoolQueryBuilder.should(QueryBuilders.matchQuery("content", searchString));
            titleBoolQueryBuilder.should(QueryBuilders.matchQuery("title", searchString));
        }
        keywordsBoolQueryBuilder.should(contentBoolQueryBuilder);
        keywordsBoolQueryBuilder.should(titleBoolQueryBuilder);
        boolQueryBuilder.must(keywordsBoolQueryBuilder);
        return this;
    }

    public ElasticSearchQuery JoinTitleAndContentQueryBuilders(List<String> events)
    {
        BoolQueryBuilder keywordsBoolQueryBuilder=new BoolQueryBuilder();
        for (String event : events)
        {
            String[] searchSplitArray1 = event.trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
            BoolQueryBuilder contentBoolQueryBuilder=new BoolQueryBuilder();
            BoolQueryBuilder titleBoolQueryBuilder=new BoolQueryBuilder();
            for (String searchString : searchSplitArray) {
                contentBoolQueryBuilder.must(QueryBuilders.matchQuery("content", searchString));
                titleBoolQueryBuilder.must(QueryBuilders.matchQuery("title", searchString));
            }
            keywordsBoolQueryBuilder.should(contentBoolQueryBuilder);
            keywordsBoolQueryBuilder.should(titleBoolQueryBuilder);
        }
        boolQueryBuilder.must(keywordsBoolQueryBuilder);
        return this;
    }

    public ElasticSearchQuery JoinPublishedDayQueryBuilders(String startDay, String endDay)
    {
        RangeQueryBuilder rangeQueryBuilder=new RangeQueryBuilder("publishedDay");
        if (!startDay.isEmpty() && !endDay.isEmpty())
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date startDate = sdf.parse(startDay);
                Date endDate = sdf.parse(endDay);
                rangeQueryBuilder.from(startDate);
                rangeQueryBuilder.to(endDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        boolQueryBuilder.must(rangeQueryBuilder);
        return this;
    }

    public ElasticSearchQuery JoinFidQueryBuilders(int fid)
    {
        boolQueryBuilder.must(QueryBuilders.termQuery("fid", fid));
        return this;
    }

    public ElasticSearchQuery JoinResourceQueryBuilders(String resource)
    {
        boolQueryBuilder.must(QueryBuilders.matchQuery("resource", resource));
        return this;
    }

    public ElasticSearchQuery JoinTagQueryBuilders(String tag)
    {
        boolQueryBuilder.must(QueryBuilders.matchQuery("tag", tag));
        return this;
    }

    public ElasticSearchQuery JoinEmotionQueryBuilders(String emotion)
    {
        boolQueryBuilder.must(QueryBuilders.matchQuery("emotion", emotion));
        return this;
    }

    public ElasticSearchQuery JoinSensitiveTypeQueryBuilders(String sensitiveType)
    {
        boolQueryBuilder.must(QueryBuilders.matchQuery("sensitiveType", sensitiveType));
        return this;
    }

    public void Finished()
    {

    }

    public void SetPageParameter(int page, int pageSize, int timeOrder)
    {
        this.page = page; this.pageSize= pageSize; this.timeOrder = timeOrder;
    }

    public NativeSearchQuery GetQuery()
    {
        NativeSearchQueryBuilder nativeSearchQueryBuilder=new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        if (timeOrder == -2)  // timeOrder没有初始化，不做分页
        {
            NativeSearchQuery nativeSearchQuery=nativeSearchQueryBuilder.build();
            return nativeSearchQuery;
        }
        if (timeOrder == 0) {
            Pageable pageable=PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "publishedDay"));
            nativeSearchQueryBuilder.withPageable(pageable);
        }
        else {
            Pageable pageable=PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "publishedDay"));
            nativeSearchQueryBuilder.withPageable(pageable);
        }

        NativeSearchQuery nativeSearchQuery=nativeSearchQueryBuilder.build();
        return nativeSearchQuery;
    }
}
