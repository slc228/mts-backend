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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        for(SearchHit<Data> hit : searchHits){
            Data data = hit.getContent();
            fileContents.add(data.getTitle());
        }
        //System.out.println(textclassRpc.sayHello());
        String re = textclassRpc.textclass(fileContents);
        JSONArray result = new JSONArray();
        JSONObject jsonObject = JSONObject.parseObject(re);
        result.add(jsonObject);
        return result;
    }
}
