package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.MonitoringWebsite;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface MonitoringWebsiteRepository extends JpaRepository<MonitoringWebsite, Integer> {
    MonitoringWebsite findByName(String name);

    @Transactional(rollbackOn = Exception.class)
    void  deleteByName(String name);
}
