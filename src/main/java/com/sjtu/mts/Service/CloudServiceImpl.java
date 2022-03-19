package com.sjtu.mts.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.Dao.ElasticSearchDao;
import com.sjtu.mts.Dao.FangAnCloudDao;
import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.FangAn;
import com.sjtu.mts.Entity.FangAnCloud;
import com.sjtu.mts.Entity.YuQing;
import com.sjtu.mts.Keyword.KeywordResponse;
import com.sjtu.mts.Query.ElasticSearchQuery;
import com.sjtu.mts.Repository.AreaRepository;
import com.sjtu.mts.Response.YuQingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import static com.sjtu.mts.Keyword.Wrapper.min;

@Service
@EnableScheduling
public class CloudServiceImpl implements CloudService {
    private final AreaRepository areaRepository;

    @Autowired
    private FangAnDao fangAnDao;

    @Autowired
    private ElasticSearchDao elasticSearchDao;

    @Autowired
    private FangAnCloudDao fangAnCloudDao;

    public CloudServiceImpl(AreaRepository areaRepository)
    {
        this.areaRepository = areaRepository;
    }


    @Override
    public JSONObject getWordCloudByFid(long fid) {
        JSONObject ret=new JSONObject();
        List<FangAnCloud> fangAnCloudList=fangAnCloudDao.SelectFanganCloudByFid(fid);
        if (fangAnCloudList.size()>0)
        {
            FangAnCloud fangAnCloud=fangAnCloudList.get(0);
            String words=fangAnCloud.getCloudWord();
            ret.put("words",words);
        }else {
            FangAn fangAn=fangAnDao.findByFid(fid);
            List<List<String>> sum=new ArrayList<>();
            ElasticSearchQuery query=new ElasticSearchQuery(areaRepository,fangAnDao);
            query.JoinFidQueryBuilders(fid);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar c = Calendar.getInstance();
            //过去七天
            c.setTime(new Date());
            Date end=c.getTime();
            String endDay=format.format(end);
            c.add(Calendar.DATE, - 7);
            Date start = c.getTime();
            String startDay = format.format(start);
            query.JoinPublishedDayQueryBuilders(startDay,endDay);
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
            JSONArray jsonArray=new JSONArray();
            for(int i=0; i<min(50, keywordList.size()); i++)
            {
                JSONObject keywordObject = new JSONObject();
                String name = keywordList.get(i).getKey().replace(" ", "");
                Integer value = keywordList.get(i).getValue();
                keywordObject.put("name",name);
                keywordObject.put("value",value);
                jsonArray.add(keywordObject);
                keywords.add(new KeywordResponse(name, value));
            }
            ret.put("words",jsonArray.toJSONString());
            fangAnCloudDao.InsertFanganCloud(fid,jsonArray.toJSONString());
        }
        return ret;
    }
}
