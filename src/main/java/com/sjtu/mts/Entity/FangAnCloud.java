package com.sjtu.mts.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "fangan_cloud")
public class FangAnCloud {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "fid")
    private long fid;

    @Column(name = "cloud_word")
    private String cloudWord;

    public FangAnCloud(){

    }

    public FangAnCloud(long fid,String cloudWord)
    {
        this.fid=fid;
        this.cloudWord=cloudWord;
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

    public void setCloudWord(String cloudWord) {
        this.cloudWord = cloudWord;
    }

    public String getCloudWord() {
        return cloudWord;
    }
}
