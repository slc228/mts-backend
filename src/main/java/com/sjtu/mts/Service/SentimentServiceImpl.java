package com.sjtu.mts.Service;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.rpc.SentimentRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SentimentServiceImpl implements SentimentService {
    @Autowired
    private SentimentRpc sentimentRpc;

    @Override
    public JSONObject sentimentPredict(List<String> textList){
        String rpc = sentimentRpc.sentimentAnalysis(textList);

        JSONObject jsonObject = JSONObject.parseObject(rpc);
        return jsonObject;
    }
}
