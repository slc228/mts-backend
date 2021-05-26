package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.Cluster;
import net.minidev.json.JSONArray;

import java.util.List;
/*
* @author：FYR
* 文本分类和聚类*/
public interface TextClassService {

    JSONArray textClass(long fid, String startPublishedDay, String endPublishedDay);
    com.alibaba.fastjson.JSONObject textClass2(List<String> textList);

    JSONArray clustering(long fid, String startPublishedDay, String endPublishedDay);

    /*
    * mbw要的聚类。聚类的编号，按照聚类里面第一条（最早的那条）舆情的时间顺序编成，取出每一个聚类里面最早的那条舆情，
    * 按照时间最早的顺序，最早的为聚类num:1.
    * ClusteredData的time属性为该聚类中最早舆情的时间*/
    List<Cluster> clusteringData(long fid, String startPublishedDay, String endPublishedDay);


}
