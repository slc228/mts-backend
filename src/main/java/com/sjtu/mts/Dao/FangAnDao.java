package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAn;

import java.util.List;

public interface FangAnDao {
    FangAn save(FangAn fangAn);
    List<FangAn> findAllByUsername(String username);

    List<FangAn> findByFangAnName(String fangAnName);
}
