package com.sjtu.mts.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "user_uuid_queue")
public class UserUUidQueue {
    @Id
    @Column(name = "user_uuid")
    private String userUuid;

    @Column(name = "created_time")
    private Timestamp createdTime;

    public UserUUidQueue()
    {

    }

    public UserUUidQueue(String userUuid, Timestamp createdTime)
    {
        this.userUuid=userUuid;
        this.createdTime=createdTime;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }
}
