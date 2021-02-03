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

    @Column(name = "fangan")
    private String fangAn;

    public FangAn(){}

    public FangAn(String username, String fangAnname,String fangAn ){
        this.username = username;
        this.fangAnname = fangAnname;
        this.fangAn = fangAn;
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

    public void setFangAn(String fangAn) {
        this.fangAn = fangAn;
    }

    public String getFangAn() {
        return fangAn;
    }

    public long getFid() {
        return fid;
    }

    public void setFid(long fid) {
        this.fid = fid;
    }
}
