package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.WarningReceiver;
import com.sjtu.mts.Response.WarningReceiverResponse;
import net.minidev.json.JSONObject;

import java.util.List;

public interface WarningReceiverService {
    WarningReceiverResponse getAllWarningReceiver(long fid);
    JSONObject addWarningReceiver(long fid, String name, String phone, String email, String wechat);
    JSONObject deleteWarningReceiver(int id);
}
