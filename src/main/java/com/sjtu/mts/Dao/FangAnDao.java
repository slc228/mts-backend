package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAn;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.List;

public interface FangAnDao {
    List<FangAn> findAllByUsername(String username);
    List<FangAn> findAll();

    Boolean existsByUsernameAndProgrammeName(String username,String programme);

    FangAn findByFid(long fid);
    void deleteByFid(long fid);

    /*传入方案id，返回方案查询的舆情结果
     * @author：FU Yongrui*/
    Criteria criteriaByFid(long fid);
    List<Criteria> FindCriteriasByFid(long fid);

    int InsertFangan(String username, String programme_name, int match_type,
                      String rekeyword, int rekeymatch, String rokeyword,
                      int rokeymatch, String ekeyword, int ekeymatch,
                      boolean enable_alert, String sensitiveword, int priority);

    void UpdateFangan(long fid, String username, String programme_name, int match_type,
                      String rekeyword, int rekeymatch, String rokeyword,
                      int rokeymatch, String ekeyword, int ekeymatch,
                      boolean enable_alert, String sensitiveword, int priority);

    List<FangAn> getAllFangan(int offset, int size);

    List<FangAn> getAllFanganByUsername(int offset, int size, String username);

}
