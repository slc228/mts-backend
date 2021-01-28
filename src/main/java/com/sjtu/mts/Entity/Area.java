package com.sjtu.mts.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
@Entity
@Table(name = "tb_da_area")
public class Area implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "codeid")
    private int codeid;

    @Column(name = "parentid")
    private int parentid;

    @Column(name = "cityName")
    private String cityName;

    public Area(){

    }
    public Area(int codeid,int parentid,String cityName){
        this.codeid = codeid;
        this.parentid = parentid;
        this.cityName = cityName;

    }

    public void setCodeid(int codeid) {
        this.codeid = codeid;
    }

    public int getCodeid() {
        return codeid;
    }

    public void setParentid(int parentid) {
        this.parentid = parentid;
    }

    public int getParentid() {
        return parentid;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityName() {
        return cityName;
    }
}
