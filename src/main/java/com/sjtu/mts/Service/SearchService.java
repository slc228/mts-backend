package com.sjtu.mts.Service;

import com.sjtu.mts.Keyword.KeywordResponse;
import com.sjtu.mts.Response.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import com.sjtu.mts.Entity.Wuser;

public interface SearchService {

    public DataResponse Search(String keyword, String cflag, String startPublishedDay, String endPublishedDay,
                               String fromType, int page, int pageSize, int timeOrder);


    public ResourceCountResponse globalSearchResourceCount(String keyword, String startPublishedDay,
                                                           String endPublishedDay);
    public ResourceCountResponse globalSearchResourceCount2(long fid,String startPublishedDay, String endPublishedDay);

    public CflagCountResponse globalSearchCflagCount(String keyword, String startPublishedDay, String endPublishedDay);

    public CflagCountResponse globalSearchCflagCount2(long fid,String startPublishedDay, String endPublishedDay);


    public AmountTrendResponse globalSearchTrendCount(String keyword, String startPublishedDay, String endPublishedDay);

    public AmountTrendResponse globalSearchTrendCount2(long fid,String startPublishedDay, String endPublishedDay);
    public AmountTrendResponse globalSearchTrendCount3(long fid,String startPublishedDay, String endPublishedDay);


    public AreaAnalysisResponse countArea(String keyword, String startPublishedDay, String endPublishedDay);

    public AreaAnalysisResponse countArea2(long fid,String startPublishedDay, String endPublishedDay);

    /* 研判预警模块接口 */
    public DataResponse searchByUser(long fid, String username, int pageSize, int pageId) throws UnsupportedEncodingException;
    public Map<String, Integer> getActivateUser(long fid);


    public DataResponse fangAnSearch(long fid,String cflag, String startPublishedDay, String endPublishedDay,
                                     String fromType, int page, int pageSize, int timeOrder);
    public DataResponse fangAnSearch2(long fid,String keyword,String cflag, String startPublishedDay, String endPublishedDay,
                                     String fromType, int page, int pageSize, int timeOrder);

    public JSONObject addSensitiveWord(String sensitiveWord);
    public JSONObject delSensitiveWord(String sensitiveWord);
    /*
    * DFA方法提取敏感词*/
    public JSONArray sensitiveWordFiltering(String text);
    /*
     * 分词方法提取敏感词*/
    public JSONArray sensitiveWordFilteringHanLp(String text);
    public JSONArray sensitiveWord(long fid, String startPublishedDay, String endPublishedDay);

    public List<KeywordResponse> extractKeyword(long fid, String startPublishedDay, String endPublishedDay
            , int keywordNumber, String extractMethod);
}
