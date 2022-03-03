package com.sjtu.mts.Controller;

import com.itextpdf.text.DocumentException;
import com.sjtu.mts.Entity.*;
import com.sjtu.mts.EventTrack.EventTreeNode;
import com.sjtu.mts.Keyword.KeywordResponse;
import com.sjtu.mts.Repository.DataRepository;
import com.sjtu.mts.Response.*;
import com.sjtu.mts.Service.*;
import com.sjtu.mts.WeiboTrack.WeiboRepostTree;
import freemarker.template.TemplateException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.openqa.selenium.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data")
@CrossOrigin()
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

    @Autowired
    private SentimentService sentimentService;

    @Autowired
    private TextAlertService textAlertService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private FangAnService fangAnService;

    @Autowired
    private FangAnWarningService fangAnWarningService;

    @Autowired
    private WarningReceiverService warningReceiverService;

    @Autowired
    private WarningRecordService warningRecordService;

    @GetMapping("/testApi")
    @ResponseBody
    public String heartBeating() {
        return "healthy";
    }

    @GetMapping("/findByCflag/{cflag}")
    @ResponseBody
    public List<Data> findById(@PathVariable("cflag") int cflag) {
        return dataRepository.findByCflag(String.valueOf(cflag));
    }

    @GetMapping("/globalSearch/searchByUser")
    @ResponseBody
    public DataResponse searchByUser (
            @RequestParam("fid") long fid,
            @RequestParam("username") String username,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("pageId") int pageId
    ) throws UnsupportedEncodingException {
        return searchService.searchByUser(fid, username, pageSize, pageId);
    }

    @GetMapping("/getActivateUser")
    @ResponseBody
    public Map<String, Integer> getActivateUser(@RequestParam("fid") long fid) {
        return searchService.getActivateUser(fid);
    }

    @GetMapping("/globalSearch/dataSearch")
    @ResponseBody
    public YuQingResponse findByKeywordAndCflagAndPublishedDayAndFromType(
            @RequestParam("keyword") String keyword,
            @RequestParam("sensitiveFlag") String sensitiveFlag,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay,
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("timeOrder") int timeOrder
    ) {
        String decodeKeyword = "";
        try{
            decodeKeyword = java.net.URLDecoder.decode(keyword, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.Search(decodeKeyword, startPublishedDay, endPublishedDay, sensitiveFlag,
                page, pageSize, timeOrder);
    }

    @GetMapping("/globalSearch/dataSearchWithObject")
    @ResponseBody
    public YuQingResponse dataSearchWithObject(
            @RequestParam("keyword") String keyword,
            @RequestParam("sensitiveType") String sensitiveType,
            @RequestParam("emotion") String emotion,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay,
            @RequestParam("resource") String resource,
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("timeOrder") int timeOrder,
            @RequestParam("keywords") String keywords
    ) {
        String decodeKeyword = "";
        String decodeKeywords = "";
        String decodeSensitiveType="";
        String decodeResource="";
        try{
            decodeKeyword = java.net.URLDecoder.decode(keyword, "utf-8");
            decodeKeywords = java.net.URLDecoder.decode(keywords, "utf-8");
            decodeSensitiveType= java.net.URLDecoder.decode(sensitiveType, "utf-8");
            decodeResource= java.net.URLDecoder.decode(resource, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.SearchWithObject(decodeKeyword, decodeSensitiveType, emotion, startPublishedDay, endPublishedDay, decodeResource,page, pageSize, timeOrder,decodeKeywords);
    }


    @GetMapping("/getResources")
    @ResponseBody
    public JSONArray getResources(
    ) {
        return searchService.getResources();
    }

    @GetMapping("/globalSearch/resourceCount")
    @ResponseBody
    public JSONArray countByKeywordAndPublishedDayAndFromType(
            @RequestParam("keyword") String keyword,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.globalSearchResourceCount(keyword, startPublishedDay,
                endPublishedDay);
    }
    @GetMapping("/globalSearch/resourceCount2")
    @ResponseBody
    public JSONArray countByKeywordAndPublishedDayAndFromType2(
            @RequestParam("fid") long fid,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay

    ) {
        return searchService.globalSearchResourceCountByFid(fid,startPublishedDay,endPublishedDay);
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

    @GetMapping("/globalSearch/cflagCount2")
    @ResponseBody
    public CflagCountResponse countByKeywordAndPublishedDayAndCflag2(
            @RequestParam("fid") long fid,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay

    ) {
        return searchService.globalSearchCflagCountByFid(fid,startPublishedDay,endPublishedDay);
    }
    @GetMapping("/globalSearch/amountTrendCount")
    @ResponseBody
    public JSONObject countAmountTrendByKeywordAndPublishedDay(
            @RequestParam("keyword") String keyword,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.globalSearchTrendCount(keyword, startPublishedDay, endPublishedDay);
    }

    @GetMapping("/globalSearch/totalAmountTrendCount")
    @ResponseBody
    public JSONObject totalAmountTrendCount(
            @RequestParam("keyword") String keyword,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.totalAmountTrendCount(keyword, startPublishedDay, endPublishedDay);
    }

    @GetMapping("/globalSearch/sourceAmountTrendCount")
    @ResponseBody
    public JSONObject sourceAmountTrendCount(
            @RequestParam("keyword") String keyword,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.sourceAmountTrendCount(keyword, startPublishedDay, endPublishedDay);
    }

    @GetMapping("/globalSearch/amountTrendCount2")
    @ResponseBody
    public JSONObject countAmountTrendByKeywordAndPublishedDay2(
            @RequestParam("fid") long fid,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay

    ) {
        return searchService.globalSearchTrendCount2(fid,startPublishedDay,endPublishedDay);
    }

    @GetMapping("/globalSearch/amountTrendCount3")
    @ResponseBody
    public AmountTrendResponse countAmountTrendByKeywordAndPublishedDay3(
            @RequestParam("fid") long fid,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.globalSearchTrendCount3(fid,startPublishedDay,endPublishedDay);
    }

    @GetMapping("/getProgrammeSourceTrend")
    @ResponseBody
    public JSONObject getProgrammeSourceTrend(
            @RequestParam("fid") long fid,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.getProgrammeSourceTrend(fid,startPublishedDay,endPublishedDay);
    }

    @GetMapping("/getProgrammeTotalAmountTrend")
    @ResponseBody
    public JSONObject getProgrammeTotalAmountTrend(
            @RequestParam("fid") long fid,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay
    ) {
        return searchService.getProgrammeTotalAmountTrend(fid,startPublishedDay,endPublishedDay);
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
    @GetMapping("/globalSearch/areaCount2")
    @ResponseBody
    public AreaAnalysisResponse countAreaByKeywordAndPublishedDay2(
            @RequestParam("fid") long fid,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay

    ) {
        return searchService.countAreaByFid(fid,startPublishedDay,endPublishedDay);
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
    /*
    * 根据方案二次查找舆情
    * @author FYR*/
    @GetMapping("/singleSearch/findByFangAn2")
    @ResponseBody
    public YuQingResponse fangAnSearch2(
            @RequestParam("fid") long fid,
            @RequestParam("keyword")String keyword,
            @RequestParam("sensitiveType") String sensitiveType,
            @RequestParam("emotion") String emotion,
            @RequestParam("startPublishedDay") String startPublishedDay,
            @RequestParam("endPublishedDay") String endPublishedDay,
            @RequestParam("resource") String resource,
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("timeOrder") int timeOrder
    ){
        String decodeKeyword = "";
        String decodeSensitiveType="";
        String decodeResource="";
        try{
            decodeKeyword = java.net.URLDecoder.decode(keyword, "utf-8");
            decodeSensitiveType= java.net.URLDecoder.decode(sensitiveType, "utf-8");
            decodeResource= java.net.URLDecoder.decode(resource, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.fangAnSearch2(fid,decodeKeyword,decodeSensitiveType,emotion,startPublishedDay,endPublishedDay,decodeResource,page,pageSize,timeOrder);
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
        //return searchService.sensitiveWordFiltering(textinfo.get("text"));
        return searchService.sensitiveWordFilteringHanLp(textinfo.get("text"));
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
            @RequestParam("keywordNumber") int keywordNumber,
            @RequestParam("extractMethod") String extractMethod
    ) {
        return searchService.extractKeyword(fid,startPublishedDay,endPublishedDay,keywordNumber,extractMethod);
    }

    /*单条文本关键词提取
   @author Sun Liangchen
    */
    @PostMapping(value = "/keywordExtractionForSingleText")
    @ResponseBody
    public JSONObject keywordExtractionForSingleText(@RequestBody Map<String,String> textInfo)
    {
        return searchService.keywordExtractionForSingleText(textInfo.get("title"),textInfo.get("content"));
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

    @RequestMapping(value = "/sensitiveCount")
    @ResponseBody
    public com.alibaba.fastjson.JSONObject sensitiveCount(@RequestParam("fid") long fid)
    {
        return textAlertService.sensitiveCount(fid);
    }

    /*文本预警
    @author HZT
     */
    @PostMapping(value = "/textAlert")
    @ResponseBody
    public com.alibaba.fastjson.JSONObject textAlert(@RequestBody Map<String,List<String>> textInfo)
    {
        return textAlertService.textAlert(textInfo.get("textList"));
    }


    /*文本分类2，返回数据格式不同
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
    /*文本聚类2,返回数据格式不同
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
    /*单文档摘要
    @author Huang Sicheng
     */
    @RequestMapping(value = "/singleDocumentSummary")
    @ResponseBody
    public JSONObject singleDocumentSummary(@RequestParam("document") String document)
    {
        return summaryService.singleSummary(document);
    }
    /*多文档摘要
    @author Huang Sicheng
     */
    @RequestMapping(value = "/multiDocumentSummary")
    @ResponseBody
    public JSONObject multiDocumentSummary(@RequestParam("fid") long fid,
                                           @RequestParam("startPublishedDay") String startPublishedDay,
                                           @RequestParam("endPublishedDay") String endPublishedDay)
    {
        return summaryService.multiSummary(fid, startPublishedDay, endPublishedDay);
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
    /*情感分析
    @author Ma Baowei
     */
    @PostMapping(value = "/sentimentAnalysis")
    @ResponseBody
    public com.alibaba.fastjson.JSONObject predictSentiment(@RequestBody Map<String,List<String>> textInfo)
    {
        return sentimentService.sentimentPredict(textInfo.get("textList"));
    }
    /*情感数量统计（饼图接口）
    @author Ma Baowei
     */
    @GetMapping(value = "/sentimentCount")
    @ResponseBody
    public SentimentCountResponse sentimentCount(@RequestParam("fid") long fid,
                                                   @RequestParam("startPublishedDay") String startPublishedDay,
                                                   @RequestParam("endPublishedDay") String endPublishedDay)
    {
        return sentimentService.countSentiment(fid,startPublishedDay,endPublishedDay);
    }
    /*情感趋势统计
    @author Ma Baowei
     */
    @GetMapping(value = "/sentimentTrendCount")
    @ResponseBody
    public SentimentTrendResponse sentimentTrendCount(@RequestParam("fid") long fid,
                                                 @RequestParam("startPublishedDay") String startPublishedDay,
                                                 @RequestParam("endPublishedDay") String endPublishedDay)
    {
        return sentimentService.sentimentTrendCount(fid,startPublishedDay,endPublishedDay);
    }
    /*自动识别事件关键词
    @author Fu Yongrui
     */
    @PostMapping(value = "/autoaddEkeyword")
    @ResponseBody
    public JSONObject autoaddEkeyword(@RequestBody Map<String,String> textinfo
                                      ){
        return searchService.autoaddEkeyword(Long.parseLong(textinfo.get("fid")),textinfo.get("text"));
    }
    /*为方案添加敏感词
    @author Fu Yongrui
     */
    @PostMapping(value = "/addSensitivewordForFid")
    @ResponseBody
    public JSONObject addSensitivewordForFid(@RequestBody Map<String,String> textinfo
    ){
        return searchService.addSensitivewordForFid(Long.parseLong(textinfo.get("fid")),textinfo.get("text"));
    }
    /*查看方案敏感词
    @author Fu Yongrui
     */
    @GetMapping(value = "/sensitivewordForFid")
    @ResponseBody
    public JSONArray sensitivewordForFid(@RequestParam("fid") long fid
    ){
        return searchService.sensitivewordForFid(fid);
    }
    /*按方案查找敏感词
    @author Fu Yongrui
     */
    @PostMapping(value = "/sensitiveWordByFid")
    @ResponseBody
    public JSONArray sensitiveWordByFid(@RequestBody Map<String,String> textinfo )
    {
        //return searchService.sensitiveWordFiltering(textinfo.get("text"));
        return searchService.sensitiveWordByFid(Long.parseLong(textinfo.get("fid")),textinfo.get("text"));
    }

    /*获取所有的方案fid
    @author Fu Sicheng
     */
    @GetMapping("/getAllFid")
    @ResponseBody
    public JSONObject getAllFid() {
        return fangAnService.getAllFid();
    }

    /*方案的事件关键词
    @author Sun liangchen
     */
    @PostMapping(value = "/eventKeyWordByFid")
    @ResponseBody
    public JSONArray eventKeyWordByFid(@RequestBody Map<String,String> textinfo
    ){
        return searchService.eventKeyWordByFid(Long.parseLong(textinfo.get("fid")));
    }

    /*所有热门文章
        @author Sun liangchen
    */
    @GetMapping("/getHotArticle")
    @ResponseBody
    public HotArticleResponse getHotArticle(
            @RequestParam("pageId") int pageId,
            @RequestParam("pageSize") int pageSize
    ){
        return searchService.getHotArticle(pageId,pageSize);
    }

    /*获得近似的微博用户id和昵称
       @author Sun liangchen
   */
    @GetMapping("/searchBriefWeiboUser")
    @ResponseBody
    public List<BriefWeiboUser> searchBriefWeiboUser (
            @RequestParam("fid") long fid,
            @RequestParam("WeiboUserForSearch") String WeiboUserForSearch
    ) throws UnsupportedEncodingException {
        String weiboUserForSearch = java.net.URLDecoder.decode(WeiboUserForSearch, "utf-8");
        return searchService.searchBriefWeiboUser(fid,weiboUserForSearch);
    }

    /*添加方案监测的微博用户
       @author Sun liangchen
   */
    @GetMapping("/addWeiboUser")
    @ResponseBody
    public JSONObject addWeiboUser (
            @RequestParam("fid") long fid,
            @RequestParam("id") String id,
            @RequestParam("nickname") String nickname
    ) throws UnsupportedEncodingException {
        String Weibouserid = java.net.URLDecoder.decode(id, "utf-8");
        String Weibousernickname = java.net.URLDecoder.decode(nickname, "utf-8");
        return searchService.addWeiboUser(fid, Weibouserid, Weibousernickname);
    }

    /*删除方案监测的微博用户
       @author Sun liangchen
   */
    @GetMapping("/deleteWeiboUser")
    @ResponseBody
    public JSONObject deleteWeiboUser (
            @RequestParam("fid") long fid,
            @RequestParam("id") String id,
            @RequestParam("nickname") String nickname
    ) throws UnsupportedEncodingException {
        String Weibouserid = java.net.URLDecoder.decode(id, "utf-8");
        String Weibousernickname = java.net.URLDecoder.decode(nickname, "utf-8");
        return searchService.deleteWeiboUser(fid, Weibouserid, Weibousernickname);
    }

     /*获得方案监测的微博用户
       @author Sun liangchen
   */
    @GetMapping("/getFangAnMonitor")
    @ResponseBody
    public JSONArray getFangAnMonitor (
            @RequestParam("fid") long fid
    ) throws UnsupportedEncodingException, ParseException {
        return searchService.getFangAnMonitor(fid);
    }

   /*获得微博数，关注数，粉丝数
       @author Sun liangchen
   */
    @GetMapping("/getWeiboByid")
    @ResponseBody
    public JSONObject getWeiboByid (
            @RequestParam("fid") long fid,
            @RequestParam("id") String id
    ) throws UnsupportedEncodingException, ParseException {
        return searchService.getWeiboByid(fid,id);
    }

    /*获得微博列表
       @author Sun liangchen
   */
    @GetMapping("/getWeiboListByid")
    @ResponseBody
    public JSONArray getWeiboListByid (
            @RequestParam("fid") long fid,
            @RequestParam("weibouserid") String weibouserid
    ) throws UnsupportedEncodingException, ParseException {
        return searchService.getWeiboListByid(fid,weibouserid);
    }

    @GetMapping("/getOverallDatOnNetwork")
    @ResponseBody
    public JSONArray getOverallDatOnNetwork (
            @RequestParam("keyword") String keyword,
            @RequestParam("pageId") int pageId
    ) throws MalformedURLException, InterruptedException {
        return searchService.getOverallDatOnNetwork(keyword,pageId);
    }

    @GetMapping("/getOverallDataBing")
    @ResponseBody
    public JSONArray getOverallDataBing (
            @RequestParam("keyword") String keyword,
            @RequestParam("pageId") int pageId
    ) throws MalformedURLException, InterruptedException, UnsupportedEncodingException {
        String kword = java.net.URLDecoder.decode(keyword, "utf-8");
        return searchService.getOverallDataBing(kword,pageId);
    }

    @GetMapping("/getOverallData360")
    @ResponseBody
    public JSONArray getOverallData360 (
            @RequestParam("keyword") String keyword,
            @RequestParam("pageId") int pageId
    ) throws MalformedURLException, InterruptedException, UnsupportedEncodingException {
        String kword = java.net.URLDecoder.decode(keyword, "utf-8");
        return searchService.getOverallData360(kword,pageId);
    }

    @GetMapping("/getOverallDataBaidu")
    @ResponseBody
    public JSONArray getOverallDataBaidu (
            @RequestParam("keyword") String keyword,
            @RequestParam("pageId") int pageId
    ) throws MalformedURLException, InterruptedException, UnsupportedEncodingException {
        String kword = java.net.URLDecoder.decode(keyword, "utf-8");
        return searchService.getOverallDataBaidu(kword,pageId);
    }

    @GetMapping("/getBriefingTemplate")
    @ResponseBody
    public List<FangAnTemplate> getBriefingTemplate (
            @RequestParam("fid") long fid
    ) {
        return searchService.getBriefingTemplate(fid);
    }

    @GetMapping("/saveBriefingTemplate")
    @ResponseBody
    public JSONObject saveBriefingTemplate (
            @RequestParam("id") int id,
            @RequestParam("fid") long fid,
            @RequestParam("title")String title,
            @RequestParam("version") String version,
            @RequestParam("institution") String institution,
            @RequestParam("time") String time,
            @RequestParam("keylist") String keylist,
            @RequestParam("text") String text
    ) throws ParseException {
        String decodeTitle = "";
        String decodeVersion = "";
        String decodeInstitution = "";
        String decodeTime = "";
        String decodeKeylist = "";
        String decodeText = "";
        try{
            decodeTitle = java.net.URLDecoder.decode(title, "utf-8");
            decodeVersion = java.net.URLDecoder.decode(version, "utf-8");
            decodeInstitution = java.net.URLDecoder.decode(institution, "utf-8");
            decodeTime = java.net.URLDecoder.decode(time, "utf-8");
            decodeKeylist = java.net.URLDecoder.decode(keylist, "utf-8");
            decodeText = java.net.URLDecoder.decode(text, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.saveBriefingTemplate(id,fid,decodeTitle,decodeVersion,decodeInstitution,decodeTime,decodeKeylist,decodeText);
    }


    @GetMapping("/deleteBriefingTemplate")
    @ResponseBody
    public JSONObject deleteBriefingTemplate (
            @RequestParam("id") int id
    ) throws ParseException {
        return searchService.deleteBriefingTemplate(id);
    }


    @GetMapping("/getMaterial")
    @ResponseBody
    public JSONArray getMaterial (
            @RequestParam("fid") long fid
    ) {
        return searchService.getMaterial(fid);
    }

    @GetMapping("/getMaterialDetail")
    @ResponseBody
    public YuQingResponse getMaterialDetail (
            @RequestParam("fid") long fid,
            @RequestParam("materiallib") String materiallib
    ) {
        String decodemateriallib="";
        try{
            decodemateriallib= java.net.URLDecoder.decode(materiallib, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.getMaterialDetail(fid,decodemateriallib);
    }

    @GetMapping("/addNewMaterialLib")
    @ResponseBody
    public JSONObject addNewMaterialLib (
            @RequestParam("fid") long fid,
            @RequestParam("materiallib") String materiallib
    ) {
        String decodemateriallib="";
        try{
            decodemateriallib= java.net.URLDecoder.decode(materiallib, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.addNewMaterialLib(fid,decodemateriallib);
    }

    @GetMapping("/renameMaterial")
    @ResponseBody
    public JSONObject renameMaterial (
            @RequestParam("fid") long fid,
            @RequestParam("oldname") String oldname,
            @RequestParam("newname") String newname
    ) {
        String decodeoldname="";
        String decodenewname="";
        try{
            decodeoldname= java.net.URLDecoder.decode(oldname, "utf-8");
            decodenewname= java.net.URLDecoder.decode(newname, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.renameMaterial(fid,decodeoldname,decodenewname);
    }

    @GetMapping("/deleteMaterial")
    @ResponseBody
    public JSONObject deleteMaterial (
            @RequestParam("fid") long fid,
            @RequestParam("materiallib") String materiallib
    ) {
        String decodemateriallib="";
        try{
            decodemateriallib= java.net.URLDecoder.decode(materiallib, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.deleteMaterial(fid,decodemateriallib);
    }

    @PostMapping("/deleteMaterialIDs")
    @ResponseBody
    public JSONObject deleteMaterialIDs (@RequestBody Map<String,String> deleteyMaterialInfo
    ) throws ParseException {
        long fid = Long.parseLong(deleteyMaterialInfo.get("fid"));
        String materiallib=deleteyMaterialInfo.get("materiallib");
        String ids = deleteyMaterialInfo.get("ids");
        String decodeIds = "";
        String decodemateriallib="";
        try{
            decodeIds = java.net.URLDecoder.decode(ids, "utf-8");
            decodemateriallib= java.net.URLDecoder.decode(materiallib, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.deleteMaterialIDs(fid,decodemateriallib,decodeIds);
    }


    @PostMapping("/modeifyMaterial")
    @ResponseBody
    public JSONObject modeifyMaterial (@RequestBody Map<String,String> modeifyMaterialInfo
    ) throws ParseException {
        long fid = Long.parseLong(modeifyMaterialInfo.get("fid"));
        String materiallib=modeifyMaterialInfo.get("materiallib");
        String ids = modeifyMaterialInfo.get("ids");
        String decodeIds = "";
        String decodemateriallib="";
        try{
            decodeIds = java.net.URLDecoder.decode(ids, "utf-8");
            decodemateriallib= java.net.URLDecoder.decode(materiallib, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.modeifyMaterial(fid,decodemateriallib,decodeIds);
    }


    @PostMapping("/generateFile")
    @ResponseBody
    public JSONObject generateFile (@RequestBody Map<String,String> modeifyMaterialInfo
    ) throws ParseException, TemplateException, IOException, DocumentException, com.lowagie.text.DocumentException {
        int fileID=Integer.parseInt(modeifyMaterialInfo.get("fileID"));
        long fid = Long.parseLong(modeifyMaterialInfo.get("fid"));
        int templateId=Integer.parseInt(modeifyMaterialInfo.get("templateId"));
        String title=modeifyMaterialInfo.get("title");
        String institution=modeifyMaterialInfo.get("institution");
        String yuQingIds = modeifyMaterialInfo.get("yuQingIds");
        String echartsData = modeifyMaterialInfo.get("echartsData");
        String decodeTitle = "";
        String decodeInstitution="";
        String decodeYuQingIds = "";
        try{
            decodeTitle = java.net.URLDecoder.decode(title, "utf-8");
            decodeInstitution= java.net.URLDecoder.decode(institution, "utf-8");
            decodeYuQingIds= java.net.URLDecoder.decode(yuQingIds, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.generateFile(fileID,fid,templateId,decodeTitle,decodeInstitution,decodeYuQingIds,echartsData);
    }

    @GetMapping("/getBriefingFiles")
    @ResponseBody
    public JSONArray getBriefingFiles (
            @RequestParam("fid") long fid
    ) {
        return searchService.getBriefingFiles(fid);
    }

    @GetMapping("/addNewBriefingFileRecord")
    @ResponseBody
    public JSONObject addNewBriefingFileRecord (
            @RequestParam("fid") long fid,
            @RequestParam("title") String title
    ) {
        String decodeTitle="";
        try{
            decodeTitle = java.net.URLDecoder.decode(title, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.addNewBriefingFileRecord(fid,decodeTitle);
    }

    @GetMapping("/updateBriefingFileProgess")
    @ResponseBody
    public JSONObject updateBriefingFileProgess (
            @RequestParam("id") int id,
            @RequestParam("percent") int percent
    ) {
        return searchService.updateBriefingFileProgess(id,percent);
    }

    @GetMapping("/deleteBriefingFiles")
    @ResponseBody
    public JSONObject deleteBriefingFiles (
            @RequestParam("id") int id
    ) {
        return searchService.deleteBriefingFiles(id);
    }

    @GetMapping("/downloadBriefingFiles")
    @ResponseBody
    public void downloadBriefingFiles (
            @RequestParam("id") int id,
            @RequestParam("type") String type,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        searchService.downloadBriefingFiles(id,type,request,response);
    }

    @GetMapping("/getSensitiveWordTypes")
    @ResponseBody
    public JSONArray getSensitiveWordTypes () {
        return searchService.getSensitiveWordTypes();
    }

    @GetMapping("/getSensitiveWords")
    @ResponseBody
    public List<SensitiveWords> getSensitiveWords (
            @RequestParam("type") String type
    ) throws Exception {
        String decodeType="";
        try{
            decodeType = java.net.URLDecoder.decode(type, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        System.out.println(decodeType);
       return searchService.getSensitiveWords(decodeType);
    }

    @PostMapping("/deleteSensitiveWords")
    @ResponseBody
    public JSONObject deleteSensitiveWords (@RequestBody Map<String,String> deleteSensitiveWordsInfo
    ) throws ParseException {
        String type=deleteSensitiveWordsInfo.get("type");
        String words = deleteSensitiveWordsInfo.get("words");
        String decodeType = "";
        String decodeWords="";
        try{
            decodeType = java.net.URLDecoder.decode(type, "utf-8");
            decodeWords= java.net.URLDecoder.decode(words, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.deleteSensitiveWords(decodeType,decodeWords);
    }

    @GetMapping("/addSensitiveWordForAll")
    @ResponseBody
    public JSONObject addSensitiveWordForAll (
            @RequestParam("type") String type,
            @RequestParam("word") String word
    ) throws Exception {
        String decodeType="";
        String decodeWord="";
        try{
            decodeType = java.net.URLDecoder.decode(type, "utf-8");
            decodeWord = java.net.URLDecoder.decode(word, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.addSensitiveWordForAll(decodeType,decodeWord);
    }

    @GetMapping("/getSensitiveWordsByFid")
    @ResponseBody
    public JSONArray getSensitiveWordsByFid (
            @RequestParam("fid") long fid
    ) {
        return searchService.getSensitiveWordsByFid(fid);
    }

    @GetMapping("/generate")
    @ResponseBody
    public JSONObject gengrate () throws com.lowagie.text.DocumentException, IOException {
        return searchService.generate();
    }

    @GetMapping("/getFangAnWarning")
    @ResponseBody
    public FangAnWarning getFangAnWarning (
            @RequestParam("fid") long fid
    )  {
        return fangAnWarningService.getFangAnWarning(fid);
    }


    @GetMapping("/modifyFangAnWarning")
    @ResponseBody
    public JSONObject modifyFangAnWarning (
            @RequestParam("fid") long fid,
            @RequestParam("warningSwitch") int warningSwitch,
            @RequestParam("words") String words,
            @RequestParam("sensitiveAttribute") int sensitiveAttribute,
            @RequestParam("similarArticle") int similarArticle,
            @RequestParam("area") String area,
            @RequestParam("sourceSite") int sourceSite,
            @RequestParam("result") int result,
            @RequestParam("involve") int involve,
            @RequestParam("matchingWay") int matchingWay,
            @RequestParam("weiboType") int weiboType,
            @RequestParam("deWeight") int deWeight,
            @RequestParam("filtrate") int filtrate,
            @RequestParam("informationType") String informationType,
            @RequestParam("warningType") int warningType
    )  {
        String decodeWords="";
        String decodeArea="";
        String decodeInformationType="";
        try{
            decodeWords = java.net.URLDecoder.decode(words, "utf-8");
            decodeArea = java.net.URLDecoder.decode(area, "utf-8");
            decodeInformationType = java.net.URLDecoder.decode(informationType, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return fangAnWarningService.modifyFangAnWarning(fid, warningSwitch, decodeWords, sensitiveAttribute,
        similarArticle, decodeArea, sourceSite, result, involve,
        matchingWay,weiboType, deWeight, filtrate,
        decodeInformationType,warningType);
    }

    @GetMapping("/getAllWarningReceiver")
    @ResponseBody
    public WarningReceiverResponse getAllWarningReceiver (
            @RequestParam("fid") long fid
    )  {
        return warningReceiverService.getAllWarningReceiver(fid);
    }

    @GetMapping("/addWarningReceiver")
    @ResponseBody
    public JSONObject getAllWarningReceiver (
            @RequestParam("fid") long fid,
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            @RequestParam("email") String email,
            @RequestParam("wechat") String wechat
    )  {
        String decodeName="";
        String decodePhone="";
        String decodeEmail="";
        String decodeWechat="";
        try{
            decodeName = java.net.URLDecoder.decode(name, "utf-8");
            decodePhone = java.net.URLDecoder.decode(phone, "utf-8");
            decodeEmail = java.net.URLDecoder.decode(email, "utf-8");
            decodeWechat = java.net.URLDecoder.decode(wechat, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return warningReceiverService.addWarningReceiver(fid,decodeName,decodePhone,decodeEmail,decodeWechat);
    }

    @GetMapping("/deleteWarningReceiver")
    @ResponseBody
    public JSONObject getAllWarningReceiver (
            @RequestParam("id") int id
    )  {
        return warningReceiverService.deleteWarningReceiver(id);
    }

    @GetMapping("/getWarningRecord")
    @ResponseBody
    public WarningRecordResponse getWarningRecord (
            @RequestParam("fid") long fid,
            @RequestParam("type") int type,
            @RequestParam("start") String start,
            @RequestParam("end") String end
    ) throws ParseException {
        String decodeStart="";
        String decodeEnd="";
        try{
            decodeStart = java.net.URLDecoder.decode(start, "utf-8");
            decodeEnd = java.net.URLDecoder.decode(end, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return warningRecordService.getWarningRecord(fid,type,decodeStart,decodeEnd);
    }

    @GetMapping("/getVideoData")
    @ResponseBody
    public VideoResponse getVideoData(
            @RequestParam("fid") long fid,
            @RequestParam("videoKeyword")String keyword,
            @RequestParam("videoStartPublishedDay") String startPublishedDay,
            @RequestParam("videoEndPublishedDay") String endPublishedDay,
            @RequestParam("videoSource") String resource,
            @RequestParam("videoPageId") int page,
            @RequestParam("videoPageSize") int pageSize,
            @RequestParam("videoTimeOrder") int timeOrder
    ){
        String decodeKeyword = "";
        String decodeResource="";
        String decodeStart = "";
        String decodeEnd="";
        try{
            decodeKeyword = java.net.URLDecoder.decode(keyword, "utf-8");
            decodeResource= java.net.URLDecoder.decode(resource, "utf-8");
            decodeStart = java.net.URLDecoder.decode(startPublishedDay, "utf-8");
            decodeEnd= java.net.URLDecoder.decode(endPublishedDay, "utf-8");
        }catch (Exception e){
            System.out.println(e);
        }
        return searchService.getVideoData(fid,decodeKeyword,decodeStart,decodeEnd,decodeResource,page,pageSize,timeOrder);
    }

}
