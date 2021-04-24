package com.sjtu.mts.Entity;


import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class Cluster {
    private  int clusterNum;
    private  int hit;
    private String time;
    private String summary;
    private List<BigDecimal> center = new Vector<>();
    private List<Data> clusterDatas = new LinkedList<>();

    public int getClusterNum() {
        return clusterNum;
    }

    public String getSummary() {
        return summary;
    }

    public String getTime() {
        return time;
    }

    public List<Data> getClusterDatas() {
        return clusterDatas;
    }

    public List<BigDecimal> getCenter() {
        return center;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setCenter(List<BigDecimal> center) {
        this.center = center;
    }

    public void setClusterDatas(List<Data> clusterDatas) {
        this.clusterDatas = clusterDatas;
    }

    public void setClusterNum(int clusterNum) {
        this.clusterNum = clusterNum;
    }

    public void addClusterData(Data data){
        this.clusterDatas.add(data);
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }
}
