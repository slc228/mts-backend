package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.MonitoringWebsite;
import com.sjtu.mts.Entity.UserRights;
import com.sjtu.mts.Repository.MonitoringWebsiteRepository;
import com.sjtu.mts.Repository.UserRightsRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MonitoringWebsiteDaoImpl implements MonitoringWebsiteDao {
    private final MonitoringWebsiteRepository monitoringWebsiteRepository;

    public MonitoringWebsiteDaoImpl(MonitoringWebsiteRepository monitoringWebsiteRepository) {
        this.monitoringWebsiteRepository = monitoringWebsiteRepository;
    }

    @Override
    public MonitoringWebsite save(MonitoringWebsite monitoringWebsite)
    {
        return monitoringWebsiteRepository.save(monitoringWebsite);
    }

    @Override
    public MonitoringWebsite findByName(String name)
    {
        return monitoringWebsiteRepository.findByName(name);
    }

    @Override
    public void  deleteByName(String name)
    {
        monitoringWebsiteRepository.deleteByName(name);
    }
}
