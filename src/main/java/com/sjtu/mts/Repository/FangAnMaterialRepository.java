package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAn;
import com.sjtu.mts.Entity.FangAnMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;

public interface FangAnMaterialRepository extends JpaRepository<FangAnMaterial, Integer> {
    List<FangAnMaterial> findAllByFid(Long fid);

    FangAnMaterial findByFidAndMateriallib(long fid, String materiallib);

    boolean existsByFidAndMateriallib(long fid, String materiallib);

    boolean existsByFid(long fid);

    @Transactional(rollbackOn = Exception.class)
    void deleteByFidAndMateriallib(long fid, String materiallib);


    @Query(nativeQuery = true,value = "call usp_SelectFanganMaterialByFid(:fid)")
    List<FangAnMaterial> SelectFanganMaterialByFid(@Param("fid") long fid);

    @Query(nativeQuery = true,value = "call usp_SelectFanganMaterialByFidAndMateriallib(:fid,:materiallib)")
    FangAnMaterial SelectFanganMaterialByFidAndMateriallib(@Param("fid") long fid, @Param("materiallib") String materiallib);

    @Query(nativeQuery = true,value = "call usp_ExistsFanganMaterialByFid(:fid)")
    BigInteger ExistsFanganMaterialByFid(@Param("fid") long fid);

    @Query(nativeQuery = true,value = "call usp_ExistsFanganMaterialByFidAndMateriallib(:fid,:materiallib)")
    BigInteger ExistsFanganMaterialByFidAndMateriallib(@Param("fid") long fid, @Param("materiallib") String materiallib);

    @Procedure(procedureName="usp_InsertFanganMaterial")
    void InsertFanganMaterial(long fid,String materiallib,String ids);

    @Procedure(procedureName="usp_UpdateFanganMaterial")
    void UpdateFanganMaterial(int id, long fid, String materiallib, String ids);

    @Procedure(procedureName="usp_DeleteFanganMaterialByFidAndMateriallib")
    void DeleteFanganMaterialByFidAndMateriallib(long fid, String materiallib);
}
