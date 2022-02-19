package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.WarningRecord;

import java.util.Date;
import java.util.List;

public interface WarningRecordDao {
    List<WarningRecord> findAllByFidAndTimeBetweenOrderByTimeDesc(long fid, Date start, Date end);
    List<WarningRecord> findAllByFidAndTypeAndTimeBetweenOrderByTimeDesc(long fid, int type, Date start, Date end);
    void save(WarningRecord warningRecord);
}
