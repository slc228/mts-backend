package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.MonitorUrl;

import java.util.List;

public interface MonitorUrlDao {
    MonitorUrl save(MonitorUrl monitorUrl);
    List<MonitorUrl> findAll();
    void deleteById(long id);
}
