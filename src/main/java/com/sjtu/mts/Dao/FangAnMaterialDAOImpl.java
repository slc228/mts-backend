package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnMaterial;
import com.sjtu.mts.Repository.FangAnMaterialRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FangAnMaterialDAOImpl implements FangAnMaterialDAO {
    private final FangAnMaterialRepository fangAnMaterialRepository;

    public FangAnMaterialDAOImpl(FangAnMaterialRepository fangAnMaterialRepository) {
        this.fangAnMaterialRepository = fangAnMaterialRepository;
    }

    @Override
    public FangAnMaterial save(FangAnMaterial fangAnMaterial){
        return fangAnMaterialRepository.save(fangAnMaterial);
    }

    @Override
    public FangAnMaterial findByFid(Long fid){
        return fangAnMaterialRepository.findByFid(fid);
    }

    @Override
    public boolean existsByFid(long fid){
        return fangAnMaterialRepository.existsByFid(fid);
    }
}
