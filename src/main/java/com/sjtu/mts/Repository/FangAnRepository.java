package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAn;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface FangAnRepository extends JpaRepository<FangAn, Long> {

    List<FangAn> findAllByUsername(String username);

    Boolean existsByUsernameAndProgrammeName(String username,String programmeName);

    FangAn findByFid(long fid);

    @Transactional(rollbackOn = Exception.class)
    void  deleteByFid(long fid);
}
