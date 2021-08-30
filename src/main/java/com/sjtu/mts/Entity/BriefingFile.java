package com.sjtu.mts.Entity;

import javax.persistence.*;
import java.sql.Blob;
import java.util.Date;

@Entity
@Table(name = "briefingfile")
public class BriefingFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "fid")
    private long fid;

    @Column(name = "name")
    private String name;

    @Column(name = "generatetime")
    private Date generatetime;

    @Column(name = "pdf")
    private byte[] pdf;

    @Column(name = "word")
    private byte[] word;

    @Column(name = "excel")
    private byte[] excel;

    @Column(name = "percent")
    private int percent;

    public BriefingFile(){}
    public BriefingFile(
            long fid,
            String name,
            Date generatetime,
            byte[] pdf,
            byte[] word,
            byte[] excel,
            int percent
    ){
        this.fid = fid;
        this.name=name;
        this.generatetime=generatetime;
        this.pdf=pdf;
        this.word=word;
        this.excel=excel;
        this.percent=percent;
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

    public String getName(){return name;}

    public void setName(String name){this.name=name;}

    public Date getGeneratetime(){return generatetime;}

    public void setGeneratetime(Date generatetime){this.generatetime=generatetime;}

    public byte[] getPdf(){return pdf;}

    public void setPdf(byte[] pdf){this.pdf=pdf;}

    public byte[] getWord(){return word;}

    public void setWord(byte[] word){this.word=word;}

    public byte[] getExcel(){return excel;}

    public void setExcel(byte[] excel){this.excel=excel;}

    public int getPercent(){return percent;}

    public void setPercent(int percent){this.percent=percent;}
}
