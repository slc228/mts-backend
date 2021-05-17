package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.*;
import com.sjtu.mts.Keyword.KeywordResponse;
import com.sjtu.mts.Keyword.MultipleThreadExtraction;
import com.sjtu.mts.Repository.AreaRepository;
import com.sjtu.mts.Repository.SensitiveWordRepository;
import com.sjtu.mts.Response.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.sjtu.mts.Keyword.Wrapper.min;


@Service
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final AreaRepository areaRepository;
    private final SensitiveWordRepository sensitiveWordRepository;
    static  boolean flag = false;
    static  boolean flagIK = false;

    @Autowired
    private FangAnDao fangAnDao;


    public SearchServiceImpl(ElasticsearchOperations elasticsearchOperations,AreaRepository areaRepository,SensitiveWordRepository sensitiveWordRepository)
    {
        this.elasticsearchOperations = elasticsearchOperations;
        this.areaRepository = areaRepository;
        this.sensitiveWordRepository = sensitiveWordRepository;
    }

    @Override
    public DataResponse Search(String keyword, String cflag, String startPublishedDay, String endPublishedDay,
                               String fromType, int page, int pageSize, int timeOrder)
    {
        Criteria criteria = new Criteria();
        if (!keyword.isEmpty())
        {
            String[] searchSplitArray = keyword.trim().split("\\s+");;
            for (String searchString : searchSplitArray) {
                criteria.subCriteria(new Criteria().and("content").contains(searchString).
                        or("title").contains(searchString));
            }
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
    public ResourceCountResponse globalSearchResourceCount2(long fid,String startPublishedDay, String endPublishedDay){
        List<Long> resultList = new ArrayList<>();
        for (int fromType = 1; fromType <= 7 ; fromType++) {

            Criteria criteria = fangAnDao.criteriaByFid(fid);
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
    public CflagCountResponse globalSearchCflagCount2(long fid, String startPublishedDay, String endPublishedDay){
        List<Long> resultList = new ArrayList<>();
        for (int cflag = 0; cflag <= 1 ; cflag++) {
            Criteria criteria = fangAnDao.criteriaByFid(fid);
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
            List<Long> resultList = new ArrayList<>();
            for (int j = 0; j < pointNum; j++) {
                Criteria criteria = fangAnDao.criteriaByFid(fid);
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
    public AreaAnalysisResponse countArea2(long fid,String startPublishedDay, String endPublishedDay){
        List<Long> resultList = new ArrayList<>();
        List<Integer> codeids = Arrays.asList(11,12,13,14,15,21,22,23,31,32,33,34,35,36,37,41,42,43,44,45,46,50,51,52,53,54,61,62,63,64,65,71,81,91);
        for(int i =0;i<codeids.size();i++){
            Criteria criteria = fangAnDao.criteriaByFid(fid);
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
    public DataResponse fangAnSearch(long fid,String cflag, String startPublishedDay, String endPublishedDay,
                                     String fromType, int page, int pageSize, int timeOrder){
        Criteria criteria = fangAnDao.criteriaByFid(fid);
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
        result.setDataContent(pageDataContent);

        return result;
    }
    @Override
    public DataResponse fangAnSearch2(long fid,String keyword,String cflag, String startPublishedDay, String endPublishedDay,
                                     String fromType, int page, int pageSize, int timeOrder){
        Criteria criteria = fangAnDao.criteriaByFid(fid);
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
        result.setDataContent(pageDataContent);

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
        if (false == flagIK){

            // 从数据库中获取敏感词对象集合
            List<SensitiveWord> sensitiveWords = sensitiveWordRepository.findAll();
            // 构建敏感词库
            Set<String> sensitiveWordSet = new HashSet<>();
            for (SensitiveWord s : sensitiveWords)
            {
                sensitiveWordSet.add(s.getContent().trim());
            }
            SensitiveWordUtil2.init(sensitiveWordSet);
            flagIK = true;
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
        Criteria criteria = fangAnDao.criteriaByFid(fid);
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

        List<String> fileContents = new ArrayList<>();
        JSONArray result = new JSONArray();

        for(SearchHit<Data> hit : searchHits){
            Data data = hit.getContent();
            JSONArray r1 = sensitiveWordFiltering(data.getContent());
            result.add(r1);
            //fileContents.add(data.getContent());

        }
        long end = System.currentTimeMillis();
        System.out.println("敏感词提取耗时：" + (end-start) + "ms");
        return result;
    }

    @Override
    public List<KeywordResponse> extractKeyword(long fid, String startPublishedDay, String endPublishedDay, int keywordNumber){
        Criteria criteria = fangAnDao.criteriaByFid(fid);
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

        List<String> fileContents = new ArrayList<>();
        for(SearchHit<Data> hit : searchHits){
            Data data = hit.getContent();
            fileContents.add(data.getContent());
        }

        //开始使用多线程提取关键词
        int threadCounts = 8;//采用的线程数

        long start=  System.currentTimeMillis();
        MultipleThreadExtraction countListIntegerSum=new MultipleThreadExtraction(fileContents,threadCounts);

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
}
