package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.ElasticSearchDao;
import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.FangAn;
import com.sjtu.mts.Keyword.ESSaveThread;
import com.sjtu.mts.Query.ElasticSearchQuery;
import com.sjtu.mts.Repository.AreaRepository;
import com.sjtu.mts.Repository.SwordFidRepository;
import com.sjtu.mts.Response.FangAnResponse;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class FangAnServiceImpl implements FangAnService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final AreaRepository areaRepository;

    @Autowired
    private FangAnDao fangAnDao;

    @Autowired
    private ElasticSearchDao elasticSearchDao;

    @Autowired
    private SwordFidRepository swordFidRepository;

    public FangAnServiceImpl(ElasticsearchOperations elasticsearchOperations, AreaRepository areaRepository) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.areaRepository = areaRepository;
    }

    @Override
    public JSONObject findAllByUsername(String username){

        JSONArray jsonArray = new JSONArray();
        List<FangAn> fangAnList =   fangAnDao.findAllByUsername(username);
        for(FangAn fangAn : fangAnList){
            JSONObject object = new JSONObject();
            object.put("fid", fangAn.getFid());
            object.put("username", fangAn.getUsername());
            object.put("programmeName", fangAn.getProgrammeName());
            object.put("matchType", fangAn.getMatchType());
            object.put("regionKeyword", fangAn.getRegionKeyword());
            object.put("regionKeywordMatch", fangAn.getRegionKeywordMatch());
            object.put("roleKeyword", fangAn.getRoleKeyword());
            object.put("roleKeywordMatch", fangAn.getRoleKeywordMatch());
            //object.put("eventKeyword", fangAn.getEventKeyword());
            object.put("eventKeywordMatch", fangAn.getEventKeywordMatch());
            object.put("enableAlert", fangAn.getEnableAlert());
            object.put("sensitiveWord", fangAn.getSensitiveword());
            object.put("priority",fangAn.getPriority());
            JSONArray eventKeyword = new JSONArray();
            String event = fangAn.getEventKeyword();
            while(event.length()>0)
            {
                int tag=event.indexOf('+');
                eventKeyword.appendElement(event.substring(0,tag));
                event=event.substring(tag+1);
            }
            object.put("eventKeyword", eventKeyword);
            jsonArray.appendElement(object);
        }
        JSONObject object = new JSONObject();
        object.put("data",jsonArray);
        return  object;

    }

    @Override
    public JSONObject saveFangAn(String username,
                                 String programmeName,
                                 int matchType,
                                 String regionKeyword,
                                 int regionKeywordMatch,
                                 String roleKeyword,
                                 int roleKeywordMatch,
                                 String eventKeyword,
                                 int eventKeywordMatch,
                                 boolean enableAlert,
                                 String sensitiveWord,
                                 Integer priority){
        JSONObject result = new JSONObject();
        result.put("saveFangAn", 0);
        Boolean ifExist = fangAnDao.existsByUsernameAndProgrammeName(username,programmeName);
        if(ifExist){
            result.put("saveFangAn", 0);
            result.put("方案名重复", 1);
            return result;
        }
        try {
            fangAnDao.InsertFangan(username,programmeName,matchType,regionKeyword,regionKeywordMatch,roleKeyword,roleKeywordMatch,eventKeyword,eventKeywordMatch,enableAlert,sensitiveWord,priority);
            result.put("saveFangAn", 1);
            return result;
        }catch (Exception e){
            result.put("saveFangAn", 0);
        }
        return result;
    }
    @Override
    public JSONObject changeFangAn(long fid,
                                   String username,
                                   String programmeName,
                                   int matchType,
                                   String regionKeyword,
                                   int regionKeywordMatch,
                                   String roleKeyword,
                                   int roleKeywordMatch,
                                   String eventKeyword,
                                   int eventKeywordMatch,
                                   boolean enableAlert,
                                   String sensitiveWord,
                                   Integer priority
    ){
        JSONObject result = new JSONObject();
        result.put("changeFangAn", 0);
        FangAn oldFangAn = fangAnDao.findByFid(fid);
        String[] oldRegionKeyword = oldFangAn.getRegionKeyword().trim().split("\\s+");
        List<String> oldRegionKeywordList = Arrays.asList(oldRegionKeyword);
        String[] oldRoleKeyword = oldFangAn.getRoleKeyword().trim().split("\\s+");
        List<String> oldRoleKeywordList = Arrays.asList(oldRoleKeyword);
        String[] oldEventKeyword = oldFangAn.getEventKeyword().trim().split("\\+");
        List<String> oldEventKeywordList = Arrays.asList(oldEventKeyword);

        String[] newRegionKeyword = regionKeyword.trim().split("\\s+");
        List<String> newRegionKeywordList = Arrays.asList(newRegionKeyword);
        String[] newRoleKeyword = roleKeyword.trim().split("\\s+");
        List<String> newRoleKeywordList = Arrays.asList(newRoleKeyword);
        String[] newEventKeyword = eventKeyword.trim().split("\\+");
        List<String> newEventKeywordList = Arrays.asList(newEventKeyword);
        if (newRegionKeywordList.containsAll(oldRegionKeywordList)
                &&newRoleKeywordList.containsAll(oldRoleKeywordList)
                &&newEventKeywordList.containsAll(oldEventKeywordList))
        {
            try {
                if(!oldFangAn.getUsername().equals(username)){
                    result.put("该方案不是你的",1);
                    result.put("changeFangAn", 0);
                    return result;
                }
                oldFangAn.setUsername(username);
                oldFangAn.setProgrammeName(programmeName);
                oldFangAn.setRegionKeyword(regionKeyword);
                oldFangAn.setRegionKeywordMatch(regionKeywordMatch);
                oldFangAn.setRoleKeyword(roleKeyword);
                oldFangAn.setRoleKeywordMatch(roleKeywordMatch);
                oldFangAn.setEventKeyword(eventKeyword);
                oldFangAn.setEventKeywordMatch(eventKeywordMatch);
                oldFangAn.setEnableAlert(enableAlert);
                oldFangAn.setSensitiveword(sensitiveWord);
                oldFangAn.setPriority(priority);
                //fangAnDao.deleteByFid(fid);
                fangAnDao.UpdateFangan(fid,username,programmeName,matchType,regionKeyword,regionKeywordMatch,roleKeyword,roleKeywordMatch,
                                        eventKeyword,eventKeywordMatch,enableAlert,sensitiveWord,priority);
                result.put("changeFangAn", 1);
            }catch (Exception e){
                result.put("changeFangAn", 0);
            }

            ElasticSearchQuery elasticSearchQuery=new ElasticSearchQuery(areaRepository,fangAnDao);
            Thread t1=new ESSaveThread(elasticSearchQuery,fid,this.elasticsearchOperations,elasticSearchDao);
            t1.start();

//            ElasticSearchQuery elasticSearchQuery=new ElasticSearchQuery(areaRepository,fangAnDao);
//            elasticSearchQuery.JoinFidQueryBuildersWithOutFid(fid);
//            elasticSearchQuery.SetBoolQuery();
//            List<YuQingElasticSearch> yuQings=elasticSearchDao.findESByQuery(elasticSearchQuery);
//            System.out.println(yuQings.size());
//            List<YuQingElasticSearch> newYuqings=new ArrayList<>();
//            for (YuQingElasticSearch yuQing:yuQings)
//            {
//                yuQing.setFid((int) fid);
//                yuQing.setId(UUID.randomUUID().toString());
//                newYuqings.add(yuQing);
//            }
//            this.elasticsearchOperations.save(newYuqings,this.elasticsearchOperations.getIndexCoordinatesFor(YuQingElasticSearch.class));
            System.out.println("aka");
        }else
        {
            long newfid = 0;
            try {
                FangAn fangAn=new FangAn();
                fangAn.setUsername(username);
                fangAn.setProgrammeName(programmeName);
                fangAn.setRegionKeyword(regionKeyword);
                fangAn.setRegionKeywordMatch(regionKeywordMatch);
                fangAn.setRoleKeyword(roleKeyword);
                fangAn.setRoleKeywordMatch(roleKeywordMatch);
                fangAn.setEventKeyword(eventKeyword);
                fangAn.setEventKeywordMatch(eventKeywordMatch);
                fangAn.setEnableAlert(enableAlert);
                fangAn.setSensitiveword(sensitiveWord);
                fangAn.setPriority(priority);
                newfid=fangAnDao.InsertFangan(username,programmeName,matchType,regionKeyword,regionKeywordMatch,roleKeyword,roleKeywordMatch,eventKeyword,eventKeywordMatch,enableAlert,sensitiveWord,priority);
                fangAnDao.deleteByFid(fid);
                result.put("changeFangAn", 1);
            }catch (Exception e){
                result.put("changeFangAn", 0);
            }

            ElasticSearchQuery elasticSearchQuery=new ElasticSearchQuery(areaRepository,fangAnDao);
            Thread t1=new ESSaveThread(elasticSearchQuery,newfid,this.elasticsearchOperations,elasticSearchDao);
            t1.start();
//            elasticSearchQuery.JoinFidQueryBuildersWithOutFid(newfid);
//            elasticSearchQuery.SetBoolQuery();
//            List<YuQingElasticSearch> yuQings=elasticSearchDao.findESByQuery(elasticSearchQuery);
//            System.out.println(yuQings.size());
//            List<YuQingElasticSearch> newYuqings=new ArrayList<>();
//            for (YuQingElasticSearch yuQing:yuQings)
//            {
//                yuQing.setFid((int) newfid);
//                yuQing.setId(UUID.randomUUID().toString());
//                newYuqings.add(yuQing);
//            }
//            this.elasticsearchOperations.save(newYuqings,this.elasticsearchOperations.getIndexCoordinatesFor(YuQingElasticSearch.class));

            System.out.println("bkb");
        }
        return result;
    }
    @Override
    public JSONObject delFangAn(String username,long fid){
        JSONObject result = new JSONObject();
        result.put("delFangAn", 0);

        try {
            FangAn fangAn = fangAnDao.findByFid(fid);
            if(!fangAn.getUsername().equals(username)){
                result.put("该方案不是你的",1);
                result.put("delFangAn", 0);
                return result;
            }
            fangAnDao.deleteByFid(fid);
            swordFidRepository.deleteByFid(fid);
            result.put("delFangAn", 1);
            return result;
        }catch (Exception e){
            result.put("delFangAn", 0);
        }
        return result;
    }

    @Override
    public JSONObject findFangAnByFid(String username,long fid){
        JSONObject result = new JSONObject();
        FangAn fangAn = fangAnDao.findByFid(fid);
        if(!fangAn.getUsername().equals(username)){
            result.put("该方案不是你的",1);
            result.put("findFangAn", 0);
            return result;
        }else {
            result.put("username", fangAn.getUsername());
            result.put("programmeName", fangAn.getProgrammeName());
            result.put("matchType", fangAn.getMatchType());
            result.put("regionKeyword", fangAn.getRegionKeyword());
            result.put("regionKeywordMatch", fangAn.getRegionKeywordMatch());
            result.put("roleKeyword", fangAn.getRoleKeyword());
            result.put("roleKeywordMatch", fangAn.getRoleKeywordMatch());
            //result.put("eventKeyword", fangAn.getEventKeyword());
            result.put("eventKeywordMatch", fangAn.getEventKeywordMatch());
            result.put("enableAlert", fangAn.getEnableAlert());
            result.put("sensitiveWord", fangAn.getSensitiveword());
            result.put("priority",fangAn.getPriority());
            JSONArray eventKeyword = new JSONArray();
            String event = fangAn.getEventKeyword();
            while(event.length()>0)
            {
                int tag=event.indexOf('+');
                eventKeyword.appendElement(event.substring(0,tag));
                event=event.substring(tag+1);
            }
            result.put("eventKeyword", eventKeyword);
            return result;
        }

    }

    @Override
    public JSONObject getAllFid()
    {
        JSONArray jsonArray = new JSONArray();
        List<FangAn> fangAnList = fangAnDao.findAll();
        for(FangAn fangAn : fangAnList){
            JSONObject object = new JSONObject();
            object.put("fid", fangAn.getFid());
            jsonArray.appendElement(object);
        }
        JSONObject object = new JSONObject();
        object.put("data",jsonArray);
        return  object;
    }

    @Override
    public FangAnResponse getAllFangan(int pageID, int pageSize, String username) {
        int offset=(pageID-1)*pageSize;
        FangAnResponse fangAnResponse=new FangAnResponse();
        if (!username.isEmpty()&&!username.equals(""))
        {
            List<FangAn> ret=fangAnDao.getAllFanganByUsername(offset,pageSize,username);
            int hitNumber=fangAnDao.findAllByUsername(username).size();
            fangAnResponse.setFangAnContent(ret);
            fangAnResponse.setHitNumber(hitNumber);
        }else {
            List<FangAn> ret=fangAnDao.getAllFangan(offset,pageSize);
            int hitNumber=fangAnDao.findAll().size();
            fangAnResponse.setFangAnContent(ret);
            fangAnResponse.setHitNumber(hitNumber);
        }
        return fangAnResponse;
    }

}
