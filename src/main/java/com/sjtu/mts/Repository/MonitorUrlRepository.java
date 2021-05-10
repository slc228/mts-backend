package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.MonitorUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface MonitorUrlRepository extends JpaRepository<MonitorUrl, Long> {

    List<MonitorUrl> findAll();

    @Transactional(rollbackOn = Exception.class)
    void  deleteById(long id);
}
