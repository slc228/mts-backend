package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAnMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FangAnMaterialRepository extends JpaRepository<FangAnMaterial, Integer> {
    FangAnMaterial findByFid(Long fid);

    boolean existsByFid(long fid);
}
