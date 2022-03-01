package com.sjtu.mts.Query;

import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.FangAn;
import com.sjtu.mts.Expression.ElasticSearchExpression;
import com.sjtu.mts.Repository.AreaRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ElasticSearchQuery {
    private NativeSearchQueryBuilder nativeSearchQueryBuilder=new NativeSearchQueryBuilder();
    public final BoolQueryBuilder boolQueryBuilder;
    private int page;
    private int pageSize;
    private int timeOrder;
    private final AreaRepository areaRepository;
    private final FangAnDao fangAnDao;

    public ElasticSearchQuery(AreaRepository areaRepository, FangAnDao fangAnDao) {
        boolQueryBuilder= new BoolQueryBuilder();
        this.page = 0;
        this.pageSize = 20;
        this.timeOrder = -2;
        this.areaRepository = areaRepository;
        this.fangAnDao=fangAnDao;
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

    public ElasticSearchQuery JoinTitleAndContentQueryBuildersByAreas(List<String> areas)
    {
        BoolQueryBuilder keywordsBoolQueryBuilder=new BoolQueryBuilder();
        for (String area : areas)
        {
            BoolQueryBuilder contentBoolQueryBuilder=new BoolQueryBuilder();
            BoolQueryBuilder titleBoolQueryBuilder=new BoolQueryBuilder();
            contentBoolQueryBuilder.should(QueryBuilders.termQuery("content", area));
            titleBoolQueryBuilder.should(QueryBuilders.termQuery("title", area));
            keywordsBoolQueryBuilder.should(contentBoolQueryBuilder);
            keywordsBoolQueryBuilder.should(titleBoolQueryBuilder);
        }
        boolQueryBuilder.must(keywordsBoolQueryBuilder);
        return this;
    }

    public ElasticSearchQuery JoinQueryBuildersByUrls(List<String> urls)
    {
        BoolQueryBuilder urlsBoolQueryBuilder=new BoolQueryBuilder();
        for (String url : urls)
        {
            BoolQueryBuilder urlBoolQueryBuilder=new BoolQueryBuilder();
            urlBoolQueryBuilder.should(QueryBuilders.termQuery("yuqing_url", url));
            urlsBoolQueryBuilder.should(urlBoolQueryBuilder);
        }
        boolQueryBuilder.must(urlsBoolQueryBuilder);
        return this;
    }

    public ElasticSearchQuery JoinPublishedDayQueryBuilders(String startDay, String endDay)
    {
        RangeQueryBuilder rangeQueryBuilder=new RangeQueryBuilder("publishedDay");
        if (!startDay.isEmpty() && !endDay.isEmpty()&& !startDay.equals("null") && !endDay.equals("null"))
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

    public ElasticSearchQuery JoinFidQueryBuilders(long fid)
    {
        FangAn fangAn = fangAnDao.findByFid(fid);
        String regionKeyword = fangAn.getRegionKeyword();
        int regionKeywordMatch = fangAn.getRegionKeywordMatch();
        BoolQueryBuilder regionKeywordBoolQueryBuilder=new BoolQueryBuilder();

        String roleKeyword = fangAn.getRoleKeyword();
        int roleKeywordMatch = fangAn.getRoleKeywordMatch();
        BoolQueryBuilder roleKeywordBoolQueryBuilder=new BoolQueryBuilder();

        String eventKeyword = fangAn.getEventKeyword();
        List<String> events=new ArrayList<String>();
        BoolQueryBuilder eventKeywordBoolQueryBuilder=new BoolQueryBuilder();

        while(eventKeyword.length()>0)
        {
            int tag=eventKeyword.indexOf('+');
            events.add(eventKeyword.substring(0,tag));
            eventKeyword=eventKeyword.substring(tag+1);
        }

        boolQueryBuilder.must(QueryBuilders.termQuery("fid", fid));

        if (!roleKeyword.isEmpty())
        {
            String[] searchSplitArray1 = roleKeyword.trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
            BoolQueryBuilder contentBoolQueryBuilder=new BoolQueryBuilder();
            BoolQueryBuilder titleBoolQueryBuilder=new BoolQueryBuilder();
            for (String searchString : searchSplitArray) {
                if (roleKeywordMatch == 1) {
                    contentBoolQueryBuilder.must(QueryBuilders.matchQuery("content", searchString));
                    titleBoolQueryBuilder.must(QueryBuilders.matchQuery("title", searchString));
                } else {
                    contentBoolQueryBuilder.should(QueryBuilders.matchQuery("content", searchString));
                    titleBoolQueryBuilder.should(QueryBuilders.matchQuery("title", searchString));
                }
            }
            roleKeywordBoolQueryBuilder.should(contentBoolQueryBuilder);
            roleKeywordBoolQueryBuilder.should(titleBoolQueryBuilder);
        }

        for (int numOfEvents=0;numOfEvents<events.size();numOfEvents++)
        {
            String[] searchSplitArray1 = events.get(numOfEvents).trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
            BoolQueryBuilder contentBoolQueryBuilder=new BoolQueryBuilder();
            BoolQueryBuilder titleBoolQueryBuilder=new BoolQueryBuilder();
            for (String searchString : searchSplitArray) {
                contentBoolQueryBuilder.must(QueryBuilders.matchQuery("content",searchString));
                titleBoolQueryBuilder.must(QueryBuilders.matchQuery("title",searchString));
            }
            eventKeywordBoolQueryBuilder.should(contentBoolQueryBuilder);
            eventKeywordBoolQueryBuilder.should(titleBoolQueryBuilder);
        }

        if (!regionKeyword.isEmpty()) {
            String[] searchSplitArray1 = regionKeyword.trim().split("\\s+");
            List<String> searchSplitArray = Arrays.asList(searchSplitArray1);
            if (searchSplitArray.size() == 1) {
                BoolQueryBuilder contentBoolQueryBuilder = new BoolQueryBuilder();
                BoolQueryBuilder titleBoolQueryBuilder = new BoolQueryBuilder();
                List<Integer> codeid = areaRepository.findCodeidByCityName(searchSplitArray.get(0));
                List<String> citys = new ArrayList<>();
                for (Integer co : codeid) {
                    List<String> tmp = areaRepository.findCityNameByCodeid(co);
                    for (int i = 0; i < tmp.size(); i++) {
                        tmp.set(i, tmp.get(i).replaceAll("\\s*", ""));
                        if (tmp.get(i).contains("市辖") || tmp.get(i).contains("县辖")) {
                            tmp.remove(i);
                        }
                    }
                    citys.addAll(tmp);
                }

                citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重
                for (String city : citys) {
                    contentBoolQueryBuilder.should(QueryBuilders.matchQuery("content", city));
                    titleBoolQueryBuilder.should(QueryBuilders.matchQuery("title", city));
                }
                regionKeywordBoolQueryBuilder.should(contentBoolQueryBuilder);
                regionKeywordBoolQueryBuilder.should(titleBoolQueryBuilder);
            } else if (searchSplitArray.size() > 1 && regionKeywordMatch == 1) {
                BoolQueryBuilder allContentBoolQueryBuilder = new BoolQueryBuilder();
                BoolQueryBuilder allTitleBoolQueryBuilder = new BoolQueryBuilder();
                for (String searchString : searchSplitArray) {
                    BoolQueryBuilder contentBoolQueryBuilder = new BoolQueryBuilder();
                    BoolQueryBuilder titleBoolQueryBuilder = new BoolQueryBuilder();
                    List<Integer> codeid = areaRepository.findCodeidByCityName(searchString);
                    List<String> citys = new ArrayList<>();
                    for (Integer co : codeid) {
                        List<String> tmp = areaRepository.findCityNameByCodeid(co);
                        for (int i = 0; i < tmp.size(); i++) {
                            tmp.set(i, tmp.get(i).replaceAll("\\s*", ""));
                            if (tmp.get(i).contains("市辖") || tmp.get(i).contains("县辖")) {
                                tmp.remove(i);
                            }
                        }
                        citys.addAll(tmp);
                    }

                    citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重
                    for (String city : citys) {
                        contentBoolQueryBuilder.should(QueryBuilders.matchQuery("content", city));
                        titleBoolQueryBuilder.should(QueryBuilders.matchQuery("title", city));
                    }
                    allContentBoolQueryBuilder.must(contentBoolQueryBuilder);
                    allTitleBoolQueryBuilder.must(titleBoolQueryBuilder);
                }
                regionKeywordBoolQueryBuilder.should(allContentBoolQueryBuilder);
                regionKeywordBoolQueryBuilder.should(allTitleBoolQueryBuilder);
            } else if (searchSplitArray.size() > 1 && regionKeywordMatch == 0) {
                BoolQueryBuilder contentBoolQueryBuilder = new BoolQueryBuilder();
                BoolQueryBuilder titleBoolQueryBuilder = new BoolQueryBuilder();
                List<String> citys = new ArrayList<>();
                for (String searchString : searchSplitArray) {
                    List<Integer> codeid = areaRepository.findCodeidByCityName(searchString);
                    for (Integer co : codeid) {
                        List<String> tmp = areaRepository.findCityNameByCodeid(co);
                        for (int i = 0; i < tmp.size(); i++) {
                            tmp.set(i, tmp.get(i).replaceAll("\\s*", ""));
                            if (tmp.get(i).contains("市辖") || tmp.get(i).contains("县辖")) {
                                tmp.remove(i);
                            }
                        }
                        citys.addAll(tmp);
                    }
                    citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重

                }
                for (String city : citys) {
                    contentBoolQueryBuilder.should(QueryBuilders.matchQuery("content", city));
                    titleBoolQueryBuilder.should(QueryBuilders.matchQuery("title", city));
                }
                regionKeywordBoolQueryBuilder.should(contentBoolQueryBuilder);
                regionKeywordBoolQueryBuilder.should(titleBoolQueryBuilder);
            }
        }
        boolQueryBuilder.must(regionKeywordBoolQueryBuilder);
        boolQueryBuilder.must(roleKeywordBoolQueryBuilder);
        boolQueryBuilder.must(eventKeywordBoolQueryBuilder);

        return this;
    }

    public ElasticSearchQuery JoinFidQueryBuildersWithOutFid(long fid)
    {
        FangAn fangAn = fangAnDao.findByFid(fid);
        String regionKeyword = fangAn.getRegionKeyword();
        int regionKeywordMatch = fangAn.getRegionKeywordMatch();
        BoolQueryBuilder regionKeywordBoolQueryBuilder=new BoolQueryBuilder();

        String roleKeyword = fangAn.getRoleKeyword();
        int roleKeywordMatch = fangAn.getRoleKeywordMatch();
        BoolQueryBuilder roleKeywordBoolQueryBuilder=new BoolQueryBuilder();

        String eventKeyword = fangAn.getEventKeyword();
        List<String> events=new ArrayList<String>();
        BoolQueryBuilder eventKeywordBoolQueryBuilder=new BoolQueryBuilder();

        while(eventKeyword.length()>0)
        {
            int tag=eventKeyword.indexOf('+');
            events.add(eventKeyword.substring(0,tag));
            eventKeyword=eventKeyword.substring(tag+1);
        }

        boolQueryBuilder.mustNot(QueryBuilders.termQuery("fid", fid));

        if (!roleKeyword.isEmpty())
        {
            String[] searchSplitArray1 = roleKeyword.trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
            BoolQueryBuilder contentBoolQueryBuilder=new BoolQueryBuilder();
            BoolQueryBuilder titleBoolQueryBuilder=new BoolQueryBuilder();
            for (String searchString : searchSplitArray) {
                if (roleKeywordMatch == 1) {
                    contentBoolQueryBuilder.must(QueryBuilders.matchQuery("content", searchString));
                    titleBoolQueryBuilder.must(QueryBuilders.matchQuery("title", searchString));
                } else {
                    contentBoolQueryBuilder.should(QueryBuilders.matchQuery("content", searchString));
                    titleBoolQueryBuilder.should(QueryBuilders.matchQuery("title", searchString));
                }
            }
            roleKeywordBoolQueryBuilder.should(contentBoolQueryBuilder);
            roleKeywordBoolQueryBuilder.should(titleBoolQueryBuilder);
        }

        for (int numOfEvents=0;numOfEvents<events.size();numOfEvents++)
        {
            String[] searchSplitArray1 = events.get(numOfEvents).trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
            BoolQueryBuilder contentBoolQueryBuilder=new BoolQueryBuilder();
            BoolQueryBuilder titleBoolQueryBuilder=new BoolQueryBuilder();
            for (String searchString : searchSplitArray) {
                contentBoolQueryBuilder.must(QueryBuilders.matchQuery("content",searchString));
                titleBoolQueryBuilder.must(QueryBuilders.matchQuery("title",searchString));
            }
            eventKeywordBoolQueryBuilder.should(contentBoolQueryBuilder);
            eventKeywordBoolQueryBuilder.should(titleBoolQueryBuilder);
        }

        if (!regionKeyword.isEmpty()) {
            String[] searchSplitArray1 = regionKeyword.trim().split("\\s+");
            List<String> searchSplitArray = Arrays.asList(searchSplitArray1);
            if (searchSplitArray.size() == 1) {
                BoolQueryBuilder contentBoolQueryBuilder = new BoolQueryBuilder();
                BoolQueryBuilder titleBoolQueryBuilder = new BoolQueryBuilder();
                List<Integer> codeid = areaRepository.findCodeidByCityName(searchSplitArray.get(0));
                List<String> citys = new ArrayList<>();
                for (Integer co : codeid) {
                    List<String> tmp = areaRepository.findCityNameByCodeid(co);
                    for (int i = 0; i < tmp.size(); i++) {
                        tmp.set(i, tmp.get(i).replaceAll("\\s*", ""));
                        if (tmp.get(i).contains("市辖") || tmp.get(i).contains("县辖")) {
                            tmp.remove(i);
                        }
                    }
                    citys.addAll(tmp);
                }

                citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重
                for (String city : citys) {
                    contentBoolQueryBuilder.should(QueryBuilders.matchQuery("content", city));
                    titleBoolQueryBuilder.should(QueryBuilders.matchQuery("title", city));
                }
                regionKeywordBoolQueryBuilder.should(contentBoolQueryBuilder);
                regionKeywordBoolQueryBuilder.should(titleBoolQueryBuilder);
            } else if (searchSplitArray.size() > 1 && regionKeywordMatch == 1) {
                BoolQueryBuilder allContentBoolQueryBuilder = new BoolQueryBuilder();
                BoolQueryBuilder allTitleBoolQueryBuilder = new BoolQueryBuilder();
                for (String searchString : searchSplitArray) {
                    BoolQueryBuilder contentBoolQueryBuilder = new BoolQueryBuilder();
                    BoolQueryBuilder titleBoolQueryBuilder = new BoolQueryBuilder();
                    List<Integer> codeid = areaRepository.findCodeidByCityName(searchString);
                    List<String> citys = new ArrayList<>();
                    for (Integer co : codeid) {
                        List<String> tmp = areaRepository.findCityNameByCodeid(co);
                        for (int i = 0; i < tmp.size(); i++) {
                            tmp.set(i, tmp.get(i).replaceAll("\\s*", ""));
                            if (tmp.get(i).contains("市辖") || tmp.get(i).contains("县辖")) {
                                tmp.remove(i);
                            }
                        }
                        citys.addAll(tmp);
                    }

                    citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重
                    for (String city : citys) {
                        contentBoolQueryBuilder.should(QueryBuilders.matchQuery("content", city));
                        titleBoolQueryBuilder.should(QueryBuilders.matchQuery("title", city));
                    }
                    allContentBoolQueryBuilder.must(contentBoolQueryBuilder);
                    allTitleBoolQueryBuilder.must(titleBoolQueryBuilder);
                }
                regionKeywordBoolQueryBuilder.should(allContentBoolQueryBuilder);
                regionKeywordBoolQueryBuilder.should(allTitleBoolQueryBuilder);
            } else if (searchSplitArray.size() > 1 && regionKeywordMatch == 0) {
                BoolQueryBuilder contentBoolQueryBuilder = new BoolQueryBuilder();
                BoolQueryBuilder titleBoolQueryBuilder = new BoolQueryBuilder();
                List<String> citys = new ArrayList<>();
                for (String searchString : searchSplitArray) {
                    List<Integer> codeid = areaRepository.findCodeidByCityName(searchString);
                    for (Integer co : codeid) {
                        List<String> tmp = areaRepository.findCityNameByCodeid(co);
                        for (int i = 0; i < tmp.size(); i++) {
                            tmp.set(i, tmp.get(i).replaceAll("\\s*", ""));
                            if (tmp.get(i).contains("市辖") || tmp.get(i).contains("县辖")) {
                                tmp.remove(i);
                            }
                        }
                        citys.addAll(tmp);
                    }
                    citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重

                }
                for (String city : citys) {
                    contentBoolQueryBuilder.should(QueryBuilders.matchQuery("content", city));
                    titleBoolQueryBuilder.should(QueryBuilders.matchQuery("title", city));
                }
                regionKeywordBoolQueryBuilder.should(contentBoolQueryBuilder);
                regionKeywordBoolQueryBuilder.should(titleBoolQueryBuilder);
            }
        }
        boolQueryBuilder.must(regionKeywordBoolQueryBuilder);
        boolQueryBuilder.must(roleKeywordBoolQueryBuilder);
        boolQueryBuilder.must(eventKeywordBoolQueryBuilder);

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

    public ElasticSearchQuery SortBytimeOrder()
    {
        if (timeOrder == -2)  // timeOrder没有初始化，不做分页
        {
            return this;
        }
        if (timeOrder == 0) {
            FieldSortBuilder fieldSortBuilder= SortBuilders.fieldSort("publishedDay").order(SortOrder.DESC);
            nativeSearchQueryBuilder.withSort(fieldSortBuilder);
            return this;
        }
        else {
            FieldSortBuilder fieldSortBuilder= SortBuilders.fieldSort("publishedDay").order(SortOrder.ASC);
            nativeSearchQueryBuilder.withSort(fieldSortBuilder);
            return this;
        }
    }

    public ElasticSearchQuery SortBySensitiveType()
    {
        Script script=new Script("doc['sensitiveType'].value.indexOf('政治敏感')!=-1?3:doc['sensitiveType'].value.indexOf('正常信息')!=-1?1:2");
        ScriptSortBuilder scriptSortBuilder = SortBuilders.scriptSort(script, ScriptSortBuilder.ScriptSortType.NUMBER).order(SortOrder.DESC);
        nativeSearchQueryBuilder.withSort(scriptSortBuilder);
        return this;
    }

    public ElasticSearchQuery AggregateByResource()
    {
        TermsAggregationBuilder termsAggregationBuilder= AggregationBuilders.terms("resource_count").field("resource").size(20);
        nativeSearchQueryBuilder.addAggregation(termsAggregationBuilder);
        return this;
    }

    public ElasticSearchQuery AggregateBySensitiveType()
    {
        TermsAggregationBuilder termsAggregationBuilder= AggregationBuilders.terms("sensitiveType_count").field("sensitiveType").size(20);
        nativeSearchQueryBuilder.addAggregation(termsAggregationBuilder);
        return this;
    }

    public void Finished()
    {

    }

    public void SetPageParameter(int page, int pageSize, int timeOrder)
    {
        this.page = page; this.pageSize= pageSize; this.timeOrder = timeOrder;
    }

    public ElasticSearchQuery SetBoolQuery()
    {
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        return this;
    }

    public ElasticSearchQuery SetPageableAndBoolQuery()
    {
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        Pageable pageable=PageRequest.of(page, pageSize);
        nativeSearchQueryBuilder.withPageable(pageable);
        return this;
    }

    public NativeSearchQuery GetQuery()
    {
//        AggregationBuilder aggregationBuilder = AggregationBuilders
//            .terms("url_aggs").field("yuqing_url").size(10000)
//            .subAggregation(AggregationBuilders.topHits("url_top"));
//        nativeSearchQueryBuilder.addAggregation((AbstractAggregationBuilder<?>) aggregationBuilder);
        NativeSearchQuery nativeSearchQuery=nativeSearchQueryBuilder.build();
        return nativeSearchQuery;
    }
}
