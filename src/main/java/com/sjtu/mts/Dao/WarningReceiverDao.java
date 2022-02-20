package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.WarningReceiver;

import javax.transaction.Transactional;
import java.util.List;

public interface WarningReceiverDao {
    List<WarningReceiver> findAllByFid(long fid);

    boolean existsByFid(long fid);

    boolean existsByName(String name);

    boolean existsById(int id);

    boolean existsByFidAndName(long fid, String name);

    void deleteById(int id);

    void save(WarningReceiver warningReceiver);
}
