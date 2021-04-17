package com.sjtu.mts.Entity;

public class ClusteredData implements Comparable<ClusteredData> {
    private String id;

    private String content;

    private String cflag;

    private String publishedDay;

    private String resource;

    private String title;

    private String webpageUrl;

    private String fromType;

    private String captureTime;

    private String published;

    private Long _version_;

    private String time;

    private String num;

    private String summary;

    public ClusteredData(Data data){
        this.id = data.getId();
        this.content = data.getContent();
        this.cflag = data.getCflag();
        this.publishedDay = data.getPublishedDay();
        this.resource = data.getResource();
        this.title = data.getTitle();
        this.webpageUrl = data.getWebpageUrl();
        this.fromType = data.getFromType();
        this.captureTime = data.getCaptureTime();
        this.published = data.getPublished();
        this._version_ = data.get_version_();
        this.summary = "";
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getCflag() {
        return cflag;
    }

    public String getFromType() {
        return fromType;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public Long get_version_() {
        return _version_;
    }

    public String getResource() {
        return resource;
    }

    public String getCaptureTime() {
        return captureTime;
    }

    public String getWebpageUrl() {
        return webpageUrl;
    }

    public String getNum() {
        return num;
    }

    public String getPublished() {
        return published;
    }

    public String getSummary() {
        return summary;
    }

    public String getPublishedDay() {
        return publishedDay;
    }

    @Override
    public int compareTo(ClusteredData clusteredData) {
        int res=publishedDay.compareTo(clusteredData.publishedDay);
        if (res > 0){
            return 1;
        }else if(res == 0) {
            return 0;
        }
        return -1;

    }
    @Override
    public String toString(){
        return  this.id+this.content+this.publishedDay;
    }
}
