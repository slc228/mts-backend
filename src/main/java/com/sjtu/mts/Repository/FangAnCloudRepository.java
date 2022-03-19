package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAnCloud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FangAnCloudRepository extends JpaRepository<FangAnCloud, String> {
    @Query(nativeQuery = true,value = "call usp_SelectFanganCloudByFid(:fid)")
    List<FangAnCloud> SelectFanganCloudByFid(@Param("fid") long fid);

    @Procedure(procedureName="usp_InsertFanganCloud")
    void InsertFanganCloud(long fid, String cloudWord);
}
