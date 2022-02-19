package com.sjtu.mts.Service;

import com.sjtu.mts.Response.WarningRecordResponse;
import net.minidev.json.JSONObject;

import java.text.ParseException;

public interface WarningRecordService {
    WarningRecordResponse getWarningRecord(long fid, int type, String start, String end) throws ParseException;
}
