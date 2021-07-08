package com.sjtu.mts.Service;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.Response.AmountTrendResponse;
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
    @Autowired
    private SentimentRpc sentimentRpc;

    @Autowired
    private FangAnDao fangAnDao;

    private final ElasticsearchOperations elasticsearchOperations;

    public SentimentServiceImpl(ElasticsearchOperations elasticsearchOperations)
    {
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


        String rpc = sentimentRpc.sentimentAnalysis(fileContents);
        JSONObject jsonObject = JSONObject.parseObject(rpc);

        long happyCount = 0;
        long surpriseCount = 0;
        long sadCount = 0;
        long fearCount = 0;
        long angryCount = 0;
        long neutralCount = 0;

        for (Object object : jsonObject.values()) {
            if (object.toString().equals("happy")){
                happyCount++;
            }
            else if (object.toString().equals("sad")){
                sadCount++;
            }
            else if (object.toString().equals("fear")){
                fearCount++;
            }
            else if (object.toString().equals("angry")){
                angryCount++;
            }
            else if (object.toString().equals("surprise")){
                surpriseCount++;
            }
            else if (object.toString().equals("neutral")){
                neutralCount++;
            }
        }
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
            //Criteria criteria = fangAnDao.criteriaByFid(fid);
            List<String> fileContents = new ArrayList<>();

            List<Criteria> criterias=fangAnDao.FindCriteriasByFid(fid);
            for (Criteria criteria:criterias)
            {
                criteria.subCriteria(new Criteria().and("publishedDay").between(dateList.get(j), dateList.get(j + 1)));
                CriteriaQuery query = new CriteriaQuery(criteria);

                SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);

                for (SearchHit<Data> hit : searchHits) {
                    Data data = hit.getContent();
                    fileContents.add(data.getContent());
                }
            }


            String rpc = sentimentRpc.sentimentAnalysis(fileContents);
            JSONObject jsonObject = JSONObject.parseObject(rpc);

            long happyCount = 0;
            long surpriseCount = 0;
            long sadCount = 0;
            long fearCount = 0;
            long angryCount = 0;
            long neutralCount = 0;
            for (Object object : jsonObject.values()) {
                if (object.toString().equals("happy")){
                    happyCount++;
                }
                else if (object.toString().equals("sad")){
                    sadCount++;
                }
                else if (object.toString().equals("fear")){
                    fearCount++;
                }
                else if (object.toString().equals("angry")){
                    angryCount++;
                }
                else if (object.toString().equals("surprise")){
                    surpriseCount++;
                }
                else if (object.toString().equals("neutral")){
                    neutralCount++;
                }
            }
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
