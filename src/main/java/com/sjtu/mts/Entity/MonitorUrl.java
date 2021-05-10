package com.sjtu.mts.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "mourl")
public class MonitorUrl implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @Column(name = "create_by")
    private String create_by;

    @Column(name = "create_date")
    private String create_date;

    @Column(name = "update_by")
    private String update_by;

    @Column(name = "update_date")
    private String update_date;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "del_flag")
    private char del_flag;

    public  MonitorUrl(){

    }
    public MonitorUrl(String name,String url,String create_by,String create_date,String update_by,String update_date,String remarks,char del_flag ){
        this.name = name;
        this.url = url;
        this.create_by = create_by;
        this.create_date =create_date;
        this.update_by = update_by;
        this.update_date = update_date;
        this.remarks = remarks;
        this.del_flag = del_flag;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCreate_by() {
        return create_by;
    }

    public void setCreate_by(String create_by) {
        this.create_by = create_by;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getUpdate_by() {
        return update_by;
    }

    public void setUpdate_by(String update_by) {
        this.update_by = update_by;
    }

    public String getUpdate_date() {
        return update_date;
    }

    public void setUpdate_date(String update_date) {
        this.update_date = update_date;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public char getDel_flag() {
        return del_flag;
    }

    public void setDel_flag(char del_flag) {
        this.del_flag = del_flag;
    }
}
