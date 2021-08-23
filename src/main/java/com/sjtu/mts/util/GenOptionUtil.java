package com.sjtu.mts.util;

import com.alibaba.fastjson.JSON;
import com.sjtu.mts.Response.AmountTrendResponse;
import com.sjtu.mts.Response.AreaAnalysisResponse;
import com.sjtu.mts.Service.SearchService;
import freemarker.template.TemplateException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class GenOptionUtil {
    @Autowired
    private SearchService searchService;

    public String totalAmountTrend(long fid,String timeStart,String timeEnd) throws ParseException, TemplateException, IOException {
        String strDateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

        AmountTrendResponse amountTrendResponse=searchService.globalSearchTrendCount2(fid,timeStart,timeEnd);
        List<String> xAxis=new ArrayList<>();
        for (String time:amountTrendResponse.getTimeRange())
        {
            String[] moments = time.trim().split(" to ");
            Date fromMoment=sdf.parse(moments[0]);
            Date toMoment=sdf.parse(moments[1]);
            String avgTime=sdf.format(fromMoment)+"~\\n"+sdf.format(toMoment);
            xAxis.add(avgTime);
        }
        String[] xValue= xAxis.toArray(new String[0]);
        String title = "总量趋势";
        List<Long> yAxis = amountTrendResponse.getTotalAmountTrend();
        Long[] yValue= yAxis.toArray(new Long[0]);

        // 模板参数
        HashMap<String, Object> datas = new HashMap<>();
        datas.put("yValue", JSON.toJSONString(yValue));
        datas.put("xValue", JSON.toJSONString(xValue));
        datas.put("title", title);

        // 生成option字符串
        String option = FreemarkerUtil.generateString("totalAmountTrend.ftl", "/com/sjtu/mts/template", datas);

        // 根据option参数
        String base64 = EchartsUtil.generateEchartsBase64(option);

        System.out.println("BASE64:" + base64);

        return base64;
    }

    public String sourceAmountTrend(long fid,String timeStart,String timeEnd) throws ParseException, TemplateException, IOException {
        String strDateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        AmountTrendResponse amountTrendResponse=searchService.globalSearchTrendCount2(fid,timeStart,timeEnd);

        List<String> yAxis=new ArrayList<>();
        for (String time:amountTrendResponse.getTimeRange())
        {
            String[] moments = time.trim().split(" to ");
            Date fromMoment=sdf.parse(moments[0]);
            Date toMoment=sdf.parse(moments[1]);
            String avgTime=sdf.format(fromMoment)+"~\\n"+sdf.format(toMoment);
            yAxis.add(avgTime);
        }
        String[] yValue= yAxis.toArray(new String[0]);
        String title = "来源趋势";

        JSONArray options = new JSONArray();
        JSONObject jsonObject=new JSONObject();
        options.add(jsonObject);
        jsonObject.put("label","网站");
        jsonObject.put("name","网站");
        jsonObject.put("value",amountTrendResponse.getFromTypeAmountTrend1());
        options.add(jsonObject);
        jsonObject.put("label","论坛");
        jsonObject.put("name","论坛");
        jsonObject.put("value",amountTrendResponse.getFromTypeAmountTrend2());
        options.add(jsonObject);
        jsonObject.put("label","微博");
        jsonObject.put("name","微博");
        jsonObject.put("value",amountTrendResponse.getFromTypeAmountTrend3());
        options.add(jsonObject);
        jsonObject.put("label","微信");
        jsonObject.put("name","微信");
        jsonObject.put("value",amountTrendResponse.getFromTypeAmountTrend4());
        options.add(jsonObject);
        jsonObject.put("label","博客");
        jsonObject.put("name","微信");
        jsonObject.put("value",amountTrendResponse.getFromTypeAmountTrend5());
        options.add(jsonObject);
        jsonObject.put("label","外媒");
        jsonObject.put("name","外媒");
        jsonObject.put("value",amountTrendResponse.getFromTypeAmountTrend6());
        options.add(jsonObject);
        jsonObject.put("label","新闻");
        jsonObject.put("name","新闻");
        jsonObject.put("value",amountTrendResponse.getFromTypeAmountTrend7());
        options.add(jsonObject);

        // 模板参数
        HashMap<String, Object> datas = new HashMap<>();
        datas.put("yAxis", JSON.toJSONString(yValue));
        datas.put("xAxis", JSON.toJSONString(options));
        datas.put("title", title);

        // 生成option字符串
        String option = FreemarkerUtil.generateString("sourceAmountTrend.ftl", "/com/sjtu/mts/template", datas);

        // 根据option参数
        String base64 = EchartsUtil.generateEchartsBase64(option);

        System.out.println("BASE64:" + base64);

        return base64;
    }


    public String regionLayout(long fid,String timeStart,String timeEnd) throws ParseException, TemplateException, IOException {
        String strDateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        AreaAnalysisResponse areaAnalysisResponse=searchService.countAreaByFid(fid,timeStart,timeEnd);

        JSONArray jsonArray=new JSONArray();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("name","北京");
        jsonObject.put("value",areaAnalysisResponse.getFrom11());
        jsonArray.add(jsonObject);
        jsonObject.put("name","天津");
        jsonObject.put("value",areaAnalysisResponse.getFrom12());
        jsonArray.add(jsonObject);
        jsonObject.put("name","河北");
        jsonObject.put("value",areaAnalysisResponse.getFrom13());
        jsonArray.add(jsonObject);
        jsonObject.put("name","山西");
        jsonObject.put("value",areaAnalysisResponse.getFrom14());
        jsonArray.add(jsonObject);
        jsonObject.put("name","内蒙古");
        jsonObject.put("value",areaAnalysisResponse.getFrom15());
        jsonArray.add(jsonObject);
        jsonObject.put("name","辽宁");
        jsonObject.put("value",areaAnalysisResponse.getFrom21());
        jsonArray.add(jsonObject);
        jsonObject.put("name","吉林");
        jsonObject.put("value",areaAnalysisResponse.getFrom22());
        jsonArray.add(jsonObject);
        jsonObject.put("name","黑龙江");
        jsonObject.put("value",areaAnalysisResponse.getFrom23());
        jsonArray.add(jsonObject);
        jsonObject.put("name","上海");
        jsonObject.put("value",areaAnalysisResponse.getFrom31());
        jsonArray.add(jsonObject);
        jsonObject.put("name","江苏");
        jsonObject.put("value",areaAnalysisResponse.getFrom32());
        jsonArray.add(jsonObject);
        jsonObject.put("name","浙江");
        jsonObject.put("value",areaAnalysisResponse.getFrom33());
        jsonArray.add(jsonObject);
        jsonObject.put("name","安徽");
        jsonObject.put("value",areaAnalysisResponse.getFrom34());
        jsonArray.add(jsonObject);
        jsonObject.put("name","福建");
        jsonObject.put("value",areaAnalysisResponse.getFrom35());
        jsonArray.add(jsonObject);
        jsonObject.put("name","江西");
        jsonObject.put("value",areaAnalysisResponse.getFrom36());
        jsonArray.add(jsonObject);
        jsonObject.put("name","山东");
        jsonObject.put("value",areaAnalysisResponse.getFrom37());
        jsonArray.add(jsonObject);
        jsonObject.put("name","河南");
        jsonObject.put("value",areaAnalysisResponse.getFrom41());
        jsonArray.add(jsonObject);
        jsonObject.put("name","湖北");
        jsonObject.put("value",areaAnalysisResponse.getFrom42());
        jsonArray.add(jsonObject);
        jsonObject.put("name","湖南");
        jsonObject.put("value",areaAnalysisResponse.getFrom43());
        jsonArray.add(jsonObject);
        jsonObject.put("name","广东");
        jsonObject.put("value",areaAnalysisResponse.getFrom44());
        jsonArray.add(jsonObject);
        jsonObject.put("name","广西");
        jsonObject.put("value",areaAnalysisResponse.getFrom45());
        jsonArray.add(jsonObject);
        jsonObject.put("name","海南");
        jsonObject.put("value",areaAnalysisResponse.getFrom46());
        jsonArray.add(jsonObject);
        jsonObject.put("name","重庆");
        jsonObject.put("value",areaAnalysisResponse.getFrom50());
        jsonArray.add(jsonObject);
        jsonObject.put("name","四川");
        jsonObject.put("value",areaAnalysisResponse.getFrom51());
        jsonArray.add(jsonObject);
        jsonObject.put("name","贵州");
        jsonObject.put("value",areaAnalysisResponse.getFrom52());
        jsonArray.add(jsonObject);
        jsonObject.put("name","云南");
        jsonObject.put("value",areaAnalysisResponse.getFrom53());
        jsonArray.add(jsonObject);
        jsonObject.put("name","西藏");
        jsonObject.put("value",areaAnalysisResponse.getFrom54());
        jsonArray.add(jsonObject);
        jsonObject.put("name","陕西");
        jsonObject.put("value",areaAnalysisResponse.getFrom61());
        jsonArray.add(jsonObject);
        jsonObject.put("name","甘肃");
        jsonObject.put("value",areaAnalysisResponse.getFrom62());
        jsonArray.add(jsonObject);
        jsonObject.put("name","青海");
        jsonObject.put("value",areaAnalysisResponse.getFrom63());
        jsonArray.add(jsonObject);
        jsonObject.put("name","宁夏");
        jsonObject.put("value",areaAnalysisResponse.getFrom64());
        jsonArray.add(jsonObject);
        jsonObject.put("name","新疆");
        jsonObject.put("value",areaAnalysisResponse.getFrom65());
        jsonArray.add(jsonObject);
        jsonObject.put("name","台湾");
        jsonObject.put("value",areaAnalysisResponse.getFrom71());
        jsonArray.add(jsonObject);
        jsonObject.put("name","香港");
        jsonObject.put("value",areaAnalysisResponse.getFrom81());
        jsonArray.add(jsonObject);
        jsonObject.put("name","澳门");
        jsonObject.put("value",areaAnalysisResponse.getFrom91());
        jsonArray.add(jsonObject);

        long min=0;
        long max=0;
        for(int i=0;i<jsonArray.size();i++)
        {
            if (Integer.parseInt(jsonArray.getJSONObject(i).get("value").toString())<min){
                min=Integer.parseInt(jsonArray.getJSONObject(i).get("value").toString());
            }
            if (Integer.parseInt(jsonArray.getJSONObject(i).get("value").toString())>min){
                max=Integer.parseInt(jsonArray.getJSONObject(i).get("value").toString());
            }
        }
        // 模板参数
        HashMap<String, Object> datas = new HashMap<>();
        datas.put("regions", JSON.toJSONString(jsonArray));
        datas.put("min", JSON.toJSONString(min));
        datas.put("max", JSON.toJSONString(max));
        datas.put("title", "地域分布");

        // 生成option字符串
        String option = FreemarkerUtil.generateString("regionLayout.ftl", "/com/sjtu/mts/template", datas);

        // 根据option参数
        String base64 = EchartsUtil.generateEchartsBase64(option);

        System.out.println("BASE64:" + base64);

        return base64;
    }
}
