package com.sjtu.mts.Service;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.Response.SentimentCountResponse;
import com.sjtu.mts.Response.SentimentTrendResponse;

import java.util.List;

public interface SentimentService {
    JSONObject sentimentPredict(List<String> textList);
    SentimentCountResponse countSentiment(long fid, String startPublishedDay, String endPublishedDay);
    SentimentTrendResponse sentimentTrendCount(long fid, String startPublishedDay, String endPublishedDay);
}
