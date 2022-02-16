package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.BriefingFile;
import com.sjtu.mts.Entity.SensitiveWords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public interface BriefingFileRepository extends JpaRepository<BriefingFile,Integer> {
    List<BriefingFile> findAllByFidOrderByGeneratetimeDesc(long fid);

    List<BriefingFile> findAllByFid(long fid);

    BriefingFile findById(int id);

    boolean existsById(int id);

    @Transactional(rollbackOn = Exception.class)
    void deleteById(int id);

    @Query(nativeQuery = true,value = "call usp_SelectBriefingFileById()")
    BriefingFile SelectBriefingFileById(@Param("id") int id);

    @Query(nativeQuery = true,value = "call usp_SelectBriefingFileByFid(:fid)")
    List<BriefingFile> SelectBriefingFileByFid(@Param("fid") long fid);

    @Query(nativeQuery = true,value = "call usp_SelectBriefingFileByFidOrderByGeneratetimeDesc(:fid)")
    List<BriefingFile> SelectBriefingFileByFidOrderByGeneratetimeDesc(@Param("fid") long fid);

    @Query(nativeQuery = true,value = "call usp_ExistsBriefingFileById(:id)")
    BigInteger ExistsBriefingFileById(@Param("id") int id);

    @Procedure(procedureName="usp_InsertBriefingFile")
    void InsertBriefingFile(long fid, String name, Date generatetime, byte[] pdf, byte[] word, byte[] excel, int percent);

    @Procedure(procedureName="usp_UpdateBriefingFile")
    void UpdateBriefingFile(int id, long fid, String name, Date generatetime, byte[] pdf, byte[] word, byte[] excel, int percent);

    @Procedure(procedureName="usp_DeleteBriefingFileById")
    void DeleteBriefingFileById(int id);
}
