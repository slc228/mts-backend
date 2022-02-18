package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAnWarning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FangAnWarningRepository extends JpaRepository<FangAnWarning,Integer> {
    List<FangAnWarning> findAllByFid(long fid);
    boolean existsByFid(long fid);

}
