package com.sjtu.mts.Entity;

import org.elasticsearch.search.DocValueFormat;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "fanganweibouser")
public class FangAnWeiboUser {
    @Id
    @Column(name = "id")

    private int id;

    @Column(name = "fid")
    private long fid;

    @Column(name = "weibousernickname")
    private String weibousernickname;

    @Column(name = "weibouserid")
    private String weibouserid;

    @Column(name = "newweibotime")
    private Date newweibotime;

    public FangAnWeiboUser(){}
    public FangAnWeiboUser(
            long fid,
            String weibousernickname,
            String weibouserid,
            Date newweibotime
    ){
        this.fid = fid;
        this.weibousernickname = weibousernickname;
        this.weibouserid=weibouserid;
        this.newweibotime=newweibotime;
    }

    public Integer getId() {
        return id;
    }

    public void setFid(long fid) {
        this.fid = fid;
    }

    public long getFid() {
        return fid;
    }

    public String getWeibousernickname() {
        return weibousernickname;
    }

    public void setWeibousernickname(String weibousernickname) {
        this.weibousernickname = weibousernickname;
    }

    public String getWeibouserid(){return weibouserid;}

    public void setWeibouserid(String weibouserid){this.weibouserid=weibouserid;}

    public Date getNewweibotime(){return newweibotime;}

    public void setNewweibotime(Date newweibotime){this.newweibotime=newweibotime;}
}
