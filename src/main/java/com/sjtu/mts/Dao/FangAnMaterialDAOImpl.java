package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnMaterial;
import com.sjtu.mts.Repository.FangAnMaterialRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public List<FangAnMaterial> findAllByFid(Long fid)
    {
        return fangAnMaterialRepository.findAllByFid(fid);
    }

    @Override
    public FangAnMaterial findByFidAndMateriallib(long fid, String materiallib)
    {
        return fangAnMaterialRepository.findByFidAndMateriallib(fid,materiallib);
    }

    @Override
    public boolean existsByFidAndMateriallib(long fid, String materiallib)
    {
        return fangAnMaterialRepository.existsByFidAndMateriallib(fid,materiallib);
    }

    @Override
    public boolean existsByFid(long fid){
        return fangAnMaterialRepository.existsByFid(fid);
    }

    @Override
    public void deleteByFidAndMateriallib(long fid, String materiallib)
    {
        fangAnMaterialRepository.deleteByFidAndMateriallib(fid,materiallib);
    }
}
