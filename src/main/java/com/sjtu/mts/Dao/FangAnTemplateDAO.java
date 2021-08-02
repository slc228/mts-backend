package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnTemplate;

import java.util.List;

public interface FangAnTemplateDAO {
    FangAnTemplate save(FangAnTemplate fangAnTemplate);

    List<FangAnTemplate> findAllByFid(long fid);

    Boolean existsById(int id);

    void deleteById(int id);
}
