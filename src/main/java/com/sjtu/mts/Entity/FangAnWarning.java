package com.sjtu.mts.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "fangan_warning")
public class FangAnWarning {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "fid")
    private long fid;

    @Column(name = "warning_switch")
    private int warningSwitch;

    @Column(name = "words")
    private String words;

    @Column(name = "sensitive_attribute")
    private int sensitiveAttribute;

    @Column(name = "similar_article")
    private int similarArticle;

    @Column(name = "area")
    private String area;

    @Column(name = "source_site")
    private int sourceSite;

    @Column(name = "result")
    private int result;

    @Column(name = "involve")
    private int involve;

    @Column(name = "matching_way")
    private int matchingWay;

    @Column(name = "weibo_type")
    private int weiboType;

    @Column(name = "de_weight")
    private int deWeight;

    @Column(name = "filtrate")
    private int filtrate;

    @Column(name = "information_type")
    private String informationType;

    @Column(name = "warning_type")
    private int warningType;

    public FangAnWarning(){}
    public FangAnWarning(long fid){
        this.fid = fid;
        this.warningSwitch=0;
        this.words="";
        this.sensitiveAttribute=0;
        this.similarArticle=0;
        this.area="全国";
        this.sourceSite=0;
        this.result=0;
        this.involve=0;
        this.matchingWay=0;
        this.weiboType=0;
        this.deWeight=0;
        this.filtrate=0;
        this.informationType="微博,政务,网站,外媒,论坛,报刊,客户端,微信,视频,博客,新闻";
        this.warningType=0;
    }
    public FangAnWarning(
            long fid, int warningSwitch, String words, int sensitiveAttribute, int similarArticle,
            String area, int sourceSite, int result, int involve, int matchingWay, int weiboType,
            int deWeight, int filtrate, String informationType, int warningType
    ){
        this.fid = fid;
        this.warningSwitch=warningSwitch;
        this.words=words;
        this.sensitiveAttribute=sensitiveAttribute;
        this.similarArticle=similarArticle;
        this.area=area;
        this.sourceSite=sourceSite;
        this.result=result;
        this.involve=involve;
        this.matchingWay=matchingWay;
        this.weiboType=weiboType;
        this.deWeight=deWeight;
        this.filtrate=filtrate;
        this.informationType=informationType;
        this.warningType=warningType;
    }

    public int getId() {
        return id;
    }

    public void setFid(long fid) {
        this.fid = fid;
    }

    public long getFid() {
        return fid;
    }

    public void setWarningSwitch(int warningSwitch) {
        this.warningSwitch = warningSwitch;
    }

    public int getWarningSwitch() {
        return warningSwitch;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public String getWords() {
        return words;
    }

    public void setSensitiveAttribute(int sensitiveAttribute) {
        this.sensitiveAttribute = sensitiveAttribute;
    }

    public int getSensitiveAttribute() {
        return sensitiveAttribute;
    }

    public void setSimilarArticle(int similarArticle) {
        this.similarArticle = similarArticle;
    }

    public int getSimilarArticle() {
        return similarArticle;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getArea() {
        return area;
    }

    public void setSourceSite(int sourceSite) {
        this.sourceSite = sourceSite;
    }

    public int getSourceSite() {
        return sourceSite;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }

    public void setInvolve(int involve) {
        this.involve = involve;
    }

    public int getInvolve() {
        return involve;
    }

    public void setMatchingWay(int matchingWay) {
        this.matchingWay = matchingWay;
    }

    public int getMatchingWay() {
        return matchingWay;
    }

    public void setWeiboType(int weiboType) {
        this.weiboType = weiboType;
    }

    public int getWeiboType() {
        return weiboType;
    }

    public void setDeWeight(int deWeight) {
        this.deWeight = deWeight;
    }

    public int getDeWeight() {
        return deWeight;
    }

    public void setFiltrate(int filtrate) {
        this.filtrate = filtrate;
    }

    public int getFiltrate() {
        return filtrate;
    }

    public void setInformationType(String informationType) {
        this.informationType = informationType;
    }

    public String getInformationType() {
        return informationType;
    }

    public void setWarningType(int warningType) {
        this.warningType = warningType;
    }

    public int getWarningType() {
        return warningType;
    }
}
