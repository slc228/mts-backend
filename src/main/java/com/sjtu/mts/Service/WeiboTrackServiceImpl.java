package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.Entity.FangAn;
import com.sjtu.mts.Repository.AreaRepository;
import com.sjtu.mts.Repository.FangAnRepository;
import com.sjtu.mts.WeiboTrack.WeiboData;
import com.sjtu.mts.WeiboTrack.WeiboRepostTree;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeiboTrackServiceImpl implements WeiboTrackService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final AreaRepository areaRepository;
    private final FangAnRepository fangAnRepository;

    public WeiboTrackServiceImpl(ElasticsearchOperations elasticsearchOperations,FangAnRepository fangAnRepository,AreaRepository areaRepository)
    {
        this.elasticsearchOperations = elasticsearchOperations;
        this.areaRepository = areaRepository;
        this.fangAnRepository = fangAnRepository;
    }

    @Override
    public WeiboRepostTree trackWeibo(long fid, String startPublishedDay, String endPublishedDay){
        FangAn fangAn = fangAnRepository.findByFid(fid);
        int matchType = fangAn.getMatchType();
        String regionKeyword = fangAn.getRegionKeyword();
        int regionKeywordMatch = fangAn.getRegionKeywordMatch();

        String roleKeyword = fangAn.getRoleKeyword();
        int roleKeywordMatch = fangAn.getRoleKeywordMatch();

        String eventKeyword = fangAn.getEventKeyword();
        int eventKeywordMatch = fangAn.getEventKeywordMatch();

        Criteria criteria = new Criteria();
        if (!roleKeyword.isEmpty())
        {
            String[] searchSplitArray1 = roleKeyword.trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
            System.out.println(searchSplitArray.size());
            System.out.println(searchSplitArray.get(0));
            if(searchSplitArray.size()>1){
                if(roleKeywordMatch==1){
                    for (String searchString : searchSplitArray) {

                        criteria.subCriteria(new Criteria().and("content").contains(searchString).
                                or("title").contains(searchString));
                    }
                }else {
                    criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
                }
            }else {
                criteria.subCriteria(new Criteria().and("content").contains(searchSplitArray.get(0)).
                        or("title").contains(searchSplitArray.get(0)));
            }


        }
        if (!eventKeyword.isEmpty())
        {
            String[] searchSplitArray1 = eventKeyword.trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
            System.out.println(searchSplitArray.size());
            if(searchSplitArray.size()>1){
                if(eventKeywordMatch==1){
                    for (String searchString : searchSplitArray) {

                        criteria.subCriteria(new Criteria().and("content").contains(searchString).
                                or("title").contains(searchString));
                    }
                }else {
                    criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
                }
            }else {
                criteria.subCriteria(new Criteria().and("content").contains(searchSplitArray.get(0)).
                        or("title").contains(searchSplitArray.get(0)));
            }


        }
        if (!regionKeyword.isEmpty())
        {

            String[] searchSplitArray1 = regionKeyword.trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
            if(searchSplitArray.size()==1 ){
                List<Integer> codeid  = areaRepository.findCodeidByCityName(searchSplitArray.get(0));
                List<String> citys = new ArrayList<>();
                for (Integer co:codeid){
                    List<String> tmp = areaRepository.findCityNameByCodeid(co) ;
                    for(int i=0;i<tmp.size();i++){
                        tmp.set(i,tmp.get(i).replaceAll("\\s*", ""));
                        if(tmp.get(i).contains("市辖")||tmp.get(i).contains("县辖")){
                            tmp.remove(i);
                        }
                    }
                    citys.addAll(tmp);
                }

                citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重
                //System.out.println(Arrays.toString(citys.toArray()));
                criteria.subCriteria(new Criteria("content").in(citys).or("title").in(citys));
            }else if(searchSplitArray.size()>1 && regionKeywordMatch == 1){
                for (String searchString : searchSplitArray){
                    List<Integer> codeid  = areaRepository.findCodeidByCityName(searchString);
                    List<String> citys = new ArrayList<>();
                    for (Integer co:codeid){
                        List<String> tmp = areaRepository.findCityNameByCodeid(co) ;
                        for(int i=0;i<tmp.size();i++){
                            tmp.set(i,tmp.get(i).replaceAll("\\s*", ""));
                            if(tmp.get(i).contains("市辖")||tmp.get(i).contains("县辖")){
                                tmp.remove(i);
                            }
                        }
                        citys.addAll(tmp);
                    }

                    citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重
                    //System.out.println(Arrays.toString(citys.toArray()));
                    criteria.subCriteria(new Criteria("content").in(citys).or("title").in(citys));
                }
            }
            else if(searchSplitArray.size()>1 && regionKeywordMatch ==0){
                List<String> citys = new ArrayList<>();
                for (String searchString : searchSplitArray){
                    List<Integer> codeid  = areaRepository.findCodeidByCityName(searchString);
                    for (Integer co:codeid){
                        List<String> tmp = areaRepository.findCityNameByCodeid(co) ;
                        for(int i=0;i<tmp.size();i++){
                            tmp.set(i,tmp.get(i).replaceAll("\\s*", ""));
                            if(tmp.get(i).contains("市辖")||tmp.get(i).contains("县辖")){
                                tmp.remove(i);
                            }
                        }
                        citys.addAll(tmp);
                    }
                    citys = (List) citys.stream().distinct().collect(Collectors.toList());//去重

                }
                //System.out.println(Arrays.toString(citys.toArray()));
                criteria.subCriteria(new Criteria("content").in(citys).or("title").in(citys));
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
                if (colon == -1){
                    break;
                }
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
