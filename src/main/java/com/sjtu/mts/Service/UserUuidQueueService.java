package com.sjtu.mts.Service;

import java.sql.Timestamp;

public interface UserUuidQueueService {
    boolean isUuidValid(String uuid);
    void InsertUserUuid(String uuid, Timestamp createdTime);
}
