package com.sjtu.mts.Service;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.Dao.ElasticSearchDao;
import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.Cluster;
import com.sjtu.mts.Entity.ClusteredData;
import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.Query.ElasticSearchQuery;
import com.sjtu.mts.Repository.AreaRepository;
import com.sjtu.mts.Repository.SensitiveWordRepository;
import com.sjtu.mts.Response.CflagCountResponse;
import com.sjtu.mts.Response.YuQingResponse;
import com.sjtu.mts.rpc.SummaryRpc;
import com.sjtu.mts.rpc.TextAlertRpc;
import com.sjtu.mts.rpc.TextclassRpc;
import net.minidev.json.JSONArray;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

@Service
public class TextAlertServiceImpl implements TextAlertService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final AreaRepository areaRepository;
    //    private final SensitiveWordRepository sensitiveWordRepository;
    static  boolean flag = false;
    static  boolean flagIK = false;

    public TextAlertServiceImpl(ElasticsearchOperations elasticsearchOperations,AreaRepository areaRepository,SensitiveWordRepository sensitiveWordRepository)
    {
        this.elasticsearchOperations = elasticsearchOperations;
        this.areaRepository = areaRepository;
        // this.sensitiveWordRepository = sensitiveWordRepository;
    }

    @Autowired
    private TextAlertRpc textAlertRpc;

    @Autowired
    private FangAnDao fangAnDao;

    @Autowired
    private ElasticSearchDao elasticSearchDao;

    @Override
    public com.alibaba.fastjson.JSONObject textAlert(List<String> textList){
        String rpc = textAlertRpc.textAlert(textList);
        System.out.println(rpc);
        JSONObject jsonObject = JSONObject.parseObject(rpc);
        return jsonObject;
    }

    @Override
    public JSONObject sensitiveCount(long fid) {
        ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
        query.JoinFidQueryBuilders(fid);
        query.SetBoolQuery();
        query.AggregateBySensitiveType();
        List<Long> resultList = elasticSearchDao.aggregateSixTypeBySensitiveType(query);

        ElasticSearchQuery politicQuery=new ElasticSearchQuery(areaRepository,fangAnDao);
        politicQuery.JoinSensitiveTypeQueryBuilders("政治敏感 ");
        politicQuery.SetBoolQuery();
        YuQingResponse politicResponse = elasticSearchDao.findByQuery(politicQuery);

        ElasticSearchQuery pornQuery=new ElasticSearchQuery(areaRepository,fangAnDao);
        pornQuery.JoinSensitiveTypeQueryBuilders("低俗信息 ");
        pornQuery.SetBoolQuery();
        YuQingResponse pornResponse = elasticSearchDao.findByQuery(pornQuery);

        ElasticSearchQuery adQuery=new ElasticSearchQuery(areaRepository,fangAnDao);
        adQuery.JoinSensitiveTypeQueryBuilders("广告营销 ");
        adQuery.SetBoolQuery();
        YuQingResponse adResponse = elasticSearchDao.findByQuery(adQuery);

        ElasticSearchQuery insultQuery=new ElasticSearchQuery(areaRepository,fangAnDao);
        insultQuery.JoinSensitiveTypeQueryBuilders("人身攻击 ");
        insultQuery.SetBoolQuery();
        YuQingResponse insultResponse = elasticSearchDao.findByQuery(insultQuery);


//
//        List<String> textList = new ArrayList<String>();
//        JSONArray pageDataContent = new JSONArray();
//        //Criteria criteria = fangAnDao.criteriaByFid(fid);
//        List<Criteria> criterias=fangAnDao.FindCriteriasByFid(fid);
//        for(Criteria criteria:criterias)
//        {
//            //criteria.subCriteria(new Criteria().and("fromType").is("3"));
//            CriteriaQuery query = new CriteriaQuery(criteria);
//            SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);
//
//            for (SearchHit<Data> hit : searchHits)
//            {
//                Data data = hit.getContent();
//                pageDataContent.add(data);
//                textList.add(data.getContent());
//            }
//        }
//
//        Integer maxSize = 5000;
//        if (textList.size() > maxSize) textList = textList.subList(0, maxSize);
//        System.out.println(textList.size());
//        String rpc = textAlertRpc.textAlert(textList);
//        JSONObject result = JSONObject.parseObject(rpc);
//        Iterator iter = result.entrySet().iterator();
//        Integer normalCnt = 0;
//        JSONArray pornList = new JSONArray();
//        JSONArray adList = new JSONArray();
//        JSONArray politicList = new JSONArray();
//        JSONArray insultList = new JSONArray();
//        while (iter.hasNext()) {
//            Map.Entry entry = (Map.Entry) iter.next();
//            Integer index = Integer.valueOf(entry.getKey().toString());
//            String tags = entry.getValue().toString();
//            if (tags.contains("政治敏感")) politicList.add(pageDataContent.get(index));
//            if (tags.contains("低俗信息")) pornList.add(pageDataContent.get(index));
//            if (tags.contains("人身攻击")) insultList.add(pageDataContent.get(index));
//            if (tags.contains("广告营销")) adList.add(pageDataContent.get(index));
//            if (tags.contains("正常信息")) normalCnt ++;
//        }
        JSONObject ret = new JSONObject();
        ret.put("正常信息", resultList.get(0));
        ret.put("政治敏感", politicResponse.getYuQingContent());
        ret.put("低俗信息", pornResponse.getYuQingContent());
        ret.put("广告营销", adResponse.getYuQingContent());
        ret.put("人身攻击", insultResponse.getYuQingContent());
        return ret;
    }

}
