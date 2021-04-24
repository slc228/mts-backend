package com.sjtu.mts.EventTrack;

import com.sjtu.mts.Entity.Cluster;
import com.sjtu.mts.Entity.Data;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonIgnore;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventTreeNode {
    private int clusterNum;
    private int hit;
    private String time;
    private String summary;
    @JsonIgnore
    private List<BigDecimal> center = new Vector<>();
    @JsonIgnore
    private List<Data> clusterDatas = new LinkedList<>();
    private List<EventTreeNode> childList;

    public EventTreeNode(Cluster cluster){
        this.clusterNum = cluster.getClusterNum();
        this.hit = cluster.getHit();
        this.time = cluster.getTime();
        this.summary = cluster.getSummary();
        this.center = cluster.getCenter();
        this.clusterDatas = cluster.getClusterDatas();
        this.childList = new ArrayList<>();
    }
}
