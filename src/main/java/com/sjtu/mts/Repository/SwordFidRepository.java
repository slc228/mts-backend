package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.SensitiveWordForFid;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

public interface SwordFidRepository extends JpaRepository<SensitiveWordForFid, Long>  {

    Boolean existsByFid(long fid);
    SensitiveWordForFid findByFid(long fid);
    @Transactional(rollbackOn = Exception.class)
    void deleteByFid(long fid);
}
