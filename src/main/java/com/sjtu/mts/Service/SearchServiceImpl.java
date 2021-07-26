package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Dao.FangAnWeiboUserDAO;
import com.sjtu.mts.Entity.*;
import com.sjtu.mts.Keyword.KeywordResponse;
import com.sjtu.mts.Keyword.MultipleThreadExtraction;
import com.sjtu.mts.Keyword.TextRankKeyword;
import com.sjtu.mts.Repository.AreaRepository;
import com.sjtu.mts.Repository.SensitiveWordRepository;
import com.sjtu.mts.Repository.SwordFidRepository;
import com.sjtu.mts.Response.*;
import com.sjtu.mts.rpc.WeiboSpiderRpc;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryShardContext;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
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
    private WeiboSpiderRpc weiboSpiderRpc;

    public SearchServiceImpl(ElasticsearchOperations elasticsearchOperations,AreaRepository areaRepository,SensitiveWordRepository sensitiveWordRepository,SwordFidRepository swordFidRepository)
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
    public DataResponse Search(String keyword, String cflag, String startPublishedDay, String endPublishedDay,
                               String fromType, int page, int pageSize, int timeOrder)
    {
        Criteria criteria = new Criteria();
        if (!keyword.isEmpty())
        {
            String[] searchSplitArray1 = keyword.trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
            criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
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
    @Override
    public DataResponse SearchWithObject(String keyword, String sensitiveType, String emotion, String startPublishedDay, String endPublishedDay,
                                         String fromType, int page, int pageSize, int timeOrder,String keywords)
    {
        String eventKeyword = keywords;
        System.out.println(eventKeyword);
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

        List<Data> pageDataContent = new ArrayList<>();
        for(int numOfEvents=0;numOfEvents<events.size();numOfEvents++)
        {
            Criteria criteria=new Criteria();
            if (!keyword.isEmpty()){
                String[] searchSplitArray1 = keyword.trim().split("\\s+");
                List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
                criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
            }
            if (!sensitiveType.isEmpty())
            {
                criteria.subCriteria(new Criteria("sensitiveType").in(SensitiveTypeStr(sensitiveType)));
            }
            if (!emotion.isEmpty())
            {
                criteria.subCriteria(new Criteria("emotion").contains(EmotionStr(emotion)));
            }
            if (!startPublishedDay.equals("null")&& !endPublishedDay.equals("null"))
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
            if (!events.get(numOfEvents).isEmpty())
            {
                String[] searchSplitArray1 = events.get(numOfEvents).trim().split("\\s+");
                List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
                if(searchSplitArray.size()>1){
                    for (String searchString : searchSplitArray) {
                        List<String> subArray = new LinkedList<>();
                        subArray.add(searchString);
                        criteria.subCriteria(new Criteria("content").in(subArray).or("title").in(subArray));
                    }
                }else {
                    criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
                }
            }
            CriteriaQuery query = new CriteriaQuery(criteria);
            SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);
            for (SearchHit<Data> hit : searchHits.getSearchHits())
            {
                pageDataContent.add(hit.getContent());
            }
        }

        if (events.size()==0)
        {
            Criteria criteria=new Criteria();
            if (!keyword.isEmpty()){
                String[] searchSplitArray1 = keyword.trim().split("\\s+");
                List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
                criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
            }
            if (!sensitiveType.isEmpty())
            {
                criteria.subCriteria(new Criteria("sensitiveType").in(SensitiveTypeStr(sensitiveType)));
            }
            if (!emotion.isEmpty())
            {
                criteria.subCriteria(new Criteria("emotion").contains(EmotionStr(emotion)));
            }
            if (!startPublishedDay.equals("null")&& !endPublishedDay.equals("null"))
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

        /*Collections.sort(pageDataContent,(Data b1, Data b2) -> (EmotionToInt(b1.getEmotion())>EmotionToInt(b2.getEmotion()))?-1:
            ((EmotionToInt(b1.getEmotion())==EmotionToInt(b2.getEmotion()))?0:1));

        Collections.sort(pageDataContent,(Data b1, Data b2) -> (SensitiveTypeToInt(b1.getSensitiveType())>SensitiveTypeToInt(b2.getSensitiveType()))?-1:
                ((SensitiveTypeToInt(b1.getSensitiveType())==SensitiveTypeToInt(b2.getSensitiveType()))?0:1));*/

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
    };

    @Override
    public ResourceCountResponse globalSearchResourceCount(String keyword, String startPublishedDay, String endPublishedDay) {
        List<Long> resultList = new ArrayList<>();
        for (int fromType = 1; fromType <= 7 ; fromType++) {
            Criteria criteria = new Criteria();
            if (!keyword.isEmpty())
            {
                String[] searchSplitArray = keyword.trim().split("\\s+");;
                for (String searchString : searchSplitArray) {
                    criteria.subCriteria(new Criteria().and("content").contains(searchString).
                            or("title").contains(searchString));
                }
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
        return new ResourceCountResponse(resultList.get(0), resultList.get(1), resultList.get(2),
                resultList.get(3), resultList.get(4), resultList.get(5), resultList.get(6));
    }

    @Override
    public ResourceCountResponse globalSearchResourceCountByFid(long fid,String startPublishedDay, String endPublishedDay){
        List<Long> resultList = new ArrayList<>();
        for (int fromType = 1; fromType <= 7 ; fromType++) {
            resultList.add((long) 0);
        }
        for (int fromType = 1; fromType <= 7 ; fromType++) {

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
                criteria.subCriteria(new Criteria().and("fromType").is(fromType));
                CriteriaQuery query = new CriteriaQuery(criteria);
                long searchHitCount = this.elasticsearchOperations.count(query, Data.class);
                resultList.set(fromType-1,resultList.get(fromType-1)+searchHitCount);
                //resultList.add(searchHitCount);
            }
        }
        return new ResourceCountResponse(resultList.get(0), resultList.get(1), resultList.get(2),
                resultList.get(3), resultList.get(4), resultList.get(5), resultList.get(6));
    }

    @Override
    public CflagCountResponse globalSearchCflagCount(String keyword, String startPublishedDay, String endPublishedDay) {
        List<Long> resultList = new ArrayList<>();
        for (int cflag = 0; cflag <= 1 ; cflag++) {
            Criteria criteria = new Criteria();
            if (!keyword.isEmpty())
            {
                String[] searchSplitArray = keyword.trim().split("\\s+");;
                for (String searchString : searchSplitArray) {
                    criteria.subCriteria(new Criteria().and("content").contains(searchString).
                            or("title").contains(searchString));
                }
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
        return new CflagCountResponse(resultList.get(0), resultList.get(1));
    }
    @Override
    public CflagCountResponse globalSearchCflagCountByFid(long fid, String startPublishedDay, String endPublishedDay){
        List<Long> resultList = new ArrayList<>();
        for (int cflag = 0; cflag <= 1 ; cflag++) {
            resultList.add((long)0);
        }
        for (int cflag = 0; cflag <= 1 ; cflag++) {
            //Criteria criteria = fangAnDao.criteriaByFid(fid);
            List<Criteria> criterias=fangAnDao.FindCriteriasByFid(fid);
            for(Criteria criteria:criterias)
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
                criteria.subCriteria(new Criteria().and("cflag").is(cflag));
                CriteriaQuery query = new CriteriaQuery(criteria);
                long searchHitCount = this.elasticsearchOperations.count(query, Data.class);
                resultList.set(cflag,resultList.get(cflag)+searchHitCount);
            }
        }
        return new CflagCountResponse(resultList.get(0), resultList.get(1));
    }

    @Override
    public AmountTrendResponse globalSearchTrendCount(String keyword, String startPublishedDay, String endPublishedDay) {
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
        List<List<Long>> fromTypeResultList = new ArrayList<>();
        for (int fromType = 0; fromType <= 7 ; fromType++) {
            List<Long> resultList = new ArrayList<>();
            for (int j = 0; j < pointNum; j++) {
                Criteria criteria = new Criteria();
                if (!keyword.isEmpty())
                {
                    String[] searchSplitArray = keyword.trim().split("\\s+");;
                    for (String searchString : searchSplitArray) {
                        criteria.subCriteria(new Criteria().and("content").contains(searchString).
                                or("title").contains(searchString));
                    }
                }
                // fromType 0 indicates searching all fromTypes
                if (fromType != 0) {
                    criteria.subCriteria(new Criteria().and("fromType").is(fromType));
                }
                else {
                    // only add once
                    timeRange.add(sdf.format(dateList.get(j)) + " to " + sdf.format(dateList.get(j + 1)));
                }
                criteria.subCriteria(new Criteria().and("publishedDay").between(dateList.get(j), dateList.get(j + 1)));
                CriteriaQuery query = new CriteriaQuery(criteria);
                long searchHitCount = this.elasticsearchOperations.count(query, Data.class);
                resultList.add(searchHitCount);
            }
            fromTypeResultList.add(resultList);
        }
        return new AmountTrendResponse(timeRange, fromTypeResultList.get(0),
                fromTypeResultList.get(1), fromTypeResultList.get(2), fromTypeResultList.get(3),
                fromTypeResultList.get(4), fromTypeResultList.get(5), fromTypeResultList.get(6),
                fromTypeResultList.get(7));
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
    public AmountTrendResponse globalSearchTrendCount2(long fid,String startPublishedDay, String endPublishedDay){
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
    public AreaAnalysisResponse countArea(String keyword, String startPublishedDay, String endPublishedDay){
        List<Long> resultList = new ArrayList<>();
        List<Integer> codeids = Arrays.asList(11,12,13,14,15,21,22,23,31,32,33,34,35,36,37,41,42,43,44,45,46,50,51,52,53,54,61,62,63,64,65,71,81,91);
        for(int i =0;i<codeids.size();i++){
            Criteria criteria = new Criteria();
            if (!keyword.isEmpty())
            {
                String[] searchSplitArray = keyword.trim().split("\\s+");;
                for (String searchString : searchSplitArray) {
                    criteria.subCriteria(new Criteria().and("content").contains(searchString).
                            or("title").contains(searchString));
                }
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
            List<String> citys;
            citys = areaRepository.findCityNameByCodeid(codeids.get(i));
            for(int j=0;j<citys.size();j++){
                citys.set(j,citys.get(j).replaceAll("\\s*", ""));
                if(citys.get(j).contains("市辖")||citys.get(j).contains("县辖")){
                    citys.remove(j);
                }
            }
            citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重
            //System.out.println(Arrays.toString(citys.toArray()));
            criteria.subCriteria(new Criteria("content").in(citys).or("title").in(citys));
            CriteriaQuery query = new CriteriaQuery(criteria);
            long searchHitCount = this.elasticsearchOperations.count(query, Data.class);
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
            resultList.add((long)0);
        }
        for(int i =0;i<codeids.size();i++){
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
                List<String> citys;
                citys = areaRepository.findCityNameByCodeid(codeids.get(i));
                for(int j=0;j<citys.size();j++){
                    citys.set(j,citys.get(j).replaceAll("\\s*", ""));
                    if(citys.get(j).contains("市辖")||citys.get(j).contains("县辖")){
                        citys.remove(j);
                    }
                }
                citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重
                //System.out.println(Arrays.toString(citys.toArray()));
                criteria.subCriteria(new Criteria("content").in(citys).or("title").in(citys));
                CriteriaQuery query = new CriteriaQuery(criteria);
                long searchHitCount = this.elasticsearchOperations.count(query, Data.class);
                resultList.set(i, resultList.get(i)+searchHitCount);
            }
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
    public DataResponse fangAnSearch2(long fid,String keyword,String sensitiveType,String emotion, String startPublishedDay, String endPublishedDay,
                                     String fromType, int page, int pageSize, int timeOrder){
        List<Criteria> criterias=fangAnDao.FindCriteriasByFid(fid);
        List<Data> pageDataContent = new ArrayList<>();
        for(Criteria criteria:criterias)
        {
            if (!keyword.isEmpty()){
                String[] searchSplitArray1 = keyword.trim().split("\\s+");
                List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
                criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
            }
            if (!sensitiveType.isEmpty())
            {
                criteria.subCriteria(new Criteria("sensitiveType").in(SensitiveTypeStr(sensitiveType)));
            }
            if (!emotion.isEmpty())
            {
                criteria.subCriteria(new Criteria("emotion").contains(EmotionStr(emotion)));
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
            System.out.println(criteria);
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
/*
        Collections.sort(pageDataContent,(Data b1, Data b2) -> (EmotionToInt(b1.getEmotion())>EmotionToInt(b2.getEmotion()))?-1:
                ((EmotionToInt(b1.getEmotion())==EmotionToInt(b2.getEmotion()))?0:1));

        Collections.sort(pageDataContent,(Data b1, Data b2) -> (SensitiveTypeToInt(b1.getSensitiveType())>SensitiveTypeToInt(b2.getSensitiveType()))?-1:
                ((SensitiveTypeToInt(b1.getSensitiveType())==SensitiveTypeToInt(b2.getSensitiveType()))?0:1));*/

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

       /* Criteria criteria = fangAnDao.criteriaByFid(fid);
        if (!keyword.isEmpty()){
            criteria.subCriteria(new Criteria().and("content").contains(keyword).
                    or("title").contains(keyword));
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
            String[] searchSplitArray1 = fromType.trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);

            if(searchSplitArray.size()>1){
                criteria.subCriteria(new Criteria().and("fromType").in(searchSplitArray));
            }else {
                criteria.subCriteria(new Criteria().and("fromType").is(searchSplitArray.get(0)));
            }

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
        result.setDataContent(pageDataContent);*/

        return result;
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
        List<String> fileContents = new ArrayList<>();
        //Criteria criteria = fangAnDao.criteriaByFid(fid);
        List<Criteria> criterias=fangAnDao.FindCriteriasByFid(fid);
        for (Criteria criteria:criterias){
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
                fileContents.add(data.getContent());
            }
        }

        //开始使用多线程提取关键词
        int threadCounts = 8;//采用的线程数

        long start=  System.currentTimeMillis();
        MultipleThreadExtraction countListIntegerSum=new MultipleThreadExtraction(fileContents,threadCounts, extractMethod);

        List<List<String>> sum=countListIntegerSum.getIntegerSum();
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
        long end = System.currentTimeMillis();
        System.out.println("关键词提取耗时：" + (end-start) + "ms");
        return keywords;
    }
    @Override
    public  JSONObject autoaddEkeyword(long fid,String text){
        System.out.println(text);
        System.out.println(fid);
        List<String> Ekeyword = new TextRankKeyword().getKeyword("", text);
        System.out.println(Ekeyword);

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
        System.out.println(fid);
        System.out.println(weibouserid);
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
            System.out.println(hit.getContent().getNickname());
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
                System.out.println(original_pictures);
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
        System.out.println("sjdygfk");
        System.out.println(keyword);
        System.out.println(pageId);
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
            jsonObject.put("url",x.findElement(By.xpath(".//a")).getAttribute("href"));
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

        webDriver.findElements(By.className("res-title")).forEach(x -> {
            JSONObject jsonObject =new JSONObject();
            jsonObject.put("title",x.getText());
            jsonObject.put("url",x.findElement(By.xpath(".//a")).getAttribute("href"));
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

        webDriver.findElements(By.className("b_algo")).forEach(x -> {
            JSONObject jsonObject =new JSONObject();
            jsonObject.put("title",x.findElement(By.xpath(".//h2")).getText());
            jsonObject.put("url",x.findElement(By.xpath(".//h2")).findElement(By.xpath(".//a")).getAttribute("href"));
            Arraybing.appendElement(jsonObject);
        });
        webDriver.quit();
        return Arraybing;
    };
}