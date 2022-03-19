package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnCloud;
import com.sjtu.mts.Repository.FangAnCloudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FangAnCloudDaoImpl implements FangAnCloudDao {
    @Autowired
    private FangAnCloudRepository fangAnCloudRepository;

    @Override
    public List<FangAnCloud> SelectFanganCloudByFid(long fid) {
        return fangAnCloudRepository.SelectFanganCloudByFid(fid);
    }

    @Override
    public void InsertFanganCloud(long fid, String cloudWord) {
        fangAnCloudRepository.InsertFanganCloud(fid, cloudWord);
    }
}
