package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.BriefingFile;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.Date;
import java.util.List;

public interface BriefingFileDao {
    void save(BriefingFile briefingFile);

    int InsertBriefingFile(long fid, String name, Date generatetime, byte[] pdf, byte[] word, byte[] excel, int percent);

    void UpdateBriefingFile(int id, long fid, String name, Date generatetime, byte[] pdf, byte[] word, byte[] excel, int percent);


    List<BriefingFile> findAllByFidOrderByGeneratetimeDesc(long fid);

    List<BriefingFile> findAllByFid(long fid);

    boolean existsById(int id);

    BriefingFile findById(int id);

    void deleteById(int id);
}
