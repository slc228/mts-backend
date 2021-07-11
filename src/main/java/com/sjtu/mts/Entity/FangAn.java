package com.sjtu.mts.Entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "fangan")

public class FangAn implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "fid")

    private long fid;

    @Column(name = "username")
    private String username;

    @Column(name = "programme_name")
    private String programmeName;

    @Column(name = "match_type")
    private int matchType;

    @Column(name = "rekeyword")
    private String regionKeyword;

    @Column(name = "rekeymatch")
    private int regionKeywordMatch;



    @Column(name = "rokeyword")
    private String roleKeyword;

    @Column(name = "rokeymatch")
    private int roleKeywordMatch;

    @Column(name = "ekeyword")
    private String eventKeyword;

    @Column(name = "ekeymatch")
    private int eventKeywordMatch;

    @Column(name = "enable_alert")
    private boolean enableAlert;

    @Column(name = "sensitiveword")
    private String sensitiveword;

    public FangAn(){}

    public FangAn(
            String username,
            String programmeName,
            int matchType ,
            String regionKeyword,
            int regionKeywordMatch,
            String roleKeyword,
            int roleKeywordMatch,
            String eventKeyword,
            int eventKeywordMatch,
            boolean enableAlert,
            String sensitiveword
            ){
        this.username = username;
        this.programmeName = programmeName;
        this.matchType = matchType;
        this.regionKeyword = regionKeyword;
        this.regionKeywordMatch = regionKeywordMatch;
        this.roleKeyword = roleKeyword;
        this.roleKeywordMatch = roleKeywordMatch;
        this.eventKeyword = eventKeyword;
        this.eventKeywordMatch = eventKeywordMatch;
        this.enableAlert = enableAlert;
        this.sensitiveword=sensitiveword;

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public long getFid() {
        return fid;
    }

    public int getMatchType() {
        return matchType;
    }

    public void setMatchType(int matchType) {
        this.matchType = matchType;
    }

    public String getProgrammeName() {
        return programmeName;
    }

    public void setProgrammeName(String programmeName) {
        this.programmeName = programmeName;
    }

    public String getRegionKeyword() {
        return regionKeyword;
    }

    public void setRegionKeyword(String regionKeyword) {
        this.regionKeyword = regionKeyword;
    }

    public int getRegionKeywordMatch() {
        return regionKeywordMatch;
    }

    public void setRegionKeywordMatch(int regionKeywordMatch) {
        this.regionKeywordMatch = regionKeywordMatch;
    }

    public String getRoleKeyword() {
        return roleKeyword;
    }

    public void setRoleKeyword(String roleKeyword) {
        this.roleKeyword = roleKeyword;
    }

    public int getRoleKeywordMatch() {
        return roleKeywordMatch;
    }

    public void setRoleKeywordMatch(int roleKeywordMatch) {
        this.roleKeywordMatch = roleKeywordMatch;
    }

    public String getEventKeyword() {
        return eventKeyword;
    }

    public void setEventKeyword(String eventKeyword) {
        this.eventKeyword = eventKeyword;
    }

    public int getEventKeywordMatch() {
        return eventKeywordMatch;
    }

    public void setEventKeywordMatch(int eventKeywordMatch) {
        this.eventKeywordMatch = eventKeywordMatch;
    }

    public boolean getEnableAlert(){
        return enableAlert;
    }

    public void setEnableAlert(boolean enableAlert) {
        this.enableAlert = enableAlert;
    }

    public String getSensitiveword() {
        return sensitiveword;
    }

    public void setSensitiveword(String sensitiveword) {
        this.sensitiveword = sensitiveword;
    }
}
