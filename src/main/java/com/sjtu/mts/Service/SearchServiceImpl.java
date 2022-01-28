package com.sjtu.mts.Service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;

import java.beans.Expression;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.sjtu.mts.Dao.*;
import com.sjtu.mts.Entity.*;
import com.sjtu.mts.Expression.ElasticSearchExpression;
import com.sjtu.mts.Keyword.KeywordResponse;
import com.sjtu.mts.Keyword.MultipleThreadExtraction;
import com.sjtu.mts.Keyword.TextRankKeyword;
import com.sjtu.mts.Query.ElasticSearchQuery;
import com.sjtu.mts.Repository.AreaRepository;
import com.sjtu.mts.Repository.SensitiveWordRepository;
import com.sjtu.mts.Repository.SwordFidRepository;
import com.sjtu.mts.Response.*;
import com.sjtu.mts.rpc.WeiboSpiderRpc;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.hibernate.Hibernate;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Blob;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.codec.digest.DigestUtils;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.sjtu.mts.Keyword.Wrapper.min;


@Service
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final AreaRepository areaRepository;
    private final SensitiveWordRepository sensitiveWordRepository;
    private final SwordFidRepository swordFidRepository;
    static  boolean flag = false;
    static  boolean flagHanLp = false;
    static  long nowFid = -1;

    @Autowired
    private FangAnDao fangAnDao;

    @Autowired
    private FangAnWeiboUserDAO fangAnWeiboUserDAO;

    @Autowired
    private FangAnTemplateDAO fangAnTemplateDAO;

    @Autowired
    private FangAnMaterialDAO fangAnMaterialDAO;

    @Autowired
    private DimensionDao dimensionDao;

    @Autowired
    private BriefingFileDao briefingFileDao;

    @Autowired
    private SensitiveWordsDao sensitiveWordsDao;

    @Autowired
    private MonitoringWebsiteDao monitoringWebsiteDao;

    @Autowired
    private WeiboSpiderRpc weiboSpiderRpc;

    @Autowired
    private ElasticSearchDao elasticSearchDao;

    public SearchServiceImpl(ElasticsearchOperations elasticsearchOperations, AreaRepository areaRepository,SensitiveWordRepository sensitiveWordRepository,SwordFidRepository swordFidRepository)
    {
        this.elasticsearchOperations = elasticsearchOperations;
        this.areaRepository = areaRepository;
        this.sensitiveWordRepository = sensitiveWordRepository;
        this.swordFidRepository = swordFidRepository;
    }

    public int SensitiveTypeToInt(String SensitiveType)
    {
        if (SensitiveType==null)
        {
            return 0;
        }
        if (SensitiveType.indexOf("政治敏感")!=-1)
        {
            return 5;
        }
        if (SensitiveType.indexOf("人身攻击")!=-1)
        {
            return 4;
        }
        if (SensitiveType.indexOf("正常信息")!=-1)
        {
            return 0;
        }
        return 1;
    }

    public int EmotionToInt(String Emotion)
    {
        if (Emotion==null)
        {
            return 0;
        }
        if (Emotion.indexOf("angry")!=-1)
        {
            return 5;
        }
        if (Emotion.indexOf("fear")!=-1)
        {
            return 4;
        }
        if (Emotion.indexOf("neutral")!=-1)
        {
            return 0;
        }
        return 1;
    }

    public String EmotionToChinese(String text)
    {
        if (text.equals("happy")) return "积极 🥰";
        if (text.equals("angry")) return "愤怒 😡";
        if (text.equals("sad")) return "悲伤 😭";
        if (text.equals("fear")) return "恐惧 😰";
        if (text.equals("surprise")) return "惊讶 😮";
        if (text.equals("neutral")) return "中立 😐";
        return "";
    }

    public String SensitiveTypeStr(String SensitiveType)
    {
        if (SensitiveType==null){
            return null;
        }
        if (SensitiveType.equals("1"))
        {
            return "正常信息";
        }
        if (SensitiveType.equals("2"))
        {
            return "政治敏感";
        }
        if (SensitiveType.equals("3"))
        {
            return "广告营销";
        }
        if (SensitiveType.equals("4"))
        {
            return "不实信息";
        }
        if (SensitiveType.equals("5"))
        {
            return "人身攻击";
        }
        if (SensitiveType.equals("6"))
        {
            return "低俗信息";
        }
        return null;
    }

    public String EmotionStr(String Emotion)
    {
        if (Emotion == null) {
            return null;
        }
        if (Emotion.equals("1"))
        {
            return "neutral";
        }
        if (Emotion.equals("2"))
        {
            return "angry";
        }
        if (Emotion.equals("3"))
        {
            return "fear";
        }
        if (Emotion.equals("4"))
        {
            return "surprise";
        }
        if (Emotion.equals("5"))
        {
            return "sad";
        }
        if (Emotion.equals("6"))
        {
            return "happy";
        }
        return null;
    }

    @Override
    public YuQingResponse Search(String keyword, String startPublishedDay, String endPublishedDay,
                                 String sensitiveFlag, int page, int pageSize, int timeOrder)
    {
        ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
        query.SetPageParameter(page,pageSize,timeOrder);
        if (!keyword.isEmpty())
            query.JoinTitleAndContentQueryBuilders(keyword);
        if (startPublishedDay != null && endPublishedDay != null &&
                !startPublishedDay.isEmpty() && !endPublishedDay.isEmpty() )
            query.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
        if (Objects.equals(sensitiveFlag, "1"))
        {
            query.JoinSensitiveTypeQueryBuilders("政治敏感 ");
        }

        query.SortBytimeOrder();
        query.SetPageableAndBoolQuery();

        YuQingResponse response = elasticSearchDao.findByQuery(query);
        return response;
    }

    @Override
    public YuQingResponse SearchWithObject(String keyword, String sensitiveType, String emotion, String startPublishedDay, String endPublishedDay,
                                         String resource, int page, int pageSize, int timeOrder,String keywords)
    {
        String eventKeyword = keywords;
        eventKeyword=eventKeyword.substring(1,eventKeyword.length()-1);
        List<String> events=new ArrayList<String>();
        if (eventKeyword.length()!=0)
        {
            eventKeyword=eventKeyword+",";
            while(eventKeyword.length()>0)
            {
                int tag=eventKeyword.indexOf(',');
                events.add(eventKeyword.substring(1,tag-1));
                eventKeyword=eventKeyword.substring(tag+1);
            }
        }

        ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
        query.SetPageParameter(page,pageSize,timeOrder);
        if (!keyword.isEmpty())
            query.JoinTitleAndContentQueryBuilders(keyword);
        if (events.size()>0)
            query.JoinTitleAndContentQueryBuilders(events);
        if (!Objects.equals(startPublishedDay, "null") && !Objects.equals(endPublishedDay, "null") &&
                !startPublishedDay.isEmpty() && !endPublishedDay.isEmpty() )
            query.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
        if (!sensitiveType.isEmpty())
            query.JoinSensitiveTypeQueryBuilders(sensitiveType);
        if (!resource.isEmpty())
            query.JoinResourceQueryBuilders(resource);
        if (!emotion.isEmpty())
            query.JoinEmotionQueryBuilders(emotion);
        query.SortBySensitiveType();
        query.SortBytimeOrder();
        query.SetPageableAndBoolQuery();

        YuQingResponse response = elasticSearchDao.findByQuery(query);
        return response;
    };

    @Override
    public JSONArray getResources()
    {
        TermsAggregationBuilder termsAggregationBuilder= AggregationBuilders.terms("resource_count").field("resource").size(20);
        NativeSearchQueryBuilder nativeSearchQueryBuilder=new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.addAggregation(termsAggregationBuilder);
        NativeSearchQuery nativeSearchQuery=nativeSearchQueryBuilder.build();
        nativeSearchQuery.setMaxResults(0);
        Aggregations aggregations=this.elasticsearchOperations.search(nativeSearchQuery,YuQingElasticSearch.class).getAggregations();
        Terms aggregation = aggregations.get("resource_count");

        JSONArray ret=new JSONArray();
        JSONObject nullObject=new JSONObject();
        nullObject.put("label","不限");
        nullObject.put("value",null);
        ret.appendElement(nullObject);
        for (Terms.Bucket bucket : aggregation.getBuckets()) {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("label",bucket.getKey());
            jsonObject.put("value",bucket.getKey());
            ret.appendElement(jsonObject);
        }
        return ret;
    }

    @Override
    public JSONArray globalSearchResourceCount(String keyword, String startPublishedDay, String endPublishedDay) {
        ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
        query.AggregateByResource();
        return elasticSearchDao.aggregateByResource(query);
    }

    @Override
    public JSONArray globalSearchResourceCountByFid(long fid,String startPublishedDay, String endPublishedDay){
        ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
        query.JoinFidQueryBuilders(fid);
        query.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
        query.SetBoolQuery();
        query.AggregateByResource();

        return elasticSearchDao.aggregateByResource(query);
    }

    @Override
    public CflagCountResponse globalSearchCflagCount(String keyword, String startPublishedDay, String endPublishedDay) {
        ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
        query.AggregateBySensitiveType();
        List<Long> resultList = elasticSearchDao.aggregateBySensitiveType(query);
        return new CflagCountResponse(resultList.get(0), resultList.get(1));
    }
    @Override
    public CflagCountResponse globalSearchCflagCountByFid(long fid, String startPublishedDay, String endPublishedDay){
        ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
        query.JoinFidQueryBuilders(fid);
        query.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
        query.SetBoolQuery();
        query.AggregateBySensitiveType();
        List<Long> resultList = elasticSearchDao.aggregateBySensitiveType(query);
        return new CflagCountResponse(resultList.get(0), resultList.get(1));
//
//        List<Long> resultList = new ArrayList<>();
//        for (int cflag = 0; cflag <= 1 ; cflag++) {
//            resultList.add((long)0);
//        }
//        List<Criteria> criterias=fangAnDao.FindCriteriasByFid(fid);
//        for(Criteria criteria:criterias)
//        {
//            if (!startPublishedDay.isEmpty() && !endPublishedDay.isEmpty())
//            {
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                try {
//                    Date startDate = sdf.parse(startPublishedDay);
//                    Date endDate = sdf.parse(endPublishedDay);
//                    criteria.subCriteria(new Criteria().and("publishedDay").between(startDate, endDate));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//            CriteriaQuery queryall = new CriteriaQuery(criteria);
//            long searchHitCountall = this.elasticsearchOperations.count(queryall, Data.class);
//
//            criteria.subCriteria(new Criteria("sensitiveType").in("政治敏感"));
//            CriteriaQuery querySen = new CriteriaQuery(criteria);
//            long searchHitCountquerySen = this.elasticsearchOperations.count(querySen, Data.class);
//            System.out.println(searchHitCountall);
//            System.out.println(searchHitCountquerySen);
//            resultList.set(0,resultList.get(0)+searchHitCountall);
//            resultList.set(1,resultList.get(1)+searchHitCountquerySen);
//        }
//        resultList.set(0,resultList.get(0)-resultList.get(1));
//        return new CflagCountResponse(resultList.get(0), resultList.get(1));
    }

    @Override
    public JSONObject globalSearchTrendCount(String keyword, String startPublishedDay, String endPublishedDay) {
        JSONObject ret=new JSONObject();
        JSONArray xAxisForSourceAmountTrend=new JSONArray();
        List<Long> xAxisForTotalAmountTrend=new ArrayList<>();
        int pointNum = 6;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Date> dateList = new ArrayList<>();
        try {
            Date startDate = sdf.parse(startPublishedDay);
            Date endDate = sdf.parse(endPublishedDay);
            dateList.add(startDate);
            for (int i = 1; i <= pointNum; i++){
                Date dt = new Date((long)(startDate.getTime()+(endDate.getTime()-startDate.getTime())*i/(double)pointNum));
                dateList.add(dt);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> timeRange = new ArrayList<>();
        for (int j = 0; j < pointNum; j++) {
            timeRange.add(sdf.format(dateList.get(j)) + " to " + sdf.format(dateList.get(j + 1)));
        }

        List<List<Long>> fromTypeResultList = new ArrayList<>();
        List<MonitoringWebsite> monitoringWebsiteList=monitoringWebsiteDao.findAll();

        for (int j = 0; j < pointNum; j++) {
            xAxisForTotalAmountTrend.add((long) 0);
        }
        for (int j = 0; j < pointNum; j++) {
            Criteria criteria = new Criteria();
            criteria.subCriteria(new Criteria().and("publishedDay").between(dateList.get(j), dateList.get(j + 1)));
            CriteriaQuery query = new CriteriaQuery(criteria);
            long searchHitCount = this.elasticsearchOperations.count(query, Data.class);
            xAxisForTotalAmountTrend.set(j, xAxisForTotalAmountTrend.get(j) + searchHitCount);
        }

        for (MonitoringWebsite monitoringWebsite:monitoringWebsiteList) {
            JSONObject jsonObject=new JSONObject();
            List<Long> resultList = new ArrayList<>();
            for (int j = 0; j < pointNum; j++) {
                resultList.add((long)0);
            }
            for (int j = 0; j < pointNum; j++) {
                Criteria criteria = new Criteria();
                criteria.subCriteria(new Criteria().and("resource").is(monitoringWebsite.getName()));
                criteria.subCriteria(new Criteria().and("publishedDay").between(dateList.get(j), dateList.get(j + 1)));
                CriteriaQuery query = new CriteriaQuery(criteria);
                long searchHitCount = this.elasticsearchOperations.count(query, Data.class);
                resultList.set(j, resultList.get(j) + searchHitCount);
            }
            jsonObject.put("name",monitoringWebsite.getName());
            jsonObject.put("label",monitoringWebsite.getName());
            jsonObject.put("value",resultList);
            xAxisForSourceAmountTrend.appendElement(jsonObject);
        }
        ret.put("timeRange",timeRange);
        ret.put("totalAmountTrend",xAxisForTotalAmountTrend);
        ret.put("xAxisForSourceAmountTrend",xAxisForSourceAmountTrend);
        return ret;
    }

    @Override
    public JSONObject totalAmountTrendCount(String keyword,String startPublishedDay,String endPublishedDay)
    {
        JSONObject ret=new JSONObject();
        List<Long> xAxisForTotalAmountTrend=new ArrayList<>();

        int pointNum = 6;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Date> dateList = new ArrayList<>();
        try {
            Date startDate = sdf.parse(startPublishedDay);
            Date endDate = sdf.parse(endPublishedDay);
            dateList.add(startDate);
            for (int i = 1; i <= pointNum; i++){
                Date dt = new Date((long)(startDate.getTime()+(endDate.getTime()-startDate.getTime())*i/(double)pointNum));
                dateList.add(dt);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> timeRange = new ArrayList<>();
        for (int j = 0; j < pointNum; j++) {
            timeRange.add(sdf.format(dateList.get(j)) + " to " + sdf.format(dateList.get(j + 1)));
        }

        for (int j = 0; j < pointNum; j++) {
            xAxisForTotalAmountTrend.add((long) 0);
        }
        for (int j = 0; j < pointNum; j++) {
            ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
            query.JoinPublishedDayQueryBuilders(sdf.format(dateList.get(j)),sdf.format(dateList.get(j+1)));
            query.SetBoolQuery();
            long hit=elasticSearchDao.countByQuery(query);
            xAxisForTotalAmountTrend.set(j, xAxisForTotalAmountTrend.get(j) + hit);
        }

        ret.put("timeRange",timeRange);
        ret.put("totalAmountTrend",xAxisForTotalAmountTrend);
        return ret;
    }

    @Override
    public JSONObject sourceAmountTrendCount(String keyword,String startPublishedDay,String endPublishedDay)
    {
        JSONObject ret=new JSONObject();
        JSONArray xAxisForSourceAmountTrend=new JSONArray();
        int pointNum = 6;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Date> dateList = new ArrayList<>();
        try {
            Date startDate = sdf.parse(startPublishedDay);
            Date endDate = sdf.parse(endPublishedDay);
            dateList.add(startDate);
            for (int i = 1; i <= pointNum; i++){
                Date dt = new Date((long)(startDate.getTime()+(endDate.getTime()-startDate.getTime())*i/(double)pointNum));
                dateList.add(dt);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> timeRange = new ArrayList<>();
        for (int j = 0; j < pointNum; j++) {
            timeRange.add(sdf.format(dateList.get(j)) + " to " + sdf.format(dateList.get(j + 1)));
        }

        ElasticSearchQuery query =new ElasticSearchQuery(areaRepository,fangAnDao);
        query.AggregateByResource();
        Terms terms=elasticSearchDao.getAggregateByResource(query);
        for (Terms.Bucket bucket : terms.getBuckets()) {
            JSONObject jsonObject=new JSONObject();

            List<Long> resultList = new ArrayList<>();
            for (int j = 0; j < pointNum; j++) {
                resultList.add((long)0);
            }
            for (int j = 0; j < pointNum; j++) {
                ElasticSearchQuery querySearch =new ElasticSearchQuery(areaRepository,fangAnDao);
                querySearch.JoinPublishedDayQueryBuilders(sdf.format(dateList.get(j)),sdf.format(dateList.get(j+1)));
                querySearch.JoinResourceQueryBuilders(bucket.getKeyAsString());
                querySearch.SetBoolQuery();
                long searchHitCount = elasticSearchDao.countByQuery(querySearch);
                resultList.set(j, resultList.get(j) + searchHitCount);
            }

            jsonObject.put("name",bucket.getKey());
            jsonObject.put("label",bucket.getKey());
            jsonObject.put("value",resultList);
            xAxisForSourceAmountTrend.appendElement(jsonObject);
        }
        ret.put("timeRange",timeRange);
        ret.put("xAxisForSourceAmountTrend",xAxisForSourceAmountTrend);
        return ret;
    }

    @Override
    public JSONObject getProgrammeSourceTrend(long fid,String startPublishedDay,String endPublishedDay)
    {
        JSONObject ret=new JSONObject();
        JSONArray xAxisForSourceAmountTrend=new JSONArray();
        int pointNum = 6;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Date> dateList = new ArrayList<>();
        try {
            Date startDate = sdf.parse(startPublishedDay);
            Date endDate = sdf.parse(endPublishedDay);
            dateList.add(startDate);
            for (int i = 1; i <= pointNum; i++){
                Date dt = new Date((long)(startDate.getTime()+(endDate.getTime()-startDate.getTime())*i/(double)pointNum));
                dateList.add(dt);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> timeRange = new ArrayList<>();
        for (int j = 0; j < pointNum; j++) {
            timeRange.add(sdf.format(dateList.get(j)) + " to " + sdf.format(dateList.get(j + 1)));
        }

        ElasticSearchQuery query =new ElasticSearchQuery(areaRepository,fangAnDao);
        query.JoinFidQueryBuilders(fid);
        query.SetBoolQuery();
        query.AggregateByResource();
        Terms terms=elasticSearchDao.getAggregateByResource(query);
        for (Terms.Bucket bucket : terms.getBuckets()) {
            JSONObject jsonObject=new JSONObject();

            List<Long> resultList = new ArrayList<>();
            for (int j = 0; j < pointNum; j++) {
                resultList.add((long)0);
            }
            for (int j = 0; j < pointNum; j++) {
                ElasticSearchQuery querySearch =new ElasticSearchQuery(areaRepository,fangAnDao);
                querySearch.JoinFidQueryBuilders(fid);
                querySearch.JoinPublishedDayQueryBuilders(sdf.format(dateList.get(j)),sdf.format(dateList.get(j+1)));
                querySearch.JoinResourceQueryBuilders(bucket.getKeyAsString());
                querySearch.SetBoolQuery();
                long searchHitCount = elasticSearchDao.countByQuery(querySearch);
                resultList.set(j, resultList.get(j) + searchHitCount);
            }

            jsonObject.put("name",bucket.getKey());
            jsonObject.put("label",bucket.getKey());
            jsonObject.put("value",resultList);
            xAxisForSourceAmountTrend.appendElement(jsonObject);
        }
        ret.put("timeRange",timeRange);
        ret.put("xAxisForSourceAmountTrend",xAxisForSourceAmountTrend);
        return ret;
    }

    @Override
    public JSONObject getProgrammeTotalAmountTrend(long fid,String startPublishedDay,String endPublishedDay)
    {
        JSONObject ret=new JSONObject();
        List<Long> xAxisForTotalAmountTrend=new ArrayList<>();

        int pointNum = 6;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Date> dateList = new ArrayList<>();
        try {
            Date startDate = sdf.parse(startPublishedDay);
            Date endDate = sdf.parse(endPublishedDay);
            dateList.add(startDate);
            for (int i = 1; i <= pointNum; i++){
                Date dt = new Date((long)(startDate.getTime()+(endDate.getTime()-startDate.getTime())*i/(double)pointNum));
                dateList.add(dt);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> timeRange = new ArrayList<>();
        for (int j = 0; j < pointNum; j++) {
            timeRange.add(sdf.format(dateList.get(j)) + " to " + sdf.format(dateList.get(j + 1)));
        }

        for (int j = 0; j < pointNum; j++) {
            xAxisForTotalAmountTrend.add((long) 0);
        }
        for (int j = 0; j < pointNum; j++) {
            ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
            query.JoinFidQueryBuilders(fid);
            query.JoinPublishedDayQueryBuilders(sdf.format(dateList.get(j)),sdf.format(dateList.get(j+1)));
            query.SetBoolQuery();
            long hit=elasticSearchDao.countByQuery(query);
            xAxisForTotalAmountTrend.set(j, xAxisForTotalAmountTrend.get(j) + hit);
        }

        ret.put("timeRange",timeRange);
        ret.put("totalAmountTrend",xAxisForTotalAmountTrend);
        return ret;
    }

    @Override
    public AmountTrendResponse globalSearchTrendCount3(long fid,String startPublishedDay, String endPublishedDay){
        int pointNum = 48;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Date> dateList = new ArrayList<>();
        try {
            Date startDate = sdf.parse(startPublishedDay);
            Date endDate = sdf.parse(endPublishedDay);
            dateList.add(startDate);
            for (int i = 1; i <= pointNum; i++) {
                Date dt = new Date((long)(startDate.getTime()+(endDate.getTime()-startDate.getTime())*i/(double)pointNum));
                dateList.add(dt);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> timeRange = new ArrayList<>();
        List<List<Long>> fromTypeResultList = new ArrayList<>();
        for (int fromType = 0; fromType <= 7 ; fromType++) {
            if (fromType==0){
                for (int j = 0; j < pointNum; j++) {
                    timeRange.add(sdf.format(dateList.get(j)) + " to " + sdf.format(dateList.get(j + 1)));
                }
            }

            List<Long> resultList = new ArrayList<>();
            for (int j = 0; j < pointNum; j++) {
                resultList.add((long)0);
            }
            for (int j = 0; j < pointNum; j++) {
                //Criteria criteria = fangAnDao.criteriaByFid(fid);
                // fromType 0 indicates searching all fromTypes
                List<Criteria> criterias=fangAnDao.FindCriteriasByFid(fid);
                for (Criteria criteria:criterias)
                {
                    if (fromType != 0) {
                        criteria.subCriteria(new Criteria().and("fromType").is(fromType));
                    }
                    criteria.subCriteria(new Criteria().and("publishedDay").between(dateList.get(j), dateList.get(j + 1)));
                    CriteriaQuery query = new CriteriaQuery(criteria);
                    long searchHitCount = this.elasticsearchOperations.count(query, Data.class);
                    resultList.set(j,resultList.get(j)+searchHitCount);
                }
            }
            fromTypeResultList.add(resultList);
        }
        return new AmountTrendResponse(timeRange, fromTypeResultList.get(0),
                fromTypeResultList.get(1), fromTypeResultList.get(2), fromTypeResultList.get(3),
                fromTypeResultList.get(4), fromTypeResultList.get(5), fromTypeResultList.get(6),
                fromTypeResultList.get(7));
    }

    @Override
    public JSONObject globalSearchTrendCount2(long fid,String startPublishedDay, String endPublishedDay){
        JSONObject ret=new JSONObject();
        JSONArray xAxisForSourceAmountTrend=new JSONArray();
        List<Long> xAxisForTotalAmountTrend=new ArrayList<>();
        int pointNum = 6;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Date> dateList = new ArrayList<>();
        try {
            Date startDate = sdf.parse(startPublishedDay);
            Date endDate = sdf.parse(endPublishedDay);
            dateList.add(startDate);
            for (int i = 1; i <= pointNum; i++){
                Date dt = new Date((long)(startDate.getTime()+(endDate.getTime()-startDate.getTime())*i/(double)pointNum));
                dateList.add(dt);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> timeRange = new ArrayList<>();
        for (int j = 0; j < pointNum; j++) {
            timeRange.add(sdf.format(dateList.get(j)) + " to " + sdf.format(dateList.get(j + 1)));
        }

        List<List<Long>> fromTypeResultList = new ArrayList<>();
        List<MonitoringWebsite> monitoringWebsiteList=monitoringWebsiteDao.findAll();

        for (int j = 0; j < pointNum; j++) {
            xAxisForTotalAmountTrend.add((long) 0);
        }
        for (int j = 0; j < pointNum; j++) {
            List<Criteria> criterias = fangAnDao.FindCriteriasByFid(fid);
            for (Criteria criteria : criterias) {
                criteria.subCriteria(new Criteria().and("publishedDay").between(dateList.get(j), dateList.get(j + 1)));
                CriteriaQuery query = new CriteriaQuery(criteria);
                long searchHitCount = this.elasticsearchOperations.count(query, Data.class);
                xAxisForTotalAmountTrend.set(j, xAxisForTotalAmountTrend.get(j) + searchHitCount);
            }
        }

        for (MonitoringWebsite monitoringWebsite:monitoringWebsiteList) {
            JSONObject jsonObject=new JSONObject();
            List<Long> resultList = new ArrayList<>();
            for (int j = 0; j < pointNum; j++) {
                resultList.add((long)0);
            }
            for (int j = 0; j < pointNum; j++) {
                List<Criteria> criterias=fangAnDao.FindCriteriasByFid(fid);
                for (Criteria criteria:criterias)
                {
                    criteria.subCriteria(new Criteria().and("resource").is(monitoringWebsite.getName()));
                    criteria.subCriteria(new Criteria().and("publishedDay").between(dateList.get(j), dateList.get(j + 1)));
                    CriteriaQuery query = new CriteriaQuery(criteria);
                    long searchHitCount = this.elasticsearchOperations.count(query, Data.class);
                    resultList.set(j,resultList.get(j)+searchHitCount);
                }
            }
            jsonObject.put("name",monitoringWebsite.getName());
            jsonObject.put("label",monitoringWebsite.getName());
            jsonObject.put("value",resultList);
            xAxisForSourceAmountTrend.appendElement(jsonObject);
        }
        ret.put("timeRange",timeRange);
        ret.put("totalAmountTrend",xAxisForTotalAmountTrend);
        ret.put("xAxisForSourceAmountTrend",xAxisForSourceAmountTrend);
        return ret;
    }
    @Override
    public AreaAnalysisResponse countArea(String keyword, String startPublishedDay, String endPublishedDay){
        List<Long> resultList = new ArrayList<>();
        List<Integer> codeids = Arrays.asList(11,12,13,14,15,21,22,23,31,32,33,34,35,36,37,41,42,43,44,45,46,50,51,52,53,54,61,62,63,64,65,71,81,91);
        for(int i =0;i<codeids.size();i++){
            ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
            query.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
            List<String> citys;
            citys = areaRepository.findCityNameByCodeid(codeids.get(i));
            for(int j=0;j<citys.size();j++){
                citys.set(j,citys.get(j).replaceAll("\\s*", ""));
                if(citys.get(j).contains("市辖")||citys.get(j).contains("县辖")){
                    citys.remove(j);
                }
            }
            citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重
            query.JoinTitleAndContentQueryBuildersByAreas(citys);
            query.SetBoolQuery();
            long searchHitCount = elasticSearchDao.countByQuery(query);
            resultList.add(searchHitCount);
        }
        return new AreaAnalysisResponse(resultList.get(0),resultList.get(1),resultList.get(2),resultList.get(3),resultList.get(4),
                resultList.get(5),resultList.get(6),resultList.get(7),resultList.get(8),resultList.get(9),resultList.get(10),resultList.get(11),
                resultList.get(12),resultList.get(13),resultList.get(14),resultList.get(15),resultList.get(16),resultList.get(17),
                resultList.get(18),resultList.get(19),resultList.get(20),resultList.get(21),resultList.get(22),resultList.get(23),
                resultList.get(24),resultList.get(25),resultList.get(26),resultList.get(27),resultList.get(28),resultList.get(29),
                resultList.get(30),resultList.get(31),resultList.get(32),resultList.get(33));
    }
    @Override
    public AreaAnalysisResponse countAreaByFid(long fid,String startPublishedDay, String endPublishedDay){
        List<Long> resultList = new ArrayList<>();
        List<Integer> codeids = Arrays.asList(11,12,13,14,15,21,22,23,31,32,33,34,35,36,37,41,42,43,44,45,46,50,51,52,53,54,61,62,63,64,65,71,81,91);
        for(int i =0;i<codeids.size();i++){
            ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
            query.JoinFidQueryBuilders(fid);
            query.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
            List<String> citys;
            citys = areaRepository.findCityNameByCodeid(codeids.get(i));
            for(int j=0;j<citys.size();j++){
                citys.set(j,citys.get(j).replaceAll("\\s*", ""));
                if(citys.get(j).contains("市辖")||citys.get(j).contains("县辖")){
                    citys.remove(j);
                }
            }
            citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重
            query.JoinTitleAndContentQueryBuildersByAreas(citys);
            query.SetBoolQuery();
            long searchHitCount = elasticSearchDao.countByQuery(query);
            resultList.add(searchHitCount);
        }
        return new AreaAnalysisResponse(resultList.get(0),resultList.get(1),resultList.get(2),resultList.get(3),resultList.get(4),
                resultList.get(5),resultList.get(6),resultList.get(7),resultList.get(8),resultList.get(9),resultList.get(10),resultList.get(11),
                resultList.get(12),resultList.get(13),resultList.get(14),resultList.get(15),resultList.get(16),resultList.get(17),
                resultList.get(18),resultList.get(19),resultList.get(20),resultList.get(21),resultList.get(22),resultList.get(23),
                resultList.get(24),resultList.get(25),resultList.get(26),resultList.get(27),resultList.get(28),resultList.get(29),
                resultList.get(30),resultList.get(31),resultList.get(32),resultList.get(33));
    }

    @Override
    public DataResponse fangAnSearch(long fid,String cflag, String startPublishedDay, String endPublishedDay,
                                     String fromType, int page, int pageSize, int timeOrder){
        List<Criteria> criterias=fangAnDao.FindCriteriasByFid(fid);
        List<Data> pageDataContent = new ArrayList<>();
        for(Criteria criteria:criterias)
        {
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
                String[] searchSplitArray1 = fromType.trim().split("\\s+");
                List<String>searchSplitArray = Arrays.asList(searchSplitArray1);

                if(searchSplitArray.size()>1){
                    criteria.subCriteria(new Criteria().and("fromType").in(searchSplitArray));
                }else {
                    criteria.subCriteria(new Criteria().and("fromType").is(searchSplitArray.get(0)));
                }
            }
            CriteriaQuery query = new CriteriaQuery(criteria);
            SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);
            for (SearchHit<Data> hit : searchHits.getSearchHits())
            {
                pageDataContent.add(hit.getContent());
            }
        }
        if (timeOrder == 0) {
            Collections.sort(pageDataContent , (Data b1, Data b2) -> b2.getPublishedDay().compareTo(b1.getPublishedDay()));
        }
        else {
            Collections.sort(pageDataContent , (Data b1, Data b2) -> b1.getPublishedDay().compareTo(b2.getPublishedDay()));
        }

        int hitNumber=pageDataContent.size();

        List<Data> resultDataContent = new ArrayList<>();
        if ((page+1)*pageSize>hitNumber)
        {
            resultDataContent=pageDataContent.subList(page*pageSize,hitNumber);
        }else{
            resultDataContent=pageDataContent.subList(page*pageSize,(page+1)*pageSize);
        }

        DataResponse result = new DataResponse();
        result.setHitNumber(hitNumber);
        result.setDataContent(resultDataContent);

        return result;
    }

    @Override
    public DataResponse searchByUser(long fid, String username, int pageSize, int pageId) throws UnsupportedEncodingException {
        String decodeUsername = java.net.URLDecoder.decode(username, "utf-8");
        List<Criteria> criterias=fangAnDao.FindCriteriasByFid(fid);
        List<Data> pageDataContent = new ArrayList<>();
        for(Criteria criteria:criterias)
        {
            if (!username.isEmpty()){
            /*DataResponse result = new DataResponse();
            result.setHitNumber(0);
            result.setDataContent(new ArrayList<>());
            return result;*/
                criteria.subCriteria(new Criteria().and("title").is(decodeUsername));
            }
            String fromType = "3";
            criteria.subCriteria(new Criteria().and("fromType").is(fromType));
            CriteriaQuery query = new CriteriaQuery(criteria);
            SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);
            for (SearchHit<Data> hit : searchHits.getSearchHits())
            {
                pageDataContent.add(hit.getContent());
            }
        }

        Collections.sort(pageDataContent , (Data b1, Data b2) -> b2.getPublishedDay().compareTo(b1.getPublishedDay()));

        int hitNumber=pageDataContent.size();

        List<Data> resultDataContent = new ArrayList<>();
        if ((pageId+1)*pageSize>hitNumber)
        {
            resultDataContent=pageDataContent.subList(pageId*pageSize,hitNumber);
        }else{
            resultDataContent=pageDataContent.subList(pageId*pageSize,(pageId+1)*pageSize);
        }

        DataResponse result = new DataResponse();
        result.setHitNumber(hitNumber);
        result.setDataContent(resultDataContent);

        return result;

    }


    @Override
    public Map<String, Integer> getActivateUser (long fid) {
        //Criteria criteria = fangAnDao.criteriaByFid(fid);
        Map<String, Integer> m = new HashMap<>();
        List<Criteria> criterias=fangAnDao.FindCriteriasByFid(fid);
        for (Criteria criteria:criterias){
            criteria.subCriteria(new Criteria().and("fromType").is("3"));
            CriteriaQuery query = new CriteriaQuery(criteria);
            SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);
            for (SearchHit<Data> hit : searchHits)
            {
                String title = hit.getContent().getTitle();
                Integer num = m.get(title);
                if (num == null) m.put(title, 1);
                else m.put(title, num + 1);
            }
        }
        return m;
    }



    @Override
    public YuQingResponse fangAnSearch2(long fid,String keyword,String sensitiveType,String emotion, String startPublishedDay, String endPublishedDay,
                                     String resource, int page, int pageSize, int timeOrder){
        ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
        query.SetPageParameter(page,pageSize,timeOrder);
        query.JoinFidQueryBuilders(fid);
        if (!keyword.isEmpty())
            query.JoinTitleAndContentQueryBuilders(keyword);
        if (!Objects.equals(startPublishedDay, "null") && !Objects.equals(endPublishedDay, "null") &&
                !startPublishedDay.isEmpty() && !endPublishedDay.isEmpty() )
            query.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
        if (!sensitiveType.isEmpty())
            query.JoinSensitiveTypeQueryBuilders(sensitiveType);
        if (!resource.isEmpty())
            query.JoinResourceQueryBuilders(resource);
        if (!emotion.isEmpty())
            query.JoinEmotionQueryBuilders(emotion);
        query.SortBySensitiveType();
        query.SortBytimeOrder();
        query.SetPageableAndBoolQuery();

        YuQingResponse response = elasticSearchDao.findByQuery(query);
        return response;
    }

    @Override
    public JSONObject addSensitiveWord(String sensitiveWord){
        JSONObject result = new JSONObject();
        result.put("addSensitiveWord", 0);
        if(sensitiveWordRepository.existsByContent(sensitiveWord)){
            result.put("addSensitiveWord", 0);
            result.put("敏感词已存在", 1);
            return result;
        }
        try {
            SensitiveWord sensitiveWord1 = new SensitiveWord(sensitiveWord);
            sensitiveWordRepository.save(sensitiveWord1);
            flag =false;
            flagHanLp = false;
            result.put("addSensitiveWord", 1);
        }catch (Exception e){
            result.put("addSensitiveWord", 0);
        }
        return result;
    }

    @Override
    public JSONObject delSensitiveWord(String sensitiveWord){
        JSONObject result = new JSONObject();
        result.put("delSensitiveWord", 0);
        try {
            sensitiveWordRepository.deleteByContent(sensitiveWord);
            result.put("delSensitiveWord", 1);
            flag = false;
            flagHanLp = false;
            return result;
        }catch (Exception e){
            result.put("delSensitiveWord", 0);
        }
        return result;
    }


    @Override
    public JSONArray sensitiveWordFiltering(String text){
        long start=  System.currentTimeMillis();
        if(false==flag){
            // 初始化敏感词库对象
            SensitiveWordInit sensitiveWordInit = new SensitiveWordInit();
            // 从数据库中获取敏感词对象集合
            List<SensitiveWord> sensitiveWords = sensitiveWordRepository.findAll();
            // 构建敏感词库
            Map sensitiveWordMap = sensitiveWordInit.initKeyWord(sensitiveWords);
            // 传入SensitivewordEngine类中的敏感词库
            SensitivewordEngine.sensitiveWordMap = sensitiveWordMap;
            flag = true;
        }
        // 得到敏感词有哪些，传入2表示获取所有敏感词
        JSONArray result = SensitivewordEngine.getSwAndpos2(text, 2);
        long end = System.currentTimeMillis();
        System.out.println("DFA敏感词提取耗时：" + (end-start) + "ms");
        return result;
    }
    @Override
    public  JSONArray sensitiveWordFilteringHanLp(String text){
        long start=  System.currentTimeMillis();
        if (false == flagHanLp){

            // 从数据库中获取敏感词对象集合
            List<SensitiveWord> sensitiveWords = sensitiveWordRepository.findAll();
            // 构建敏感词库
            Set<String> sensitiveWordSet = new HashSet<>();
            for (SensitiveWord s : sensitiveWords)
            {
                sensitiveWordSet.add(s.getContent().trim());
            }
            SensitiveWordUtil2.init(sensitiveWordSet);
            flagHanLp = true;
        }
        try {
            JSONArray result= SensitiveWordUtil2.getSensitiveWord(text);
            long end = System.currentTimeMillis();
            System.out.println("hanLp敏感词提取耗时：" + (end-start) + "ms");
            return result;
        }catch (Exception e){
            System.out.println(e);
        }
        return null;
    }
    @Override
    public JSONArray sensitiveWord(long fid, String startPublishedDay, String endPublishedDay){
        JSONArray result = new JSONArray();
        if(false==flag){
            // 初始化敏感词库对象
            SensitiveWordInit sensitiveWordInit = new SensitiveWordInit();
            // 从数据库中获取敏感词对象集合
            List<SensitiveWord> sensitiveWords = sensitiveWordRepository.findAll();
            // 构建敏感词库
            Map sensitiveWordMap = sensitiveWordInit.initKeyWord(sensitiveWords);
            // 传入SensitivewordEngine类中的敏感词库
            SensitivewordEngine.sensitiveWordMap = sensitiveWordMap;
            flag = true;
        }
        long start=  System.currentTimeMillis();
        //Criteria criteria = fangAnDao.criteriaByFid(fid);
        List<Criteria> criterias=fangAnDao.FindCriteriasByFid(fid);
        for (Criteria criteria:criterias)
        {
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

            CriteriaQuery query = new CriteriaQuery(criteria);
            SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);
            for(SearchHit<Data> hit : searchHits){
                Data data = hit.getContent();
                JSONArray r1 = sensitiveWordFiltering(data.getContent());
                result.add(r1);
                //fileContents.add(data.getContent());
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("敏感词提取耗时：" + (end-start) + "ms");
        return result;
    }

    @Override
    public List<KeywordResponse> extractKeyword(long fid, String startPublishedDay, String endPublishedDay
            , int keywordNumber, String extractMethod){
        List<List<String>> sum=new ArrayList<>();
        ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
        query.JoinFidQueryBuilders(fid);
        query.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
        query.SetBoolQuery();
        YuQingResponse response=elasticSearchDao.findByQuery(query);
        List<YuQing> yuQings=response.getYuQingContent();
        for (YuQing yuQing:yuQings)
        {
            String[] keywordExtractionArray = yuQing.getKeywordExtraction().trim().split("\\,");
            List<String> keywordExtractionsArray = Arrays.asList(keywordExtractionArray);
            sum.add(keywordExtractionsArray);
        }

        Map<String, Integer> wordScore = new HashMap<>();
        for (List<String> singleDocList : sum)
        {
            for (int i=0; i<singleDocList.size(); i++){
                if (!wordScore.containsKey(singleDocList.get(i))){
                    wordScore.put(singleDocList.get(i), 0);
                }
                wordScore.put(singleDocList.get(i),wordScore.get(singleDocList.get(i))+(singleDocList.size()-i));
            }
        }
        List<Map.Entry<String, Integer>> keywordList = new ArrayList<Map.Entry<String, Integer>>(wordScore.entrySet());
        Collections.sort(keywordList, new Comparator<Map.Entry<String, Integer>>()
        {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
            {
                return Integer.compare(0, o1.getValue() - o2.getValue());
            }
        });

        List<KeywordResponse> keywords = new ArrayList<>();
        for(int i=0; i<min(keywordNumber, keywordList.size()); i++)
        {
            String name = keywordList.get(i).getKey().replace(" ", "");
            Integer value = keywordList.get(i).getValue();
            keywords.add(new KeywordResponse(name, value));
        }
        return keywords;
    }

    @Override
    public JSONObject keywordExtractionForSingleText(String title,String content)
    {
        List<String> keywordList = new TextRankKeyword().getKeyword(title,content);
        String str = String.join(",",keywordList);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("words",str);
        return jsonObject;
    }

    @Override
    public  JSONObject autoaddEkeyword(long fid,String text){
        List<String> Ekeyword = new TextRankKeyword().getKeyword("", text);

        JSONObject result = new JSONObject();
        result.put("autoaddEkeyword", 0);
        try {
            FangAn oldFangAn = fangAnDao.findByFid(fid);
            String oldEkeyword = oldFangAn.getEventKeyword();
            for (String s:Ekeyword){
                if (oldEkeyword.indexOf(s)==-1){
                    oldEkeyword=oldEkeyword+" "+s;
                }else {

                }
            }
            oldFangAn.setEventKeyword(oldEkeyword);
            //fangAnDao.deleteByFid(fid);
            fangAnDao.save(oldFangAn);
            result.put("autoaddEkeyword", 1);
            return result;
        }catch (Exception e){
            result.put("autoaddEkeyword", 0);
        }
        return result;
    }
    @Override
    public JSONObject addSensitivewordForFid(long fid,String text){
        JSONObject result = new JSONObject();
        result.put("addSensitivewordForFid", 0);
        try {
            SensitiveWordForFid sensitiveWordForFid = new SensitiveWordForFid(fid,text);
            swordFidRepository.save(sensitiveWordForFid);
            result.put("addSensitivewordForFid", 1);
            return result;
        }catch (Exception e){
            result.put("addSensitivewordForFid", 0);
        }
        return result;
    }
    @Override
    public  JSONArray sensitivewordForFid(long fid){
        JSONArray result = new JSONArray();
        if (swordFidRepository.existsByFid(fid)){
            String sword = swordFidRepository.findByFid(fid).getContents();
            JSONObject so = new JSONObject();
            so.put("sword",sword);
            result.add(so);
        }
        return result;
    }
    @Override
    public JSONArray sensitiveWordByFid(long fid,String text){
        long start=  System.currentTimeMillis();
        if (fid != nowFid){
            // 从数据库中获取敏感词对象集合
            List<SensitiveWord> sensitiveWords = sensitiveWordRepository.findAll();
            // 构建敏感词库
            Set<String> sensitiveWordSet = new HashSet<>();
            for (SensitiveWord s : sensitiveWords)
            {
                sensitiveWordSet.add(s.getContent().trim());
            }
            String swordfid ="";
            String[] swordfidArray = {};
            if (swordFidRepository.existsByFid(fid)){
                swordfid = swordFidRepository.findByFid(fid).getContents();
                 swordfidArray = swordfid.trim().split("\\s+");
            }
            if (swordfidArray.length>0){
                for (String s:swordfidArray){
                    sensitiveWordSet.add(s);
                }
            }

            SensitiveWordUtil2.init(sensitiveWordSet);
            flagHanLp = true;
        }
        try {
            JSONArray result= SensitiveWordUtil2.getSensitiveWord(text);
            long end = System.currentTimeMillis();
            System.out.println("hanLp敏感词提取耗时：" + (end-start) + "ms");
            return result;
        }catch (Exception e){
            System.out.println(e);
        }
        return null;
    }

    @Override
    public JSONArray eventKeyWordByFid(long fid){
        JSONArray ret=new JSONArray();

        FangAn fangAn=fangAnDao.findByFid(fid);
        String eventKeyword = fangAn.getEventKeyword();
        while(eventKeyword.length()>0)
        {
            int tag=eventKeyword.indexOf('+');
            String singleEventKeyword=eventKeyword.substring(0,tag);
            String[] searchSplitArray1 = singleEventKeyword.trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
            for (String str:searchSplitArray)
            {
                JSONObject object = new JSONObject();
                object.put("kw",str);
                ret.appendElement(object);
            }
            eventKeyword=eventKeyword.substring(tag+1);
        }
        return ret;
    }

    @Override
    public HotArticleResponse getHotArticle(int pageId,int pageSize){
        Criteria criteria = new Criteria();
        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(PageRequest.of(pageId, pageSize, Sort.by(Sort.Direction.DESC, "publishedDay")));

        SearchHits<hotArticle> searchHits = this.elasticsearchOperations.search(query, hotArticle.class);
        SearchPage<hotArticle> searchPage = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
        long hitNumber = this.elasticsearchOperations.count(query, hotArticle.class);

        List<hotArticle> pageHotArticleContent = new ArrayList<>();
        for (SearchHit<hotArticle> hit : searchPage.getSearchHits())
        {
            pageHotArticleContent.add(hit.getContent());
        }

        HotArticleResponse result = new HotArticleResponse();
        result.setHitNumber(hitNumber);
        result.setHotArticleContent(pageHotArticleContent);

        return result;
    }

    @Override
    public List<BriefWeiboUser> searchBriefWeiboUser(long fid,String WeiboUserForSearch)
    {
//        这条代码触发从https://s.weibo.com/user?q= 网页实时爬取相关用户
//        List<BriefWeiboUser> currentSearch = weiboSpiderRpc.searchBriefWeiboUser(WeiboUserForSearch);
        weiboSpiderRpc.searchBriefWeiboUser(WeiboUserForSearch);

        Criteria criteria = new Criteria();
        criteria.subCriteria(new Criteria("nickname").in(WeiboUserForSearch).or("uri").in(WeiboUserForSearch));
        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<BriefWeiboUser> searchHits = this.elasticsearchOperations.search(query, BriefWeiboUser.class);

        List<BriefWeiboUser> result = new ArrayList<>();
        for (SearchHit<BriefWeiboUser> hit : searchHits.getSearchHits())
        {
            if (!fangAnWeiboUserDAO.existsByFidAndWeibouserid(fid,hit.getContent().getUri()))
            {
                result.add(hit.getContent());
            }
        }

        return result;
    }

//    @Override
//    public JSONObject getAllSelectWeiboUser() {
//        List<FangAnWeiboUser> fangAnWeiboUserList = fangAnWeiboUserDAO.findAll();
//        JSONArray array = new JSONArray();
//        for (FangAnWeiboUser user : fangAnWeiboUserList)
//        {
//            array.appendElement(user.getWeibouserid());
//        }
//        JSONObject result = new JSONObject();
//        result.put("data", array);
//        return result;
//    }

    @Override
    public JSONObject addWeiboUser(long fid,String Weibouserid,String Weibousernickname){
        JSONObject result = new JSONObject();
        result.put("addweibouser", 0);
        Boolean ifExist = fangAnWeiboUserDAO.existsByFidAndWeibouserid(fid,Weibouserid);
        if(ifExist){
            result.put("addweibouser", 0);
            result.put("该条已存在", 1);
            return result;
        }
        try {

            Date PublishDay=new Date();
            FangAnWeiboUser fangAnWeiboUser = new FangAnWeiboUser(fid, Weibousernickname, Weibouserid, PublishDay);
            fangAnWeiboUserDAO.save(fangAnWeiboUser);

            List<FangAnWeiboUser> fangAnWeiboUserList = fangAnWeiboUserDAO.findAll();
            List<String> array = new ArrayList<>();
            for (FangAnWeiboUser user : fangAnWeiboUserList)
            {
                array.add(user.getWeibouserid());
            }
            weiboSpiderRpc.crawlNewUserids(array);
          
            result.put("addweibouser", 1);
            return result;
        }catch (Exception e){
            if (fangAnWeiboUserDAO.existsByFidAndWeibouserid(fid,Weibouserid))
            {
                result.put("addweibouser", 1);
            }
            else {
                result.put("addweibouser", 0);
            }
            e.printStackTrace();
        }
        return result;
    };

    @Override
    public JSONObject deleteWeiboUser(long fid,String Weibouserid,String Weibousernickname)
    {
        JSONObject result = new JSONObject();
        result.put("deleteWeiboUser", 0);
        try {
            fangAnWeiboUserDAO.deleteByFidAndWeibousernickname(fid,Weibousernickname);
            result.put("deleteWeiboUser", 1);
        }catch (Exception e){
            result.put("deleteWeiboUser", 0);
            e.printStackTrace();
        }
        return result;
    };

    @Override
    public JSONArray getFangAnMonitor(long fid) throws ParseException {
        JSONArray jsonArray=new JSONArray();
        List<FangAnWeiboUser> fangAnWeiboUsers=fangAnWeiboUserDAO.findAllByFid(fid);
        for (FangAnWeiboUser fangAnWeiboUser:fangAnWeiboUsers)
        {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("fid",fangAnWeiboUser.getFid());

            Criteria criteria = new Criteria();
            criteria.subCriteria(new Criteria("userid").contains(fangAnWeiboUser.getWeibouserid()));
            CriteriaQuery query = new CriteriaQuery(criteria);
            SearchHits<WeiboUser> searchHits = this.elasticsearchOperations.search(query, WeiboUser.class);
            for (SearchHit<WeiboUser> hit : searchHits.getSearchHits()) {
                jsonObject.put("userid",hit.getContent().getUserid());
                jsonObject.put("nickname",hit.getContent().getNickname());
                jsonObject.put("user_avatar",hit.getContent().getUser_avatar());
                jsonObject.put("tags",hit.getContent().getTags());
                jsonObject.put("gender",hit.getContent().getGender());
                jsonObject.put("location",hit.getContent().getLocation());
                jsonObject.put("birthday",hit.getContent().getBirthday());
                jsonObject.put("description",hit.getContent().getDescription());
                jsonObject.put("verified_reason",hit.getContent().getVerified_reason());
                jsonObject.put("talent",hit.getContent().getTalent());
                jsonObject.put("education",hit.getContent().getEducation());
                jsonObject.put("work",hit.getContent().getWork());
                jsonObject.put("weibo_num",hit.getContent().getWeibo_num());
                jsonObject.put("following",hit.getContent().getFollowing());
                jsonObject.put("followers",hit.getContent().getFollowers());
            }
            Criteria criteriaForWeibo = new Criteria();
            criteriaForWeibo.subCriteria(new Criteria("userid").contains(fangAnWeiboUser.getWeibouserid()));
            CriteriaQuery queryForWeibo = new CriteriaQuery(criteriaForWeibo);
            query.setPageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publish_time")));
            SearchHits<Weibo> searchHitsForWeibo = this.elasticsearchOperations.search(queryForWeibo, Weibo.class);
            SearchPage<Weibo> searchPageForWeibo = SearchHitSupport.searchPageFor(searchHitsForWeibo, queryForWeibo.getPageable());
            List<Weibo> pageContent = new ArrayList<>();
            for (SearchHit<Weibo> hitForWeibo : searchPageForWeibo.getSearchHits()) {
                pageContent.add(hitForWeibo.getContent());
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (pageContent.size()==0) {
                jsonObject.put("isnew",0);
            }
            else {
                String PublishDayStr=pageContent.get(0).getPublish_time();
                PublishDayStr=PublishDayStr+":00";
                Date PublishDay = sdf.parse(PublishDayStr);
                if (fangAnWeiboUser.getNewweibotime().before(PublishDay)) {
                    jsonObject.put("isnew",1);
                }
                else {
                    jsonObject.put("isnew",0);
                }
            }
            jsonArray.appendElement(jsonObject);
        }
        return jsonArray;
    };

    @Override
    public JSONObject getWeiboByid(long fid,String id) throws ParseException {
        Criteria criteria = new Criteria();
        criteria.subCriteria(new Criteria("userid").contains(id));
        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<WeiboUser> searchHits = this.elasticsearchOperations.search(query, WeiboUser.class);
        long hitNumber = this.elasticsearchOperations.count(query, WeiboUser.class);

        JSONArray WeiboUserContent=new JSONArray();
        for (SearchHit<WeiboUser> hit : searchHits.getSearchHits())
        {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("id",hit.getContent().getUserid());
            jsonObject.put("nickname",hit.getContent().getNickname());
            jsonObject.put("NumOfWeibo",hit.getContent().getWeibo_num());
            jsonObject.put("NumOfFollow",hit.getContent().getFollowing());
            jsonObject.put("NumOfFans",hit.getContent().getFollowers());
            WeiboUserContent.appendElement(jsonObject);
        }

        JSONObject result= (JSONObject) WeiboUserContent.get(0);

        FangAnWeiboUser fangAnWeiboUser=fangAnWeiboUserDAO.findByFidAndWeibouserid(fid,id);
        Criteria criteria1 = new Criteria();
        criteria1.subCriteria(new Criteria("userid").contains(fangAnWeiboUser.getWeibouserid()));
        CriteriaQuery query1 = new CriteriaQuery(criteria);
        query1.setPageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publish_time")));
        SearchHits<Weibo> searchHits1 = this.elasticsearchOperations.search(query, Weibo.class);
        SearchPage<Weibo> searchPage = SearchHitSupport.searchPageFor(searchHits1, query.getPageable());
        List<Weibo> pageContent = new ArrayList<>();
        for (SearchHit<Weibo> hit : searchPage.getSearchHits()) {
            pageContent.add(hit.getContent());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (pageContent.size()==0) {

        }
        else {
            String PublishDayStr=pageContent.get(0).getPublish_time();
            PublishDayStr=PublishDayStr+":00";
            Date PublishDay = sdf.parse(PublishDayStr);
            fangAnWeiboUser.setNewweibotime(PublishDay);
            fangAnWeiboUserDAO.save(fangAnWeiboUser);
        }

        return result;
    };

    @Override
    public JSONArray getWeiboListByid(long fid,String weibouserid) throws ParseException {
        Criteria criteriaForWeiboUser = new Criteria();
        criteriaForWeiboUser.subCriteria(new Criteria("userid").contains(weibouserid));
        CriteriaQuery queryForWeiboUser = new CriteriaQuery(criteriaForWeiboUser);
        SearchHits<WeiboUser> searchHitsForWeiboUser = this.elasticsearchOperations.search(queryForWeiboUser, WeiboUser.class);

        JSONArray Res=new JSONArray();

        String id="";
        String nickname="";
        String user_avatar="";
        for (SearchHit<WeiboUser> hit : searchHitsForWeiboUser.getSearchHits())
        {
            id=hit.getContent().getUserid();
            nickname=hit.getContent().getNickname();
            user_avatar=hit.getContent().getUser_avatar();
        }
        Criteria criteriaForWeibo = new Criteria();
        criteriaForWeibo.subCriteria(new Criteria("userid").contains(weibouserid));
        CriteriaQuery queryForWeibo = new CriteriaQuery(criteriaForWeibo);
        SearchHits<Weibo> searchHitsForWeibo = this.elasticsearchOperations.search(queryForWeibo, Weibo.class);

        for (SearchHit<Weibo> hit : searchHitsForWeibo.getSearchHits())
        {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("id",id);
            jsonObject.put("nickname",nickname);
            jsonObject.put("user_avatar",user_avatar);
            jsonObject.put("retweet_num",hit.getContent().getRetweet_num());
            jsonObject.put("comment_num",hit.getContent().getComment_num());
            jsonObject.put("up_num",hit.getContent().getUp_num());
            jsonObject.put("publish_time",hit.getContent().getPublish_time());
            jsonObject.put("publish_tool",hit.getContent().getPublish_tool());
            jsonObject.put("publish_place",hit.getContent().getPublish_place());
            jsonObject.put("article_url",hit.getContent().getArticle_url());
            jsonObject.put("content",hit.getContent().getContent());
            jsonObject.put("weiboid",hit.getContent().getWeiboid());

            JSONArray picturesArray=new JSONArray();
            String original_pictures=hit.getContent().getOriginal_pictures();
            if (original_pictures.equals("无"))
            {
                jsonObject.put("original_pictures",picturesArray);
            }
            else
            {
                original_pictures=original_pictures+',';
                while(original_pictures.length()>0)
                {
                    int tag=original_pictures.indexOf(',');
                    picturesArray.appendElement(original_pictures.substring(0,tag));
                    original_pictures=original_pictures.substring(tag+1);
                }
                jsonObject.put("original_pictures",picturesArray);
            }

            Res.appendElement(jsonObject);
        }

        FangAnWeiboUser fangAnWeiboUser=fangAnWeiboUserDAO.findByFidAndWeibouserid(fid,weibouserid);
        Criteria criteria1 = new Criteria();
        criteria1.subCriteria(new Criteria("userid").contains(fangAnWeiboUser.getWeibouserid()));
        CriteriaQuery query1 = new CriteriaQuery(criteria1);
        query1.setPageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publish_time")));
        SearchHits<Weibo> searchHits1 = this.elasticsearchOperations.search(query1, Weibo.class);
        SearchPage<Weibo> searchPage = SearchHitSupport.searchPageFor(searchHits1, query1.getPageable());
        List<Weibo> pageContent = new ArrayList<>();
        for (SearchHit<Weibo> hit : searchPage.getSearchHits()) {
            pageContent.add(hit.getContent());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (pageContent.size()==0) {

        }
        else {
            String PublishDayStr=pageContent.get(0).getPublish_time();
            PublishDayStr=PublishDayStr+":00";
            Date PublishDay = sdf.parse(PublishDayStr);
            fangAnWeiboUser.setNewweibotime(PublishDay);
            fangAnWeiboUserDAO.save(fangAnWeiboUser);
        }

        return Res;
    };

    @Override
    public JSONArray getOverallDatOnNetwork(String keyword,Integer pageId) throws MalformedURLException, InterruptedException {
        JSONArray jsonArray=new JSONArray();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        options.addArguments("--window-size=4000,1600");
        options.addArguments("User-Agent=Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE");
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        WebDriver webDriver = new RemoteWebDriver(new URL("http://192.168.0.3:4444/wd/hub"), capabilities);
        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        JSONArray Arraybaidu=new JSONArray();
        webDriver.get("https://www.baidu.com/s?wd=java&pn=10");
        Thread.sleep(1000);

        webDriver.findElements(By.className("t")).forEach(x -> {
            JSONObject jsonObject =new JSONObject();
            jsonObject.put("title",x.getText());
            jsonObject.put("url",x.findElement(By.xpath(".//a")).getAttribute("href"));
            Arraybaidu.appendElement(jsonObject);
            //System.out.println(x.getText());
            //System.out.println(x.findElement(By.xpath(".//a")).getAttribute("href"));
        });

        JSONArray Array360=new JSONArray();
        webDriver.get("https://www.so.com/s?q=java&pn=2");
        Thread.sleep(1000);

        webDriver.findElements(By.className("res-title")).forEach(x -> {
            JSONObject jsonObject =new JSONObject();
            jsonObject.put("title",x.getText());
            jsonObject.put("url",x.findElement(By.xpath(".//a")).getAttribute("href"));
            Array360.appendElement(jsonObject);
            //System.out.println(x.getText());
            //System.out.println(x.findElement(By.xpath(".//a")).getAttribute("href"));
        });

        JSONArray Arraybing=new JSONArray();
        webDriver.get("https://cn.bing.com/search?q=java&first=9");
        Thread.sleep(1000);

        webDriver.findElements(By.className("b_algo")).forEach(x -> {
            JSONObject jsonObject =new JSONObject();
            jsonObject.put("title",x.findElement(By.xpath(".//h2")).getText());
            jsonObject.put("url",x.findElement(By.xpath(".//h2")).findElement(By.xpath(".//a")).getAttribute("href"));
            Arraybing.appendElement(jsonObject);
            //System.out.println(x.findElement(By.xpath(".//h2")).getText());
            //System.out.println(x.findElement(By.xpath(".//h2")).findElement(By.xpath(".//a")).getAttribute("href"));
        });

        webDriver.quit();

        jsonArray.appendElement(Arraybaidu);
        jsonArray.appendElement(Array360);
        jsonArray.appendElement(Arraybing);
        return jsonArray;
    }

    @Override
    public JSONArray getOverallDataBaidu(String keyword,Integer pageId) throws MalformedURLException, InterruptedException
    {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        options.addArguments("--window-size=4000,1600");
        options.addArguments("User-Agent=Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE");
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        WebDriver webDriver = new RemoteWebDriver(new URL("http://192.168.0.3:4444/wd/hub"), capabilities);
        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        JSONArray Arraybaidu=new JSONArray();
        String url="https://www.baidu.com/s?wd="+keyword+"&pn="+String.valueOf(pageId*10);
        //"https://www.baidu.com/s?wd=java&pn=10"
        webDriver.get(url);
        Thread.sleep(1000);

        webDriver.findElements(By.className("t")).forEach(x -> {
            JSONObject jsonObject =new JSONObject();
            jsonObject.put("title",x.getText());
            jsonObject.put("url",x.findElement(By.xpath("./a")).getAttribute("href"));
            Arraybaidu.appendElement(jsonObject);
        });
        webDriver.quit();
        return Arraybaidu;
    };

    @Override
    public JSONArray getOverallData360(String keyword,Integer pageId) throws MalformedURLException, InterruptedException
    {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        options.addArguments("--window-size=4000,1600");
        options.addArguments("User-Agent=Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE");
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        WebDriver webDriver = new RemoteWebDriver(new URL("http://192.168.0.3:4444/wd/hub"), capabilities);
        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        JSONArray Array360=new JSONArray();
        String url="https://www.so.com/s?q="+keyword+"&pn="+String.valueOf(pageId+1);
        //"https://www.so.com/s?q=java&pn=2"
        webDriver.get(url);
        Thread.sleep(1000);

        //*[@id="main"]/ul/li/h3/a
        webDriver.findElements(By.xpath("//*[@id=\"main\"]/ul/li/h3/a")).forEach(x -> {
            JSONObject jsonObject =new JSONObject();
            jsonObject.put("title",x.getText());
            jsonObject.put("url",x.getAttribute("href"));
            Array360.appendElement(jsonObject);
        });
        webDriver.quit();
        return Array360;
    };

    @Override
    public JSONArray getOverallDataBing(String keyword,Integer pageId) throws MalformedURLException, InterruptedException
    {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        options.addArguments("--window-size=4000,1600");
        options.addArguments("User-Agent=Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE");
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        WebDriver webDriver = new RemoteWebDriver(new URL("http://192.168.0.3:4444/wd/hub"), capabilities);
        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        JSONArray Arraybing=new JSONArray();
        String url="https://cn.bing.com/search?q="+keyword+"&first="+String.valueOf(pageId*10+1);
        webDriver.get(url);
        Thread.sleep(1000);
        WebElement webElement= webDriver.findElement(By.id("b_results"));
        //*[@id="b_results"]/li/h2/a
        for (WebElement x : webDriver.findElements(By.xpath(" //*[@id=\"b_results\"]/li/h2/a"))) {
            JSONObject jsonObject = new JSONObject();

                jsonObject.put("title", x.getText());
                jsonObject.put("url", x.getAttribute("href"));
                Arraybing.appendElement(jsonObject);

        }
        //h3/a
        webDriver.quit();
        return Arraybing;
    };

    @Override
    public List<FangAnTemplate> getBriefingTemplate(long fid)
    {
        return fangAnTemplateDAO.findAllByFid(fid);
    }

    @Override
    public JSONObject saveBriefingTemplate(int id,long fid,String decodeTitle,String decodeVersion,String decodeInstitution,String time,String keylist,String text) throws ParseException {
        JSONObject result = new JSONObject();
        result.put("savebriefingtemplate", 0);
        Boolean ifExist = fangAnTemplateDAO.existsById(id);
        if(ifExist){
            try {
                FangAnTemplate fangAnTemplate = fangAnTemplateDAO.findById(id);
                fangAnTemplate.setFid(fid);
                fangAnTemplate.setTitle(decodeTitle);
                fangAnTemplate.setVersion(decodeVersion);
                fangAnTemplate.setInstitution(decodeInstitution);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date dateTime = sdf.parse(time);
                fangAnTemplate.setTime(dateTime);
                fangAnTemplate.setKeylist(keylist);
                fangAnTemplate.setText(text);
                fangAnTemplateDAO.save(fangAnTemplate);
                result.put("savebriefingtemplate", 1);
                return result;
            }
            catch (Exception e) {
                if (fangAnTemplateDAO.existsById(id)) {
                    result.put("savebriefingtemplate", 1);
                }
                else {
                    result.put("savebriefingtemplate", 0);
                }
                e.printStackTrace();
            }
        }
        else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date dateTime = sdf.parse(time);
                FangAnTemplate fangAnTemplate=new FangAnTemplate(fid,decodeTitle,decodeVersion,decodeInstitution,dateTime,keylist,text);
                fangAnTemplateDAO.save(fangAnTemplate);
                result.put("savebriefingtemplate", 1);
                return result;
            }
            catch (Exception e) {
                if (fangAnTemplateDAO.existsById(id)) {
                    result.put("savebriefingtemplate", 1);
                }
                else {
                    result.put("savebriefingtemplate", 0);
                }
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public JSONObject deleteBriefingTemplate(int id)
    {
        JSONObject result = new JSONObject();
        result.put("deletebriefingtemplate", 0);
        try {
            fangAnTemplateDAO.deleteById(id);
            result.put("deletebriefingtemplate", 1);
        }catch (Exception e){
            result.put("deletebriefingtemplate", 0);
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public JSONArray getMaterial(long fid)
    {
        JSONArray jsonArray=new JSONArray();
        List<FangAnMaterial> fangAnMaterialList=fangAnMaterialDAO.findAllByFid(fid);
        for(FangAnMaterial fangAnMaterial:fangAnMaterialList)
        {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("materiallib",fangAnMaterial.getMateriallib());
            String ids=fangAnMaterial.getIds();
            if (ids.length()==0)
            {
                jsonObject.put("num",0);
            }
            else {
                String[] idarray = ids.trim().split("\\,");
                List<String> idArray = Arrays.asList(idarray);
                jsonObject.put("num",idArray.size());
            }
            jsonArray.appendElement(jsonObject);
        }
        return jsonArray;
    }

    @Override
    public DataResponse getMaterialDetail(long fid,String materiallib)
    {
        if (fangAnMaterialDAO.existsByFidAndMateriallib(fid,materiallib))
        {
            FangAnMaterial fangAnMaterial=fangAnMaterialDAO.findByFidAndMateriallib(fid,materiallib);
            String ids=fangAnMaterial.getIds();
            String[] idarray = ids.trim().split("\\,");
            List<String>searchSplitArray = Arrays.asList(idarray);
            Criteria criteria = new Criteria();
            criteria.subCriteria(new Criteria("_id").in(searchSplitArray));
            CriteriaQuery query = new CriteriaQuery(criteria);
            SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);
            long hitNumber = this.elasticsearchOperations.count(query, Data.class);

            List<Data> pageDataContent = new ArrayList<>();
            for (SearchHit<Data> hit : searchHits.getSearchHits())
            {
                pageDataContent.add(hit.getContent());
            }

            DataResponse result = new DataResponse();
            result.setHitNumber(hitNumber);
            result.setDataContent(pageDataContent);

            return result;
        }else {
            return new DataResponse();
        }
    }

    @Override
    public JSONObject addNewMaterialLib(long fid,String decodemateriallib)
    {
        JSONObject ret=new JSONObject();
        if (fangAnMaterialDAO.existsByFidAndMateriallib(fid,decodemateriallib))
        {
            ret.put("isexisted",1);
            ret.put("addNewMaterialLib",0);
        }else
        {
            try {
                FangAnMaterial fangAnMaterial=new FangAnMaterial(fid,decodemateriallib,"");
                fangAnMaterialDAO.save(fangAnMaterial);
                ret.put("addNewMaterialLib",1);
            }
            catch (Exception e) {
                if (fangAnMaterialDAO.existsByFidAndMateriallib(fid,decodemateriallib)) {
                    ret.put("addNewMaterialLib", 1);
                }
                else {
                    ret.put("addNewMaterialLib", 0);
                }
                e.printStackTrace();
            }
        }
        return ret;
    }

    @Override
    public JSONObject renameMaterial(long fid,String decodeoldname,String decodenewname)
    {
        JSONObject ret=new JSONObject();

        try {
            FangAnMaterial fangAnMaterial = fangAnMaterialDAO.findByFidAndMateriallib(fid, decodeoldname);
            fangAnMaterial.setMateriallib(decodenewname);
            fangAnMaterialDAO.save(fangAnMaterial);
            ret.put("renameMaterial", 1);
        } catch (Exception e) {
            if (fangAnMaterialDAO.existsByFidAndMateriallib(fid, decodenewname)) {
                ret.put("renameMaterial", 1);
            } else {
                ret.put("renameMaterial", 0);
            }
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public JSONObject deleteMaterial(long fid,String decodemateriallib)
    {
        JSONObject ret=new JSONObject();

        try {
            fangAnMaterialDAO.deleteByFidAndMateriallib(fid,decodemateriallib);
            ret.put("deleteMaterial", 1);
        } catch (Exception e) {
            if (fangAnMaterialDAO.existsByFidAndMateriallib(fid, decodemateriallib)) {
                ret.put("deleteMaterial", 0);
            } else {
                ret.put("deleteMaterial", 1);
            }
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public JSONObject deleteMaterialIDs(long fid,String decodemateriallib,String decodeIds)
    {
        JSONObject ret=new JSONObject();

        try {
            FangAnMaterial fangAnMaterial= fangAnMaterialDAO.findByFidAndMateriallib(fid,decodemateriallib);
            String nowids=fangAnMaterial.getIds();
            String[] nowidarray = nowids.trim().split("\\,");
            List<String> nowIdarray = new ArrayList<>(Arrays.asList(nowidarray));

            String[] deleteIds = decodeIds.trim().split("\\,");
            List<String> DeleteIds = new ArrayList<>(Arrays.asList(deleteIds));
            for (String deleteid:DeleteIds)
            {
                int index=nowIdarray.indexOf(deleteid);
                if (index!=-1)
                {
                    nowIdarray.remove(index);
                }
            }
            StringBuilder stringBuilder= new StringBuilder();
            for (String id:nowIdarray)
            {
                if (!id.equals("") && !id.equals(" "))
                {
                    stringBuilder.append(id).append(",");
                }
            }
            String ids= stringBuilder.toString();
            if (!ids.equals(""))
            {
                ids=ids.substring(0,ids.length()-1);
            }
            fangAnMaterial.setIds(ids);
            fangAnMaterialDAO.save(fangAnMaterial);

            ret.put("deleteMaterialIDs", 1);
        } catch (Exception e) {
            ret.put("deleteMaterialIDs", 0);
            e.printStackTrace();
        }

        return ret;
    }

    @Override
    public JSONObject modeifyMaterial(long fid,String materiallib,String decodeIds)
    {
        JSONObject result = new JSONObject();
        result.put("modeifyMaterial", 0);
        Boolean ifExist = fangAnMaterialDAO.existsByFidAndMateriallib(fid,materiallib);
        if(ifExist){
            try {
                FangAnMaterial fangAnMaterial = fangAnMaterialDAO.findByFidAndMateriallib(fid,materiallib);
                fangAnMaterial.setMateriallib(materiallib);

                String nowids=fangAnMaterial.getIds();
                String[] nowidarray = nowids.trim().split("\\,");
                List<String> nowIdarray = new ArrayList<>(Arrays.asList(nowidarray));

                String[] newIds = decodeIds.trim().split("\\,");
                List<String> NewIds = new ArrayList<>(Arrays.asList(newIds));
                for (String newid:NewIds)
                {
                    int index=nowIdarray.indexOf(newid);
                    if (index==-1)
                    {
                        nowIdarray.add(newid);
                    }
                }

                StringBuilder stringBuilder= new StringBuilder();
                for (String id:nowIdarray)
                {
                    if (!id.equals("") && !id.equals(" "))
                    {
                        stringBuilder.append(id).append(",");
                    }
                }
                String ids= stringBuilder.toString();
                if (!ids.equals(""))
                {
                    ids=ids.substring(0,ids.length()-1);
                }

                fangAnMaterial.setIds(ids);
                fangAnMaterialDAO.save(fangAnMaterial);
                result.put("modeifyMaterial", 1);
                return result;
            }
            catch (Exception e) {
                if (fangAnMaterialDAO.existsByFid(fid)) {
                    result.put("modeifyMaterial", 1);
                }
                else {
                    result.put("modeifyMaterial", 0);
                }
                e.printStackTrace();
            }
        }
        else {
            try {
                FangAnMaterial fangAnMaterial=new FangAnMaterial(fid,materiallib,decodeIds);
                fangAnMaterialDAO.save(fangAnMaterial);
                result.put("modeifyMaterial", 1);
                return result;
            }
            catch (Exception e) {
                if (fangAnMaterialDAO.existsByFid(fid)) {
                    result.put("modeifyMaterial", 1);
                }
                else {
                    result.put("modeifyMaterial", 0);
                }
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public JSONObject generate() throws IOException, com.lowagie.text.DocumentException {
        JSONObject ret= new JSONObject();
        String rnd = DigestUtils.sha1Hex(new Date().toString());
        String wordOutFilePath = String.format("%s%s-%s.doc", "/home/pubsys/jar_dir/fileTemp/" , "word", rnd);
        String pdfOutFilePath = String.format("%s%s-%s.pdf", "/home/pubsys/jar_dir/fileTemp/" , "pdf", rnd);
        String excelOutFilePath = String.format("%s%s-%s.xls", "/home/pubsys/jar_dir/fileTemp/" , "excel", rnd);

        Configuration configuration = new Configuration();
        /* 设置编码 */
        configuration.setDefaultEncoding("utf-8");
        String fileDirectory = "/home/pubsys/jar_dir/fileTemplate/";
        Template template = null;
        try {
            /* 加载文件 */
            configuration.setDirectoryForTemplateLoading(new File(fileDirectory));
            /* 加载模板 */
            template = configuration.getTemplate("1.ftl");
        } catch (IOException ex) {
            System.out.println("找不到模板文件，路径为:") ;
            System.out.println(fileDirectory);
        }

        /* 组装数据 */
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("NO","129");
        dataMap.put("department","舆情部门");
        dataMap.put("reporter","孙良辰");
        dataMap.put("phone","18769007910");
        dataMap.put("sender","舆情部门");
        dataMap.put("type","文件");
        dataMap.put("year","2021");
        dataMap.put("month","10");
        dataMap.put("day","18");
        dataMap.put("writer","孙良辰");
        dataMap.put("writerPos","学生");
        dataMap.put("approver","黄思诚");
        dataMap.put("approverPos","学生");
        dataMap.put("sum","啥都没有");

        /* 按照模板生成doc和html数据 */
        File docFile = new File(wordOutFilePath);
        FileOutputStream fos;
        Writer outDoc = null;
        Writer outPdf = new StringWriter();
        String content = null;
        try {
            fos = new FileOutputStream(docFile);
            outDoc = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"),10240);
            template.process(dataMap,outDoc);

            template.process(dataMap, outPdf); //将合并后的数据和模板写入到流中，这里使用的字符流
            outPdf.flush();
            content=outPdf.toString();
        } catch (FileNotFoundException ex) {
            System.out.println("找不到输出文件路径，路径为：{}");
        } catch (TemplateException | IOException ex) {
            System.out.println("找不到输出文件路径，路径为：{}");
        } finally {
            if(outPdf != null){
                try {
                    outPdf.close();
                } catch (IOException ex) {
                    System.out.println("找不到输出文件路径，路径为：{}");
                }
            }
        }

        /* 解析html数据生成pdf */
        String FONT = "/home/pubsys/jar_dir/font/SimHei.ttf";

        ITextRenderer render = new ITextRenderer();
        ITextFontResolver fontResolver = render.getFontResolver();
        fontResolver.addFont(FONT, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        // 解析html生成pdf
        render.setDocumentFromString(content);
        //解决图片相对路径的问题
        render.layout();
        render.createPDF(new FileOutputStream(pdfOutFilePath));

        return ret;
    }

    @Override
    public JSONObject generateFile(int fileID,long fid,int templateId,String decodeTitle,String decodeInstitution,String decodeYuQingIds,String echartsData) throws TemplateException, IOException, ParseException, DocumentException, com.lowagie.text.DocumentException {
        JSONObject ret=new JSONObject();
        ret.put("generateFile",1);
        String rnd = DigestUtils.sha1Hex(new Date().toString());
        String wordOutFilePath = String.format("%s%s-%s.doc", "/home/pubsys/jar_dir/fileTemp/" , "word", rnd);
        String pdfOutFilePath = String.format("%s%s-%s.pdf", "/home/pubsys/jar_dir/fileTemp/" , "pdf", rnd);
        String excelOutFilePath = String.format("%s%s-%s.xls", "/home/pubsys/jar_dir/fileTemp/" , "excel", rnd);

        Configuration configuration = new Configuration();
        /* 设置编码 */
        configuration.setDefaultEncoding("utf-8");
        String fileDirectory = "/home/pubsys/jar_dir/fileTemplate/";
        Template template = null;
        try {
            /* 加载文件 */
            configuration.setDirectoryForTemplateLoading(new File(fileDirectory));
            /* 加载模板 */
            template = configuration.getTemplate("pdf.ftl");
        } catch (IOException ex) {
            System.out.println("找不到模板文件，路径为:") ;
            System.out.println(fileDirectory);
        }

        /* 组装数据 */
        Map<String,Object> dataMap = new HashMap<>();

        // 标题数据
        FangAnTemplate fangAnTemplate=fangAnTemplateDAO.findById(templateId);
        dataMap.put("title", decodeTitle);
        dataMap.put("version", fangAnTemplate.getVersion());
        dataMap.put("institution", decodeInstitution);
        String strDateFormat = "yyyy-MM-dd HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        dataMap.put("time", sdf.format(fangAnTemplate.getTime()));

        // 维度数据
        String keylistString=fangAnTemplate.getKeylist();
        String[] keylistStrings = keylistString.trim().split("\\,");
        List<String> keylist = new ArrayList<>(Arrays.asList(keylistStrings));
        List<Dimension> dimensions=dimensionDao.findAllByKeyIn(keylist);
        dataMap.put("dimensions",dimensions);

        // echarts图片数据
        com.alibaba.fastjson.JSONArray echarts = com.alibaba.fastjson.JSONArray.parseArray(echartsData);
        dataMap.put("echarts",echarts);

        // 舆情信息数据
        String[] idarray = decodeYuQingIds.trim().split("\\,");
        List<String>searchSplitArray = Arrays.asList(idarray);
        Criteria criteria = new Criteria();
        criteria.subCriteria(new Criteria("_id").in(searchSplitArray));
        CriteriaQuery query = new CriteriaQuery(criteria);
        SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);
        List<Data> pageDataContent = new ArrayList<>();
        for (SearchHit<Data> hit : searchHits.getSearchHits())
        {
            pageDataContent.add(hit.getContent());
        }
        dataMap.put("data",pageDataContent);


        /* 生成excel */
        Field[] declaredFields = Data.class.getDeclaredFields();
        String[] fieldNames = new String[declaredFields.length];
        for (int i = 0; i < declaredFields.length; i++) {
            fieldNames[i] = declaredFields[i].getName(); //通过反射获取属性名
        }

        String[] headerCode={"标题","内容","网址","敏感类型","分类","情感","发布日期"};
        String[] header = {"title","content", "webpageUrl", "sensitiveType", "tag", "emotion", "publishedDay" };

        Workbook wb = new HSSFWorkbook();
        int rowSize = 0;
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(rowSize);
        for (int i = 0; i < headerCode.length; i++) {
            row.createCell(i).setCellValue(headerCode[i]);
        }

        try {
            for (int x = 0; x < pageDataContent.size(); x++) {
                rowSize = 1;
                Row rowNew = sheet.createRow(rowSize + x);
                for (int i = 0; i < header.length; i++) {
                    Data data = pageDataContent.get(x);
                    for (String fieldName : fieldNames) {
                        if (header[i].equals(fieldName)) {
                            String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);//获取属性的get方法名
                            Method method = data.getClass().getMethod(methodName);
                            Object invoke = method.invoke(data);//获取属性值
                            if (invoke==null)
                            {
                                rowNew.createCell(i).setCellValue("");
                            }
                            else {
                                if (fieldName.equals("emotion"))
                                {
                                    rowNew.createCell(i).setCellValue(EmotionToChinese(invoke.toString()));
                                }
                                else {
                                    rowNew.createCell(i).setCellValue(invoke.toString());
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(excelOutFilePath);
            wb.write(outputStream);
        } catch (Exception e) {

        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (Exception e) {

            }
            try {
                if (wb != null) {
                    wb.close();
                }
            } catch (Exception e) {

            }
        }

        /* 按照模板生成doc和html数据 */
        File docFile = new File(wordOutFilePath);
        FileOutputStream fos;
        Writer outDoc = null;
        Writer outPdf = new StringWriter();
        String content = null;
        try {
            fos = new FileOutputStream(docFile);
            outDoc = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"),10240);
            template.process(dataMap,outDoc);
            
            template.process(dataMap, outPdf); //将合并后的数据和模板写入到流中，这里使用的字符流
            outPdf.flush();
            content=outPdf.toString();
        } catch (FileNotFoundException ex) {
            System.out.println("找不到输出文件路径，路径为：{}");
        } catch (TemplateException | IOException ex) {
            System.out.println("找不到输出文件路径，路径为：{}");
        } finally {
            if(outPdf != null){
                try {
                    outPdf.close();
                } catch (IOException ex) {
                    System.out.println("找不到输出文件路径，路径为：{}");
                }
            }
        }

        /* 解析html数据生成pdf */
        String FONT = "/home/pubsys/jar_dir/font/SimHei.ttf";

        ITextRenderer render = new ITextRenderer();
        ITextFontResolver fontResolver = render.getFontResolver();
        fontResolver.addFont(FONT, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        // 解析html生成pdf
        render.setDocumentFromString(content);
        //解决图片相对路径的问题
        render.layout();
        render.createPDF(new FileOutputStream(pdfOutFilePath));

        /* 将生成的文件存到mysql中 */
        File pdffile = new File(pdfOutFilePath);
        InputStream pdffileInputStream = new FileInputStream(pdffile);
        byte[] pdfByteArray = new byte[pdffileInputStream.available()];
        pdffileInputStream.read(pdfByteArray);

        File wordfile = new File(wordOutFilePath);
        InputStream wordFileInputStream = new FileInputStream(wordfile);
        byte[] wordByteArray = new byte[wordFileInputStream.available()];
        wordFileInputStream.read(wordByteArray);

        File excelfile = new File(excelOutFilePath);
        InputStream excelFileInputStream = new FileInputStream(excelfile);
        byte[] excelByteArray = new byte[excelFileInputStream.available()];
        excelFileInputStream.read(excelByteArray);

        try {
            BriefingFile briefingFile = briefingFileDao.findById(fileID);
            briefingFile.setPercent(100);
            briefingFile.setPdf(pdfByteArray);
            briefingFile.setExcel(excelByteArray);
            briefingFile.setWord(wordByteArray);
            briefingFileDao.save(briefingFile);
            ret.put("generateFile",1);
        }catch (Exception e){
            ret.put("generateFile",0);
        }

        return ret;
    }

    @Override
    public JSONArray getBriefingFiles(long fid)
    {
        JSONArray ret =new JSONArray();
        List<BriefingFile> briefingFiles=briefingFileDao.findAllByFidOrderByGeneratetimeDesc(fid);
        String strDateFormat = "yyyy-MM-dd HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        for(BriefingFile briefingFile:briefingFiles)
        {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("id",briefingFile.getId());
            jsonObject.put("briefingName",briefingFile.getName());
            jsonObject.put("briefingTime",sdf.format(briefingFile.getGeneratetime()));
            if (briefingFile.getPercent()!=100)
            {
                jsonObject.put("percent",briefingFile.getPercent());
            }
            ret.appendElement(jsonObject);
        }
        return ret;
    }

    @Override
    public JSONObject addNewBriefingFileRecord(long fid, String title)
    {
        JSONObject ret=new JSONObject();
        try {
            BriefingFile briefingFile=new BriefingFile(fid,title,new Date(),null,null,null,10);
            briefingFileDao.save(briefingFile);
            ret.put("addNewBriefingFileRecord",1);
            ret.put("fileId",briefingFile.getId());
        }
        catch (Exception e) {
            ret.put("addNewBriefingFileRecord", 0);
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public JSONObject updateBriefingFileProgess(int id,int percent)
    {
        JSONObject ret=new JSONObject();
        BriefingFile briefingFile=briefingFileDao.findById(id);
        briefingFile.setPercent(briefingFile.getPercent()+percent);
        try {
            briefingFileDao.save(briefingFile);
            ret.put("updateBriefingFileProgess",1);
        }
        catch (Exception e) {
            ret.put("updateBriefingFileProgess", 0);
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public JSONObject deleteBriefingFiles(int id)
    {
        JSONObject ret=new JSONObject();
        try {
            briefingFileDao.deleteById(id);
            ret.put("deleteBriefingFiles", 1);
        } catch (Exception e) {
            if (briefingFileDao.existsById(id)) {
                ret.put("deleteBriefingFiles", 0);
            } else {
                ret.put("deleteBriefingFiles", 1);
            }
            e.printStackTrace();
        }

        return ret;

    }

    @Override
    public void downloadBriefingFiles(int id, String type, HttpServletRequest request, HttpServletResponse response) throws IOException {
        BriefingFile briefingFile=briefingFileDao.findById(id);
        byte[] retFile = new byte[0];
        String storeName = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (type.equals("pdf"))
        {
            retFile=briefingFile.getPdf();
            storeName=briefingFile.getName()+"_"+sdf.format(briefingFile.getGeneratetime())+"_pdf.pdf";
        }
        if (type.equals("word"))
        {
            retFile=briefingFile.getWord();
            storeName=briefingFile.getName()+"_"+sdf.format(briefingFile.getGeneratetime())+"_doc.doc";
        }
        if (type.equals("excel"))
        {
            retFile=briefingFile.getExcel();
            storeName=briefingFile.getName()+"_"+sdf.format(briefingFile.getGeneratetime())+"_excel.xls";
        }
        long length=retFile.length;

        ByteArrayInputStream ret=new ByteArrayInputStream(retFile);

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment; filename="
                + new String(storeName.getBytes("utf-8"), "ISO8859-1"));
        response.setHeader("Content-Length", String.valueOf(length));
        bis = new BufferedInputStream(ret);
        bos = new BufferedOutputStream(response.getOutputStream());
        byte[] buff = new byte[1024];
        int bytesRead;
        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
        }
        bis.close();
        bos.close();
    }

    @Override
    public JSONArray getSensitiveWordTypes()
    {
         List<SensitiveWords> sensitiveWordsList= sensitiveWordsDao.findAll();
         List<String> strings=new ArrayList<>();
         for (SensitiveWords sensitiveWords:sensitiveWordsList)
         {
             strings.add(sensitiveWords.getType());
         }
         LinkedHashSet set=new LinkedHashSet(strings);
         List<String> stringsWithOutDuplicates =new ArrayList<>(set);
         JSONArray jsonArray=new JSONArray();
         for (String s:stringsWithOutDuplicates)
         {
             JSONObject jsonObject=new JSONObject();
             jsonObject.put("type",s);
             jsonArray.appendElement(jsonObject);
         }
         return jsonArray;
    }

    @Override
    public List<SensitiveWords> getSensitiveWords(String type)
    {
        return sensitiveWordsDao.findAllByType(type);
    }

    @Override
    public JSONObject deleteSensitiveWords(String type,String words)
    {
        JSONObject ret=new JSONObject();

        String[] deleteWords = words.trim().split("\\,");
        List<String> DeleteWords = new ArrayList<>(Arrays.asList(deleteWords));
        for (String s : DeleteWords) {
            try{
                sensitiveWordsDao.deleteByTypeAndWord(type,s);
            }
            catch (Exception e) {
                ret.put("deleteSensitiveWords", 0);
                e.printStackTrace();
                return ret;
            }
        }
        ret.put("deleteSensitiveWords", 1);
        return ret;
    }

    @Override
    public JSONObject addSensitiveWordForAll(String type,String word)
    {
        JSONObject ret=new JSONObject();
        if (sensitiveWordsDao.existsByTypeAndWord(type,word))
        {
            ret.put("isexisted",1);
            ret.put("addSensitiveWordForAll",0);
        }else
        {
            try {
                SensitiveWords sensitiveWords=new SensitiveWords(type,word);
                sensitiveWordsDao.save(sensitiveWords);
                ret.put("addSensitiveWordForAll",1);
            }
            catch (Exception e) {
                if (sensitiveWordsDao.existsByTypeAndWord(type,word)) {
                    ret.put("addSensitiveWordForAll", 1);
                }
                else {
                    ret.put("addSensitiveWordForAll", 0);
                }
                e.printStackTrace();
            }
        }
        return ret;
    }

    @Override
    public JSONArray getSensitiveWordsByFid(long fid)
    {
        JSONArray ret = new JSONArray();

        FangAn fangAn = fangAnDao.findByFid(fid);
        String sensitiveWords = fangAn.getSensitiveword();
        String[] searchSplitArray1 = sensitiveWords.trim().split("\\s+");
        List<String> searchSplitArray = Arrays.asList(searchSplitArray1);
        for (String str : searchSplitArray) {
            JSONObject object = new JSONObject();
            object.put("sw", str);
            ret.appendElement(object);
        }

        return ret;
    }
}