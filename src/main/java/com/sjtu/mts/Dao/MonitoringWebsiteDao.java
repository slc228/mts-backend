package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.MonitoringWebsite;

import javax.transaction.Transactional;

public interface MonitoringWebsiteDao {
    MonitoringWebsite save(MonitoringWebsite monitoringWebsite);

    MonitoringWebsite findByName(String name);

    void  deleteByName(String name);
}
