package com.sjtu.mts.Controller;

import com.sjtu.mts.Entity.Cluster;
import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.EventTrack.EventTreeNode;
import com.sjtu.mts.Keyword.KeywordResponse;
import com.sjtu.mts.Repository.DataRepository;
import com.sjtu.mts.Response.*;
import com.sjtu.mts.Service.EventTrackService;
import com.sjtu.mts.Service.SearchService;
import com.sjtu.mts.Service.TextClassService;
import com.sjtu.mts.Service.WeiboTrackService;
import com.sjtu.mts.WeiboTrack.WeiboRepostTree;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data")
@CrossOrigin
public class DataController {

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private WeiboTrackService weiboTrackService;

    @Autowired
    private TextClassService textClassService;

    @Autowired
    private EventTrackService eventTrackService;



    @GetMapping("/findByCflag/{cflag}")
    @ResponseBody
    public List<Data> findById(@PathVariable("cflag") int cflag) {
        return dataRepository.findByCflag(String.valueOf(cflag));
    }

    @GetMapping("/globalSearch/dataSearch")
    @ResponseBody
    public DataResponse findByKeywordAndCflagAndPublishedDayAndFromType(
            @RequestParam("keyword") String keyword,
            @RequestParam("cflag") String cflag,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay,
            @RequestParam("fromType") String fromType,
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("timeOrder") int timeOrder
    ) {
        return searchService.Search(keyword, cflag, startPublishedDay, endPublishedDay, fromType,
                page, pageSize, timeOrder);
    }

    @GetMapping("/globalSearch/resourceCount")
    @ResponseBody
    public ResourceCountResponse countByKeywordAndPublishedDayAndFromType(
            @RequestParam("keyword") String keyword,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.globalSearchResourceCount(keyword, startPublishedDay,
                endPublishedDay);
    }

    @GetMapping("/globalSearch/cflagCount")
    @ResponseBody
    public CflagCountResponse countByKeywordAndPublishedDayAndCflag(
            @RequestParam("keyword") String keyword,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.globalSearchCflagCount(keyword, startPublishedDay, endPublishedDay);
    }

    @GetMapping("/globalSearch/amountTrendCount")
    @ResponseBody
    public AmountTrendResponse countAmountTrendByKeywordAndPublishedDay(
            @RequestParam("keyword") String keyword,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.globalSearchTrendCount(keyword, startPublishedDay, endPublishedDay);
    }

    /*某事件各地区发文
    @author FYR
     */
    @GetMapping("/globalSearch/areaCount")
    @ResponseBody
    public AreaAnalysisResponse countAreaByKeywordAndPublishedDay(
            @RequestParam("keyword") String keyword,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.countArea(keyword,startPublishedDay,endPublishedDay);
    }

    /*根据方案查找舆情
    @author FYR
     */
    @GetMapping("/singleSearch/findByFangAn")
    @ResponseBody
    public DataResponse fangAnSearch(
            @RequestParam("fid") long fid,
            @RequestParam("cflag") String cflag,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay,
            @RequestParam("fromType") String fromType,
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("timeOrder") int timeOrder


    ){
        return searchService.fangAnSearch(fid,cflag,startPublishedDay,endPublishedDay,fromType,page,pageSize,timeOrder);
    }

    /*溯源微博，生成并返回微博转发关系树
    @author Ma Baowei
     */
    @GetMapping("/weiboTrack")
    @ResponseBody
    public WeiboRepostTree trackWeiboByKeywordAndPublishedDay(
            @RequestParam("fid") long fid,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return weiboTrackService.trackWeibo(fid,startPublishedDay,endPublishedDay);
    }

    /*单个text敏感词识别
    @author Fu Yongrui
     */
    @PostMapping(value = "/sensitiveWord")
    @ResponseBody
    public JSONArray sensitiveWord(@RequestBody Map<String,String> textinfo )
    {
        return searchService.sensitiveWordFiltering(textinfo.get("text"));
    }
    /*多个舆情文本敏感词识别
    @author Fu Yongrui
     */
    @RequestMapping(value = "/sensitive")
    @ResponseBody
    public JSONArray sensitiveWord(@RequestParam("fid") long fid,
                                   @RequestParam("startPublishedDay") String startPublishedDay,
                                   @RequestParam("endPublishedDay") String endPublishedDay)
    {
        return searchService.sensitiveWord(fid,startPublishedDay,endPublishedDay);
    }
    /*添加敏感词
        @author Fu Yongrui
         */
    @PostMapping(value = "/addSensitive")
    @ResponseBody
    public JSONObject addSensitiveWord(@RequestBody Map<String,String> sensitiveWordInfo)

    {
        return searchService.addSensitiveWord(sensitiveWordInfo.get("sensitiveWord"));
    }
    /*
    * 删除敏感词
    * @author Fu Yongrui
    */
    @RequestMapping(value = "/delSensitive")
    @ResponseBody
    public JSONObject delSensitiveWord(@RequestParam("sensitiveWord") String sensitiveWord)
    {
        return searchService.delSensitiveWord(sensitiveWord);
    }

    /*关键词提取
    @author Ma Baowei
     */
    @GetMapping("/keywordExtraction")
    @ResponseBody
    public List<KeywordResponse> extractKeyWordByFidAndPublishedDay(
            @RequestParam("fid") long fid,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay,
            @RequestParam("keywordNumber") int keywordNumber
    ) {
        return searchService.extractKeyword(fid,startPublishedDay,endPublishedDay,keywordNumber);
    }

    /*文本分类
    @author Fu Yongrui
     */
    @RequestMapping(value = "/textClass")
    @ResponseBody
    public JSONArray textClass(@RequestParam("fid") long fid,
                               @RequestParam("startPublishedDay") String startPublishedDay,
                               @RequestParam("endPublishedDay") String endPublishedDay)
    {
        return textClassService.textClass(fid,startPublishedDay,endPublishedDay);
    }
    /*文本分类2
    @author Fu Yongrui
     */
    @PostMapping(value = "/textClass2")
    @ResponseBody
    public com.alibaba.fastjson.JSONObject textClass2(@RequestBody Map<String,List<String>> textInfo)
    {
        return textClassService.textClass2(textInfo.get("textList"));
    }
    /*文本聚类
    @author Fu Yongrui
     */
    @RequestMapping(value = "/textClustering")
    @ResponseBody
    public JSONArray textClustering(@RequestParam("fid") long fid,
                               @RequestParam("startPublishedDay") String startPublishedDay,
                               @RequestParam("endPublishedDay") String endPublishedDay)
    {
        return textClassService.clustering(fid,startPublishedDay,endPublishedDay);
    }
    /*文本聚类2
    @author Fu Yongrui
     */
    @RequestMapping(value = "/clusteringData")
    @ResponseBody
    public List<Cluster> clusteringData(@RequestParam("fid") long fid,
                                        @RequestParam("startPublishedDay") String startPublishedDay,
                                        @RequestParam("endPublishedDay") String endPublishedDay)
    {
        return textClassService.clusteringData(fid,startPublishedDay,endPublishedDay);
    }
    /*事件溯源（生成事件关系树）
    @author Ma Baowei
     */
    @RequestMapping(value = "/getEventTree")
    @ResponseBody
    public EventTreeNode getEventTree(@RequestParam("fid") long fid,
                                      @RequestParam("startPublishedDay") String startPublishedDay,
                                      @RequestParam("endPublishedDay") String endPublishedDay)
    {
        return eventTrackService.getEventTree(fid,startPublishedDay,endPublishedDay);
    }
}
