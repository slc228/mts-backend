package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.WarningRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface WarningRecordRepository extends JpaRepository<WarningRecord,Integer> {
    List<WarningRecord> findAllByFidAndTimeBetweenOrderByTimeDesc(long fid, Date start, Date end);
    List<WarningRecord> findAllByFidAndTypeAndTimeBetweenOrderByTimeDesc(long fid, int type, Date start, Date end);
}
