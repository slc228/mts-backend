package com.sjtu.mts.Service;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.mts.Dao.FangAnDao;
import com.sjtu.mts.Entity.Cluster;
import com.sjtu.mts.Entity.ClusteredData;
import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.rpc.SummaryRpc;
import com.sjtu.mts.rpc.TextAlertRpc;
import com.sjtu.mts.rpc.TextclassRpc;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TextAlertServiceImpl implements TextAlertService {
    @Autowired
    private TextAlertRpc textAlertRpc;

    @Override
    public com.alibaba.fastjson.JSONObject textAlert(List<String> textList){
        String rpc = textAlertRpc.textAlert(textList);
        JSONObject jsonObject = JSONObject.parseObject(rpc);
        return jsonObject;
    }
}
