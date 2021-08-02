package com.sjtu.mts.Entity;

import org.elasticsearch.search.DocValueFormat;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "fangantemplate")
public class FangAnTemplate {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "fid")
    private long fid;

    @Column(name = "title")
    private String title;

    @Column(name = "version")
    private String version;

    @Column(name = "institution")
    private String institution;

    @Column(name = "time")
    private Date time;

    @Column(name = "keylist")
    private String keylist;

    public FangAnTemplate(){}
    public FangAnTemplate(
            long fid,
            String title,
            String version,
            String institution,
            Date time,
            String keylist
    ){
        this.fid = fid;
        this.title=title;
        this.version=version;
        this.institution=institution;
        this.time=time;
        this.keylist=keylist;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion(){return version;}

    public void setVersion(String version){this.version=version;}

    public String getInstitution(){return institution;}

    public void setInstitution(String institution){this.institution=institution;}

    public Date getTime(){return time;}

    public void setTime(Date time){this.time=time;}

    public String getKeylist(){return keylist;}

    public void setKeylist(String keylist){this.keylist=keylist;}
}
