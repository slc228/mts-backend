package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAnMaterial;
import com.sjtu.mts.Entity.FangAnTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public interface FangAnTemplateRepository extends JpaRepository<FangAnTemplate, Integer> {
    List<FangAnTemplate> findAllByFid(long fid);

    Boolean existsById(int id);

    FangAnTemplate findById(int id);

    @Transactional(rollbackOn = Exception.class)
    void deleteById(int id);

    @Query(nativeQuery = true,value = "call usp_SelectFanganTemplateByFid(:fid)")
    List<FangAnTemplate> SelectFanganTemplateByFid(@Param("fid") long fid);

    @Query(nativeQuery = true,value = "call usp_SelectFanganTemplateById(:id)")
    FangAnTemplate SelectFanganTemplateById(@Param("id") int id);

    @Query(nativeQuery = true,value = "call usp_ExistsFanganTemplateById(:id)")
    BigInteger ExistsFanganTemplateById(@Param("id") int id);

    @Procedure(procedureName="usp_InsertFanganTemplate")
    void InsertFanganTemplate(long fid, String title, String version, String institution, Date time, String keylist, String text);

    @Procedure(procedureName="usp_UpdateFanganTemplate")
    void UpdateFanganTemplate(int id, long fid, String title, String version, String institution, Date time, String keylist, String text);

    @Procedure(procedureName="usp_DeleteFanganTemplateById")
    void DeleteFanganTemplateById(int id);
}
