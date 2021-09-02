package com.sjtu.mts.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "fanganmaterial")
public class FangAnMaterial {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "fid")
    private long fid;

    @Column(name = "materiallib")
    private String materiallib;

    @Column(name = "ids")
    private String ids;

    public FangAnMaterial(){}
    public FangAnMaterial(
            long fid,
            String materiallib,
            String ids
    ){
        this.fid = fid;
        this.materiallib=materiallib;
        this.ids=ids;
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

    public String getMateriallib(){return materiallib;}

    public void setMateriallib(String materiallib){this.materiallib=materiallib;}

    public String getIds(){return ids;}

    public void setIds(String ids){this.ids=ids;}
}
