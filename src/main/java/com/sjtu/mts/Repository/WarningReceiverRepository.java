package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.WarningReceiver;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface WarningReceiverRepository extends JpaRepository<WarningReceiver,Integer> {
    List<WarningReceiver> findAllByFid(long fid);

    boolean existsByFid(long fid);

    boolean existsByName(String name);

    boolean existsByFidAndName(long fid, String name);

    boolean existsById(int id);

    @Transactional(rollbackOn = Exception.class)
    void deleteById(int id);
}
