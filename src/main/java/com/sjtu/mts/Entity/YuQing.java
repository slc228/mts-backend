package com.sjtu.mts.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class YuQing {

    private String id;

    private String content;  // 舆情内容

    private String sender;  // 发布者

    private String senderAvatar;  // 发布者头像链接

    private String publishedDay;  // 舆情发布事件

    private String resource;  // 舆情来源

    private String title;  // 标题

    private String webpageUrl;  // 网页链接

    private String captureTime;  // 舆情抓取时间

    private String published;  // 舆情系统发布时间

    private String commentAmount;  // 评论数量

    private String likesAmount;  // 点赞数量

    private String fid;  // 方案id

    private String keywordExtraction;  // 舆情关键词分析结果

    private String tag;  // 舆情类型分析结果

    private String emotion;  // 情感分析结果

    private String sensitiveType;  // 敏感度分析结果

    public YuQing(JSONObject yuqing)
    {
        this.id = yuqing.getAsString("id");
        this.content = yuqing.getAsString("content");
        this.sender = yuqing.getAsString("sender");
        this.senderAvatar = yuqing.getAsString("senderAvatar");
        this.publishedDay = yuqing.getAsString("publishedDay");
        this.resource = yuqing.getAsString("resource");
        this.title = yuqing.getAsString("title");
        this.webpageUrl = yuqing.getAsString("webpageUrl");
        this.captureTime = yuqing.getAsString("captureTime");
        this.published = yuqing.getAsString("published");
        this.commentAmount = yuqing.getAsString("commentAmount");
        this.likesAmount = yuqing.getAsString("likesAmount");
        this.fid = yuqing.getAsString("fid");
        this.keywordExtraction = yuqing.getAsString("keywordExtraction");
        this.tag = yuqing.getAsString("tag");
        this.emotion = yuqing.getAsString("emotion");
        this.sensitiveType = yuqing.getAsString("sensitiveType");
    }

    public void setFid(String fid) {
        this.fid = fid;
    }
    public void setId(String id) {
        this.id = id;
    }
}
