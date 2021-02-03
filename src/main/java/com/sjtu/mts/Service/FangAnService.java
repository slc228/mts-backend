package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.FangAn;
import net.minidev.json.JSONObject;

import java.util.List;

public interface FangAnService {
    List<FangAn> findAllByUsername(String username);
    JSONObject saveFangAn(String username,String fangAnname,String fangAn);
}
