package com.sjtu.mts.Entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_rights")
public class UserRights implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "data_screen")
    private boolean dataScreen;

    @Column(name = "scheme_configuration")
    private boolean schemeConfiguration;

    @Column(name = "global_search")
    private boolean globalSearch;

    @Column(name = "analysis")
    private boolean analysis;

    @Column(name = "warning")
    private boolean warning;

    @Column(name = "briefing")
    private boolean briefing;

    @Column(name = "user_role")
    private boolean userRole;

    @Column(name = "sensitive_words")
    private boolean sensitiveWords;

    public UserRights(){

    }
    public UserRights(String username,boolean dataScreen,boolean schemeConfiguration,boolean globalSearch,boolean analysis,boolean warning,boolean briefing,boolean userRole,boolean sensitiveWords){
        this.username = username;
        this.dataScreen = dataScreen;
        this.schemeConfiguration = schemeConfiguration;
        this.globalSearch = globalSearch;
        this.analysis = analysis;
        this.warning = warning;
        this.briefing = briefing;
        this.userRole = userRole;
        this.sensitiveWords=sensitiveWords;
    }

    public UserRights(String username,String role){
        this.username = username;
        if (role.equals("systemAdmin"))
        {
            this.dataScreen = true;
            this.schemeConfiguration = true;
            this.globalSearch = true;
            this.analysis = true;
            this.warning = true;
            this.briefing = true;
            this.userRole = true;
            this.sensitiveWords=true;
        }
        if (role.equals("admin"))
        {
            this.dataScreen = false;
            this.schemeConfiguration = false;
            this.globalSearch = false;
            this.analysis = false;
            this.warning = false;
            this.briefing = false;
            this.userRole = false;
            this.sensitiveWords=true;
        }
        if (role.equals("default"))
        {
            this.dataScreen = true;
            this.schemeConfiguration = true;
            this.globalSearch = true;
            this.analysis = true;
            this.warning = true;
            this.briefing = true;
            this.userRole = false;
            this.sensitiveWords=false;
        }
        if (role.equals("tourist"))
        {
            this.dataScreen = true;
            this.schemeConfiguration = false;
            this.globalSearch = true;
            this.analysis = true;
            this.warning = false;
            this.briefing = false;
            this.userRole = false;
            this.sensitiveWords=false;
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setDataScreen(boolean dataScreen) {
        this.dataScreen = dataScreen;
    }

    public boolean getDataScreen () {
        return dataScreen;
    }

    public void setSchemeConfiguration(boolean schemeConfiguration) {
        this.schemeConfiguration = schemeConfiguration;
    }

    public boolean getSchemeConfiguration() {
        return schemeConfiguration;
    }

    public void setGlobalSearch(boolean globalSearch) {
        this.globalSearch = globalSearch;
    }

    public boolean getGlobalSearch() {
        return globalSearch;
    }

    public void setAnalysis(boolean analysis) {
        this.analysis = analysis;
    }

    public boolean getAnalysis() {
        return analysis;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    public boolean getWarning() {
        return warning;
    }

    public void setBriefing(boolean briefing) {
        this.briefing = briefing;
    }

    public boolean getBriefing() {
        return briefing;
    }

    public void setUserRole(boolean userRole) {
        this.userRole = userRole;
    }

    public boolean getUserRole() {
        return userRole;
    }

    public void setSensitiveWords(boolean sensitiveWords) {
        this.sensitiveWords = sensitiveWords;
    }

    public boolean getSensitiveWords() {
        return sensitiveWords;
    }

}
