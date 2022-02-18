package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnWarning;
import com.sjtu.mts.Repository.FangAnWarningRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FangAnWarningDaoImpl implements FangAnWarningDao {
    private final FangAnWarningRepository fangAnWarningRepository;

    public FangAnWarningDaoImpl(FangAnWarningRepository fangAnWarningRepository) {
        this.fangAnWarningRepository = fangAnWarningRepository;
    }

    @Override
    public List<FangAnWarning> findAllByFid(long fid) {
        return fangAnWarningRepository.findAllByFid(fid);
    }

    @Override
    public boolean existsByFid(long fid) {
        return fangAnWarningRepository.existsByFid(fid);
    }

    @Override
    public void save(FangAnWarning fangAnWarning) {
        fangAnWarningRepository.save(fangAnWarning);
    }
}
