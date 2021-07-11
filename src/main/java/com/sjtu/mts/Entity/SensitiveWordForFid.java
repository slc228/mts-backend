package com.sjtu.mts.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "swordfid")
public class SensitiveWordForFid {

    @Id
    @Column(name = "fid")

    private long fid;

    @Column(name = "contents")
    private String contents;

    public SensitiveWordForFid(){

    }
    public SensitiveWordForFid(long fid,String contents){
        this.fid=fid;
        this.contents = contents;
    }

    public long getFid() {
        return fid;
    }

    public void setFid(long fid) {
        this.fid = fid;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
