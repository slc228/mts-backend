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
import org.apache.pdfbox.pdmodel.PDDocument;
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
            fangAnDao.UpdateFangan(oldFangAn.getFid(),oldFangAn.getUsername(),oldFangAn.getProgrammeName(),oldFangAn.getMatchType(),
                                    oldFangAn.getRegionKeyword(),oldFangAn.getRegionKeywordMatch(),oldFangAn.getRoleKeyword(),oldFangAn.getEventKeywordMatch(),
                                    oldFangAn.getEventKeyword(),oldFangAn.getEventKeywordMatch(),oldFangAn.getEnableAlert(),
                                    oldFangAn.getSensitiveword(),oldFangAn.getPriority());
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
//        weiboSpiderRpc.searchBriefWeiboUser(WeiboUserForSearch);

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

//        FangAnWeiboUser fangAnWeiboUser=fangAnWeiboUserDAO.findByFidAndWeibouserid(fid,weibouserid);
//        Criteria criteria1 = new Criteria();
//        criteria1.subCriteria(new Criteria("userid").contains(fangAnWeiboUser.getWeibouserid()));
//        CriteriaQuery query1 = new CriteriaQuery(criteria1);
//        query1.setPageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publish_time")));
//        SearchHits<Weibo> searchHits1 = this.elasticsearchOperations.search(query1, Weibo.class);
//        SearchPage<Weibo> searchPage = SearchHitSupport.searchPageFor(searchHits1, query1.getPageable());
//        List<Weibo> pageContent = new ArrayList<>();
//        for (SearchHit<Weibo> hit : searchPage.getSearchHits()) {
//            pageContent.add(hit.getContent());
//        }
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        if (pageContent.size()==0) {
//
//        }
//        else {
//            String PublishDayStr=pageContent.get(0).getPublish_time();
//            PublishDayStr=PublishDayStr+":00";
//            Date PublishDay = sdf.parse(PublishDayStr);
//            fangAnWeiboUser.setNewweibotime(PublishDay);
//            fangAnWeiboUserDAO.save(fangAnWeiboUser);
//        }

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
                fangAnTemplateDAO.UpdateFanganTemplate(id,fid,decodeTitle,decodeVersion,decodeInstitution,dateTime,keylist,text);
//                fangAnTemplateDAO.save(fangAnTemplate);
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
                fangAnTemplateDAO.InsertFanganTemplate(fid,decodeTitle,decodeVersion,decodeInstitution,dateTime,keylist,text);
//                fangAnTemplateDAO.save(fangAnTemplate);
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
    public YuQingResponse getMaterialDetail(long fid,String materiallib)
    {
        if (fangAnMaterialDAO.existsByFidAndMateriallib(fid,materiallib))
        {
            FangAnMaterial fangAnMaterial=fangAnMaterialDAO.findByFidAndMateriallib(fid,materiallib);
            String ids=fangAnMaterial.getIds();
            String[] idarray = ids.trim().split("\\,");
            List<String>searchSplitArray = Arrays.asList(idarray);

            ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
            query.JoinQueryBuildersByUrls(searchSplitArray);

            query.SortBytimeOrder();
            query.SetPageableAndBoolQuery();

            YuQingResponse response = elasticSearchDao.findByQuery(query);
            return response;
        }else {
            return new YuQingResponse();
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
                fangAnMaterialDAO.InsertFanganMaterial(fid,decodemateriallib,"");
//                fangAnMaterialDAO.save(fangAnMaterial);
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
            fangAnMaterialDAO.UpdateFanganMaterial(fangAnMaterial.getId(),fangAnMaterial.getFid(),fangAnMaterial.getMateriallib(),fangAnMaterial.getIds());
//            fangAnMaterialDAO.save(fangAnMaterial);
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
            fangAnMaterialDAO.UpdateFanganMaterial(fangAnMaterial.getId(),fangAnMaterial.getFid(),fangAnMaterial.getMateriallib(),fangAnMaterial.getIds());
//            fangAnMaterialDAO.save(fangAnMaterial);

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
                fangAnMaterialDAO.UpdateFanganMaterial(fangAnMaterial.getId(),fangAnMaterial.getFid(),fangAnMaterial.getMateriallib(),fangAnMaterial.getIds());
//                fangAnMaterialDAO.save(fangAnMaterial);
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
                fangAnMaterialDAO.InsertFanganMaterial(fid,materiallib,decodeIds);
//                fangAnMaterialDAO.save(fangAnMaterial);
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
        String wordOutFilePath = String.format("%s%s-%s.doc", "/root/codes/backend/fileTemp/" , "word", rnd);
        String pdfOutFilePath = String.format("%s%s-%s.pdf", "/root/codes/backend/fileTemp/" , "pdf", rnd);
        String excelOutFilePath = String.format("%s%s-%s.xls", "/root/codes/backend/fileTemp/" , "excel", rnd);

        Configuration configuration = new Configuration();
        /* 设置编码 */
        configuration.setDefaultEncoding("utf-8");
        String fileDirectory = "/root/codes/backend/fileTemplate/";
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
        String FONT = "/root/codes/backend/font/SimHei.ttf";

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
        String wordOutFilePath = String.format("%s%s-%s.doc", "/root/codes/backend/fileTemp/" , "word", rnd);
        String pdfOutFilePath = String.format("%s%s-%s.pdf", "/root/codes/backend/fileTemp/" , "pdf", rnd);
        String excelOutFilePath = String.format("%s%s-%s.xls", "/root/codes/backend/fileTemp/" , "excel", rnd);

        Configuration configuration = new Configuration();
        /* 设置编码 */
        configuration.setDefaultEncoding("utf-8");
        String fileDirectory = "/root/codes/backend/fileTemplate/";
        Template pdfTemplate = null;
        try {
            /* 加载文件 */
            configuration.setDirectoryForTemplateLoading(new File(fileDirectory));
            /* 加载模板 */
            pdfTemplate = configuration.getTemplate("pdf.ftl");
        } catch (IOException ex) {
            System.out.println("找不到模板文件，路径为:") ;
            System.out.println(fileDirectory);
        }

        Template wordTemplate = null;
        try {
            /* 加载文件 */
            configuration.setDirectoryForTemplateLoading(new File(fileDirectory));
            /* 加载模板 */
            wordTemplate = configuration.getTemplate("word.ftl");
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
        System.out.println("hereDimension");
        System.out.println(dimensions.size());
        dataMap.put("dimensions",dimensions);

        String text=fangAnTemplate.getText();
        com.alibaba.fastjson.JSONObject textObject = com.alibaba.fastjson.JSONObject.parseObject(text);
        List<textEntity> textArray=new ArrayList<>();
        for(Map.Entry<String, Object> entry : textObject.entrySet()) {
            System.out.println(entry.getKey() + entry.getValue());
            textEntity textE=new textEntity(entry.getKey(), (String) entry.getValue());
            System.out.println(textE.getName());
            textArray.add(textE);
        }
        System.out.println(textArray.size());
        dataMap.put("texts",textArray);

            // echarts图片数据
        com.alibaba.fastjson.JSONArray echarts = com.alibaba.fastjson.JSONArray.parseArray(echartsData);
        dataMap.put("echarts",echarts);

        System.out.println(decodeYuQingIds);
        // 舆情信息数据
        FangAnMaterial fangAnMaterial=fangAnMaterialDAO.findByFidAndMateriallib(93,"日本");
        String Yids=fangAnMaterial.getIds();
//        String Yids=new String("http://ishare.ifeng.com/c/s/v002PIED--kFs--5fkJLRmbCuCfsKdniZcPhWI61yF0NWRhcY__,http://ishare.ifeng.com/c/s/v00240TP7zGkp9oFvCtl--yc9XbWjRZr168UB-_DNNiE4y5yw__,http://ishare.ifeng.com/c/s/v002tAIkCdgGNwH8vrJL9PEL810i5ZLcUt1u7QnoQphFLfs__,http://ishare.ifeng.com/c/s/v002YjfW5BEIwNJkjX-_NITM-_7w0eXBEXJO6hXAsMgpyVOOw__,http://ishare.ifeng.com/c/s/v002Bl3m6uz8tVWVd9U0mIQHoQfUpRQIdQxwq9HAJQ-_PGPM__,http://ishare.ifeng.com/c/s/v002wl5KdQ9MvlBUMjwn55csU1NqLeih--XrxJPFGeCFEfGU__,http://ishare.ifeng.com/c/s/v002NTe06uRyrmDNTRT4UmVThvDV5r-_WKeSG5Ne--hiZSH3g__,http://ishare.ifeng.com/c/s/v002IxS4Rj0Cxxm8WQy0--bYY1GDj--11G12p9QcHGwkAP9Lg__,http://ishare.ifeng.com/c/s/v002YIVWzWry23heCY0euLDU6EAKH-_Krk1zax3e--CRsrr1M__,http://ishare.ifeng.com/c/s/v0023FuaamVSlCqPy6gScdlwAB2tjd5nmc6vLe5J4UZd5bw__,http://ishare.ifeng.com/c/s/v002LHNhkHFHrbHc4g0BgdScwIIOb2A7kaYC-_KG3r44bt54__,http://ishare.ifeng.com/c/s/v002VvpnOYknQrKDVZKIj5MKh-_fAIkOEow2ayDTRcwTaZ55FHWVThrjZ32sg5P9jLSrzyTK26dVY7Fs68DC2nz8Fsw____,http://ishare.ifeng.com/c/s/v002BM1VwKZv752iwD23FNcHWWgOJ-_nWvvnlxHRPPnKkshc__,http://ishare.ifeng.com/c/s/v002Nvm55HeLSgldADC0sTUPR42i-_-_AKiKGtqr0Vkt0vx9I__,http://ishare.ifeng.com/c/s/v002CODY5P6ORuv6bqOC0wMDH60bfxXieIxSiGA6D6zoYLv6kl7sVtXfX--7GBukfTbkCZE2xfDTbq-_ikJvBFoA2Uyw____,http://ishare.ifeng.com/c/s/v002lqGh41Cf-_7mf2cw-_7biO2LdedgIto8jEtUxIitgIjsybvubuuaoi9zISnfAQsqlIb0f4NAEGGHM052KB-_AWtNg____,http://ishare.ifeng.com/c/s/v0020-_--RQGVODmMYadoYNijz-_NJXZx3goM--s70QO8HMcIIo__,http://ishare.ifeng.com/c/s/v002y0P6e2kh0NH8QqY9AF---_L5GfUXPigOmG0Q7No2M2Bvgmi2kvVeImk4mxfv3XT8q4aAgMooq--BxTpzY5cVjxSqQ____,http://ishare.ifeng.com/c/s/v002lDjjBjd-_6YJHpBlUlRIp--n8rKgwXHzb-_KQt--SzUNV1I__,http://ishare.ifeng.com/c/s/v002dni3oHo2lxf9D9J7QMl-_c9fkJWTE4l4U8YMUp7sFaM4__,http://ishare.ifeng.com/c/s/v002TxEgBmMvwxQLwWRUWyNUTbcE6wnOs3KgoA--3DkeE8pw__,http://ishare.ifeng.com/c/s/v002PIDAebOTdnZLTt9iQhVaKVIoZUQufnQKc6j-_FEFPOAU__,http://ishare.ifeng.com/c/s/v002fH51ajaQIzmFZXBlf1G--i5rt0mGQrwiy85QVkoHyFYc__,http://ishare.ifeng.com/c/s/v002jM2Z0kMK4lmTKCQLitl5V8-_AWWam4CK6LOIWdxNAC6E__,http://ishare.ifeng.com/c/s/v002J47XHxwraP-_bmpWTuQxmjtR6TyCGul--3llB8ZR66aZHrt--olONcshTl33i50t5XetCGzw4LU82UWbzHv4g9fFA____,http://ishare.ifeng.com/c/s/v002ViklEa-_sSLJXLSQ-_ljo7JgTmLKCLn49t9IXFVR5Okl--eciuAh9gEHqhEXQhsujIa2ggvT0xm77KRukWe2TsBEA____,http://ishare.ifeng.com/c/s/v002NIwWp0Je6QGoLrO1TFdifZeSm1xKEJ5SDeJb7Jo7haIHneLHJzJ5rvoce9ZqF6x7uA9Eokmh7WBzeJtXua6nsw____,http://ishare.ifeng.com/c/s/v002F2LBcr3ifdHIRGBZmvQq0NfR6-_CLMtKDPdZJkIXgZlfEO2rBAR7dtKHeY--L7wGsBGJZ5LPpImCcUH1pYGHP--uA____,http://ishare.ifeng.com/c/s/v002fkKIhcqJmOqEpokezod9254ibiGWNwdnGX9GP3hrrb0kSRkopOFujLlTe--2Lo8j3HmZdQaBjAZAphP35--g-_qBg____,http://ishare.ifeng.com/c/s/v002Wc7rFFl7QfuoH1oPXtM7srQgC00usSuwHHBptajcK6yeliN00g4z1cl-_ZWyQsCV2i5fifqnkHTrAQnqWxcY8fQ____,http://ishare.ifeng.com/c/s/v002lCIXEuxj-_8OLiWJdNa11CCMaL--rEj6kudfUhvsklUrTtz9kPBPxajbo3scxNq3om6m0Zb6e62yEOJgnx0W0xfw____,http://ishare.ifeng.com/c/s/v002jkLwfoeIqdQgyqxelFszbJR1SwpiTb7wfAXUrGJwHSo__,http://ishare.ifeng.com/c/s/v002dPECWZfOOWGY--3bTexfA4tKxrjF5rORvO3yg2HxhSlOZcUaCfCye--39DVKN77Ix4EFnpUCKSRqW20eBRy7fo7g____,http://ishare.ifeng.com/c/s/v002cFyNWUTpUemA2tdxy-_lnJRHx1elRF09YxOhJ2vCbFu0__,http://ishare.ifeng.com/c/s/v002Yq7YaYi3IOdFc4ap9KsAWH5KpYc3--RtvS4AFEkuCPwjusV6J9fyDnqWIIyx1rGbQ--aaiHDLIh0TMpPzcsH510g____,http://ishare.ifeng.com/c/s/v0029kQDGL--VEfF7lknaNR0N--pBPh2bGooEXcltHXWERNok__,http://ishare.ifeng.com/c/s/v002BlXS2ImoDYU5Po78ZN9vW2aLNDZNq9MXeJVKFsGfi6c__,http://ishare.ifeng.com/c/s/v002dq6NmZ-_--3e2CJJIof1FMoXgK6T6mC1gitXwedq-_--rVpcz1jDPxOaaFI4oBQ1AMDv9o24vNIkKH8--pcMeXfIS4Q____,http://ishare.ifeng.com/c/s/v002HBpU89kQ73Fz3yxxcgcCsq4EDPfTdIQoKiOwqR2Sv-_U__,http://ishare.ifeng.com/c/s/v002EX--jkWzHgCDVqhEB-_fco1i-_fdDYN8MQRvmj1haNHKbI__,http://ishare.ifeng.com/c/s/v0023nim--TKDNA9O2vXXAi7x5Me8dy7hqYdd2o7ebof0FE7V4e0OFLxmEKYvWrM-_yH--vUCvdbqp1BxMZCzvKdR3XPA____,http://ishare.ifeng.com/c/s/v002P50giRCRC8bVVKvpXCUxjw7SfVTks2subJNZaYhpaxI__,http://ishare.ifeng.com/c/s/v002vED5UgM8AsGUyhY1ewSLfvwt7K72I9wLcEisU80wmSY__,http://ishare.ifeng.com/c/s/v002on2FTlgyHBy7YTyNkn1a9r9ji--YFb216uqFnqhBU0fY__,http://ishare.ifeng.com/c/s/v002mCQ7V1Yra3o2rjwtZvVxepGWkas--5kkjOdfoKI1NErM__,http://ishare.ifeng.com/c/s/v0020ZuKighGgxe7kb-_l--40BeOilV3IYNrP6p4QFWFa-_KKTtUstX9kZcc--T3uHa1YZCLmjENadRzDzINdie6MzhT4A____,http://ishare.ifeng.com/c/s/v002aNXl7zdnCO8fcJFnG3j--o2od7D7qoC721sjjFrKgjzE__,http://ishare.ifeng.com/c/s/v002--ZTYFQ-_WWQotUM4lKowVCr--QZVGDAq89DTnWBnyvNbc__,http://ishare.ifeng.com/c/s/v002bl8vzIEIb488XqD-_3fHwjTKfpEQd4036Z0pyPOwQDbA__,http://ishare.ifeng.com/c/s/v0022rqK4CZ3G8HfbzE1h5kukIB2CtU22otcD-_coZ-_ZCGIA__,http://ishare.ifeng.com/c/s/v002-_8PYj-_5esA8ya94H9UaT0tRSDVrNGHUB--glqqNK1rOI__,http://ishare.ifeng.com/c/s/v002RNrhPVqmFOf-_rze7XRtC28wnmVmlUkFgUsQ53rdt8z0__,http://ishare.ifeng.com/c/s/v002nX5NzQNUYIVRYnqfXX--wPaoIE3W2-_aPUtpdAWbq-_R0GNJcAqjDtAYt--hYbkEP-_sd4EQxvwuRKjL7GsxVanEErg____,http://ishare.ifeng.com/c/s/v002Wzb2GOIrm8-_iHGvUE5EDdh0--DaJm9VjciAxcBoMU0IQ__,http://ishare.ifeng.com/c/s/v002jpzWulY65WLEUWCQEIzmkfU2LQ6sysqhWQSZ9UdWWJ0__,http://ishare.ifeng.com/c/s/v0028OvZEOdXi9IlfYcapqqpgwcasMrgGs3D3fCq-_NKKbIc__,http://ishare.ifeng.com/c/s/v002l5w7Vc5PM9yz--NVDWkxVrlX0KkqI--47zFahna8c58G0__,http://ishare.ifeng.com/c/s/v0024u2FDaz0LAsHzuKeTd42GtvRrp-_KpATIh89nMKPlTGA__,http://ishare.ifeng.com/c/s/v002ah2--5NdFJoWNNg3538ZNQMue9YTi5p1w5tBAzwiowjA__,http://ishare.ifeng.com/c/s/v002nXTWCsB-_fDS2e2iLPzr6p7xCbz2hvca9iefr--Lqtx1I__,http://ishare.ifeng.com/c/s/v002TEKuklUTvI63URCPB0--xCMcrC3c9Kk3nvuujUx4SkGE__,http://ishare.ifeng.com/c/s/v0022u61lSJDcUXwdcyyv2DPmXq36--j2Twqkq2jFdAFgRMraVWWJnd3PBncNAlMWeNpMy1zAiN0VVo3TiF4EL2roXQ____,http://ishare.ifeng.com/c/s/v002frl0XmLq0jUMXueD-_NWaxBfTvaCqgqtRTABEOBcMjObrwsqJthc--d9TEj3UttOz-_sSsqdGnWA2sBNd1eK--a6Ag____,http://ishare.ifeng.com/c/s/v002m8D2tDvuZeYcNtBhNIedIzaK5HcQct2ZEsNkCBObJHg__,http://ishare.ifeng.com/c/s/v002-_rj7yCYfLkelW--AJ1i3J8C9TMC4QcvHeHgq8ElzszPLr23Xe6Zuq--V-_ymDnngWagMC75ccG6LwOcbFiXIjictQ____,http://ishare.ifeng.com/c/s/v002EZTVkZLqStEUlYdtQz2xIQIOm41Fk07tdxXpC6t9rjE__,http://ishare.ifeng.com/c/s/v0025xwRO9NOveHL-_jPxRS4H--5FJsFtarHNymWV2QdlTXEI__,http://ishare.ifeng.com/c/s/v002NfHuJtcBryUk--Jqte4RDjFFjGDm-_pel6VyH3kS56ywc__,http://ishare.ifeng.com/c/s/v002EpyJtzchNL6byvlSgWOGFINz1aAQCh--qCf0i1KAb--wk__,http://ishare.ifeng.com/c/s/v0028Wha7ECe1G2ncLM7vFH26OgwOtnJuX-_IAjWSJ0MRFaQY--3CTSDuVmL-_--PYp-_uIuqEOcF1vFwPEsAR5pRsVpuWw____,http://ishare.ifeng.com/c/s/v002QNpc1oVBFXivI24XVGbMGTsboBLV0-_HcnTER0u8OPaM__,http://ishare.ifeng.com/c/s/v002F89UHfZluopxcwdgQXqEkalmjz8ma4zJasUcNkuBMJ8__,http://ishare.ifeng.com/c/s/v002Ei--AiYIKfra9HcNtbJ57QDj--QTalh----zfNl1HmqLPmk__,http://ishare.ifeng.com/c/s/v002Vi--DzSWSTlCYqEj5QIYFeHjlEAFweDXcB7XirPu2Ct4__,http://ishare.ifeng.com/c/s/v002SUtLprHP5ck3KZRzE--A5gOlJG--QWMCZ53P5tVWjK8ZA__,http://ishare.ifeng.com/c/s/v0027JodLslyNNGckgPFCydaTlX6qv--NZ6DGMc5jSsIgpf8__,http://ishare.ifeng.com/c/s/v002kO1dKWUJcKJHqP--cAQroV2CaLIWQLG01gEUTgkho4UM__,http://ishare.ifeng.com/c/s/v002qRw2BYZ8klMlUAnwd9B9XejwxjIzFpXFibLWjtGV---_0__,http://ishare.ifeng.com/c/s/v002Pfy41DjfbCL2Q25uN8SnWmp4Qw4qQ0QXNnFq-_tywE7w__,http://ishare.ifeng.com/c/s/v002zs2JTMjI1YTboaE1yygYByGdNWzB6EDR3ftm5xX0Xa8__,http://ishare.ifeng.com/c/s/v002k9-_a4rPKZ--j3LzvsU9QRQDBzvgzdztBb6PHoqS--cBmk__,http://ishare.ifeng.com/c/s/v002EAvN0QOcLmZnOViPTOoDd3-_VQCdRqIokcG2LuosEk2Y__,http://ishare.ifeng.com/c/s/v002SsiHt0Iuyla21o--XwwcCZS1lVWQEwtPDKZP3tFc8RtnKxmi9AJVSi8AMfaarXjlezU1YmxCfH0j7cTnPxhyTAA____,http://ishare.ifeng.com/c/s/v002z5iNoxi-_LCiuuCTk--C3QdiyH1UJaLqd1dIgJ1XQNGsg__,http://ishare.ifeng.com/c/s/v002IGdDE06kErErbzMQf7yul80D8shRbF--iAIrSb4zh3PnaDTTfC5nkNvHCWOzsXGQqinsrAUOhnczCWYX7mUSj4w____,http://ishare.ifeng.com/c/s/v002JrKX--jJtuZEQX5Sn6Yz--4FBgjdglzpBPC4ZraZ59mBU__,http://ishare.ifeng.com/c/s/v002YM8yFQbARdb--Jf9f3N3lSXrX3IFNbl3AgGLWKkYBl1QHAHslQVV6uCrg0uOCV2AxRhCvgcg--bsYoFHTNEsFFjA____,http://ishare.ifeng.com/c/s/v002Jba1nKQW9bJ6--WpiWebSjpf32duLxD1BjbSoyio7C8zU3F2rFQ5bZHtM2p1SbwauXbgi3mguKY9ol--TkigCajg____,http://ishare.ifeng.com/c/s/v002--nILmJ3CZjyPNwlI6xr8N7mJ0JScMOlW1O7EJdXY6jTRQRPoY6oHRBA5eGfOgxJKH35ouBiRtq7oldfIYSeAWw____,http://ishare.ifeng.com/c/s/v002VGwKy70rXv--9lrCNvDz7Cjl3FReYLwzkU4Zi7BuDiEWLzwJoKQ-_MD60xGVyrwTcXJdY8Jtt--r9yZiTNAkNjSuQ____,http://ishare.ifeng.com/c/s/v002s6St3fjTYw3xG5KntQP4jMikuwOLzTKpSkHd6WXxtFBcxRnqdp9OsTen3JahfCsW--aSIHx5kbFvoGfoReB7tGA____,http://ishare.ifeng.com/c/s/v002okQgaVeAXDMWBt1ML17p9Q8hoUVR4SSGW0vQLu--PrGM__,http://ishare.ifeng.com/c/s/v002Dp2TySMqHOOswWDcmqxIntH4Hcn0zguiYtgaBy6pyPs__,http://ishare.ifeng.com/c/s/v002ABDwI3G9MSrT9bbnEB-_dm6n6XvlzW5SN6YbaCKlMC54__,http://ishare.ifeng.com/c/s/v002iBe1-_oU3UEVfuIbK0RSo--Qdl03uYWUJ5VfMd5wdKA8E__,http://ishare.ifeng.com/c/s/v002E48fLmRioZzirAWf5FZl9pi0LORgzdmHTre8p8o1cZY__,http://ishare.ifeng.com/c/s/v002tzrPJE--YrHgJiB4hasgvPGEytqSFOiUMrLjNGr7pKjE__,http://ishare.ifeng.com/c/s/v002MGwHG1AewNtQsEdS341jPiUkqYb8evE5Aw--0cPCVEiE__,http://ishare.ifeng.com/c/s/v002eIJp3sj0Z0u2cNWL9MqxzTRtWREQJZuhv-_rn7PBYbuE__,http://ishare.ifeng.com/c/s/v002-_eT3heTSVaUcRcPLNy77W-_KHGmyjz--eJZBMBxFxTRkHfnmXE3wn3sPEFTooVBRcGSBKOUd4I6qqjnt2Iojs1hg____,http://ishare.ifeng.com/c/s/v002U7B1ItijeqpcVF1kefwCT8QVR2RLFb8mOOxYTmeqfPM__,http://ishare.ifeng.com/c/s/v002xDkEI5DmMeDMnTrEivtk--L467Y7co85NM4U20LvXcIE__,http://ishare.ifeng.com/c/s/v002MvM6--wd7I1fm--lwEYdR0Rf6WpSW9rGg3--BKvLnQKCyc__,http://ishare.ifeng.com/c/s/v00292VcphCPssqR9nw95xQ-_EGFnXRIR73wIZ7cC2SXJ7jY__,http://ishare.ifeng.com/c/s/v002M6bAhSMIPjsQVUw--aonForfXx-_YYhskG50j8--foBdNk__,http://ishare.ifeng.com/c/s/v002jixwYLJqgeFn17gETqOdALy3ZgXQ0--xf2MiQpQpzFDLhKHLwYYPU2PPp605vPX0z8LixHSEdBYGtzkQa0tVmXw____,http://ishare.ifeng.com/c/s/v00290WdDT7grg4IEnhmhh8zqcFDKCEh7M0iDiQHucRs-_dQ__,http://ishare.ifeng.com/c/s/v002rmQ--znM9ItHAoVp97KpERQvdlmikHy186F2h0bNPKpI__,http://ishare.ifeng.com/c/s/v002KrJRHW99jIqJX3Y9XWtDJ6JgSwVan8mZAI0QC0OPR--A__,http://ishare.ifeng.com/c/s/v002UXLugZmlWZkcx--mw3qhlUTKREgYwAA-_oIJElh--QsDyE__,http://ishare.ifeng.com/c/s/v002LHNhkHFHrbHc4g0BgdScwIlu8t9KPLcqXOSVTJ6oe-_o__,http://ishare.ifeng.com/c/s/v002VvpnOYknQrKDVZKIj5MKh-_fAIkOEow2ayDTRcwTaZ56Nbkjv--FYDU2jWywxoaDyhm3XKIkS1XCl0iv34i6nAtw____,http://ishare.ifeng.com/c/s/v002y0P6e2kh0NH8QqY9AF---_L5GfUXPigOmG0Q7No2M2BvjJ6WhNPxkYvrqKR2MfTTV3qXfrPXFcbgCtcV7ZUSrJgA____,http://ishare.ifeng.com/c/s/v002lDjjBjd-_6YJHpBlUlRIp--g8tf7-_6iyuky0MT0QEjLrg__,http://ishare.ifeng.com/c/s/v002BM1VwKZv752iwD23FNcHWX01EzTCWzv--t5mOTWhDHrI__,http://ishare.ifeng.com/c/s/v002CODY5P6ORuv6bqOC0wMDH60bfxXieIxSiGA6D6zoYLvZNF70MGZuj9--IwFwmLmi068a9w-_IP78RdYbAO3QmFBQ____,http://ishare.ifeng.com/c/s/v002jM2Z0kMK4lmTKCQLitl5VxjO77dEP-_0F7aIvYcj3hho__,http://ishare.ifeng.com/c/s/v002ViklEa-_sSLJXLSQ-_ljo7JgTmLKCLn49t9IXFVR5Okl--HOrJD3c2CAOSYiw1WKpSqv6KfLe8ymQQJVoZEyq865g____,http://ishare.ifeng.com/c/s/v002PIDAebOTdnZLTt9iQhVaKXcTHEnbb4mkgQVchJcUx24__,http://ishare.ifeng.com/c/s/v002lqGh41Cf-_7mf2cw-_7biO2LdedgIto8jEtUxIitgIjsy20zwbuEZ8FRstqek2T9hg3GNdGzB91Bw3eNx9Ykbb0g____,http://ishare.ifeng.com/c/s/v002NIwWp0Je6QGoLrO1TFdifZeSm1xKEJ5SDeJb7Jo7haJS8vfUlKD0eOQydPcyHSbBIHng0uRC53t9ZhGi8XJT2w____,http://ishare.ifeng.com/c/s/v002fkKIhcqJmOqEpokezod9254ibiGWNwdnGX9GP3hrrb3Tdu2oAiC8bQGklUVlwh8AX6VV7Mi50t8LOXp5HZyaFw____,http://ishare.ifeng.com/c/s/v002jkLwfoeIqdQgyqxelFszbGVmicBULzR6o8oM5BOpMyQ__,http://ishare.ifeng.com/c/s/v002cFyNWUTpUemA2tdxy-_lnJS0Hd5tBe-_vt4LYRm3pmMHI__,http://ishare.ifeng.com/c/s/v0029kQDGL--VEfF7lknaNR0N--o424j--acQS1I5Ghr--nF3yE__,http://ishare.ifeng.com/c/s/v002BlXS2ImoDYU5Po78ZN9vW3qo--fi1Qj2EXa5e3p--GhYk__,http://ishare.ifeng.com/c/s/v002HBpU89kQ73Fz3yxxcgcCsjFLeQ4BrtwN4BOr0rdwUsg__,http://ishare.ifeng.com/c/s/v0023nim--TKDNA9O2vXXAi7x5Me8dy7hqYdd2o7ebof0FE7gwhtKbrHMYMEsRqRd69tHZ8Jx-_PBZi4-_xbiqKM0W--tQ____,http://ishare.ifeng.com/c/s/v002EX--jkWzHgCDVqhEB-_fco1kVJ2UNxruVc6616lmtAhN8__,http://ishare.ifeng.com/c/s/v0020ZuKighGgxe7kb-_l--40BeOilV3IYNrP6p4QFWFa-_KKTUGvtfiJ2ZgjN9B12--DSUe8MQuyjmmJA-_ZdWOpTECRMg____,http://ishare.ifeng.com/c/s/v002--ZTYFQ-_WWQotUM4lKowVCiUcPJwCCe-_dnmWviDep3W4__,http://ishare.ifeng.com/c/s/v002on2FTlgyHBy7YTyNkn1a9h7gD--LQtXdEHrseIhvqhmE__,http://ishare.ifeng.com/c/s/v002bl8vzIEIb488XqD-_3fHwjc-_e7SUoX--JZNu4x1t71eUI__,http://ishare.ifeng.com/c/s/v002RNrhPVqmFOf-_rze7XRtC292rYR--PYm751v7--pIEAI3g__,http://ishare.ifeng.com/c/s/v002nX5NzQNUYIVRYnqfXX--wPaoIE3W2-_aPUtpdAWbq-_R0FKmRdMM4Jf8uttIWM67sxYYkPJaLN8FFXcsiT1MEzE4A____,http://ishare.ifeng.com/c/s/v0028OvZEOdXi9IlfYcapqqpgyskoViwdKF9YvecKUb6nwM__,http://ishare.ifeng.com/c/s/v002NfHuJtcBryUk--Jqte4RDjAz2HACMQpgW9mMlpL0VWL4__,http://ishare.ifeng.com/c/s/v002-_rj7yCYfLkelW--AJ1i3J8C9TMC4QcvHeHgq8ElzszPIVBoiou0GtUjVuvmHBUERXiBT--1s4FZtWoS-_PDOsykyw____,http://ishare.ifeng.com/c/s/v0022u61lSJDcUXwdcyyv2DPmXq36--j2Twqkq2jFdAFgRMrWEDZAHZ--qy4zGnnF9YmvvFNF5K--snlYN3Q2WI----XWvQ____,http://ishare.ifeng.com/c/s/v002frl0XmLq0jUMXueD-_NWaxBfTvaCqgqtRTABEOBcMjObHErEIK0Vlk6QfdKoXimQq9x0vG60CX96IqO-_NywZIzA____,http://ishare.ifeng.com/c/s/v0024u2FDaz0LAsHzuKeTd42GlF3G51Z0sJJ3eWybj6RTi4__,http://ishare.ifeng.com/c/s/v002Ei--AiYIKfra9HcNtbJ57QG1rGt75YZUFovETpFzEwVU__,http://ishare.ifeng.com/c/s/v002EpyJtzchNL6byvlSgWOGFOobOns5DvNtV5zapLI-_y-_Q__,http://ishare.ifeng.com/c/s/v0028Wha7ECe1G2ncLM7vFH26OgwOtnJuX-_IAjWSJ0MRFaRR8nzcRtBX0uS0G5SyoVpydw-_LXClvKAKq--Z4JxCKMaw____,http://ishare.ifeng.com/c/s/v002EZTVkZLqStEUlYdtQz2xIRRxJy5BVGC6JBqrJHER4Ao__,http://ishare.ifeng.com/c/s/v002F89UHfZluopxcwdgQXqEkaTWdAHq5O6lnoF28X0Vy9Y__,http://ishare.ifeng.com/c/s/v002dni3oHo2lxf9D9J7QMl-_cw4Ulgvw6THucUjE6h0-_xME__,http://ishare.ifeng.com/c/s/v002Nvm55HeLSgldADC0sTUPRwEDBsAI3qHiufoY42VxDpo__,http://ishare.ifeng.com/c/s/v002TxEgBmMvwxQLwWRUWyNUTQp34OYha5kcpXf-_SSmSsRg__,http://ishare.ifeng.com/c/s/v0020-_--RQGVODmMYadoYNijz-_Di261p8SxFEnt7me5rEur0__,http://ishare.ifeng.com/c/s/v002lCIXEuxj-_8OLiWJdNa11CCMaL--rEj6kudfUhvsklUrTc--fbtrzfMD0aFMYn--yLdGVwIcAt--vtyKO3IE-_Q5KVkQ____,http://ishare.ifeng.com/c/s/v002dPECWZfOOWGY--3bTexfA4tKxrjF5rORvO3yg2HxhSlOx0VayU0uWQD8CxVRUVw4OvaCPqGObs--WStEoqaboXuw____,http://ishare.ifeng.com/c/s/v002Yq7YaYi3IOdFc4ap9KsAWH5KpYc3--RtvS4AFEkuCPwgOdz25ow0l-_KbaA0wf1svEwYAFUovQlu6F5iP7CzR--fw____,http://ishare.ifeng.com/c/s/v002fH51ajaQIzmFZXBlf1G--i7K4H7VC2eHrkSWA--rqNgKo__,http://ishare.ifeng.com/c/s/v002J47XHxwraP-_bmpWTuQxmjtR6TyCGul--3llB8ZR66aZGu85SGZzLhmdSrOwx3-_hmL--jIp2GmqUGQLSFDSj3F2Yg____,http://ishare.ifeng.com/c/s/v002dq6NmZ-_--3e2CJJIof1FMoXgK6T6mC1gitXwedq-_--rVr0vKN1c2euW7Gho6BhKHKfgP6pfB7f0XDUUvj8a32y-_Q____,http://ishare.ifeng.com/c/s/v002F2LBcr3ifdHIRGBZmvQq0NfR6-_CLMtKDPdZJkIXgZlc4IPrXkIBZlKSB-_DZQLqLn8YYx--zO--Tw8BZNRUvpSEEA____,http://ishare.ifeng.com/c/s/v002Wc7rFFl7QfuoH1oPXtM7srQgC00usSuwHHBptajcK6zT0xi--EVTy6r0lNb7DR5LgRBWMibcqdKYZAPyIJj5bEA____,http://ishare.ifeng.com/c/s/v002mCQ7V1Yra3o2rjwtZvVxetm78566OvXItco0C5OlOfE__,http://ishare.ifeng.com/c/s/v0022rqK4CZ3G8HfbzE1h5kukBWlTGz5nXuu7WXadFVeZXk__,http://ishare.ifeng.com/c/s/v002P50giRCRC8bVVKvpXCUxjwe7Zi3XBME-_VMk--Lx0GzMg__,http://ishare.ifeng.com/c/s/v002l5w7Vc5PM9yz--NVDWkxVrh1Sv5zkRHSzp9kqrHCoOCM__,http://ishare.ifeng.com/c/s/v002aNXl7zdnCO8fcJFnG3j--ox7g5DVvE-_pGV58Bz28f3-_g__,http://ishare.ifeng.com/c/s/v002okQgaVeAXDMWBt1ML17p9eW6CW4StiSSEhXHeGa3zsU__,http://ishare.ifeng.com/c/s/v002Dp2TySMqHOOswWDcmqxInmvCcRwrvh1BZ8VFmJwQ2p0__,http://ishare.ifeng.com/c/s/v002vED5UgM8AsGUyhY1ewSLfm2f0I--uoi1tbpDOFlnZ54M__,http://ishare.ifeng.com/c/s/v002qRw2BYZ8klMlUAnwd9B9Xah0UR6w7rkbFFK-_lwvDUPc__,http://ishare.ifeng.com/c/s/v002ah2--5NdFJoWNNg3538ZNQPUQVsho4PHOKbc-_6z0C5b8__,http://ishare.ifeng.com/c/s/v002-_8PYj-_5esA8ya94H9UaT0gTK3M7PIyeLvLw5GGp8kI8__,http://ishare.ifeng.com/c/s/v002Wzb2GOIrm8-_iHGvUE5EDdqihhGnxdmR8ZRo6s1OWpVA__,http://ishare.ifeng.com/c/s/v002jpzWulY65WLEUWCQEIzmkfOpTwza1GlNwy--HORyxzS0__,http://ishare.ifeng.com/c/s/v002IN3OCcnYHYLbvFhG7yvaf59yhgXJ4QZVXmsj2xrfTexvEYjfBW1Xkkbzj6d6VgsLbikxUJpjPCu--wJf3Q-_pEZQ____,http://ishare.ifeng.com/c/s/v002nXTWCsB-_fDS2e2iLPzr6p--Q23rUauxeemggWgFgJNNs__,http://ishare.ifeng.com/c/s/v002TEKuklUTvI63URCPB0--xCP9wc2nE-_LPhS3RInH-_i0Vw__,http://ishare.ifeng.com/c/s/v002m8D2tDvuZeYcNtBhNIedIxSR7yKQXPmVsc7tnhDElmI__,http://ishare.ifeng.com/c/s/v002MGwHG1AewNtQsEdS341jPrVivHO--FB7lMfUlTEVpuFw__,http://ishare.ifeng.com/c/s/v002QNpc1oVBFXivI24XVGbMGc4VN5opDMi9H616zkaodDA__,http://ishare.ifeng.com/c/s/v0025xwRO9NOveHL-_jPxRS4H--4GUtcH50OKIQ8PWBJ1cAaI__,http://ishare.ifeng.com/c/s/v0022cY-_kwW--DHLtGf8-_j8mOvFpNgKbrGb8p6FnM3L3du4g__,http://ishare.ifeng.com/c/s/v002WUyE12daAKj0VqGDIlbO7XZeVhMs62ut-_ZneEsWvOR0__,http://ishare.ifeng.com/c/s/v0026--ANdwXKwDd-_z6gHKiJ4W5JjDA7fa9R-_PBsmFJWAC4U__,http://ishare.ifeng.com/c/s/v002PIED--kFs--5fkJLRmbCuCflBkklSYV--41sYKEAKxHXZ0__,http://ishare.ifeng.com/c/s/v002tAIkCdgGNwH8vrJL9PEL821n3zbWNoTPeksqON56loI__,http://ishare.ifeng.com/c/s/v002Vi--DzSWSTlCYqEj5QIYFeHh1g2JVgwN8gsQ19leOuFY__,http://ishare.ifeng.com/c/s/v002SUtLprHP5ck3KZRzE--A5gA8uz2vsA5eDy-_--0EhETJo8__,http://ishare.ifeng.com/c/s/v002YjfW5BEIwNJkjX-_NITM-_75K--4tPiji3hoFwM1mibwM0__,http://ishare.ifeng.com/c/s/v002kO1dKWUJcKJHqP--cAQroV5HYtJ7DAvNPYHm5kWP7k6Q__,http://ishare.ifeng.com/c/s/v002KfUCBYHsAfMCLIlDmVDAkBHwfvBHlxOBjUf69cn8EmU__,http://ishare.ifeng.com/c/s/v002Pfy41DjfbCL2Q25uN8SnWqKL7TXS-_p9CT8rRq--T1Jzw__,http://ishare.ifeng.com/c/s/v002wl5KdQ9MvlBUMjwn55csU3Q--MjNZsWLUYhwMwb6rLg0__,http://ishare.ifeng.com/c/s/v002NTe06uRyrmDNTRT4UmVThklx1qsBeKN23eI9Saxxnko__,http://ishare.ifeng.com/c/s/v0027JodLslyNNGckgPFCydaTsETGQ7e7byGDoseya3uXdc__,http://ishare.ifeng.com/c/s/v00240TP7zGkp9oFvCtl--yc9XQCWH6RjnfmwpO4AgEFmzWA__,http://ishare.ifeng.com/c/s/v002k9-_a4rPKZ--j3LzvsU9QRQI4UhdWdGiS9ruW1G--f-_Gf8__,http://ishare.ifeng.com/c/s/v002Bl3m6uz8tVWVd9U0mIQHoSyGkmdqq3SYW87Dlt1T5sM__,http://ishare.ifeng.com/c/s/v002zs2JTMjI1YTboaE1yygYB6vBKx7-_OGzTq3bhGFf4IfE__,http://ishare.ifeng.com/c/s/v0023FuaamVSlCqPy6gScdlwALNwDwke7BkOUvFroqqOhHU__,http://ishare.ifeng.com/c/s/v002EAvN0QOcLmZnOViPTOoDdxS8lM5iKUjpxIE51Gatiqo__,http://ishare.ifeng.com/c/s/v002SsiHt0Iuyla21o--XwwcCZS1lVWQEwtPDKZP3tFc8RtkqVh8DgmiagOdi--vR0tnn01yggaWadw79Xqg--DbdB6Pw____,http://ishare.ifeng.com/c/s/v002YIVWzWry23heCY0euLDU6DUGFQIMTZmGk22bn6oIFtg__,http://ishare.ifeng.com/c/s/v002JrKX--jJtuZEQX5Sn6Yz--4NlTIw45M60A--S0DYMzDzEE__,http://ishare.ifeng.com/c/s/v002z5iNoxi-_LCiuuCTk--C3QdjjJIUmOP0NXVBETGudnvac__,http://ishare.ifeng.com/c/s/v002IGdDE06kErErbzMQf7yul80D8shRbF--iAIrSb4zh3PnKjb9JSpVyvwHff2----jTAyRGaRRxzOoNIk0JLAhpNLGA____,http://ishare.ifeng.com/c/s/v002YM8yFQbARdb--Jf9f3N3lSXrX3IFNbl3AgGLWKkYBl1TKU30tJ2SwGhUl7a-_dB0BlUyuI7v6n9OocXWCX3sEmDQ____,http://ishare.ifeng.com/c/s/v002--nILmJ3CZjyPNwlI6xr8N7mJ0JScMOlW1O7EJdXY6jR4l4Wf3qGrnaflRStYxYfA0Z6m5X6DMbGvauP-_TOtMyQ____,http://ishare.ifeng.com/c/s/v002VGwKy70rXv--9lrCNvDz7Cjl3FReYLwzkU4Zi7BuDiEUXuM4qvOThDnoKFgZEAXsB0TzR1u4V8kuE6ZJQhl0mhw____,http://ishare.ifeng.com/c/s/v002s6St3fjTYw3xG5KntQP4jMikuwOLzTKpSkHd6WXxtFCfll1ENTgXC4tiWb70FWQnMj6ZR--MhmRbA492CXH17jg____,http://ishare.ifeng.com/c/s/v002Jba1nKQW9bJ6--WpiWebSjpf32duLxD1BjbSoyio7C8yjC3WE2xCBVROu4r3srLpmsX3tBFkrM0bGU-_6E6E--POg____,http://ishare.ifeng.com/c/s/v002ABDwI3G9MSrT9bbnEB-_dm--jF9EiXdfeUy--HIj7UdfzM__,http://ishare.ifeng.com/c/s/v002iBe1-_oU3UEVfuIbK0RSo--Wi0FZx--0U1n328GQNPj7x8__,http://ishare.ifeng.com/c/s/v002tzrPJE--YrHgJiB4hasgvPILF6Da7-_K43Pn1dVEM-_O78__,http://ishare.ifeng.com/c/s/v002E48fLmRioZzirAWf5FZl9lbLAG0k21peHlJA-_7PgXoc__,https://weibo.com/2425492624/LiX4CuXEX?refer_flag=1001030103_&display=0&retcode=6102,http://ishare.ifeng.com/c/s/v002TxEgBmMvwxQLwWRUWyNUTTD--bBe0idngY01fKhClOKY__,http://ishare.ifeng.com/c/s/v002dni3oHo2lxf9D9J7QMl-_c3Q46-_i17Hs2nIi0zY6T2p0__,http://ishare.ifeng.com/c/s/v002Nvm55HeLSgldADC0sTUPRwwOnRVQM2Fi5zs--tEGlXKs__,http://ishare.ifeng.com/c/s/v002dPECWZfOOWGY--3bTexfA4tKxrjF5rORvO3yg2HxhSlNssOKOOnytAILOh4iPNbOadZb8LBb8CA9ZP4hevh3wpA____,http://ishare.ifeng.com/c/s/v002Yq7YaYi3IOdFc4ap9KsAWH5KpYc3--RtvS4AFEkuCPwiIztMb1GM1Z9BXJZEGmssaU-_IhduUfIKZatJGr9FZl8w____,http://ishare.ifeng.com/c/s/v002fH51ajaQIzmFZXBlf1G--i9h18vNRvKkD--Yu0HFDeYN4__,http://ishare.ifeng.com/c/s/v002J47XHxwraP-_bmpWTuQxmjtR6TyCGul--3llB8ZR66aZFWdWNbg1ldr8D4PF1aZJzbiip3rEMaxeFDMQ2pWMdWLg____,http://ishare.ifeng.com/c/s/v002dq6NmZ-_--3e2CJJIof1FMoXgK6T6mC1gitXwedq-_--rVp18ItDlWPEN-_xFm7ucJp670XG52My2YJ2QqX40YsA6Iw____,http://ishare.ifeng.com/c/s/v002F2LBcr3ifdHIRGBZmvQq0NfR6-_CLMtKDPdZJkIXgZldY1S4tSqUZo--rvHtaXJMfnjVTlOO----oJnJMLCmtDxgXQ____,http://ishare.ifeng.com/c/s/v002Wc7rFFl7QfuoH1oPXtM7srQgC00usSuwHHBptajcK6zy3q7W7W82VT4DQKl9N5cDMR0Nv4DN----KczQbQMpm5mg____,http://ishare.ifeng.com/c/s/v002lCIXEuxj-_8OLiWJdNa11CCMaL--rEj6kudfUhvsklUrSCsEXwgpUsY5sU4QXQ90oiIZZavG2fhGkjG6Eq--9quBQ____,http://ishare.ifeng.com/c/s/v002P50giRCRC8bVVKvpXCUxjyy5oLR8NQbazcV2abENLyA__,http://ishare.ifeng.com/c/s/v002vED5UgM8AsGUyhY1ewSLfuP0-_dS-_Ph2Row3sJjE--ayU__,http://ishare.ifeng.com/c/s/v002mCQ7V1Yra3o2rjwtZvVxeiTx0jlRWUyFqhOLjv4xIls__,http://ishare.ifeng.com/c/s/v002jpzWulY65WLEUWCQEIzmkcpFU4udx70otw4aa5o4qBs__,http://ishare.ifeng.com/c/s/v002Wzb2GOIrm8-_iHGvUE5EDduD0ZGRvWSVqgXgnwjomw1g__,http://ishare.ifeng.com/c/s/v002l5w7Vc5PM9yz--NVDWkxVrt0B-_a39ser5UR05uQk4rsI__,http://ishare.ifeng.com/c/s/v002aNXl7zdnCO8fcJFnG3j--o9ZUu89ncPPa5-_AEBZhmhAw__,http://ishare.ifeng.com/c/s/v002ah2--5NdFJoWNNg3538ZNQBLwLfebJ00RTsMrmNByZJE__,http://ishare.ifeng.com/c/s/v002-_8PYj-_5esA8ya94H9UaT0l1G0jSFIZwLpWkh--Fi6hCg__,http://ishare.ifeng.com/c/s/v002m8D2tDvuZeYcNtBhNIedI--sEAzE-_qWxq1kUWk9f686o__,http://ishare.ifeng.com/c/s/v002qRw2BYZ8klMlUAnwd9B9Xd3eP-_7Nw8wEvd4cuooe4lE__,http://ishare.ifeng.com/c/s/v002nXTWCsB-_fDS2e2iLPzr6p0sUTFd7KwJm5oqp1Gddgxs__,http://ishare.ifeng.com/c/s/v002TEKuklUTvI63URCPB0--xCMhC1ZF7qdwElCp76Ss6L1A__,http://ishare.ifeng.com/c/s/v002QNpc1oVBFXivI24XVGbMGabfPFCv7nzr5aH6Ri5TGDE__,http://ishare.ifeng.com/c/s/v0025xwRO9NOveHL-_jPxRS4H--17TGp9dz5iBF9k5-_4X68-_Q__,http://ishare.ifeng.com/c/s/v002MGwHG1AewNtQsEdS341jPpLPM6A6pIWFl351MZ9w80E__,http://ishare.ifeng.com/c/s/v002VvpnOYknQrKDVZKIj5MKh-_fAIkOEow2ayDTRcwTaZ54v2-_vlfGqb9KOcXPhJ4tVEd2CsgLaAIsHdbWyoqvnHGg____,http://ishare.ifeng.com/c/s/v002IN3OCcnYHYLbvFhG7yvaf59yhgXJ4QZVXmsj2xrfTex1uRQjgDl00bW2vin9DnW1--bN77jglX2L0--YPyDeeftA____,http://ishare.ifeng.com/c/s/v002LHNhkHFHrbHc4g0BgdScwMlRqfPdW1kmoIItodakBis__,http://ishare.ifeng.com/c/s/v0020-_--RQGVODmMYadoYNijz-_DgKd5hgG42WpJL3nN8CRvk__,http://ishare.ifeng.com/c/s/v002BM1VwKZv752iwD23FNcHWcaYIhI9W3rZfL2jGSoXASk__,http://ishare.ifeng.com/c/s/v002CODY5P6ORuv6bqOC0wMDH60bfxXieIxSiGA6D6zoYLuac0V7bh4ApBM-_ikOO8Vg17xmOjMO-_sG9rsoxEBF5qww____,http://ishare.ifeng.com/c/s/v002y0P6e2kh0NH8QqY9AF---_L5GfUXPigOmG0Q7No2M2BvirdkT85IyzUYBOiHBd2ObOnmOGpOKgUyI1WsxervvBlw____,http://ishare.ifeng.com/c/s/v002lDjjBjd-_6YJHpBlUlRIp--tgHIa88w8CQc1OqsX44VvI__,http://ishare.ifeng.com/c/s/v002PIDAebOTdnZLTt9iQhVaKWXPj8ehT7rCKUdWGXVrotA__,http://ishare.ifeng.com/c/s/v002lqGh41Cf-_7mf2cw-_7biO2LdedgIto8jEtUxIitgIjsxUY--yJ8rsuqULkPipRzbPiuLSgIpItrYGZnrPQkg8zpA____,http://ishare.ifeng.com/c/s/v0029kQDGL--VEfF7lknaNR0N--uAX11Rz-_5tryVCVyfvI5-_Y__,http://ishare.ifeng.com/c/s/v002ViklEa-_sSLJXLSQ-_ljo7JgTmLKCLn49t9IXFVR5Okl96SrWvB4eeFqE1Rlz3iP0Vxy8liNzTEBSbKl0e1XC3Og____,http://ishare.ifeng.com/c/s/v002jM2Z0kMK4lmTKCQLitl5VxV2xmrSY9ECiqlSVDBDKyU__,http://ishare.ifeng.com/c/s/v002fkKIhcqJmOqEpokezod9254ibiGWNwdnGX9GP3hrrb2Rl9h4aVGybb0MwM3nNSzcv25qYSecf3wrdBCL1dNUTQ____,http://ishare.ifeng.com/c/s/v002jkLwfoeIqdQgyqxelFszbFITnFvAXUVT--bE0I91quac__,http://ishare.ifeng.com/c/s/v002cFyNWUTpUemA2tdxy-_lnJWz1voznpwOKyUc2e3nsVSg__,http://ishare.ifeng.com/c/s/v002NIwWp0Je6QGoLrO1TFdifZeSm1xKEJ5SDeJb7Jo7haI--JikTzCm2L7ndqqysJ3j2DQsZQ2QvMQzBYp22J0f2XA____,http://ishare.ifeng.com/c/s/v002HBpU89kQ73Fz3yxxcgcCssSmJHpr2Hiy9uOpULPSkkY__,http://ishare.ifeng.com/c/s/v002EX--jkWzHgCDVqhEB-_fco1k8emAoAwW--eU2n85EmBMLg__,http://ishare.ifeng.com/c/s/v002BlXS2ImoDYU5Po78ZN9vWzmEq0sOZ-_DmX5ILsuorW0o__,http://ishare.ifeng.com/c/s/v0023nim--TKDNA9O2vXXAi7x5Me8dy7hqYdd2o7ebof0FE7bao9qH29qXai6AEkvK-_xsRvAbVj2LPkmMCTp3eJDvgw____,http://ishare.ifeng.com/c/s/v0022rqK4CZ3G8HfbzE1h5kukBkjL--A-_ele5cn8pu14DKFg__,http://ishare.ifeng.com/c/s/v002--ZTYFQ-_WWQotUM4lKowVCnbJWQBtHae3CqTrwSCsc-_U__,http://ishare.ifeng.com/c/s/v002on2FTlgyHBy7YTyNkn1a9rvWrQWRmdkzzzDk8RRkiuA__,http://ishare.ifeng.com/c/s/v002bl8vzIEIb488XqD-_3fHwjY9k--huHugMBKMXsnb5nZ50__,http://ishare.ifeng.com/c/s/v0028OvZEOdXi9IlfYcapqqpg--CXAzA2GQ8Lpk7x9Y71iXw__,http://ishare.ifeng.com/c/s/v0020ZuKighGgxe7kb-_l--40BeOilV3IYNrP6p4QFWFa-_KKQw8G6lYVgot--WFj1WOSM7B63gWhLp41jwmYiGF61XE9w____,http://ishare.ifeng.com/c/s/v0024u2FDaz0LAsHzuKeTd42GkPXUa1By1O9LRjoOAxhr8U__,http://ishare.ifeng.com/c/s/v002RNrhPVqmFOf-_rze7XRtC29zIL5J9exQWsh2G04DiEsE__,http://ishare.ifeng.com/c/s/v002nX5NzQNUYIVRYnqfXX--wPaoIE3W2-_aPUtpdAWbq-_R0GWCx0dCewm2KnxtzWkw---_ncKvN54kG-_psVyE1EjsNubw____,http://ishare.ifeng.com/c/s/v002frl0XmLq0jUMXueD-_NWaxBfTvaCqgqtRTABEOBcMjOY6DsomjiHDXVSusxx--IKEMpMUWBbdu5tV0CBS6yyzRig____,http://ishare.ifeng.com/c/s/v002YjfW5BEIwNJkjX-_NITM-_7wtyoOk2NU0zUNHCwH4-_FNE__,http://ishare.ifeng.com/c/s/v002EZTVkZLqStEUlYdtQz2xIUkB2Tn7SIeGMx4WK--vW1J4__,http://ishare.ifeng.com/c/s/v002NfHuJtcBryUk--Jqte4RDjOVOBLgRlqbzNKNLUgjwjog__,http://ishare.ifeng.com/c/s/v002PIED--kFs--5fkJLRmbCuCfuE7M9GytqaivG3sl--uWpRk__,http://ishare.ifeng.com/c/s/v002-_rj7yCYfLkelW--AJ1i3J8C9TMC4QcvHeHgq8ElzszPJ6psTjAX3qa8r95uW5untfDvjcKTkSIVDyMFPwMEDAbw____,http://ishare.ifeng.com/c/s/v0022u61lSJDcUXwdcyyv2DPmXq36--j2Twqkq2jFdAFgRMpoT2FxpKCjtLnW0Qlmroo8-_-_pDJm6Nmk5n2VKFRsLpxA____,http://ishare.ifeng.com/c/s/v002EpyJtzchNL6byvlSgWOGFCvlF5Yr0vkTybxucuXVDUs__,http://ishare.ifeng.com/c/s/v0028Wha7ECe1G2ncLM7vFH26OgwOtnJuX-_IAjWSJ0MRFaQ2LiXTmTOfFSsVnVvjEJxW4BOTRyGC6tq2MSPlzDZ8GQ____,http://ishare.ifeng.com/c/s/v002F89UHfZluopxcwdgQXqEkQaXeOKFCbIYdUO-_6p6cru0__,http://ishare.ifeng.com/c/s/v002tAIkCdgGNwH8vrJL9PEL8wm--Wz6c--XO2VxZkGlxZPs8__,http://ishare.ifeng.com/c/s/v002Ei--AiYIKfra9HcNtbJ57QJ-_fluqqUpajoOmvoKNwKyM__,http://ishare.ifeng.com/c/s/v0026--ANdwXKwDd-_z6gHKiJ4W5vtlep2-_ryMp0rF9ZJ1ZSY__,http://ishare.ifeng.com/c/s/v002KfUCBYHsAfMCLIlDmVDAkLJ03F7XErqPr7xVqVUCFp0__,http://ishare.ifeng.com/c/s/v002NTe06uRyrmDNTRT4UmVThvpkaY0g1adD3YffX1HlfYY__,http://ishare.ifeng.com/c/s/v00240TP7zGkp9oFvCtl--yc9Xe1GZ--u2GpcYC2vcqarFGK0__,http://ishare.ifeng.com/c/s/v002kO1dKWUJcKJHqP--cAQroV9Km3wDLRBi5heDn5PsVenY__,http://ishare.ifeng.com/c/s/v002Vi--DzSWSTlCYqEj5QIYFeC0lI--P5L59lYA0M524Smfg__,http://ishare.ifeng.com/c/s/v002wl5KdQ9MvlBUMjwn55csU4eUnh5lwKSO4P2Dw9l8RIM__,http://ishare.ifeng.com/c/s/v002SUtLprHP5ck3KZRzE--A5gD55cIBmp4Nsc1xEQqKrXCQ__,http://ishare.ifeng.com/c/s/v002Bl3m6uz8tVWVd9U0mIQHoSEWArS2cRYWdV1KGCV-_pEg__,http://ishare.ifeng.com/c/s/v002YIVWzWry23heCY0euLDU6HA04--i7p0hgSGkvXfRDDF4__,http://ishare.ifeng.com/c/s/v002Pfy41DjfbCL2Q25uN8SnWqrh7u0ukrH0NoJY8jWBkuc__,http://ishare.ifeng.com/c/s/v0023FuaamVSlCqPy6gScdlwAJ94ceoXjiFI8qT62FnYHys__,http://ishare.ifeng.com/c/s/v002k9-_a4rPKZ--j3LzvsU9QRQHnPdo5EVZEluGAQ1NmPPPw__,http://ishare.ifeng.com/c/s/v0027JodLslyNNGckgPFCydaTj7CRw5Iw--1M46LkLPslslk__,http://ishare.ifeng.com/c/s/v002zs2JTMjI1YTboaE1yygYB7p2--WS-_tlkFKrTAjnGgGhI__,http://ishare.ifeng.com/c/s/v002EAvN0QOcLmZnOViPTOoDd8vAGvwKZG8J-_OaEQBMgguU__,http://ishare.ifeng.com/c/s/v002SsiHt0Iuyla21o--XwwcCZS1lVWQEwtPDKZP3tFc8RtkJGJ6OEF95fHMyDylPZMzk0pHDAG40--V6e072JaA6O--w____,http://ishare.ifeng.com/c/s/v002z5iNoxi-_LCiuuCTk--C3Qdi5pjLDSl2K6y8b33--m2Ns0__,http://ishare.ifeng.com/c/s/v002IGdDE06kErErbzMQf7yul80D8shRbF--iAIrSb4zh3PnI4emD0eZfqAQx--NT--D3REaWiZtXyuskRgpPNNtwSpUg____,http://ishare.ifeng.com/c/s/v002JrKX--jJtuZEQX5Sn6Yz--4MKDvOyoG1XV-_IVOEAXPB1s__,http://ishare.ifeng.com/c/s/v002YM8yFQbARdb--Jf9f3N3lSXrX3IFNbl3AgGLWKkYBl1QnWMcdxPkOkA7XGrHJHXHXZewOh8Dif4cyrGIgZ7p3HA____,http://ishare.ifeng.com/c/s/v002s6St3fjTYw3xG5KntQP4jMikuwOLzTKpSkHd6WXxtFDhAhILcn59S2vsFx-_C--fg--bK7iBW0MLYzOcQtlcKlI2A____,http://ishare.ifeng.com/c/s/v002Jba1nKQW9bJ6--WpiWebSjpf32duLxD1BjbSoyio7C8yoQSW4VxYRLu5Ig-_c4QFBmCxDd0V1WKW4CRM2uWIMTjQ____,http://ishare.ifeng.com/c/s/v002--nILmJ3CZjyPNwlI6xr8N7mJ0JScMOlW1O7EJdXY6jRbziyImwNkL7aOpYpPxNPfnn2ixkkcI7oIUnKG40Q9UA____,http://ishare.ifeng.com/c/s/v002Dp2TySMqHOOswWDcmqxIno75A9GpdDkL51hZF50xo3o__,http://ishare.ifeng.com/c/s/v002ABDwI3G9MSrT9bbnEB-_dmxZXeOuV4X9pTrwLeJZOQpo__,http://ishare.ifeng.com/c/s/v002VGwKy70rXv--9lrCNvDz7Cjl3FReYLwzkU4Zi7BuDiEVFPNMkU7dyMgnSLLQXDojtY5e3DRXyqqKw1UdwJa8BLQ____,http://ishare.ifeng.com/c/s/v002okQgaVeAXDMWBt1ML17p9WuxXsretsRiQPIWTD-_Vl-_w__,http://ishare.ifeng.com/c/s/v002E48fLmRioZzirAWf5FZl9ovtSTPbaWzi2MQpE0nUW7k__,http://ishare.ifeng.com/c/s/v002tzrPJE--YrHgJiB4hasgvPGWP6vyBin4JGMWBBhJuP-_w__,http://ishare.ifeng.com/c/s/v002iBe1-_oU3UEVfuIbK0RSo--Y7iIZAk-_ptdyZ1HpxAN-_Mk__,http://ishare.ifeng.com/c/s/v002YjfW5BEIwNJkjX-_NITM-_7--ae860qUJQnee1HIRvcyPE__,http://ishare.ifeng.com/c/s/v002MvM6--wd7I1fm--lwEYdR0RfILAjvqkaknamERXJGfo4A__,http://ishare.ifeng.com/c/s/v002KfUCBYHsAfMCLIlDmVDAkDNdoxIibGUAyvpxZSXGdx8__,http://ishare.ifeng.com/c/s/v0026--ANdwXKwDd-_z6gHKiJ4W9PUSOt3yIDTQpaxa6d6u2A__,http://ishare.ifeng.com/c/s/v002dni3oHo2lxf9D9J7QMl-_c--Y7-_M6-_3x2ozPOtOdzZ8zQ__,http://ishare.ifeng.com/c/s/v002Nvm55HeLSgldADC0sTUPRw-_08K5fCfoa28gIUeGc6LA__,http://ishare.ifeng.com/c/s/v002TxEgBmMvwxQLwWRUWyNUTX-_6dvdQu5ZJL5CYD9Qq5vk__,http://ishare.ifeng.com/c/s/v0020-_--RQGVODmMYadoYNijz-_PoFQopVHUcPVhINwQhF9Mk__,http://ishare.ifeng.com/c/s/v002fH51ajaQIzmFZXBlf1G--i0ZTB---_5flJJYRBE7nLsQyk__,http://ishare.ifeng.com/c/s/v002J47XHxwraP-_bmpWTuQxmjtR6TyCGul--3llB8ZR66aZEUFoXwCw8RsBzPrChV38i7vRGiu3uKl4oGEudXuSPO0w____,http://ishare.ifeng.com/c/s/v002Wc7rFFl7QfuoH1oPXtM7srQgC00usSuwHHBptajcK6xeKmJpktuS0RHsTXgrzxRctVtHTRFIqqehe3L43Zk5yA____,http://ishare.ifeng.com/c/s/v002lCIXEuxj-_8OLiWJdNa11CCMaL--rEj6kudfUhvsklUrSeE--SUY5wWCpROdhBe9kBhkm-_bDkfFsBy8vnsFXyf1jg____,http://ishare.ifeng.com/c/s/v002dPECWZfOOWGY--3bTexfA4tKxrjF5rORvO3yg2HxhSlM8D3mTWyXwuhIr8bAsB5pbpfTQfbuy3sQutIJSg-_Uz--g____,http://ishare.ifeng.com/c/s/v002Yq7YaYi3IOdFc4ap9KsAWH5KpYc3--RtvS4AFEkuCPwiYshr8zTdlZaLqkqDQ8BcfoiBeXoXtL7fJzlaj2z-_-_SA____,http://ishare.ifeng.com/c/s/v002dq6NmZ-_--3e2CJJIof1FMoXgK6T6mC1gitXwedq-_--rVoIC-_jgDMawx--Z7OyQ2-_t8anM4--hTa4CXdYdtVD1k--PQw____,http://ishare.ifeng.com/c/s/v002F2LBcr3ifdHIRGBZmvQq0NfR6-_CLMtKDPdZJkIXgZlfqxExvaXGI-_7Grn5vSElYIX54nuh-_zCxG7ZehZupy6dA____,http://ishare.ifeng.com/c/s/v002vED5UgM8AsGUyhY1ewSLfnEtHRKoFvFvcYbjD6wS1DU__,http://ishare.ifeng.com/c/s/v002mCQ7V1Yra3o2rjwtZvVxegGsXge29yVqWLXE-_2--4uWI__,http://ishare.ifeng.com/c/s/v0022rqK4CZ3G8HfbzE1h5kukPWfUwAQhf7nRjgjpFWL9Jo__,http://ishare.ifeng.com/c/s/v002P50giRCRC8bVVKvpXCUxjy6bOQyxYZF-_oqjztYnHHwA__,http://ishare.ifeng.com/c/s/v002aNXl7zdnCO8fcJFnG3j--o8x6vHitmzxdTP7YH7F1s3I__,http://ishare.ifeng.com/c/s/v002okQgaVeAXDMWBt1ML17p9fVt1uoaXehlwbCJIbA3LoY__,http://ishare.ifeng.com/c/s/v002Dp2TySMqHOOswWDcmqxInqy-_5AcntyUE1zulX1elNAQ__,http://ishare.ifeng.com/c/s/v002jpzWulY65WLEUWCQEIzmkacR2fBG3b0tlmGII4LPlL4__,http://ishare.ifeng.com/c/s/v002l5w7Vc5PM9yz--NVDWkxVrhw-_--ZgQhPRXFaHcLXITthA__,http://ishare.ifeng.com/c/s/v002qRw2BYZ8klMlUAnwd9B9XXEOahYpQLqJUbF7a-_LztH4__,http://ishare.ifeng.com/c/s/v002ah2--5NdFJoWNNg3538ZNQDUlzaL7X9lwcuvpMH52qKQ__,http://ishare.ifeng.com/c/s/v002-_8PYj-_5esA8ya94H9UaT0rIH4gTksrC-_slrr5ByiXlM__,http://ishare.ifeng.com/c/s/v002Wzb2GOIrm8-_iHGvUE5EDdi--3CR4VnX4tv4NCumZg6n8__,http://ishare.ifeng.com/c/s/v002nXTWCsB-_fDS2e2iLPzr6p6AXT4zpX90wUNxzICAee3M__,http://ishare.ifeng.com/c/s/v002TEKuklUTvI63URCPB0--xCH4bCmP1U--GxW2NxaeQERUY__,http://ishare.ifeng.com/c/s/v002m8D2tDvuZeYcNtBhNIedI28wLuI7qS2TO4hRB0fY3FI__,http://ishare.ifeng.com/c/s/v002MGwHG1AewNtQsEdS341jPpOdG9YQWJUSlcNetUboGiI__,http://ishare.ifeng.com/c/s/v002QNpc1oVBFXivI24XVGbMGUcQGZ4aVQRz3uClo--tX9No__,http://ishare.ifeng.com/c/s/v0025xwRO9NOveHL-_jPxRS4H--1tAqsr9VkTL6AW2o33GB3A__,http://ishare.ifeng.com/c/s/v002IN3OCcnYHYLbvFhG7yvaf59yhgXJ4QZVXmsj2xrfTewK73w2FcnM6Aq5NNuqxmEu4GHvKNw5lJ39Ar6w6XnBag____,http://ishare.ifeng.com/c/s/v0022cY-_kwW--DHLtGf8-_j8mOvD4K5Ew7MjW2wqPfbEUB2iQ__,http://ishare.ifeng.com/c/s/v002CODY5P6ORuv6bqOC0wMDH60bfxXieIxSiGA6D6zoYLusu3tgaRDyfKRQB1wEzIUCHtmJ7NDhVGQIowwKHFp2eg____,http://ishare.ifeng.com/c/s/v002LHNhkHFHrbHc4g0BgdScwElxH4n--9rHqeRTg0CDIaJ0__,http://ishare.ifeng.com/c/s/v002VvpnOYknQrKDVZKIj5MKh-_fAIkOEow2ayDTRcwTaZ55Vh-_JzkctAR8kXetc--08S--CY0-_--ud908cmOpGtJnLnwg____,http://ishare.ifeng.com/c/s/v002lDjjBjd-_6YJHpBlUlRIp--ntpGYGdM3j4zFvD1wtHEvA__,http://ishare.ifeng.com/c/s/v002y0P6e2kh0NH8QqY9AF---_L5GfUXPigOmG0Q7No2M2Bvh72sfIaHv9CAayQgWyf7XfMsklyvoaKSb1dn2M7dYVpw____,http://ishare.ifeng.com/c/s/v002lqGh41Cf-_7mf2cw-_7biO2LdedgIto8jEtUxIitgIjsxVjyf0xHJjdXoR910JSM1wlJ2MbXlrPZyb4B5k--vP4mQ____,http://ishare.ifeng.com/c/s/v002BM1VwKZv752iwD23FNcHWfXYeiuuuKdcpNwqm0PtDhE__,http://ishare.ifeng.com/c/s/v0029kQDGL--VEfF7lknaNR0N--v1Glj--t6GrYGHsxwsXQZ6Q__,http://ishare.ifeng.com/c/s/v002jM2Z0kMK4lmTKCQLitl5V57DApsmZ8NpC9i-_D-_cGC--M__,http://ishare.ifeng.com/c/s/v002ViklEa-_sSLJXLSQ-_ljo7JgTmLKCLn49t9IXFVR5Okl9KnCZTKyuBKp5cd4boVh6gnEssKSUB9Gk75WGvGOo-_Rw____,http://ishare.ifeng.com/c/s/v002PIDAebOTdnZLTt9iQhVaKUHIKZ3rw3Bwl6-_OngGFBcw__,http://ishare.ifeng.com/c/s/v002fkKIhcqJmOqEpokezod9254ibiGWNwdnGX9GP3hrrb3-_7-_nq6Dp8eXu1qcvYNBDWDaVwFPsM39HWBPu1SZPcLA____,http://ishare.ifeng.com/c/s/v002NIwWp0Je6QGoLrO1TFdifZeSm1xKEJ5SDeJb7Jo7haJQ2p4IRGeG41MYgxRJ33Cnpe--3ENC3kZ6dDr6rdcASIA____,http://ishare.ifeng.com/c/s/v002jkLwfoeIqdQgyqxelFszbO85zeYy9v8NuHg3kG9CA60__,http://ishare.ifeng.com/c/s/v002cFyNWUTpUemA2tdxy-_lnJcHAjMiMBk3Of6BA51gxVAc__,http://ishare.ifeng.com/c/s/v002BlXS2ImoDYU5Po78ZN9vW--BwuOj7B1Ng6aJTIi-_9-_Dw__,http://ishare.ifeng.com/c/s/v002bl8vzIEIb488XqD-_3fHwjRMqjAEgmzwEGsvd3ut8P4U__,http://ishare.ifeng.com/c/s/v002-_eT3heTSVaUcRcPLNy77W-_KHGmyjz--eJZBMBxFxTRkEmO3RrvtWstCS1FQRrkVlBaLIxe2iXd0HMLipPWx--gFQ____,http://ishare.ifeng.com/c/s/v0023nim--TKDNA9O2vXXAi7x5Me8dy7hqYdd2o7ebof0FE6xViAQ1mPetxojcLY2icI69AjTem43lg--W0SycKr9A6w____,http://ishare.ifeng.com/c/s/v002HBpU89kQ73Fz3yxxcgcCsirZSQu36wJ2pY2KI1s2Zd8__,http://ishare.ifeng.com/c/s/v002EX--jkWzHgCDVqhEB-_fco1nCiGx7p3sJF0mGVgExAI7M__,http://ishare.ifeng.com/c/s/v002U7B1ItijeqpcVF1kefwCTwuqOxaTB--8WfrcgNPpafvE__,http://ishare.ifeng.com/c/s/v0020ZuKighGgxe7kb-_l--40BeOilV3IYNrP6p4QFWFa-_KKTcTIbkoUWkwoH--DoszA0KNiOBKEcDVgqJk78ejibLdzg____,http://ishare.ifeng.com/c/s/v002--ZTYFQ-_WWQotUM4lKowVCsyuydzb4h1triMO0MsbCXk__,http://ishare.ifeng.com/c/s/v0028OvZEOdXi9IlfYcapqqpg--25RvpAK4FAxZnFmsKMBp4__,http://ishare.ifeng.com/c/s/v002xDkEI5DmMeDMnTrEivtk--BspPjQ0X-_q3JfZdRCl--Y48__,http://ishare.ifeng.com/c/s/v002Vey2Cf8vFtNyNJxojQtQHGMaP9IA-_x0gg6Ui9--FBdcs__,http://ishare.ifeng.com/c/s/v002on2FTlgyHBy7YTyNkn1a9h4CtQ60FqOT5G2LWCPsezk__,http://ishare.ifeng.com/c/s/v002RNrhPVqmFOf-_rze7XRtC2zD8ERBxf7l1DFffi1n3TwM__,http://ishare.ifeng.com/c/s/v002M6bAhSMIPjsQVUw--aonFoiKs7rUItRXD9rgu0oBf80w__,http://ishare.ifeng.com/c/s/v002nX5NzQNUYIVRYnqfXX--wPaoIE3W2-_aPUtpdAWbq-_R0F0HR61q4XWZR9d-_8Yhq5jszNEcUU--6AcGMyIq--p4G7--Q____,http://ishare.ifeng.com/c/s/v0024u2FDaz0LAsHzuKeTd42Gq6zrl--xmLRPQlJ5Dte6crc__,http://ishare.ifeng.com/c/s/v002jixwYLJqgeFn17gETqOdALy3ZgXQ0--xf2MiQpQpzFDIYxFVIM5V2Sw7kR9KYEjJQVMrwzuhlh6nkMBJgzTzYoQ____,http://ishare.ifeng.com/c/s/v00292VcphCPssqR9nw95xQ-_EPPz9cZl1rkFoWSxeG--HZ44__,http://ishare.ifeng.com/c/s/v002NfHuJtcBryUk--Jqte4RDjB8uPKIaJkJno4sZUbLiobA__,http://ishare.ifeng.com/c/s/v002-_rj7yCYfLkelW--AJ1i3J8C9TMC4QcvHeHgq8ElzszPKkf--LM8jUN5E541TnP5DF4bhXwf5h-_aw--Q0QWR0BR37w____,http://ishare.ifeng.com/c/s/v0022u61lSJDcUXwdcyyv2DPmXq36--j2Twqkq2jFdAFgRMrYi4AwEEKku--X9VSeA-_G6YpczsK-_SyOJ7UMYjPukxlBA____,http://ishare.ifeng.com/c/s/v002frl0XmLq0jUMXueD-_NWaxBfTvaCqgqtRTABEOBcMjOZuG0DS0ivnsREPnAs-_gci8HqqZ2zl7D7Ie3kDmnfyHtQ____,http://ishare.ifeng.com/c/s/v002Tb1dwwBxfNzAHJwsQtjWmJAblOgB-_vAMcu--GEp--9zWA__,http://ishare.ifeng.com/c/s/v002EpyJtzchNL6byvlSgWOGFPDjGmZtT6N1wBJKpbyz32A__,http://ishare.ifeng.com/c/s/v0028Wha7ECe1G2ncLM7vFH26OgwOtnJuX-_IAjWSJ0MRFaTLGSY2kzKTSIgGSCiHmLB6tsGiymIYjRxmG3KX1fJKcA____,http://ishare.ifeng.com/c/s/v002rmQ--znM9ItHAoVp97KpERZWs4yfw1AwILUbMK3GGSeU__,http://ishare.ifeng.com/c/s/v002EZTVkZLqStEUlYdtQz2xIZrP-_L2A2ZQXoRMV4D--D17I__,http://ishare.ifeng.com/c/s/v00290WdDT7grg4IEnhmhh8zqY5qN--Lu--oLcFMpvWver6Tk__,http://ishare.ifeng.com/c/s/v002Ei--AiYIKfra9HcNtbJ57QC---_hU4UV7U9Y2mol2IkBOI__,http://ishare.ifeng.com/c/s/v002KrJRHW99jIqJX3Y9XWtDJ7KPnxlHhOfwTyx8DIVE8iY__,http://ishare.ifeng.com/c/s/v002F89UHfZluopxcwdgQXqEkVmif--at7BvRqDgIbtMl--ZU__,http://ishare.ifeng.com/c/s/v002SUtLprHP5ck3KZRzE--A5gDPBE9vn3W3ZQGoONAU6WV0__,http://ishare.ifeng.com/c/s/v0027JodLslyNNGckgPFCydaTgtJMhmbLQebazEGkhtrhTk__,http://ishare.ifeng.com/c/s/v002PIED--kFs--5fkJLRmbCuCfv7z1ktqbiPLtcT-_hLqiiSQ__,http://ishare.ifeng.com/c/s/v002Vi--DzSWSTlCYqEj5QIYFeAI3DFEI34Xi--NCXgge1yp8__,http://ishare.ifeng.com/c/s/v002kO1dKWUJcKJHqP--cAQroVw3vAsYHtctsCaCW9uqZPHg__,http://ishare.ifeng.com/c/s/v002Pfy41DjfbCL2Q25uN8SnWj9LyPBrO3XYdg816DosOXk__,http://ishare.ifeng.com/c/s/v002tAIkCdgGNwH8vrJL9PEL87QBFXgyoAW3SmjIMEeqXPg__,http://ishare.ifeng.com/c/s/v002k9-_a4rPKZ--j3LzvsU9QRQB8U3lOOCCstIhU8kJt1cbU__,http://ishare.ifeng.com/c/s/v002UXLugZmlWZkcx--mw3qhlUQGAQODzlIeSL9qM3yLu4nM__,http://ishare.ifeng.com/c/s/v00240TP7zGkp9oFvCtl--yc9XcLSrgpfusIIzW5LyXk2w0A__,http://ishare.ifeng.com/c/s/v002zs2JTMjI1YTboaE1yygYB475YSSK5l-_r5adjirVjVRM__,http://ishare.ifeng.com/c/s/v002wl5KdQ9MvlBUMjwn55csUwSgAtj7IpPxyq--l39RmkBg__,http://ishare.ifeng.com/c/s/v002NTe06uRyrmDNTRT4UmVThuajKlqyv8ybhxFsMu0Kv2A__,http://ishare.ifeng.com/c/s/v002z5iNoxi-_LCiuuCTk--C3Qdl4whvghSbYyIxrlbmveRw0__,http://ishare.ifeng.com/c/s/v0023FuaamVSlCqPy6gScdlwAAv-_G5mHvyapC--BMB6J0DTI__,http://ishare.ifeng.com/c/s/v002EAvN0QOcLmZnOViPTOoDdyVA2Ko8xVhaW4Ekoxh6bPc__,http://ishare.ifeng.com/c/s/v002Bl3m6uz8tVWVd9U0mIQHoYNyX8Mq3GI0nier1hVcI3U__,http://ishare.ifeng.com/c/s/v002SsiHt0Iuyla21o--XwwcCZS1lVWQEwtPDKZP3tFc8RtmZE50MokGDGeCQ0M5v2iBhuzrG7--kUCnTNkJV--99V75w____,http://ishare.ifeng.com/c/s/v002JrKX--jJtuZEQX5Sn6Yz--4PAV6codEyx1uhLILibAnZg__,http://ishare.ifeng.com/c/s/v002YIVWzWry23heCY0euLDU6Kv0hNLvpxpsapT548nVAJM__,http://ishare.ifeng.com/c/s/v002IGdDE06kErErbzMQf7yul80D8shRbF--iAIrSb4zh3PmxSrd-_--sfOGc4KD-_hmvjUU9TrTgosRfeBgIZ-_Cln38vQ____,http://ishare.ifeng.com/c/s/v002s6St3fjTYw3xG5KntQP4jMikuwOLzTKpSkHd6WXxtFAdwXQA6ltwU--W0gTqewz-_xxPmhqjdpsQVWs69eTy27uw____,http://ishare.ifeng.com/c/s/v002Jba1nKQW9bJ6--WpiWebSjpf32duLxD1BjbSoyio7C8zfW9M357rY8jhKIziHZTDey-_xGxFTWBTmT--6FbwI2nZQ____,http://ishare.ifeng.com/c/s/v002--nILmJ3CZjyPNwlI6xr8N7mJ0JScMOlW1O7EJdXY6jShf60-_CJxx--clcRMroz1uQNPHAoGFKoGlNVv9En5PFBA____,http://ishare.ifeng.com/c/s/v002YM8yFQbARdb--Jf9f3N3lSXrX3IFNbl3AgGLWKkYBl1TGOBlBzKg--k8hfqW1e3yn4XSflwt5dbPIU9nRFzggqFQ____,http://ishare.ifeng.com/c/s/v002ABDwI3G9MSrT9bbnEB-_dmxaJbm6mqXEfyjz3LAAx4kc__,http://ishare.ifeng.com/c/s/v002VGwKy70rXv--9lrCNvDz7Cjl3FReYLwzkU4Zi7BuDiEXxH5b50CSboNZSBM4v1Ou22LLlvYvELRIT8Yr4HjHBLw____,http://ishare.ifeng.com/c/s/v002E48fLmRioZzirAWf5FZl9twHeejLYEWsPcIzzfNGepo__,http://ishare.ifeng.com/c/s/v002tzrPJE--YrHgJiB4hasgvPB0P1o--6TfIlooAwWdrW9bY__,http://ishare.ifeng.com/c/s/v002iBe1-_oU3UEVfuIbK0RSo--RSalNNp0XiG5RpVi7s-_uxI__,https://weibo.com/5293558377/LiWlqFK8f?refer_flag=1001030103_,https://www.toutiao.com/a7073008939876483598/?channel=&source=search_tab&,http://ishare.ifeng.com/c/s/v002MvM6--wd7I1fm--lwEYdR0RVSQF6UNdl4--fg91oKxaJLo__,http://ishare.ifeng.com/c/s/v002KfUCBYHsAfMCLIlDmVDAkMxi7mFQ9Zj3h--rjfFCyBQ4__,http://ishare.ifeng.com/c/s/v002YjfW5BEIwNJkjX-_NITM-_7--UYF4Gv42jvp1kg9qnjwK4__,https://weibo.com/2177412401/LiWdjstJx?refer_flag=1001030103_,http://ishare.ifeng.com/c/s/v002CODY5P6ORuv6bqOC0wMDH60bfxXieIxSiGA6D6zoYLvNf5Wr--Zxf0Eu3COIcycYJeOv4pqApvrwZfPsP6x--ohw____,http://ishare.ifeng.com/c/s/v002LHNhkHFHrbHc4g0BgdScwKOxg4e1OHFsv5lMJ0QRE9w__,http://ishare.ifeng.com/c/s/v002BM1VwKZv752iwD23FNcHWTzNMjQpXa52yimDNU5PnRg__,http://ishare.ifeng.com/c/s/v002lqGh41Cf-_7mf2cw-_7biO2LdedgIto8jEtUxIitgIjsziV5It--ZiEr0fW6vtk6-_8Z0mcMoXOHW1VBhG3-_gwmaew____,http://ishare.ifeng.com/c/s/v002y0P6e2kh0NH8QqY9AF---_L5GfUXPigOmG0Q7No2M2BvgtVGygtkhsY-_-_AZ8cFJMyP0upXQ9aw6Y9GinmOEloPsQ____,http://ishare.ifeng.com/c/s/v002lDjjBjd-_6YJHpBlUlRIp--r7c4mPYCEBe08W4JuPs7dM__,http://ishare.ifeng.com/c/s/v0029kQDGL--VEfF7lknaNR0N--hnOLzc--3NUdQ2gOy--RXZCo__,http://ishare.ifeng.com/c/s/v002jM2Z0kMK4lmTKCQLitl5V4JSY1-_LACLxU8Fk1ou9BNM__,http://ishare.ifeng.com/c/s/v002ViklEa-_sSLJXLSQ-_ljo7JgTmLKCLn49t9IXFVR5Okl--ve0--YhgmmRarT1fsqgP92U5V--gW2hqBGLQBgWz5Xetg____,http://ishare.ifeng.com/c/s/v002PIDAebOTdnZLTt9iQhVaKVo2696GhxjNEBFvcHNhv4w__,http://ishare.ifeng.com/c/s/v002NIwWp0Je6QGoLrO1TFdifZeSm1xKEJ5SDeJb7Jo7haJ4pJQ-_MMLtddba-_nU7GbxeZhSKeneuOEe3hxHkulx79A____,http://ishare.ifeng.com/c/s/v002fkKIhcqJmOqEpokezod9254ibiGWNwdnGX9GP3hrrb1Kab--pUxTTbL0Sfti09lq--tlH7v--RBJivyKPpkFuIW0A____,http://ishare.ifeng.com/c/s/v002jkLwfoeIqdQgyqxelFszbIgHlbC-_5mtNo7tuBdeU2dI__,http://ishare.ifeng.com/c/s/v002cFyNWUTpUemA2tdxy-_lnJUicaKKpP37kQXrXhHVtsqc__,http://ishare.ifeng.com/c/s/v002BlXS2ImoDYU5Po78ZN9vW5TpZbys0Qo1cSUrGiK6zS8__,http://ishare.ifeng.com/c/s/v002HBpU89kQ73Fz3yxxcgcCsgE7fFdPhzypec0bvg--rvTg__,http://ishare.ifeng.com/c/s/v002EX--jkWzHgCDVqhEB-_fco1ku2NTmj335UBaxwW48CiA8__,http://ishare.ifeng.com/c/s/v002--ZTYFQ-_WWQotUM4lKowVCqYwv9MQOFW9OQ5Xt6Na--TA__,http://ishare.ifeng.com/c/s/v002on2FTlgyHBy7YTyNkn1a9u1pRla9--v0Yc-_-_QKm1nAKc__,http://ishare.ifeng.com/c/s/v002bl8vzIEIb488XqD-_3fHwjbt4vF9xBwR59x6LMimALBQ__,http://ishare.ifeng.com/c/s/v002IN3OCcnYHYLbvFhG7yvaf59yhgXJ4QZVXmsj2xrfTeyGZYDrFug3Fm9y3wV-_dOChfsGu07FXhY06DM-_9NxrEbw____,http://ishare.ifeng.com/c/s/v0023nim--TKDNA9O2vXXAi7x5Me8dy7hqYdd2o7ebof0FE6VhkqgMiYHAtYPYemdWvfevKhUeEaC3nAaicH1-_2OlGw____,http://ishare.ifeng.com/c/s/v0028OvZEOdXi9IlfYcapqqpg1khrccEi46qfdQInswepSc__,http://ishare.ifeng.com/c/s/v0022cY-_kwW--DHLtGf8-_j8mOvE2Y44r0wy7bHjd3j3sz7po__,http://ishare.ifeng.com/c/s/v0020ZuKighGgxe7kb-_l--40BeOilV3IYNrP6p4QFWFa-_KKQtWtqAUtUi-_X0Qb61Dxj-_f3X-_nq--9A4TV8jswTz807ZA____,http://ishare.ifeng.com/c/s/v002nX5NzQNUYIVRYnqfXX--wPaoIE3W2-_aPUtpdAWbq-_R0Gs--T6BlZORlw0IbOxtIU5vdFcQdZoDpSonZIxHKn8KRA____,http://ishare.ifeng.com/c/s/v002frl0XmLq0jUMXueD-_NWaxBfTvaCqgqtRTABEOBcMjObVv8zw5GpGm8dPv0j4ifxau62Fg5qiBqq7nFGv--WWSMg____,http://ishare.ifeng.com/c/s/v002RNrhPVqmFOf-_rze7XRtC26rLKahxWRsSY-_qcJuKRDLE__,http://ishare.ifeng.com/c/s/v0024u2FDaz0LAsHzuKeTd42Gk4AC41JvAYgVigAqkGXf64__,http://ishare.ifeng.com/c/s/v002EZTVkZLqStEUlYdtQz2xIbHoJ5rwTilUDO1wQeKUacU__,http://ishare.ifeng.com/c/s/v002NfHuJtcBryUk--Jqte4RDjAqzDG2KUkZe4V42sdkAU3g__,http://ishare.ifeng.com/c/s/v002-_rj7yCYfLkelW--AJ1i3J8C9TMC4QcvHeHgq8ElzszPIpVxXdtWXVzSnbnmxtoaR7yQf72wLZtPmZOaYCGCrEeA____,http://ishare.ifeng.com/c/s/v0022u61lSJDcUXwdcyyv2DPmXq36--j2Twqkq2jFdAFgRMokkFNrElb--RKjECmzMUDAWDB08lC1gYkHd--9lvRgVduw____,http://ishare.ifeng.com/c/s/v0028Wha7ECe1G2ncLM7vFH26OgwOtnJuX-_IAjWSJ0MRFaQ1yoHWOQ9mJrrkNZAnWOeja66T1O7bG7Ryh68QxSJWGw____,http://ishare.ifeng.com/c/s/v0026--ANdwXKwDd-_z6gHKiJ4W-_pmNbjSULeVLT5mmvbNjdg__,http://ishare.ifeng.com/c/s/v002Vi--DzSWSTlCYqEj5QIYFeNqCCC0O8--UujJ7NhAF0D9k__,http://ishare.ifeng.com/c/s/v002SUtLprHP5ck3KZRzE--A5gHXNYQrIcZBQmIcBuvNbJHU__,http://ishare.ifeng.com/c/s/v0027JodLslyNNGckgPFCydaTqTRRm3THE--i9zvvEUVwllc__,http://ishare.ifeng.com/c/s/v002kO1dKWUJcKJHqP--cAQroVySAWG0nw2--G--04cmpD6xmQ__,http://ishare.ifeng.com/c/s/v002zs2JTMjI1YTboaE1yygYB9Q1Eme-_u1vDM1dpw-_9bwFo__,http://ishare.ifeng.com/c/s/v002k9-_a4rPKZ--j3LzvsU9QRQBECo1kQ-_2--hafYxnI2XNVk__,http://ishare.ifeng.com/c/s/v002Pfy41DjfbCL2Q25uN8SnWpatoy2uBr--XGGsI772nw1A__,http://ishare.ifeng.com/c/s/v002EAvN0QOcLmZnOViPTOoDd6afo-_gEUysKS1hk-_slFbTM__,http://ishare.ifeng.com/c/s/v002F89UHfZluopxcwdgQXqEkUbN2dCRye6BZTBtx9t2BSQ__,http://ishare.ifeng.com/c/s/v002SsiHt0Iuyla21o--XwwcCZS1lVWQEwtPDKZP3tFc8Rtl29kt-_a8Cq7Cs4z-_ezxuj5YL2ZERwq1OvVV88pyENi4Q____,http://ishare.ifeng.com/c/s/v002EpyJtzchNL6byvlSgWOGFD408NhvbUgovHqZbPDr--gM__,http://ishare.ifeng.com/c/s/v002Ei--AiYIKfra9HcNtbJ57QJq2eaZe04O0bjou2ycvnVg__,http://ishare.ifeng.com/c/s/v002JrKX--jJtuZEQX5Sn6Yz--4Cr8yTW9yJJeuvCKcrDaS44__,http://ishare.ifeng.com/c/s/v002IGdDE06kErErbzMQf7yul80D8shRbF--iAIrSb4zh3PlmAxGFXkwZt1moWZZGqYui-_GXwlaBM3sSG09H3n9pL-_g____,http://ishare.ifeng.com/c/s/v002z5iNoxi-_LCiuuCTk--C3QdrKSUbnYEb2WVKROUCEp9Mg__,http://ishare.ifeng.com/c/s/v002s6St3fjTYw3xG5KntQP4jMikuwOLzTKpSkHd6WXxtFD11j76Krh-_cq2T1qD31o0j3t6IRk06lE58Le0u--Vyp6Q____,http://ishare.ifeng.com/c/s/v002Jba1nKQW9bJ6--WpiWebSjpf32duLxD1BjbSoyio7C8y6ylDo7m7c--T6c5HqyqetWybNpr--zqSFeqsqOtX46INg____,http://ishare.ifeng.com/c/s/v002--nILmJ3CZjyPNwlI6xr8N7mJ0JScMOlW1O7EJdXY6jRLntb5bo1zppMCZ8k2xNKxF6h5rF2-_fh1xqqYvsmGFLQ____,http://ishare.ifeng.com/c/s/v002YM8yFQbARdb--Jf9f3N3lSXrX3IFNbl3AgGLWKkYBl1T42wvN9A8WZFWQCHghKM7aOwwjWxLVEsSoOBQjFobizg____,http://ishare.ifeng.com/c/s/v002iBe1-_oU3UEVfuIbK0RSo--Wyh2QqZ--3CvcFWFcn0wjO8__,http://ishare.ifeng.com/c/s/v002ABDwI3G9MSrT9bbnEB-_dm6-_65sDoyb4JLPtlKUrkiUs__,http://ishare.ifeng.com/c/s/v002VGwKy70rXv--9lrCNvDz7Cjl3FReYLwzkU4Zi7BuDiEUI2Mc1-_qH6YjUmpTpZ-_GiBaCrEQn9C67ATsDO756P91Q____,http://ishare.ifeng.com/c/s/v002tzrPJE--YrHgJiB4hasgvPDir2Ui8CbVWhT--5N5CfucQ__,http://ishare.ifeng.com/c/s/v002E48fLmRioZzirAWf5FZl9o-_QM01D6--xHF5bRzo6yF5U__,https://weibo.com/3848311310/LiW5toxa5?refer_flag=1001030103_,https://weibo.com/5872828878/LiW2IzGR3?refer_flag=1001030103_,https://weibo.com/1663072851/LiW1Engwl?refer_flag=1001030103_,http://ishare.ifeng.com/c/s/v002-_eT3heTSVaUcRcPLNy77W-_KHGmyjz--eJZBMBxFxTRkHs2075Ca6a8zOaKMvNK-_tmXQblt0BNnp-_9hukj66Y1VA____,http://ishare.ifeng.com/c/s/v002VvpnOYknQrKDVZKIj5MKh-_fAIkOEow2ayDTRcwTaZ56aHkYTSavD-_1coLHvXo864bS3B--C8hyWK5EgVG4--QYlg____,http://ishare.ifeng.com/c/s/v002U7B1ItijeqpcVF1kefwCT-_gRncpoI2eykYwMwzSsVWw__,http://ishare.ifeng.com/c/s/v002Vey2Cf8vFtNyNJxojQtQHMEC0PvzhFAQgNwTNGR8nqY__,http://ishare.ifeng.com/c/s/v002xDkEI5DmMeDMnTrEivtk--F0TIUGmBkSuHJeJDAQ3H3c__,http://ishare.ifeng.com/c/s/v00292VcphCPssqR9nw95xQ-_ELLdMxPjIWqR3nlfFyrj-_gI__,http://ishare.ifeng.com/c/s/v002Tb1dwwBxfNzAHJwsQtjWmJ-_7BlUGddlGkCTtNUgJXz8__,http://ishare.ifeng.com/c/s/v002M6bAhSMIPjsQVUw--aonFojeLjuwJAxFE1XGEtdTnTHI__,http://ishare.ifeng.com/c/s/v002jixwYLJqgeFn17gETqOdALy3ZgXQ0--xf2MiQpQpzFDKcP43AMcXu6Z5fx1c2RO2I7aJckx80mBMpT8u9c3Wluw____,http://ishare.ifeng.com/c/s/v00290WdDT7grg4IEnhmhh8zqXZaiLhrWQKJ2sXJW1zb4os__,http://ishare.ifeng.com/c/s/v002KrJRHW99jIqJX3Y9XWtDJ9d--e--qIjE4gWCvOBzXhGtY__,http://ishare.ifeng.com/c/s/v002rmQ--znM9ItHAoVp97KpERcvrOkcr-_DvSCgKG5QzvyRc__,http://ishare.ifeng.com/c/s/v002dni3oHo2lxf9D9J7QMl-_c0vquY--dufbgk--5OAF3lUSw__,http://ishare.ifeng.com/c/s/v002Nvm55HeLSgldADC0sTUPRwqzV8iX7CmwYw2Q8kwUH58__,http://ishare.ifeng.com/c/s/v002TxEgBmMvwxQLwWRUWyNUTW4NV3LbmXxYKfjXGL--ZmwE__,http://ishare.ifeng.com/c/s/v002PIED--kFs--5fkJLRmbCuCfrynl3oKOz7hoJzN--Ljqm8A__,http://ishare.ifeng.com/c/s/v0020-_--RQGVODmMYadoYNijz-_OSu0NuXCjBoWOndV7ZY578__,http://ishare.ifeng.com/c/s/v002J47XHxwraP-_bmpWTuQxmjtR6TyCGul--3llB8ZR66aZExGClePYeI2zHgjD6b0HLWtYduDCNEtDBSRvJa7IqQVw____,http://ishare.ifeng.com/c/s/v002tAIkCdgGNwH8vrJL9PEL8ztgQMJoWsE5Ivaj0i9EkyU__,http://ishare.ifeng.com/c/s/v002fH51ajaQIzmFZXBlf1G--i8TWeNrLaZEh-_TMF--Od1--mQ__,http://ishare.ifeng.com/c/s/v002UXLugZmlWZkcx--mw3qhlUfotlYr0uoV--YAttoEVVunA__,http://ishare.ifeng.com/c/s/v002NTe06uRyrmDNTRT4UmVThgYRThgGerKRbpNZZITMaTw__,http://ishare.ifeng.com/c/s/v002lCIXEuxj-_8OLiWJdNa11CCMaL--rEj6kudfUhvsklUrTB00MinIJigoPjCBC00y6MyL943XTVe6VNkt1QhNGseQ____,http://ishare.ifeng.com/c/s/v002dPECWZfOOWGY--3bTexfA4tKxrjF5rORvO3yg2HxhSlNg1sDZ--bsGWcgFBqniCrLr24MgZnYw4oricNSBQ--8naA____,http://ishare.ifeng.com/c/s/v00240TP7zGkp9oFvCtl--yc9Xat-_5g4wAhosa3O9oinJDbY__,http://ishare.ifeng.com/c/s/v002Yq7YaYi3IOdFc4ap9KsAWH5KpYc3--RtvS4AFEkuCPwhz-_K6yuJfPMO8gDgWrYQgUXAbMW608mjIo--LhYdTET5Q____,http://ishare.ifeng.com/c/s/v002Bl3m6uz8tVWVd9U0mIQHoR4YxvsjjAEeAKvfGM0Qj9A__,http://ishare.ifeng.com/c/s/v002dq6NmZ-_--3e2CJJIof1FMoXgK6T6mC1gitXwedq-_--rVpc0LTTpJnk1gNZRNpi0sBLXYzMinsWow3ANPw2hTOTuw____,http://ishare.ifeng.com/c/s/v002F2LBcr3ifdHIRGBZmvQq0NfR6-_CLMtKDPdZJkIXgZldLj3BqiaxiXtzLzIOy1HNLGGjUrytPiNJb9yz9G2YT3Q____,http://ishare.ifeng.com/c/s/v002wl5KdQ9MvlBUMjwn55csU6M7HEwlM97WwPQ51ZljTis__,http://ishare.ifeng.com/c/s/v002Wc7rFFl7QfuoH1oPXtM7srQgC00usSuwHHBptajcK6yJLQIuw--njns7WSDrSQmfB0o6xjEhIEJ--QVeyj4p5rVg____,http://ishare.ifeng.com/c/s/v0022rqK4CZ3G8HfbzE1h5kukJqaElq-_s6JMA6dgXzjHlq0__,http://ishare.ifeng.com/c/s/v002YIVWzWry23heCY0euLDU6JO6bBcspmlp9peQwwP3vgA__,http://ishare.ifeng.com/c/s/v002P50giRCRC8bVVKvpXCUxj7qvstvO2w3vTDirWKcSALc__,http://ishare.ifeng.com/c/s/v002mCQ7V1Yra3o2rjwtZvVxev-_rvbiozJ2iVtpv0XAHiQ8__,http://ishare.ifeng.com/c/s/v0023FuaamVSlCqPy6gScdlwADur4lkMr5--xme9XePooN4s__,http://ishare.ifeng.com/c/s/v002vED5UgM8AsGUyhY1ewSLfjnpBbhfHmaomYkJVU--Q--TE__,http://ishare.ifeng.com/c/s/v002okQgaVeAXDMWBt1ML17p9YVINMfg--m6KK--BeCNLo7Wk__,http://ishare.ifeng.com/c/s/v002Dp2TySMqHOOswWDcmqxInlWnj4HV9z4t0n1uVCABwhQ__,http://ishare.ifeng.com/c/s/v002aNXl7zdnCO8fcJFnG3j--ozVdsDGjfs2ebT6quLynUoY__,http://ishare.ifeng.com/c/s/v002-_8PYj-_5esA8ya94H9UaT0m-_RHaco7hRJoqrtkJdwC8s__,http://ishare.ifeng.com/c/s/v002Wzb2GOIrm8-_iHGvUE5EDdtD6ho2dmRnBUiILkq2ZT3o__,http://ishare.ifeng.com/c/s/v002jpzWulY65WLEUWCQEIzmkVBF5--fxDKs9hMPugYV8P6s__,http://ishare.ifeng.com/c/s/v002l5w7Vc5PM9yz--NVDWkxVrryeuOSXSBOxgNL1FlF2-_wI__,http://ishare.ifeng.com/c/s/v002ah2--5NdFJoWNNg3538ZNQPbmn9RJ3kcsdWik2v0--oeo__,http://ishare.ifeng.com/c/s/v002TEKuklUTvI63URCPB0--xCMfCeP3OAHlrN6FiVUXp5Z4__,http://ishare.ifeng.com/c/s/v002m8D2tDvuZeYcNtBhNIedIw4Ld9ANVXzpXmIsKOHfKbk__,http://ishare.ifeng.com/c/s/v002qRw2BYZ8klMlUAnwd9B9XbHy4G-_r-_cJY9LOX7W-_Z27c__,http://ishare.ifeng.com/c/s/v002nXTWCsB-_fDS2e2iLPzr6p1tLMvnz6H2s0Ha1cE572R4__,http://ishare.ifeng.com/c/s/v002QNpc1oVBFXivI24XVGbMGfAO23ZtsjfbYqNNKCH4f--o__,http://ishare.ifeng.com/c/s/v0025xwRO9NOveHL-_jPxRS4H--1WHsZN-_HKL6QqfF3ug8jyI__,http://ishare.ifeng.com/c/s/v002MGwHG1AewNtQsEdS341jPvVwABbBRfgy1p5C78-_Z5Wo__,https://weibo.com/2375086267/LiVKDrIu8?refer_flag=1001030103_,http://ishare.ifeng.com/c/s/v002IN3OCcnYHYLbvFhG7yvaf59yhgXJ4QZVXmsj2xrfTeyKAAfrU3Ho--VTg4BKormIXLfBqoJ4Cpz1tgKqZEDjh4A____,http://ishare.ifeng.com/c/s/v002Jba1nKQW9bJ6--WpiWebSjpf32duLxD1BjbSoyio7C8wF7YUrLXrjMSRju13x0akBHq9YuMyP8UpqvvLz53BxFw____,http://ishare.ifeng.com/c/s/v002--nILmJ3CZjyPNwlI6xr8N7mJ0JScMOlW1O7EJdXY6jRmmgZrMyUkUrFeoFeDmr5CCj-_-_GU10gQI135Tx16JNCw____,http://ishare.ifeng.com/c/s/v002YM8yFQbARdb--Jf9f3N3lSXrX3IFNbl3AgGLWKkYBl1SM-_XAdB2S87d--d0u3dteRgvPhkQV3hubvbM2Ah4YlaYQ____,http://ishare.ifeng.com/c/s/v002ABDwI3G9MSrT9bbnEB-_dm1zz05KcPSBQH2-_3MPAHXrY__,http://ishare.ifeng.com/c/s/v002VGwKy70rXv--9lrCNvDz7Cjl3FReYLwzkU4Zi7BuDiEXx4Gfnr6OisXVj692yftVV0LqNsuTU4ft9IeNiRgby2w____,http://ishare.ifeng.com/c/s/v002s6St3fjTYw3xG5KntQP4jMikuwOLzTKpSkHd6WXxtFDpThnSi1FIOv3x--Bp-_UG82EY58xHUzLaUCah5ZkO30qA____,http://ishare.ifeng.com/c/s/v002E48fLmRioZzirAWf5FZl9tcMd1AXO52Fmd7ZiMwCLlo__,http://ishare.ifeng.com/c/s/v002tzrPJE--YrHgJiB4hasgvPHmJsrpV8--d0abKTj1LdUQQ__,http://ishare.ifeng.com/c/s/v002iBe1-_oU3UEVfuIbK0RSo--ajG9A1DJoptW9TDaU8DSkw__,https://weibo.com/2318910945/LiVFU6TL4?refer_flag=1001030103_,https://weibo.com/2925141777/LiVFUpA5e?refer_flag=1001030103_,https://weibo.com/2780457263/LiVFUmhVj?refer_flag=1001030103_,https://weibo.com/2925141777/LiVFUpA5e?refer_flag=1001030103_&display=0&retcode=6102,https://www.toutiao.com/a7072979863354966569/?channel=&source=search_tab&,https://weibo.com/2443459455/LiVCq4pHv?refer_flag=1001030103_,https://weibo.com/3858832626/LiVAppI2W?refer_flag=1001030103_,https://weibo.com/3160207440/LiVzFw3sJ?refer_flag=1001030103_,http://ishare.ifeng.com/c/s/v002Nvm55HeLSgldADC0sTUPR1ajizF2dWRPe-_JrXNfmzfM__,http://ishare.ifeng.com/c/s/v002TxEgBmMvwxQLwWRUWyNUTWqj9bN3p-_kzscJ4KxMrYYc__,http://ishare.ifeng.com/c/s/v0020-_--RQGVODmMYadoYNijz-_OPlyxzQ5BIBmEQTRQx4MUE__,http://ishare.ifeng.com/c/s/v002dni3oHo2lxf9D9J7QMl-_c6nzrdRMBO0--OhJd39-_wwMA__,http://ishare.ifeng.com/c/s/v002dPECWZfOOWGY--3bTexfA4tKxrjF5rORvO3yg2HxhSlO142fekPKzS378g1Y-_CM7so9-_XKfrLxBsELkLw6U8UrQ____,http://ishare.ifeng.com/c/s/v002Yq7YaYi3IOdFc4ap9KsAWH5KpYc3--RtvS4AFEkuCPwg4iGvv--1g5XeGLPQZQvj4BMfDWHslDmOKpM5LfJIE04Q____,http://ishare.ifeng.com/c/s/v002fH51ajaQIzmFZXBlf1G--i7b5CfE6ippUehC7HItClCU__,http://ishare.ifeng.com/c/s/v002J47XHxwraP-_bmpWTuQxmjtR6TyCGul--3llB8ZR66aZEMau0o6GNRnpi6ABHiO6CTPOrCOtX2--RdGHDDTHN2vqw____,http://ishare.ifeng.com/c/s/v002dq6NmZ-_--3e2CJJIof1FMoXgK6T6mC1gitXwedq-_--rVpw3x3q--Rm9RgOs-_Wslwrb6RDBejTKr01tKbK-_VOpbWBQ____,http://ishare.ifeng.com/c/s/v002F2LBcr3ifdHIRGBZmvQq0NfR6-_CLMtKDPdZJkIXgZldVVpq--ianUOK4P5n3lCiEulOzJsck-_gpQipGhb24Xn9A____,http://ishare.ifeng.com/c/s/v002Wc7rFFl7QfuoH1oPXtM7srQgC00usSuwHHBptajcK6zbGjofD3-_3Vy3XvaK-_anXcdgDycp4ZDjPWrffTcND9Xg____,http://ishare.ifeng.com/c/s/v002lCIXEuxj-_8OLiWJdNa11CCMaL--rEj6kudfUhvsklUrQMpqRd7hvPmb5weN3-_gS--nXDL3EWvgctF-_ZD1PzuK15Q____,http://ishare.ifeng.com/c/s/v002mCQ7V1Yra3o2rjwtZvVxerG1761U--qUjQ4CiGXvcSoo__,http://ishare.ifeng.com/c/s/v0022rqK4CZ3G8HfbzE1h5kukBodHhhE--SK52ljNulzbAEQ__,http://ishare.ifeng.com/c/s/v002P50giRCRC8bVVKvpXCUxjwSzNMACgSTiwyYdP4yn4Xk__,http://ishare.ifeng.com/c/s/v002okQgaVeAXDMWBt1ML17p9ddsWAcrcJHOJJ64gQ3IlNM__,http://ishare.ifeng.com/c/s/v002Dp2TySMqHOOswWDcmqxIngGiwuvXyqprv9lFnPz9Jec__,http://ishare.ifeng.com/c/s/v002vED5UgM8AsGUyhY1ewSLfi--TftQGHGO2h4QumYNlgfI__,http://ishare.ifeng.com/c/s/v002aNXl7zdnCO8fcJFnG3j--o6mm2v4ezjRtM5HLP9lCcvQ__,http://ishare.ifeng.com/c/s/v002Wzb2GOIrm8-_iHGvUE5EDdpvtjN4GGNQZn9TaYgxrvng__,http://ishare.ifeng.com/c/s/v002jpzWulY65WLEUWCQEIzmkZM7Gzd3BDOv3b9FK4anYzU__,http://ishare.ifeng.com/c/s/v002l5w7Vc5PM9yz--NVDWkxVrtUF86aVY4TqKT6OC-_dZuFA__,http://ishare.ifeng.com/c/s/v002TEKuklUTvI63URCPB0--xCHa4t4722nBZOoM-_sUB8QJ4__,http://ishare.ifeng.com/c/s/v002m8D2tDvuZeYcNtBhNIedI8MFrVHKd-_5oXNhX-_uHpebk__,http://ishare.ifeng.com/c/s/v002qRw2BYZ8klMlUAnwd9B9XcnEVEehb--yuR7vacUqfwoA__,http://ishare.ifeng.com/c/s/v002ah2--5NdFJoWNNg3538ZNQCojn2VSuH2SpA5ubAdDnT4__,http://ishare.ifeng.com/c/s/v002-_8PYj-_5esA8ya94H9UaT0jMQZyog7PAMkQbIl6IrE6U__,http://ishare.ifeng.com/c/s/v002MGwHG1AewNtQsEdS341jPn4iqeu8Ae0FpWHCe5J-_UHY__,http://ishare.ifeng.com/c/s/v002QNpc1oVBFXivI24XVGbMGRTNfrv--pmlbtFH1nBq0DtU__,http://ishare.ifeng.com/c/s/v0025xwRO9NOveHL-_jPxRS4H--6eHscq8IoKHMeMyon6-_rl0__,http://ishare.ifeng.com/c/s/v002nXTWCsB-_fDS2e2iLPzr6px5--TX-_qu8opFbY8wAz0PdY__,http://ishare.ifeng.com/c/s/v002CODY5P6ORuv6bqOC0wMDH60bfxXieIxSiGA6D6zoYLt8lzDMO5AKq8G--jBOO7xEgll59puoPkg4gUHllOV2Dmw____,http://ishare.ifeng.com/c/s/v002LHNhkHFHrbHc4g0BgdScwHQUhf2Ie5XAx3RXi2RkYe0__,http://ishare.ifeng.com/c/s/v002VvpnOYknQrKDVZKIj5MKh-_fAIkOEow2ayDTRcwTaZ57QDWogNLo0C9nO51f5u8yi5RJYEkBV65l3Eoba-_3WURQ____,http://ishare.ifeng.com/c/s/v002lqGh41Cf-_7mf2cw-_7biO2LdedgIto8jEtUxIitgIjsxNLo7gFBtJfQmHmGpkvGjieA5a--roc963YqsFBhw6aIA____,http://ishare.ifeng.com/c/s/v002y0P6e2kh0NH8QqY9AF---_L5GfUXPigOmG0Q7No2M2BvhnBymoi83QyiEhS1IFksoGySgMv1nDql9I9j1iaHJ20A____,http://ishare.ifeng.com/c/s/v002lDjjBjd-_6YJHpBlUlRIp--njiL-_mAOySwoZTN98eUijY__,http://ishare.ifeng.com/c/s/v002BM1VwKZv752iwD23FNcHWcEox--UroI7mbWwT9WSGov8__,http://ishare.ifeng.com/c/s/v002jM2Z0kMK4lmTKCQLitl5Vw0PfiDAeUNoSRsKvT1jR-_s__,http://ishare.ifeng.com/c/s/v002ViklEa-_sSLJXLSQ-_ljo7JgTmLKCLn49t9IXFVR5Okl8VZerq-_8nJjmPUaDAa--JX--lJFxkZkdmR-_0TjsR8OB6xA____,http://ishare.ifeng.com/c/s/v002PIDAebOTdnZLTt9iQhVaKXd1bwZ631w1KN6ex96oApQ__,http://ishare.ifeng.com/c/s/v0029kQDGL--VEfF7lknaNR0N--un--3F2aZUVNfmOKYwOcnsw__,http://ishare.ifeng.com/c/s/v002NIwWp0Je6QGoLrO1TFdifZeSm1xKEJ5SDeJb7Jo7haLE--kkFXwul6serHiDwX1YHkC4BAKQLVETCzfZ----nDByw____,http://ishare.ifeng.com/c/s/v002fkKIhcqJmOqEpokezod9254ibiGWNwdnGX9GP3hrrb3JotyiipcBBaWb7dUlfirBqWfRvg1o2MY-_gPC7wCGt5Q____,http://ishare.ifeng.com/c/s/v002jkLwfoeIqdQgyqxelFszbDUiQQiMHsdp-_QmeJcVsP4c__,http://ishare.ifeng.com/c/s/v002cFyNWUTpUemA2tdxy-_lnJXwCRxQoz-_5TNFyT7g2Vu60__,http://ishare.ifeng.com/c/s/v002BlXS2ImoDYU5Po78ZN9vWwqL6PYwegzhSPhAfkc7sEQ__,http://ishare.ifeng.com/c/s/v002HBpU89kQ73Fz3yxxcgcCsvrCP8rBrvpSmscowbenhos__,http://ishare.ifeng.com/c/s/v002EX--jkWzHgCDVqhEB-_fco1g9uIqjxTEUkoX59MkNy-_wI__,http://ishare.ifeng.com/c/s/v002xDkEI5DmMeDMnTrEivtk--Guy66QLXnKagzZHW4zBKWA__,http://ishare.ifeng.com/c/s/v0023nim--TKDNA9O2vXXAi7x5Me8dy7hqYdd2o7ebof0FE7w0OKk4IPgb0nqqIOih-_OV7rScegno-_kKo6k9GAr1ceA____,http://ishare.ifeng.com/c/s/v002-_eT3heTSVaUcRcPLNy77W-_KHGmyjz--eJZBMBxFxTRkFWuCfHIDwi6kawil89z2mpq6SxtwHVLKs1v--cV4U5eSA____,http://ishare.ifeng.com/c/s/v002bl8vzIEIb488XqD-_3fHwjYhiFUJzWmrumuRAZuMWbgk__,http://ishare.ifeng.com/c/s/v0020ZuKighGgxe7kb-_l--40BeOilV3IYNrP6p4QFWFa-_KKQPq--0zgva8vfc1JNtIoeH1g-_TOrQsIWVQlZbTZpy30-_g____,http://ishare.ifeng.com/c/s/v002jixwYLJqgeFn17gETqOdALy3ZgXQ0--xf2MiQpQpzFDKm7kkbgs6jvscUJQ-_3lrkx--ZtttvrUfPamxAJ--f--nWpQ____,http://ishare.ifeng.com/c/s/v002--ZTYFQ-_WWQotUM4lKowVCvSoUQ0Sj7HgEn--ZRXikW6M__,http://ishare.ifeng.com/c/s/v002U7B1ItijeqpcVF1kefwCT1eEcZNmeUNNm3xD36S3yB8__,http://ishare.ifeng.com/c/s/v002on2FTlgyHBy7YTyNkn1a9myhpVVKH3CikoGd46xRIH0__,http://ishare.ifeng.com/c/s/v002Vey2Cf8vFtNyNJxojQtQHGuUovMg0BVb5ITRWfU-_B--g__,http://ishare.ifeng.com/c/s/v00292VcphCPssqR9nw95xQ-_EPBEYa--RB4kwNtzI-_eqlI6c__,http://ishare.ifeng.com/c/s/v002M6bAhSMIPjsQVUw--aonForbR9SxgAY9G4YWSoW2ajqY__,http://ishare.ifeng.com/c/s/v002RNrhPVqmFOf-_rze7XRtC2yGtO1PpsX0M8IQUKsxh-_aY__,http://ishare.ifeng.com/c/s/v002nX5NzQNUYIVRYnqfXX--wPaoIE3W2-_aPUtpdAWbq-_R0GmtIIt8i6q3BYE0k1HjpBH8wnfLOkDUpXjV2gcLPiUhQ____,http://ishare.ifeng.com/c/s/v002Tb1dwwBxfNzAHJwsQtjWmIi--Fo7EfrunxhiaOrUjoZo__,http://ishare.ifeng.com/c/s/v0028OvZEOdXi9IlfYcapqqpgwZ2x2JThpIx5--GvOdA-_wVc__,http://ishare.ifeng.com/c/s/v00290WdDT7grg4IEnhmhh8zqXWYwSYgXufwSG5wx3pKwrw__,http://ishare.ifeng.com/c/s/v0022u61lSJDcUXwdcyyv2DPmXq36--j2Twqkq2jFdAFgRMq70qollhp1ANES2UMY6bRVtaT23lSKWp2vtxCYeTapTw____,http://ishare.ifeng.com/c/s/v002frl0XmLq0jUMXueD-_NWaxBfTvaCqgqtRTABEOBcMjOa3Bks9-_86WMKaQF55c9HfoZIlIgK68xoiKPqfIWTav2g____,http://ishare.ifeng.com/c/s/v0024u2FDaz0LAsHzuKeTd42GixpqAsZdseJ6qlHt--9CMFQ__,http://ishare.ifeng.com/c/s/v002NfHuJtcBryUk--Jqte4RDjIgN1NUBJqJ96-_SYMImT9Wk__,http://ishare.ifeng.com/c/s/v002rmQ--znM9ItHAoVp97KpERTm43zzjFMcOlHn-_JhpmL2Y__,http://ishare.ifeng.com/c/s/v002-_rj7yCYfLkelW--AJ1i3J8C9TMC4QcvHeHgq8ElzszPKs8STiGv4tdIDOO--guieTXqQOROsmPutbLW1ISjGIrjA____,http://ishare.ifeng.com/c/s/v002EpyJtzchNL6byvlSgWOGFGxJl6CrX1mCCtGHb83--Uq8__,http://ishare.ifeng.com/c/s/v0028Wha7ECe1G2ncLM7vFH26OgwOtnJuX-_IAjWSJ0MRFaRIDeZNh9YuxDvfbpGQE-_EETIcAEZ3m3IU5zJRQFooQqA____,http://ishare.ifeng.com/c/s/v002KrJRHW99jIqJX3Y9XWtDJ2PATgCd9H-_iBomQzUj3-_Qs__,http://ishare.ifeng.com/c/s/v002EZTVkZLqStEUlYdtQz2xIQiNw387gKbKo--FB1EgKTAQ__,http://ishare.ifeng.com/c/s/v002F89UHfZluopxcwdgQXqEkYe7EA5g1zR-_TA-_-_3ZqgFV4__,http://ishare.ifeng.com/c/s/v002Ei--AiYIKfra9HcNtbJ57QFJtorIClyWir--MMLQaQPhg__,http://ishare.ifeng.com/c/s/v002tAIkCdgGNwH8vrJL9PEL8wsJ0tfUXqP4MzLLyU0TcuU__,http://ishare.ifeng.com/c/s/v002PIED--kFs--5fkJLRmbCuCfsJ6ACZ9e9Kw5CXsvqMh--FE__,http://ishare.ifeng.com/c/s/v002wl5KdQ9MvlBUMjwn55csU0CNmR6kYEx7QF0mPir6x9U__,http://ishare.ifeng.com/c/s/v002NTe06uRyrmDNTRT4UmVThkB3hGN-_6HEZVP7EGDGDL1k__,http://ishare.ifeng.com/c/s/v00240TP7zGkp9oFvCtl--yc9XZIIBuMm0PsK-_ssP8NTRqlM__,http://ishare.ifeng.com/c/s/v002UXLugZmlWZkcx--mw3qhlUQqY4MRYzG8epXJwrKXhCWs__,http://ishare.ifeng.com/c/s/v002Bl3m6uz8tVWVd9U0mIQHoQIkdvQ8AKhIPyZjqlk3Jrs__,http://ishare.ifeng.com/c/s/v002YIVWzWry23heCY0euLDU6J5kqqo767HkMJN95Uw97bU__,http://ishare.ifeng.com/c/s/v0023FuaamVSlCqPy6gScdlwAHLpSSg4wsLhEXWBbAItcI0__,http://ishare.ifeng.com/c/s/v002ksNXjSA-_f58nKYmrZL37sO5yNzuUboBZQm6vRu9MGUc__,https://weibo.com/1750353804/LiVyw1KgA?refer_flag=1001030103_,https://weibo.com/1645705403/LiVvKo2nS?refer_flag=1001030103_,https://weibo.com/2000016880/LiVsmxNaD?refer_flag=1001030103_,https://weibo.com/2000016880/LiVsmxNaD?refer_flag=1001030103_&display=0&retcode=6102,http://ishare.ifeng.com/c/s/v002okQgaVeAXDMWBt1ML17p9dvwf1IMm8qNTJFeh7WgHeM__,http://ishare.ifeng.com/c/s/v002Dp2TySMqHOOswWDcmqxInpsdCobbHiAUcIXCyGddubM__,http://ishare.ifeng.com/c/s/v002vED5UgM8AsGUyhY1ewSLflp7Nnpw5zID8DG9gavJjh0__,http://ishare.ifeng.com/c/s/v002mCQ7V1Yra3o2rjwtZvVxelmJfnJa7PHyOrdHTvmkW-_Q__,http://ishare.ifeng.com/c/s/v002jpzWulY65WLEUWCQEIzmkX8Jy2-_lOvlGFODCRRwrnKo__,http://ishare.ifeng.com/c/s/v002l5w7Vc5PM9yz--NVDWkxVrnLYiHaOEDGxP7kE9RTBT74__,http://ishare.ifeng.com/c/s/v002aNXl7zdnCO8fcJFnG3j--o6SnN75nCRbhyfBuf--9rqzE__,http://ishare.ifeng.com/c/s/v002Wzb2GOIrm8-_iHGvUE5EDdvI6sw46lLQt37OKt9wXGag__,http://ishare.ifeng.com/c/s/v002m8D2tDvuZeYcNtBhNIedI75vqhkHFnbZVFALXT1yaak__,http://ishare.ifeng.com/c/s/v002qRw2BYZ8klMlUAnwd9B9XZ0Zpe9CmrtP9I5MeGhVlpQ__,http://ishare.ifeng.com/c/s/v002ah2--5NdFJoWNNg3538ZNQHwy5vj9WR1X6VyMSyQ7KHI__,http://ishare.ifeng.com/c/s/v002-_8PYj-_5esA8ya94H9UaT0mHNYaSCwqjBQWPOwexcRYY__,http://ishare.ifeng.com/c/s/v002nXTWCsB-_fDS2e2iLPzr6p3FKitLS94ENpMoyp47qC8w__,http://ishare.ifeng.com/c/s/v002TEKuklUTvI63URCPB0--xCHPfOru-_qBzk44I0KnLSM0c__,http://ishare.ifeng.com/c/s/v002QNpc1oVBFXivI24XVGbMGVYwYnT4ufvbL6QtUefIEnA__,http://ishare.ifeng.com/c/s/v0025xwRO9NOveHL-_jPxRS4H--8TZZI7MgIU--27hcVIgQz-_4__,http://ishare.ifeng.com/c/s/v002MGwHG1AewNtQsEdS341jPkoh3LhBzjNt07U5--JFgaCA__,http://ishare.ifeng.com/c/s/v002CODY5P6ORuv6bqOC0wMDH60bfxXieIxSiGA6D6zoYLvqIT59AUHOdfqQeJQb3IXRhKwz39kQ1Xbi7MNCcBs1lg____,http://ishare.ifeng.com/c/s/v002LHNhkHFHrbHc4g0BgdScwGgENuOKp-_sdmN-_R----m2pyg__,http://ishare.ifeng.com/c/s/v002BM1VwKZv752iwD23FNcHWfPniqM-_SLlYliSwww4tuJw__,http://ishare.ifeng.com/c/s/v002PIDAebOTdnZLTt9iQhVaKevVdLb3y7HI5qxXmvZNUbM__,http://ishare.ifeng.com/c/s/v002lqGh41Cf-_7mf2cw-_7biO2LdedgIto8jEtUxIitgIjszIMzGRJD1a4rhQaDu8qsIJw0--mPTEkv1HVm5R1k6UEkA____,http://ishare.ifeng.com/c/s/v002y0P6e2kh0NH8QqY9AF---_L5GfUXPigOmG0Q7No2M2Bvh8x9EoqqxnKPuG--Rqk--7r5znmneY3nQMEdLk92RawPmQ____,http://ishare.ifeng.com/c/s/v002lDjjBjd-_6YJHpBlUlRIp--vKzqensC--lFuG--6d9Ju3xQ__,http://ishare.ifeng.com/c/s/v0029kQDGL--VEfF7lknaNR0N--u9sWeC2Y4qKriPi0A6UmUI__,http://ishare.ifeng.com/c/s/v002jM2Z0kMK4lmTKCQLitl5V2VlSQQTDUp6TqAgttFpJVQ__,http://ishare.ifeng.com/c/s/v002-_004Oj1bKvreaxN-_2bxE9rm7XwdilwkX3eOU4phpJd8__,http://ishare.ifeng.com/c/s/v002ViklEa-_sSLJXLSQ-_ljo7JgTmLKCLn49t9IXFVR5Okl90tBO-_472jARQ294VQq79EyLR0kM78zMJOoRc3kx2O6w____,http://ishare.ifeng.com/c/s/v002NIwWp0Je6QGoLrO1TFdifZeSm1xKEJ5SDeJb7Jo7haJ9TUTavuRBjEYqjPbQScCuEwAmhGKaViJuSnV5jD3XPA____,http://ishare.ifeng.com/c/s/v002fkKIhcqJmOqEpokezod9254ibiGWNwdnGX9GP3hrrb1rzT7p97lbIAeTJ8--7zIeA8Ce59PCdc-_LYf4C8SrD1pw____,http://ishare.ifeng.com/c/s/v002jkLwfoeIqdQgyqxelFszbO--02oTvIcAfE5Ncg6d--cjk__,http://ishare.ifeng.com/c/s/v002cFyNWUTpUemA2tdxy-_lnJcESYpMelZApkiq8mA96A8o__,http://ishare.ifeng.com/c/s/v002HBpU89kQ73Fz3yxxcgcCsl62Bgr--pyCtjQcu-_QNuGv0__,http://ishare.ifeng.com/c/s/v0023nim--TKDNA9O2vXXAi7x5Me8dy7hqYdd2o7ebof0FE5ZPuCJ7vgAqZtVjK3APBVju5LhxiOrukiBoHAww111-_A____,http://ishare.ifeng.com/c/s/v002EX--jkWzHgCDVqhEB-_fco1huyAJpZXtbZzhaNREtn5hY__,http://ishare.ifeng.com/c/s/v002BlXS2ImoDYU5Po78ZN9vWyKMngvNanOOtV20uL34V1Q__,http://ishare.ifeng.com/c/s/v002--ZTYFQ-_WWQotUM4lKowVCm--DoBaLNln--AI8uSuUY6Uo__,http://ishare.ifeng.com/c/s/v002on2FTlgyHBy7YTyNkn1a9iCwyhvADj7Xl7EkLX5hvL8__,http://ishare.ifeng.com/c/s/v002bl8vzIEIb488XqD-_3fHwjemb0JN2Ro3A9uhFVFqq4nc__,http://ishare.ifeng.com/c/s/v002nX5NzQNUYIVRYnqfXX--wPaoIE3W2-_aPUtpdAWbq-_R0GtxVShlJbTkq9aRelQL85RW280CpcnjQVM69b--Q2ciDg____,http://ishare.ifeng.com/c/s/v0028OvZEOdXi9IlfYcapqqpg5T2uHa5DKoStiMhasN8I9A__,http://ishare.ifeng.com/c/s/v0020ZuKighGgxe7kb-_l--40BeOilV3IYNrP6p4QFWFa-_KKTkgGPjfMH-_HlV0VzFivfBCipPqxm0BJUgu--3ncDx1RrQ____,http://ishare.ifeng.com/c/s/v002-_rj7yCYfLkelW--AJ1i3J8C9TMC4QcvHeHgq8ElzszPI4aaXheWPnTNkZQQiJpbqZ4ioesCieRpvn7uIVBXbD9A____,http://ishare.ifeng.com/c/s/v0022u61lSJDcUXwdcyyv2DPmXq36--j2Twqkq2jFdAFgRMpaklUopBpENnmdTFemiMANtt3uBBcDUZiMLKxogl9prg____,http://ishare.ifeng.com/c/s/v002frl0XmLq0jUMXueD-_NWaxBfTvaCqgqtRTABEOBcMjOYVS4OkGOA8nREx7LmKjGFP4GIXhpohfKZCIvmtnu3lNw____,http://ishare.ifeng.com/c/s/v0024u2FDaz0LAsHzuKeTd42Gk6-_Ff1UT4m-_poXgMgIz8J0__,http://ishare.ifeng.com/c/s/v002RNrhPVqmFOf-_rze7XRtC206czSq-_i3mcP5n42jFqJKs__,http://ishare.ifeng.com/c/s/v002EpyJtzchNL6byvlSgWOGFFCSI2bD9i9tVtEbDUrJsNY__,http://ishare.ifeng.com/c/s/v0028Wha7ECe1G2ncLM7vFH26OgwOtnJuX-_IAjWSJ0MRFaQ9170UrrCamMpY8Vr1FMt2Lg7LHHDOaJRo9f6JZ84jmg____,http://ishare.ifeng.com/c/s/v002EZTVkZLqStEUlYdtQz2xIaC-_1mY1lY7Si1j-_ub5-_f5E__,http://ishare.ifeng.com/c/s/v002NfHuJtcBryUk--Jqte4RDjFGYIauTRvzZWT32zmY2fpY__,http://ishare.ifeng.com/c/s/v002F89UHfZluopxcwdgQXqEkYVmyM3pkcLZmYmucuAR5Aw__,http://ishare.ifeng.com/c/s/v002Ei--AiYIKfra9HcNtbJ57QL1xq8B3kN7xd--6O7e-_9HA8__,http://ishare.ifeng.com/c/s/v002ksNXjSA-_f58nKYmrZL37sAWOOd2bZVl3UTmxj6PtRxo__,https://weibo.com/2127460165/LiV636AhO?refer_flag=1001030103_,https://weibo.com/3804173419/LiUN8ke0w?refer_flag=1001030103_,https://weibo.com/1457834594/LiUKo2BSO?refer_flag=1001030103_,https://weibo.com/1638782947/LiUII1Or5?refer_flag=1001030103_,https://www.toutiao.com/a7072941670379192868/?channel=&source=search_tab&,https://weibo.com/1744823950/LiUzlb3gu?refer_flag=1001030103_,https://weibo.com/1744823950/LiUzlb3gu?refer_flag=1001030103_&display=0&retcode=6102,https://weibo.com/1906051633/LiUmqireE?refer_flag=1001030103_,https://weibo.com/1906051633/LiUmqireE?refer_flag=1001030103_&display=0&retcode=6102,https://weibo.com/1925878362/LiUe43bOi?refer_flag=1001030103_,https://weibo.com/1925878362/LiUe43bOi?refer_flag=1001030103_&display=0&retcode=6102,https://weibo.com/6268375888/LiTFXxOMA?refer_flag=1001030103_,https://weibo.com/2672076083/LiTcoygb2?refer_flag=1001030103_,https://weibo.com/2672076083/LiTcoygb2?refer_flag=1001030103_&display=0&retcode=6102,https://weibo.com/2258727970/LiTc7tjD4?refer_flag=1001030103_,https://weibo.com/1684012053/LiSNG6Ws7?refer_flag=1001030103_,https://weibo.com/2504282715/LiStkp3Oe?refer_flag=1001030103_,https://weibo.com/1649173367/LiSqlaYh5?refer_flag=1001030103_,https://weibo.com/2772291221/LiR594sOx?refer_flag=1001030103_,https://weibo.com/2150758415/LiP8Lcbkq?refer_flag=1001030103_,https://weibo.com/6530665481/LiP1TkyKM?refer_flag=1001030103_,https://www.toutiao.com/a7072698017501053476/?channel=&source=search_tab&,https://www.toutiao.com/a7072679334426706463/?channel=&source=search_tab&,https://weibo.com/5986590971/LiNWG2kC7?refer_flag=1001030103_,https://www.toutiao.com/a7072678639388590623/?channel=&source=search_tab&,https://weibo.com/1680685707/LiNVk76IZ?refer_flag=1001030103_,https://weibo.com/2239419537/LiNPFEKhO?refer_flag=1001030103_,https://weibo.com/1132428807/LiN6QfWh7?refer_flag=1001030103_,https://weibo.com/2656090565/LiMXGCsgU?refer_flag=1001030103_,https://weibo.com/1727161784/LiLRIF2CW?refer_flag=1001030103_,https://weibo.com/2316268360/LiLm6j7EX?refer_flag=1001030103_&display=0&retcode=6102,https://weibo.com/2316268360/LiLm6j7EX?refer_flag=1001030103_,https://weibo.com/5976914598/LiFtz086r?refer_flag=1001030103_&display=0&retcode=6102,https://weibo.com/5976914598/LiFtz086r?refer_flag=1001030103_,https://weibo.com/1132428807/LiEZxbQvC?refer_flag=1001030103_,https://weibo.com/2127460165/LiENmuvQz?refer_flag=1001030103_,https://www.toutiao.com/a7072249864641446441/?channel=&source=search_tab&,https://weibo.com/1685715705/LiDWAr4ZJ?refer_flag=1001030103_,https://weibo.com/1650111241/LiDWAr5cz?refer_flag=1001030103_,https://www.toutiao.com/a7072274019965370888/?channel=&source=search_tab&,https://weibo.com/2250602754/LiDCH7x5g?refer_flag=1001030103_,https://weibo.com/1499104401/LiD2lpqCz?refer_flag=1001030103_&display=0&retcode=6102,https://weibo.com/1499104401/LiD2lpqCz?refer_flag=1001030103_,https://weibo.com/1649470535/LiCPBfEu5?refer_flag=1001030103_&display=0&retcode=6102,https://weibo.com/1649470535/LiCPBfEu5?refer_flag=1001030103_,https://weibo.com/2000016880/LiCLwj9ZC?refer_flag=1001030103_,https://weibo.com/1265998927/LiCKF6OyH?refer_flag=1001030103_,https://weibo.com/3771857862/LiCCiePvj?refer_flag=1001030103_,https://weibo.com/1645705403/LiCw4fDjA?refer_flag=1001030103_,https://weibo.com/2292896411/LiCvsaLEv?refer_flag=1001030103_,https://weibo.com/1895623191/LiColC4dr?refer_flag=1001030103_,https://weibo.com/2375086267/LiCnaCSuu?refer_flag=1001030103_,http://www.heneng.net.cn/home/zc/infotwo/id/65398/sid/10/catId/162.html,https://www.toutiao.com/a7071911583508759071/?channel=&source=search_tab&,https://www.toutiao.com/a7071832579321791013/?channel=&source=search_tab&,https://www.toutiao.com/a7071528460035981858/?channel=&source=search_tab&,https://www.toutiao.com/a7071522816457179655/?channel=&source=search_tab&,https://www.toutiao.com/a7071497870351876647/?channel=&source=search_tab&,https://www.toutiao.com/a7071417610977509924/?channel=&source=search_tab&,https://www.toutiao.com/a7071307930322076167/?channel=&source=search_tab&,https://www.toutiao.com/a7071246980697031181/?channel=&source=search_tab&,https://www.toutiao.com/a7071246136333402655/?channel=&source=search_tab&,https://www.toutiao.com/a7071230287832678943/?channel=&source=search_tab&,https://weibo.com/5137261048/LicHRFAAi?refer_flag=1001030103_,https://weibo.com/3604378011/LicjIp6Ro?refer_flag=1001030103_,https://www.toutiao.com/a7071162722318107167/?channel=&source=search_tab&,https://www.toutiao.com/a7071162288744432136/?channel=&source=search_tab&,https://www.toutiao.com/a7071131217122296334/?channel=&source=search_tab&,https://www.toutiao.com/a7071115750366462495/?channel=&source=search_tab&,https://www.toutiao.com/a7071074065792172574/?channel=&source=search_tab&,https://www.toutiao.com/a7071070483642319367/?channel=&source=search_tab&,https://www.toutiao.com/a7070875768653840903/?channel=&source=search_tab&");
        System.out.println(Yids);
        String[] idarray = Yids.trim().split("\\,");
//        String[] idarray = decodeYuQingIds.trim().split("\\,");
        List<String>searchSplitArray = Arrays.asList(idarray);

        ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
        query.JoinQueryBuildersByUrls(searchSplitArray);

        query.SortBytimeOrder();
//        query.SetPageableAndBoolQuery();

        YuQingResponse response = elasticSearchDao.findByQuery(query);
        List<YuQing> pageDataContent=response.getYuQingContent();
        List<YuQing> aka=new ArrayList<>();
        dataMap.put("data",aka);


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
                    YuQing data = pageDataContent.get(x);
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
            wordTemplate.process(dataMap,outDoc);
            
            pdfTemplate.process(dataMap, outPdf); //将合并后的数据和模板写入到流中，这里使用的字符流
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
        String FONT = "/root/codes/backend/font/SimHei.ttf";

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

        System.out.println("processHere");

        try {
            BriefingFile briefingFile = briefingFileDao.findById(fileID);
            System.out.println(fileID);
            briefingFile.setPercent(100);
            briefingFile.setPdf(pdfByteArray);
            briefingFile.setExcel(excelByteArray);
            briefingFile.setWord(wordByteArray);
            briefingFileDao.save(briefingFile);
//            briefingFileDao.UpdateBriefingFile(briefingFile.getId(),briefingFile.getFid(),briefingFile.getName(),
//                    briefingFile.getGeneratetime(),pdfByteArray,wordByteArray,excelByteArray,
//                    100);
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

            int fileId = briefingFileDao.InsertBriefingFile(fid,title,new Date(),null,null,null,10);

            ret.put("addNewBriefingFileRecord",1);
            ret.put("fileId",fileId);
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
        System.out.println(id);
        System.out.println("UpdateFile");
        System.out.println(briefingFile.getName());
        briefingFile.setPercent(briefingFile.getPercent()+percent);
        try {
            briefingFileDao.UpdateBriefingFile(briefingFile.getId(),briefingFile.getFid(),briefingFile.getName(),
                    briefingFile.getGeneratetime(),briefingFile.getPdf(),briefingFile.getWord(),briefingFile.getExcel(),
                    briefingFile.getPercent());
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
//                SensitiveWords sensitiveWords=new SensitiveWords(type,word);
                sensitiveWordsDao.InsertSensitivewords(type, word);
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

    @Override
    public VideoResponse getVideoData(long fid, String keyword, String startPublishedDay, String endPublishedDay, String resource, int page, int pageSize, int timeOrder) {
        System.out.println(fid);
        System.out.println(keyword);
        System.out.println(startPublishedDay);
        System.out.println(endPublishedDay);
        System.out.println(resource);
        System.out.println(page);
        System.out.println(pageSize);
        System.out.println(timeOrder);

        FangAn fangAn=fangAnDao.findByFid(fid);
        String eventKeyword = fangAn.getEventKeyword();
        List<String> searchArray=new ArrayList<>();
        while(eventKeyword.length()>0)
        {
            int tag=eventKeyword.indexOf('+');
            String singleEventKeyword=eventKeyword.substring(0,tag);
            String[] searchSplitArray1 = singleEventKeyword.trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
            for (String str:searchSplitArray)
            {
                if (!str.equals(""))
                {
                    searchArray.add(str);
                }
            }
            eventKeyword=eventKeyword.substring(tag+1);
        }

        Criteria criteria=new Criteria();
        criteria.subCriteria(new Criteria("title").in(searchArray));

        List<String> searchArray2=new ArrayList<>();
        if (!keyword.isEmpty()&&!Objects.equals(keyword, "null")&&!Objects.equals(keyword, "")){
            String[] searchKeywordArray = keyword.trim().split("\\s+");
            List<String> searchKeywordSplitArray = Arrays.asList(searchKeywordArray);
            for (String str:searchKeywordSplitArray)
            {
                if (!str.equals(""))
                {
                    searchArray2.add(str);
                }
            }
            criteria.subCriteria(new Criteria("title").in(searchArray2));
        }



        System.out.println(searchArray.size());

        if (!Objects.equals(startPublishedDay, "null") && !Objects.equals(endPublishedDay, "null") &&
                !startPublishedDay.isEmpty() && !endPublishedDay.isEmpty() )
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date startDate = sdf.parse(startPublishedDay);
                Date endDate = sdf.parse(endPublishedDay);
                criteria.subCriteria(new Criteria().and("publishedDate").between(startDate, endDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (!resource.isEmpty()&&!Objects.equals(resource, "null")&&!Objects.equals(resource, ""))
            criteria.subCriteria(new Criteria().and("resource").is(resource));

        CriteriaQuery query = new CriteriaQuery(criteria);
        if (timeOrder == 0) {
            query.setPageable(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "publishedDate")));
        }
        else {
            query.setPageable(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "publishedDate")));
        }

        SearchHits<Video> searchHits = this.elasticsearchOperations.search(query, Video.class);
        SearchPage<Video> searchPage = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
        long hitNumber = this.elasticsearchOperations.count(query, Video.class);

        List<Video> pageDataContent = new ArrayList<>();
        for (SearchHit<Video> hit : searchPage.getSearchHits())
        {
            pageDataContent.add(hit.getContent());
        }

        VideoResponse result = new VideoResponse();
        result.setHitNumber(hitNumber);
        result.setDataContent(pageDataContent);

        return result;
    }
}