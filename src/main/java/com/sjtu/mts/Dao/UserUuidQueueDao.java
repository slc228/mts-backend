package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.UserUUidQueue;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;

public interface UserUuidQueueDao {
    UserUUidQueue SelectUserUUidByUuid(String uuid);
    void InsertUserUuid(String uuid, Timestamp createdTime);
}
