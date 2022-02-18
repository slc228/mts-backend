package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnWarning;

import java.util.List;

public interface FangAnWarningDao {
    List<FangAnWarning> findAllByFid(long fid);
    boolean existsByFid(long fid);
    void save(FangAnWarning fangAnWarning);
}
