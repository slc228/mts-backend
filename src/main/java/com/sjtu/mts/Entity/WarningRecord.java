package com.sjtu.mts.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "warning_record")
public class WarningRecord {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "fid")
    private long fid;

    @Column(name = "content")
    private String content;

    @Column(name = "contact")
    private String contact;

    @Column(name = "time")
    private Date time;

    @Column(name = "type")
    private int type;

    public WarningRecord(){}
    public WarningRecord(
            long fid, String content, String contact,Date time,int type
    ){
        this.fid = fid;
        this.content=content;
        this.contact=contact;
        this.time=time;
        this.type=type;
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

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContact() {
        return contact;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getTime() {
        return time;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
