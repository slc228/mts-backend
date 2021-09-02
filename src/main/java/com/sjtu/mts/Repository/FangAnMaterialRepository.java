package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAnMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface FangAnMaterialRepository extends JpaRepository<FangAnMaterial, Integer> {
    List<FangAnMaterial> findAllByFid(Long fid);

    FangAnMaterial findByFidAndMateriallib(long fid, String materiallib);

    boolean existsByFidAndMateriallib(long fid, String materiallib);

    boolean existsByFid(long fid);

    @Transactional(rollbackOn = Exception.class)
    void deleteByFidAndMateriallib(long fid, String materiallib);
}
