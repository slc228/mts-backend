package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.rpc.SummaryRpc;
import net.minidev.json.JSONObject;
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
public class SummaryServiceImpl implements SummaryService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private FangAnDao fangAnDao;
    @Autowired
    private SummaryRpc summaryRpc;

    public JSONObject multiSummary(long fid, String startPublishedDay, String endPublishedDay)
    {
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

        String rpc = summaryRpc.multiDocumentsSummary(fileContents);
        JSONObject result = new JSONObject();
        result.put("summary", rpc);
        return result;
    }

    public JSONObject singleSummary(String document)
    {
        String rpc = summaryRpc.singleDocumentSummary(document);
        JSONObject result = new JSONObject();
        result.put("summary", rpc);
        return result;
    }

}
