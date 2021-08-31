package com.sjtu.mts.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dimension")
public class Dimension {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "key")
    private String key;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    public Dimension(){}
    public Dimension(
            String key,
            String name,
            String type
    ){
        this.key = key;
        this.name=name;
        this.type=type;
    }

    public Integer getId() {
        return id;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getName(){return name;}

    public void setName(String name){this.name=name;}

    public String getType(){return type;}

    public void setType(String type){this.type=type;}
}
