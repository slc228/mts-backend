package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnTemplate;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.Date;
import java.util.List;

public interface FangAnTemplateDAO {
    void InsertFanganTemplate(long fid, String title, String version, String institution, Date time, String keylist, String text);

    void UpdateFanganTemplate(int id, long fid, String title, String version, String institution, Date time, String keylist, String text);

    List<FangAnTemplate> findAllByFid(long fid);

    FangAnTemplate findById(int id);

    Boolean existsById(int id);

    void deleteById(int id);
}
