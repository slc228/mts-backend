package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.UserUUidQueue;
import com.sjtu.mts.Repository.UserUUidQueueRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public class UserUuidQueueDaoImpl implements UserUuidQueueDao {
    private final UserUUidQueueRepository userUUidQueueRepository;

    public UserUuidQueueDaoImpl(UserUUidQueueRepository userUUidQueueRepository) {
        this.userUUidQueueRepository = userUUidQueueRepository;
    }

    @Override
    public UserUUidQueue SelectUserUUidByUuid(String uuid) {
        return userUUidQueueRepository.SelectUserUUidByUuid(uuid);
    }

    @Override
    public void InsertUserUuid(String uuid, Timestamp createdTime) {
        userUUidQueueRepository.InsertUserUuid(uuid, createdTime);
    }
}
