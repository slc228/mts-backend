package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.WeiboTrack.WeiboData;
import com.sjtu.mts.WeiboTrack.WeiboRepostTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class WeiboTrackServiceImpl implements WeiboTrackService {

    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private FangAnDao fangAnDao;

    public WeiboTrackServiceImpl(ElasticsearchOperations elasticsearchOperations)
    {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public WeiboRepostTree trackWeibo(long fid, String startPublishedDay, String endPublishedDay){
        WeiboData test = new WeiboData("root", "", "0", "unknown", "unknown");
        WeiboRepostTree root = new WeiboRepostTree(test);
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

            criteria.subCriteria(new Criteria().and("fromType").is("3"));

            CriteriaQuery query = new CriteriaQuery(criteria);

            SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);

            for (SearchHit<Data> hit : searchHits)
            {
                WeiboRepostTree current = root;
                Data weiboOriginData = hit.getContent();
                String weiboContentProcessed = weiboOriginData.getTitle() + ":" + weiboOriginData.getContent();
                List<String> repostList = Arrays.asList(weiboContentProcessed.split("//@"));
                Collections.reverse(repostList);
                for (int i = 0; i < repostList.size(); i++){
                    String singleWeiboContent = repostList.get(i);
                    int colon = singleWeiboContent.indexOf(':');
                    if (colon == -1){
                        break;
                    }
                    String author = singleWeiboContent.substring(0, colon);
                    String content = singleWeiboContent.substring(colon + 1);
                    if (i == repostList.size() - 1){
                        String cflag = weiboOriginData.getCflag();
                        String publishedDay = weiboOriginData.getPublishedDay();
                        String url = weiboOriginData.getWebpageUrl();
                        WeiboData weiboData = new WeiboData(author, content, cflag, url, publishedDay);
                        current = current.findChildAndAddInfo(weiboData);
                    }
                    else{
                        WeiboData weiboData = new WeiboData(author, content, "0","unknown","unknown");
                        current = current.findChild(weiboData);
                    }
                }
            }
        }
        return root;
    }
}
