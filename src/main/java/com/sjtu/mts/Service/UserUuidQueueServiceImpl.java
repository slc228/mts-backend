package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.UserUuidQueueDao;
import com.sjtu.mts.Entity.UserUUidQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;

@Service
public class UserUuidQueueServiceImpl  implements UserUuidQueueService{
    @Autowired
    private UserUuidQueueDao userUuidQueueDao;

    @Override
    public boolean isUuidValid(String uuid) {
        UserUUidQueue userUUidQueue= userUuidQueueDao.SelectUserUUidByUuid(uuid);
        boolean ret=false;
        if (userUUidQueue!=null)
        {
            Timestamp createdTime = userUUidQueue.getCreatedTime();
            long currentTime = System.currentTimeMillis() ;
            currentTime -=30*60*1000;
            Timestamp halfAnHour = new Timestamp(currentTime);
            System.out.println(halfAnHour);
            ret = createdTime.after(halfAnHour);
        }else {
            ret=false;
        }
        return ret;
    }

    @Override
    public void InsertUserUuid(String uuid, Timestamp createdTime) {
        userUuidQueueDao.InsertUserUuid(uuid, createdTime);
    }
}
