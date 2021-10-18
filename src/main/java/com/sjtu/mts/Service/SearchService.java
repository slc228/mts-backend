package com.sjtu.mts.Service;

import com.alibaba.fastjson.JSON;
import com.itextpdf.text.DocumentException;
import com.sjtu.mts.Entity.*;
import com.sjtu.mts.Keyword.KeywordResponse;
import com.sjtu.mts.Response.*;
import freemarker.template.TemplateException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface SearchService {

    public YuQingResponse Search(String keyword, String startPublishedDay, String endPublishedDay,
                               String sensitiveFlag, int page, int pageSize, int timeOrder);

    public YuQingResponse SearchWithObject(String keyword, String sensitiveType, String emotion, String startPublishedDay, String endPublishedDay,
                               String resource, int page, int pageSize, int timeOrder,String keywords);

    public JSONArray getResources();

    public JSONArray globalSearchResourceCount(String keyword, String startPublishedDay,
                                                           String endPublishedDay);
    public JSONArray globalSearchResourceCountByFid(long fid,String startPublishedDay, String endPublishedDay);

    public CflagCountResponse globalSearchCflagCount(String keyword, String startPublishedDay, String endPublishedDay);

    public CflagCountResponse globalSearchCflagCountByFid(long fid,String startPublishedDay, String endPublishedDay);


    public JSONObject globalSearchTrendCount(String keyword, String startPublishedDay, String endPublishedDay);

    public JSONObject totalAmountTrendCount(String keyword,String startPublishedDay,String endPublishedDay);
    public JSONObject sourceAmountTrendCount(String keyword,String startPublishedDay,String endPublishedDay);

    public JSONObject globalSearchTrendCount2(long fid,String startPublishedDay, String endPublishedDay);
    public AmountTrendResponse globalSearchTrendCount3(long fid,String startPublishedDay, String endPublishedDay);


    public AreaAnalysisResponse countArea(String keyword, String startPublishedDay, String endPublishedDay);

    public AreaAnalysisResponse countAreaByFid(long fid,String startPublishedDay, String endPublishedDay);

    /* 研判预警模块接口 */
    public DataResponse searchByUser(long fid, String username, int pageSize, int pageId) throws UnsupportedEncodingException;
    public Map<String, Integer> getActivateUser(long fid);


    public DataResponse fangAnSearch(long fid,String cflag, String startPublishedDay, String endPublishedDay,
                                     String fromType, int page, int pageSize, int timeOrder);
    public YuQingResponse fangAnSearch2(long fid,String keyword,String sensitiveType,String emotion, String startPublishedDay, String endPublishedDay,
                                     String resource, int page, int pageSize, int timeOrder);

    public JSONObject addSensitiveWord(String sensitiveWord);
    public JSONObject delSensitiveWord(String sensitiveWord);
    /*
    * DFA方法提取敏感词*/
    public JSONArray sensitiveWordFiltering(String text);
    /*
     * 分词方法提取敏感词*/
    public JSONArray sensitiveWordFilteringHanLp(String text);
    public JSONArray sensitiveWord(long fid, String startPublishedDay, String endPublishedDay);

    public List<KeywordResponse> extractKeyword(long fid, String startPublishedDay, String endPublishedDay
            , int keywordNumber, String extractMethod);

    public JSONObject keywordExtractionForSingleText(String title,String content);

    public  JSONObject autoaddEkeyword(long fid,String text);
    public JSONObject addSensitivewordForFid(long fid,String text);
    public JSONArray sensitivewordForFid(long fid);
    public JSONArray sensitiveWordByFid(long fid,String text);

    public JSONArray eventKeyWordByFid(long fid);

    public HotArticleResponse getHotArticle(int pageId,int pageSize);

    public List<BriefWeiboUser> searchBriefWeiboUser(long fid, String WeiboUserForSearch);

    public JSONObject addWeiboUser(long fid,String Weibouserid,String Weibousernickname);

    public JSONObject deleteWeiboUser(long fid,String Weibouserid,String Weibousernickname);

    public JSONArray getFangAnMonitor(long fid) throws ParseException;

    public JSONObject getWeiboByid(long fid,String id) throws ParseException;

    public JSONArray getWeiboListByid(long fid,String weibouserid) throws ParseException;

    public JSONArray getOverallDatOnNetwork(String keyword,Integer pageId) throws MalformedURLException, InterruptedException;

    public JSONArray getOverallDataBing(String keyword,Integer pageId) throws MalformedURLException, InterruptedException;

    public JSONArray getOverallData360(String keyword,Integer pageId) throws MalformedURLException, InterruptedException;

    public JSONArray getOverallDataBaidu(String keyword,Integer pageId) throws MalformedURLException, InterruptedException;

    public List<FangAnTemplate> getBriefingTemplate(long fid);

    public JSONObject saveBriefingTemplate(int id,long fid,String decodeTitle,String decodeVersion,String decodeInstitution,String time,String keylist,String text) throws ParseException;

    public JSONObject deleteBriefingTemplate(int id);

    public JSONArray getMaterial(long fid);

    public DataResponse getMaterialDetail(long fid,String materiallib);

    public JSONObject addNewMaterialLib(long fid,String decodemateriallib);

    public JSONObject renameMaterial(long fid,String decodeoldname,String decodenewname);

    public JSONObject deleteMaterial(long fid,String decodemateriallib);

    public JSONObject deleteMaterialIDs(long fid,String decodemateriallib,String decodeIds);

    public JSONObject modeifyMaterial(long fid,String materiallib,String decodeIds);

    public JSONObject generateFile(int fileID,long fid,int templateId,String decodeTitle,String decodeInstitution,String decodeYuQingIds,String echartsData) throws TemplateException, IOException, ParseException, DocumentException, com.lowagie.text.DocumentException;

    public JSONArray getBriefingFiles(long fid);

    public JSONObject addNewBriefingFileRecord(long fid, String title);

    public JSONObject updateBriefingFileProgess(int id,int percent);

    public JSONObject deleteBriefingFiles(int id);

    public void downloadBriefingFiles(int id, String type, HttpServletRequest request, HttpServletResponse response) throws IOException;

    public JSONArray getSensitiveWordTypes();

    public List<SensitiveWords> getSensitiveWords(String type);

    public JSONObject deleteSensitiveWords(String type,String words);

    public JSONObject addSensitiveWordForAll(String type,String word);

    public JSONArray getSensitiveWordsByFid(long fid);
}
