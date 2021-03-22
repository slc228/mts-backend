package com.sjtu.mts.Service;

import com.sjtu.mts.Response.*;
import net.minidev.json.JSONArray;

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

    public JSONArray sensitiveWordFiltering(String text);
    public JSONArray sensitiveWord(long fid, String startPublishedDay, String endPublishedDay);
}
