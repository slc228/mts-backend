package com.sjtu.mts.Entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "fangan")

public class FangAn implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "fid")

    private long fid;

    @Column(name = "username")
    private String username;

    @Column(name = "fanganname")
    private String fangAnname;

    @Column(name = "keyword")
    private String keyword;

    @Column(name = "kisand")
    private int kisAnd;

    @Column(name = "fromtype")
    private String fromType;

    @Column(name = "area")
    private String area;

    @Column(name = "aisand")
    private int aisAnd;

    public FangAn(){}

    public FangAn(String username, String fangAnname,String keyword ,int kisAnd,String fromType,String area,int aisAnd){
        this.username = username;
        this.fangAnname = fangAnname;
        this.keyword = keyword;
        this.kisAnd = kisAnd;
        this.fromType  = fromType;
        this.area = area;
        this.aisAnd = aisAnd;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setFangAnname(String fangAnname) {
        this.fangAnname = fangAnname;
    }

    public String getFangAnname() {
        return fangAnname;
    }


    public long getFid() {
        return fid;
    }

    public void setFid(long fid) {
        this.fid = fid;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    public String getFromType() {
        return fromType;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getArea() {
        return area;
    }
}
