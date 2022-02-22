package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.WarningRecord;
import com.sjtu.mts.Repository.WarningRecordRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class WarningRecordDaoImpl implements WarningRecordDao{
    private WarningRecordRepository warningRecordRepository;

    public WarningRecordDaoImpl(WarningRecordRepository warningRecordRepository) {
        this.warningRecordRepository = warningRecordRepository;
    }

    @Override
    public List<WarningRecord> findAllByFidAndTimeBetweenOrderByTimeDesc(long fid, Date start, Date end) {
        return warningRecordRepository.findAllByFidAndTimeBetweenOrderByTimeDesc(fid,start,end);
    }

    @Override
    public List<WarningRecord> findAllByFidAndTypeAndTimeBetweenOrderByTimeDesc(long fid, int type, Date start, Date end) {
        return warningRecordRepository.findAllByFidAndTypeAndTimeBetweenOrderByTimeDesc(fid,type,start,end);
    }

    @Override
    public void save(WarningRecord warningRecord) {
        warningRecordRepository.save(warningRecord);
    }
}
