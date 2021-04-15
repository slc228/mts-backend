package com.sjtu.mts.Service;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.rpc.TextclassRpc;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TextClassServiceImpl implements TextClassService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private FangAnDao fangAnDao;
    @Autowired
    private TextclassRpc textclassRpc;
    @Override
    public JSONArray textClass(long fid, String startPublishedDay, String endPublishedDay){
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
        List<String> fileTitles = new ArrayList<>();
        for(SearchHit<Data> hit : searchHits){
            Data data = hit.getContent();
            fileTitles.add(data.getTitle());
            fileContents.add(data.getContent());
        }
        //System.out.println(textclassRpc.sayHello());
        String rpc = textclassRpc.textclass(fileTitles);
        JSONArray result = new JSONArray();
        JSONObject jsonObject = JSONObject.parseObject(rpc);
        Map<Integer, String> data =new HashMap<>();
        Iterator it =jsonObject.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
            data.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }
        for (int i =0;i<fileContents.size();i++){
            JSONObject js= new JSONObject();
            String title = fileTitles.get(i);
            String content = fileContents.get(i);
            String textclass = data.get(i);
            js.put("index",i);
            js.put("title",title);
            js.put("content",content);
            js.put("textclass",textclass);
            result.appendElement(js);
        }
        long end = System.currentTimeMillis();
        System.out.println("文本分类耗时：" + (end-start) + "ms");
        return result;
    }

    @Override
    public  JSONArray clustering(long fid, String startPublishedDay, String endPublishedDay){
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
        List<String> fileTitles = new ArrayList<>();
        for(SearchHit<Data> hit : searchHits){
            Data data = hit.getContent();
            fileTitles.add(data.getTitle());
            fileContents.add(data.getContent());
        }
        String rpc = textclassRpc.clustering(fileTitles);
        JSONArray result = new JSONArray();
        JSONObject jsonObject = JSONObject.parseObject(rpc);
        Map<Integer, String> data =new HashMap<>();
        Iterator it =jsonObject.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
            data.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }
        for (int i =0;i<fileContents.size();i++){
            JSONObject js= new JSONObject();
            String title = fileTitles.get(i);
            String content = fileContents.get(i);
            String textclass = data.get(i);
            js.put("index",i);
            js.put("title",title);
            js.put("content",content);
            js.put("textclass",textclass);
            result.appendElement(js);
        }
        long end = System.currentTimeMillis();
        System.out.println("文本聚类耗时：" + (end-start) + "ms");
        return result;
    }
}
