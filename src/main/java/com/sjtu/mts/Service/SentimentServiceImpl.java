package com.sjtu.mts.Service;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.Dao.ElasticSearchDao;
import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.Query.ElasticSearchQuery;
import com.sjtu.mts.Repository.AreaRepository;
import com.sjtu.mts.Response.SentimentCountResponse;
import com.sjtu.mts.Response.SentimentTrendResponse;
import com.sjtu.mts.rpc.SentimentRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SentimentServiceImpl implements SentimentService {
    private final AreaRepository areaRepository;

    @Autowired
    private SentimentRpc sentimentRpc;

    @Autowired
    private FangAnDao fangAnDao;

    @Autowired
    private ElasticSearchDao elasticSearchDao;

    private final ElasticsearchOperations elasticsearchOperations;

    public SentimentServiceImpl(AreaRepository areaRepository, ElasticsearchOperations elasticsearchOperations)
    {
        this.areaRepository = areaRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public JSONObject sentimentPredict(List<String> textList){
        String rpc = sentimentRpc.sentimentAnalysis(textList);

        JSONObject jsonObject = JSONObject.parseObject(rpc);
        return jsonObject;
    }

    @Override
    public SentimentCountResponse countSentiment(long fid, String startPublishedDay, String endPublishedDay) {
        ElasticSearchQuery queryHappy=new ElasticSearchQuery(areaRepository,fangAnDao);
        queryHappy.JoinFidQueryBuilders(fid);
        queryHappy.JoinEmotionQueryBuilders("happy");
        queryHappy.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
        queryHappy.SetBoolQuery();
        long happyCount=elasticSearchDao.countByQuery(queryHappy);

        ElasticSearchQuery querySurprise=new ElasticSearchQuery(areaRepository,fangAnDao);
        querySurprise.JoinFidQueryBuilders(fid);
        querySurprise.JoinEmotionQueryBuilders("surprise");
        querySurprise.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
        querySurprise.SetBoolQuery();
        long surpriseCount=elasticSearchDao.countByQuery(querySurprise);

        ElasticSearchQuery querySad=new ElasticSearchQuery(areaRepository,fangAnDao);
        querySad.JoinFidQueryBuilders(fid);
        querySad.JoinEmotionQueryBuilders("sad");
        querySad.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
        querySad.SetBoolQuery();
        long sadCount=elasticSearchDao.countByQuery(querySad);

        ElasticSearchQuery queryFear=new ElasticSearchQuery(areaRepository,fangAnDao);
        queryFear.JoinFidQueryBuilders(fid);
        queryFear.JoinEmotionQueryBuilders("fear");
        queryFear.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
        queryFear.SetBoolQuery();
        long fearCount=elasticSearchDao.countByQuery(queryFear);

        ElasticSearchQuery queryAngry=new ElasticSearchQuery(areaRepository,fangAnDao);
        queryAngry.JoinFidQueryBuilders(fid);
        queryAngry.JoinEmotionQueryBuilders("angry");
        queryAngry.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
        queryAngry.SetBoolQuery();
        long angryCount=elasticSearchDao.countByQuery(queryAngry);

        ElasticSearchQuery queryNeutral=new ElasticSearchQuery(areaRepository,fangAnDao);
        queryNeutral.JoinFidQueryBuilders(fid);
        queryNeutral.JoinEmotionQueryBuilders("neutral");
        queryNeutral.JoinPublishedDayQueryBuilders(startPublishedDay,endPublishedDay);
        queryNeutral.SetBoolQuery();
        long neutralCount=elasticSearchDao.countByQuery(queryNeutral);

        SentimentCountResponse sentimentCountResponse = new SentimentCountResponse(happyCount,
                surpriseCount, sadCount, angryCount, fearCount, neutralCount);
        return sentimentCountResponse;
    }

    @Override
    public SentimentTrendResponse sentimentTrendCount(long fid, String startPublishedDay, String endPublishedDay){
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

        SentimentTrendResponse response = new SentimentTrendResponse(new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        for (int j = 0; j < pointNum; j++) {
            timeRange.add(sdf.format(dateList.get(j)) + " to " + sdf.format(dateList.get(j + 1)));

            ElasticSearchQuery queryHappy=new ElasticSearchQuery(areaRepository,fangAnDao);
            queryHappy.JoinFidQueryBuilders(fid);
            queryHappy.JoinEmotionQueryBuilders("happy");
            queryHappy.JoinPublishedDayQueryBuilders(sdf.format(dateList.get(j)),sdf.format(dateList.get(j+1)));
            queryHappy.SetBoolQuery();
            long happyCount=elasticSearchDao.countByQuery(queryHappy);

            ElasticSearchQuery querySurprise=new ElasticSearchQuery(areaRepository,fangAnDao);
            querySurprise.JoinFidQueryBuilders(fid);
            querySurprise.JoinEmotionQueryBuilders("surprise");
            querySurprise.JoinPublishedDayQueryBuilders(sdf.format(dateList.get(j)),sdf.format(dateList.get(j+1)));
            querySurprise.SetBoolQuery();
            long surpriseCount=elasticSearchDao.countByQuery(querySurprise);

            ElasticSearchQuery querySad=new ElasticSearchQuery(areaRepository,fangAnDao);
            querySad.JoinFidQueryBuilders(fid);
            querySad.JoinEmotionQueryBuilders("sad");
            querySad.JoinPublishedDayQueryBuilders(sdf.format(dateList.get(j)),sdf.format(dateList.get(j+1)));
            querySad.SetBoolQuery();
            long sadCount=elasticSearchDao.countByQuery(querySad);

            ElasticSearchQuery queryFear=new ElasticSearchQuery(areaRepository,fangAnDao);
            queryFear.JoinFidQueryBuilders(fid);
            queryFear.JoinEmotionQueryBuilders("fear");
            queryFear.JoinPublishedDayQueryBuilders(sdf.format(dateList.get(j)),sdf.format(dateList.get(j+1)));
            queryFear.SetBoolQuery();
            long fearCount=elasticSearchDao.countByQuery(queryFear);

            ElasticSearchQuery queryAngry=new ElasticSearchQuery(areaRepository,fangAnDao);
            queryAngry.JoinFidQueryBuilders(fid);
            queryAngry.JoinEmotionQueryBuilders("angry");
            queryAngry.JoinPublishedDayQueryBuilders(sdf.format(dateList.get(j)),sdf.format(dateList.get(j+1)));
            queryAngry.SetBoolQuery();
            long angryCount=elasticSearchDao.countByQuery(queryAngry);

            ElasticSearchQuery queryNeutral=new ElasticSearchQuery(areaRepository,fangAnDao);
            queryNeutral.JoinFidQueryBuilders(fid);
            queryNeutral.JoinEmotionQueryBuilders("neutral");
            queryNeutral.JoinPublishedDayQueryBuilders(sdf.format(dateList.get(j)),sdf.format(dateList.get(j+1)));
            queryNeutral.SetBoolQuery();
            long neutralCount=elasticSearchDao.countByQuery(queryNeutral);

            response.getHappyTrend().add(happyCount);
            response.getSurpriseTrend().add(surpriseCount);
            response.getSadTrend().add(sadCount);
            response.getFearTrend().add(fearCount);
            response.getAngryTrend().add(angryCount);
            response.getNeutralTrend().add(neutralCount);
            response.getTotalAmountTrend().add(happyCount+surpriseCount+sadCount+fearCount+angryCount+neutralCount);
        }
        response.setTimeRange(timeRange);
        return response;
    }
}
