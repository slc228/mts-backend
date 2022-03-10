package com.sjtu.mts.Entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "fangan")

public class FangAn implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "category")
    private String category;

    @Column(name = "start_date")
    private Timestamp startDate;

    @Column(name = "end_date")
    private Timestamp endDate;

    @Column(name = "notified_when_completed")
    private boolean notifiedWhenCompleted;

    @Column(name = "exceptkeyword")
    private String exceptKeyword;

    @Column(name = "created_date")
    private Timestamp createdDate;

    @Column(name = "updated_date")
    private Timestamp updatedDate;

    @Column(name = "deleted_date")
    private Timestamp deletedDate;

    @Column(name = "flag")
    private boolean flag;


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
            String sensitiveword,
            Integer priority,
            String category,
            Timestamp startDate,
            Timestamp endDate,
            boolean notifiedWhenCompleted,
            String exceptKeyword,
            Timestamp createdDate,
            Timestamp updatedDate,
            Timestamp deletedDate,
            boolean flag
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
        this.priority=priority;
        this.category=category;
        this.startDate=startDate;
        this.endDate=endDate;
        this.notifiedWhenCompleted=notifiedWhenCompleted;
        this.exceptKeyword=exceptKeyword;
        this.createdDate=createdDate;
        this.updatedDate=updatedDate;
        this.deletedDate=deletedDate;
        this.flag=flag;
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public boolean getNotifiedWhenCompleted() {
        return notifiedWhenCompleted;
    }

    public void setNotifiedWhenCompleted(boolean notifiedWhenCompleted) {
        this.notifiedWhenCompleted = notifiedWhenCompleted;
    }

    public String getExceptKeyword() {
        return exceptKeyword;
    }

    public void setExceptKeyword(String exceptKeyword) {
        this.exceptKeyword = exceptKeyword;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Timestamp getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Timestamp getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(Timestamp deletedDate) {
        this.deletedDate = deletedDate;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
