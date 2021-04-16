package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.ClusteredData;
import net.minidev.json.JSONArray;

import java.util.List;

public interface TextClassService {

    JSONArray textClass(long fid, String startPublishedDay, String endPublishedDay);
    JSONArray clustering(long fid, String startPublishedDay, String endPublishedDay);

    List<ClusteredData> clusteringData(long fid, String startPublishedDay, String endPublishedDay);
}
