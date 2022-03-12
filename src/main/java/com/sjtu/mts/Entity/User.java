package com.sjtu.mts.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "user")
public class User implements Serializable{
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "project_num")
    private Integer projectNum;

    @Column(name = "valid_date")
    private String validDate;

    @Column(name = "role")
    private String role;

    @Column(name = "state")
    private Integer state;

    @Column(name = "event_limiter")
    private String eventLimiter;

    @Column(name = "sensitive_limiter")
    private String sensitiveLimiter;

    @Column(name = "user_uuid")
    private String userUuid;

    public User(){

    }
    public User(String username, String password, String phone, String email, Integer projectNum, String validDate, String role, Integer state,String eventLimiter,String sensitiveLimiter){
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.projectNum = projectNum;
        this.validDate = validDate;
        this.role = role;
        this.state = state;
        this.eventLimiter=eventLimiter;
        this.sensitiveLimiter=sensitiveLimiter;
        this.userUuid=UUID.randomUUID().toString();
    }
    public User(String username, String password, String phone, String email, Integer projectNum, String validDate, String role, Integer state,String eventLimiter,String sensitiveLimiter,String userUuid){
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.projectNum = projectNum;
        this.validDate = validDate;
        this.role = role;
        this.state = state;
        this.eventLimiter=eventLimiter;
        this.sensitiveLimiter=sensitiveLimiter;
        this.userUuid=userUuid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setProjectNum(Integer projectNum) {
        this.projectNum = projectNum;
    }

    public Integer getProjectNum() {
        return projectNum;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getState() {
        return state;
    }

    public void setValidDate(String validDate) {
        this.validDate = validDate;
    }

    public String getValidDate() {
        return validDate;
    }

    public String getEventLimiter(){return eventLimiter;}

    public void setEventLimiter(String eventLimiter){this.eventLimiter=eventLimiter;}

    public String getSensitiveLimiter(){return sensitiveLimiter;}

    public void setSensitiveLimiter(String sensitiveLimiter){this.sensitiveLimiter=sensitiveLimiter;}

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
