package com.sjtu.mts.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "monitoring_website")
public class MonitoringWebsite {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;


    public MonitoringWebsite(){

    }
    public MonitoringWebsite(String name){
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName () {
        return name;
    }
}
