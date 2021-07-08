package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAn;
import org.springframework.data.elasticsearch.core.query.Criteria;

import java.util.List;

public interface FangAnDao {
    FangAn save(FangAn fangAn);
    List<FangAn> findAllByUsername(String username);

    Boolean existsByUsernameAndProgrammeName(String username,String programme);

    FangAn findByFid(long fid);
    void deleteByFid(long fid);

    /*传入方案id，返回方案查询的舆情结果
     * @author：FU Yongrui*/
    Criteria criteriaByFid(long fid);
    List<Criteria> FindCriteriasByFid(long fid);

}
