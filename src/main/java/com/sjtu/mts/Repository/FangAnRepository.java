package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAn;
import com.sjtu.mts.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;

public interface FangAnRepository extends JpaRepository<FangAn, Long> {

    List<FangAn> findAll();

    List<FangAn> findAllByUsername(String username);

    Boolean existsByUsernameAndProgrammeName(String username,String programmeName);

    FangAn findByFid(long fid);

    @Transactional(rollbackOn = Exception.class)
    void  deleteByFid(long fid);

    @Query(nativeQuery = true,value = "call usp_SelectFangan()")
    List<FangAn> SelectFangan();

    @Query(nativeQuery = true,value = "call usp_SelectFanganByUsername(:username)")
    List<FangAn> SelectFanganByUsername(@Param("username") String username);

    @Query(nativeQuery = true,value = "call usp_SelectFanganByFid(:fid)")
    FangAn SelectFanganByFid(@Param("fid") long fid);

    @Query(nativeQuery = true,value = "call usp_ExistsFanganByUsernameAndProgrammeName(:username,:programmeName)")
    BigInteger ExistsFanganByUsernameAndProgrammeName(@Param("username") String username, @Param("programmeName") String programmeName);

    @Query(nativeQuery = true,value = "call usp_InsertFangan(:username,:programmeName,:matchType,:rekeyword,:rekeymatch,:rokeyword,:rokeymatch,:ekeyword,:ekeymatch,:enableAlert,:sensitiveword,:priority)")
    int InsertFangan(@Param("username") String username, @Param("programmeName") String programme_name, @Param("matchType") int match_type,
                     @Param("rekeyword") String rekeyword, @Param("rekeymatch") int rekeymatch, @Param("rokeyword") String rokeyword,
                     @Param("rokeymatch") int rokeymatch, @Param("ekeyword") String ekeyword, @Param("ekeymatch") int ekeymatch,
                     @Param("enableAlert") boolean enable_alert, @Param("sensitiveword") String sensitiveword, @Param("priority") int priority);

    @Procedure(procedureName="usp_UpdateFangan")
    void UpdateFangan(long fid, String username, String programme_name, int match_type,
                    String rekeyword, int rekeymatch, String rokeyword,
                    int rokeymatch, String ekeyword, int ekeymatch,
                    boolean enable_alert, String sensitiveword, int priority);

    @Procedure(procedureName="usp_DeleteFanganByFid")
    void DeleteFanganByFid(long fid);
}
