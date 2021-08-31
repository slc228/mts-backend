package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnMaterial;

import java.util.List;

public interface FangAnMaterialDAO {
    FangAnMaterial save(FangAnMaterial fangAnMaterial);

    List<FangAnMaterial> findAllByFid(Long fid);

    FangAnMaterial findByFidAndMateriallib(long fid, String materiallib);

    boolean existsByFidAndMateriallib(long fid, String materiallib);

    boolean existsByFid(long fid);

    void deleteByFidAndMateriallib(long fid, String materiallib);
}
