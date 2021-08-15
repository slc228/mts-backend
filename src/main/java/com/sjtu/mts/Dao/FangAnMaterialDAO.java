package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnMaterial;

public interface FangAnMaterialDAO {
    FangAnMaterial save(FangAnMaterial fangAnMaterial);

    FangAnMaterial findByFid(Long fid);

    boolean existsByFid(long fid);
}
