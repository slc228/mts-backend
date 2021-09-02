package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.BriefingFile;

import java.util.List;

public interface BriefingFileDao {
    void save(BriefingFile briefingFile);

    List<BriefingFile> findAllByFidOrderByGeneratetimeDesc(long fid);

    List<BriefingFile> findAllByFid(long fid);

    boolean existsById(int id);

    BriefingFile findById(int id);

    void deleteById(int id);
}
