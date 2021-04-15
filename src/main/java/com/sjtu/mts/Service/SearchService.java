package com.sjtu.mts.Service;

import com.sjtu.mts.Response.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.List;

public interface SearchService {

    public DataResponse Search(String keyword, String cflag, String startPublishedDay, String endPublishedDay,
                               String fromType, int page, int pageSize, int timeOrder);
;

    public ResourceCountResponse globalSearchResourceCount(String keyword, String startPublishedDay,
                                                           String endPublishedDay);

    public CflagCountResponse globalSearchCflagCount(String keyword, String startPublishedDay, String endPublishedDay);

    public AmountTrendResponse globalSearchTrendCount(String keyword, String startPublishedDay, String endPublishedDay);

    public AreaAnalysisResponse countArea(String keyword, String startPublishedDay, String endPublishedDay);

    public DataResponse fangAnSearch(long fid,String cflag, String startPublishedDay, String endPublishedDay,
                                     String fromType, int page, int pageSize, int timeOrder);
    public JSONObject addSensitiveWord(String sensitiveWord);
    public JSONObject delSensitiveWord(String sensitiveWord);
    public JSONArray sensitiveWordFiltering(String text);
    public JSONArray sensitiveWord(long fid, String startPublishedDay, String endPublishedDay);

    public List<String> extractKeyword(long fid, String startPublishedDay, String endPublishedDay, int keywordNumber);
}
