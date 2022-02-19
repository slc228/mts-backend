package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.WarningRecordDao;
import com.sjtu.mts.Entity.WarningRecord;
import com.sjtu.mts.Response.WarningRecordResponse;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class WarningRecordServiceImpl implements WarningRecordService {
    @Autowired
    private WarningRecordDao warningRecordDao;

    @Override
    public WarningRecordResponse getWarningRecord(long fid, int type, String start, String end) throws ParseException {
        WarningRecordResponse warningRecordResponse=new WarningRecordResponse();
        String strDateFormat = "yyyy-MM-dd HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        Date startTime=sdf.parse(start);
        Date endTime=sdf.parse(end);
        if (type==0)
        {
            List<WarningRecord> ret=warningRecordDao.findAllByFidAndTimeBetweenOrderByTimeDesc(fid,startTime,endTime);
            warningRecordResponse.setNumber(ret.size());
            warningRecordResponse.setWarningRecordContent(ret);
        }
        else{
            List<WarningRecord> ret=warningRecordDao.findAllByFidAndTypeAndTimeBetweenOrderByTimeDesc(fid,type-1,startTime,endTime);
            warningRecordResponse.setNumber(ret.size());
            warningRecordResponse.setWarningRecordContent(ret);
        }
        return warningRecordResponse;
    }
}
