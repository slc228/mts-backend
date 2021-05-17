package com.sjtu.mts.Service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface SentimentService {
    JSONObject sentimentPredict(List<String> textList);
}
