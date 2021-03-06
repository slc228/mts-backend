package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.WeiboTrack.WeiboData;
import com.sjtu.mts.WeiboTrack.WeiboRepostTree;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class WeiboTrackServiceImpl implements WeiboTrackService {

    private final ElasticsearchOperations elasticsearchOperations;

    public WeiboTrackServiceImpl(ElasticsearchOperations elasticsearchOperations)
    {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public WeiboRepostTree trackWeibo(String keyword, String startPublishedDay, String endPublishedDay){
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

        criteria.subCriteria(new Criteria().and("fromType").is("3"));

        CriteriaQuery query = new CriteriaQuery(criteria);

        SearchHits<Data> searchHits = this.elasticsearchOperations.search(query, Data.class);

        WeiboData test = new WeiboData("root", "", "0", "unknown");
        WeiboRepostTree root = new WeiboRepostTree(test);
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
                String author = singleWeiboContent.substring(0, colon);
                String content = singleWeiboContent.substring(colon + 1);
                if (i == repostList.size() - 1){
                    String cflag = weiboOriginData.getCflag();
                    String publishedDay = weiboOriginData.getPublishedDay();
                    WeiboData weiboData = new WeiboData(author, content, cflag, publishedDay);
                    current = current.findChildAndAddInfo(weiboData);
                }
                else{
                    WeiboData weiboData = new WeiboData(author, content, "0", "unknown");
                    current = current.findChild(weiboData);
                }
            }
        }
        return root;
    }
}
