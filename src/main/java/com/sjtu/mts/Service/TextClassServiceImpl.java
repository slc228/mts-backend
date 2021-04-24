package com.sjtu.mts.Service;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.Cluster;
import com.sjtu.mts.Entity.ClusteredData;
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

import java.math.BigDecimal;
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
        String rpc = textclassRpc.textclass(fileContents);
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
    public com.alibaba.fastjson.JSONObject textClass2(List<String> textList){
        String rpc = textclassRpc.textclass(textList);

        JSONObject jsonObject = JSONObject.parseObject(rpc);
        return jsonObject;
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
        String rpc = textclassRpc.clustering(fileContents);
        JSONArray result = new JSONArray();
        JSONObject rpcJsonObject = JSONObject.parseObject(rpc);
        Map<Integer, String> data =new HashMap<>();
        Iterator it =rpcJsonObject.entrySet().iterator();
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

    @Override
    public  List<Cluster> clusteringData(long fid, String startPublishedDay, String endPublishedDay){

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
        List<Data> dataList = new LinkedList<>();
        List<String> fileContents = new ArrayList<>();
        for(SearchHit<Data> hit : searchHits){
            Data data = hit.getContent();
            dataList.add(data);
            fileContents.add(data.getContent());
        }
        String rpc = textclassRpc.clustering(fileContents);

        JSONObject rpcJsonObject = JSONObject.parseObject(rpc);
        JSONObject rpcdata = rpcJsonObject.getJSONObject("class");
        com.alibaba.fastjson.JSONArray centerList = rpcJsonObject.getJSONArray("centers");

        Map<Integer, String> classData =new HashMap<>();
        Iterator it =rpcdata.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
            classData.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }
        List<Cluster> result = new LinkedList<>();
        for (int i =0 ;i<10;i++){
            Cluster cluster = new Cluster();
            cluster.setClusterNum(i);
            List center = centerList.getJSONArray(i);
            cluster.setCenter((List<BigDecimal>)center);

            result.add(cluster);
        }
        //根据聚类类别num来add Data
        for (int i =0;i<classData.size();i++){
            int clusterNum = Integer.parseInt(classData.get(i));
            result.get(clusterNum).addClusterData(dataList.get(i));
        }

        for (int i = 0;i<result.size();i++){
            Cluster cluster = result.get(i);
            cluster.setHit(cluster.getClusterDatas().size());
            //根据发布时间排序
            Collections.sort(result.get(i).getClusterDatas(), new Comparator<Data>() {
                @Override
                public int compare(Data front, Data behind) {
                    return front.getPublishedDay().compareTo(behind.getPublishedDay());
                }
            });
        }

        //设置Cluster的time属性
        for (int i =0;i<result.size();i++){
            String time = result.get(i).getClusterDatas().get(0).getPublishedDay();
            result.get(i).setTime(time);
        }

        //根据time属性排序
        Collections.sort(result, new Comparator<Cluster>() {
            @Override
            public int compare(Cluster front, Cluster behind) {
                return front.getTime().compareTo(behind.getTime());
            }
        });

        List<ClusteredData> tmp  = new LinkedList<>();
        //重新设置ClusteredData的聚类类别num
        for (int i=0;i<result.size();i++){
            result.get(i).setClusterNum(i+1);
        }
       return result;
    }
}
