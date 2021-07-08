package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAn;
import com.sjtu.mts.Repository.AreaRepository;
import com.sjtu.mts.Repository.FangAnRepository;
import net.minidev.json.JSONArray;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class FangAnDaoImpl implements FangAnDao {
    private final FangAnRepository fangAnRepository;
    private final AreaRepository areaRepository;

    public FangAnDaoImpl(FangAnRepository fangAnRepository, AreaRepository areaRepository) {
        this.fangAnRepository = fangAnRepository;
        this.areaRepository = areaRepository;
    }

    @Override
    public  FangAn save(FangAn fangAn){return fangAnRepository.save(fangAn);}

    @Override
    public List<FangAn> findAllByUsername(String username){
        return fangAnRepository.findAllByUsername(username);
    }

    @Override
    public Boolean existsByUsernameAndProgrammeName(String username,String programme){
        return fangAnRepository.existsByUsernameAndProgrammeName(username,programme);
    }
    @Override
    public FangAn findByFid(long fid){
        return  fangAnRepository.findByFid(fid);
    }

    @Override
    public void deleteByFid(long fid){
        fangAnRepository.deleteByFid(fid);
    }

    /*传入方案id，返回方案查询的舆情结果
     * @author：FU Yongrui*/
    @Override
    public Criteria criteriaByFid(long fid){
        FangAn fangAn = fangAnRepository.findByFid(fid);
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
            if(searchSplitArray.size()>1){
                if(roleKeywordMatch==1){
                    for (String searchString : searchSplitArray) {
                        List<String> subArray = new LinkedList<>();
                        subArray.add(searchString);

                        criteria.subCriteria(new Criteria("content").in(subArray).or("title").in(subArray));
                    }
                }else {
                    criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
                }
            }else {
                criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
            }


        }

        if (!eventKeyword.isEmpty())
        {
            String[] searchSplitArray1 = eventKeyword.trim().split("\\s+");
            List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
            if(searchSplitArray.size()>1){
                if(eventKeywordMatch==1){
                    for (String searchString : searchSplitArray) {
                        List<String> subArray = new LinkedList<>();
                        subArray.add(searchString);
                        criteria.subCriteria(new Criteria("content").in(subArray).or("title").in(subArray));
                    }
                }else {
                    criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
                }
            }else {
                criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
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
                System.out.println(Arrays.toString(citys.toArray()));
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
                    System.out.println(Arrays.toString(citys.toArray()));
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
                System.out.println(Arrays.toString(citys.toArray()));
                criteria.subCriteria(new Criteria("content").in(citys).or("title").in(citys));
            }

        }


        return criteria;
    }

    @Override
    public List<Criteria> FindCriteriasByFid(long fid){
        List<Criteria> result=new ArrayList<>();

        FangAn fangAn = fangAnRepository.findByFid(fid);
        String regionKeyword = fangAn.getRegionKeyword();
        int regionKeywordMatch = fangAn.getRegionKeywordMatch();

        String roleKeyword = fangAn.getRoleKeyword();
        int roleKeywordMatch = fangAn.getRoleKeywordMatch();

        String eventKeyword = fangAn.getEventKeyword();
        List<String> events=new ArrayList<String>();
        while(eventKeyword.length()>0)
        {
            int tag=eventKeyword.indexOf('+');
            events.add(eventKeyword.substring(0,tag));
            eventKeyword=eventKeyword.substring(tag+1);
        }

        for (int numOfEvents=0;numOfEvents<events.size();numOfEvents++)
        {
            Criteria criteria = new Criteria();

            if (!roleKeyword.isEmpty())
            {
                String[] searchSplitArray1 = roleKeyword.trim().split("\\s+");
                List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
                if(searchSplitArray.size()>1){
                    if(roleKeywordMatch==1){
                        for (String searchString : searchSplitArray) {
                            List<String> subArray = new LinkedList<>();
                            subArray.add(searchString);
                            criteria.subCriteria(new Criteria("content").in(subArray).or("title").in(subArray));
                        }
                    }else {
                        criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
                    }
                }else {
                    criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
                }
            }

            if (!events.get(numOfEvents).isEmpty())
            {
                String[] searchSplitArray1 = events.get(numOfEvents).trim().split("\\s+");
                List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
                if(searchSplitArray.size()>1){
                    for (String searchString : searchSplitArray) {
                        List<String> subArray = new LinkedList<>();
                        subArray.add(searchString);
                        //criteria.subCriteria(new Criteria("content").in(subArray).or("title").in(subArray));
                        criteria.subCriteria(new Criteria("title").in(subArray));
                    }
                }else {
                    //criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
                    criteria.subCriteria(new Criteria("title").in(searchSplitArray));
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
                    criteria.subCriteria(new Criteria("content").in(citys).or("title").in(citys));
                }
            }
            result.add(criteria);
        }

        if (events.size()==0)
        {
            Criteria criteria = new Criteria();

            if (!roleKeyword.isEmpty())
            {
                String[] searchSplitArray1 = roleKeyword.trim().split("\\s+");
                List<String>searchSplitArray = Arrays.asList(searchSplitArray1);
                if(searchSplitArray.size()>1){
                    if(roleKeywordMatch==1){
                        for (String searchString : searchSplitArray) {
                            List<String> subArray = new LinkedList<>();
                            subArray.add(searchString);
                            criteria.subCriteria(new Criteria("content").in(subArray).or("title").in(subArray));
                        }
                    }else {
                        criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
                    }
                }else {
                    criteria.subCriteria(new Criteria("content").in(searchSplitArray).or("title").in(searchSplitArray));
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
                    criteria.subCriteria(new Criteria("content").in(citys).or("title").in(citys));
                }
            }
            result.add(criteria);
        }

        return result;
    }
}
