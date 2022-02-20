package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnMaterial;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.List;

public interface FangAnMaterialDAO {
    void InsertFanganMaterial(long fid,String materiallib,String ids);

    void UpdateFanganMaterial(int id, long fid, String materiallib, String ids);

    List<FangAnMaterial> findAllByFid(Long fid);

    FangAnMaterial findByFidAndMateriallib(long fid, String materiallib);

    boolean existsByFidAndMateriallib(long fid, String materiallib);

    boolean existsByFid(long fid);

    void deleteByFidAndMateriallib(long fid, String materiallib);
}
