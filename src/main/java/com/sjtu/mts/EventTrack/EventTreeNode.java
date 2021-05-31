package com.sjtu.mts.EventTrack;

import com.sjtu.mts.Entity.Cluster;
import com.sjtu.mts.Entity.Data;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import static com.sjtu.mts.Keyword.Wrapper.min;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventTreeNode {
    private int clusterNum;
    @JsonIgnore
    private int hit;
    private String time;
    private String summary;
    @JsonIgnore
    private List<BigDecimal> center = new Vector<>();
//    @JsonIgnore
    private List<Data> clusterDatas = new LinkedList<>();
    private List<EventTreeNode> childList;

    public EventTreeNode(Cluster cluster){
        this.clusterNum = cluster.getClusterNum();
        this.hit = cluster.getHit();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf1.parse(cluster.getTime().replace("T", " ").replace("Z", " "));
            this.time = sdf2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.summary = cluster.getSummary();
        this.center = cluster.getCenter();
        //取聚类中的前5条舆情
        List<Data> firstFive = new ArrayList<>();
        for (int i = 0; i < min(5,cluster.getClusterDatas().size()); i++){
            firstFive.add(cluster.getClusterDatas().get(i));
        }
        this.clusterDatas = firstFive;
        this.childList = new ArrayList<>();
    }
}
