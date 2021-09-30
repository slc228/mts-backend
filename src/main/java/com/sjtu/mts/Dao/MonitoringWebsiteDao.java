package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.MonitoringWebsite;

import javax.transaction.Transactional;
import java.util.List;

public interface MonitoringWebsiteDao {
    MonitoringWebsite save(MonitoringWebsite monitoringWebsite);

    List<MonitoringWebsite> findAll();

    MonitoringWebsite findByName(String name);

    void  deleteByName(String name);
}
