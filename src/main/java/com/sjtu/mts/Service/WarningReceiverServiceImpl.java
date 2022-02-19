package com.sjtu.mts.Service;

import com.sjtu.mts.Dao.WarningReceiverDao;
import com.sjtu.mts.Entity.WarningReceiver;
import com.sjtu.mts.Response.WarningReceiverResponse;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarningReceiverServiceImpl implements WarningReceiverService {
    @Autowired
    private WarningReceiverDao warningReceiverDao;

    @Override
    public WarningReceiverResponse getAllWarningReceiver(long fid) {
        WarningReceiverResponse warningReceiverResponse=new WarningReceiverResponse();
        List<WarningReceiver> hit=warningReceiverDao.findAllByFid(fid);
        warningReceiverResponse.setNumber(hit.size());
        warningReceiverResponse.setWarningReceiverContent(hit);
        return warningReceiverResponse;
    }

    @Override
    public JSONObject addWarningReceiver(long fid, String name, String phone, String email, String wechat) {
        JSONObject ret=new JSONObject();
        if (warningReceiverDao.existsByFidAndName(fid, name))
        {
            ret.appendField("addWarningReceiver",0);
        }
        else {
            WarningReceiver warningReceiver=new WarningReceiver(fid,name,phone,email,wechat);
            warningReceiverDao.save(warningReceiver);
            ret.appendField("addWarningReceiver",1);
        }
        return ret;
    }

    @Override
    public JSONObject deleteWarningReceiver(int id) {
        JSONObject ret =new JSONObject();
        if (warningReceiverDao.existsById(id))
        {
            warningReceiverDao.deleteById(id);
            ret.appendField("deleteWarningReceiver",1);
        }else
        {
            ret.appendField("deleteWarningReceiver",0);
        }
        return ret;
    }
}
