package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnMaterial;
import com.sjtu.mts.Repository.FangAnMaterialRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public class FangAnMaterialDAOImpl implements FangAnMaterialDAO {
    private final FangAnMaterialRepository fangAnMaterialRepository;

    public FangAnMaterialDAOImpl(FangAnMaterialRepository fangAnMaterialRepository) {
        this.fangAnMaterialRepository = fangAnMaterialRepository;
    }

    @Override
    public void InsertFanganMaterial(long fid,String materiallib,String ids)
    {
        fangAnMaterialRepository.InsertFanganMaterial(fid,materiallib,ids);
    }

    @Override
    public void UpdateFanganMaterial(int id, long fid, String materiallib, String ids)
    {
        fangAnMaterialRepository.UpdateFanganMaterial(id,fid,materiallib,ids);
    }

    @Override
    public List<FangAnMaterial> findAllByFid(Long fid)
    {
        return fangAnMaterialRepository.SelectFanganMaterialByFid(fid);
    }

    @Override
    public FangAnMaterial findByFidAndMateriallib(long fid, String materiallib)
    {
        return fangAnMaterialRepository.findByFidAndMateriallib(fid,materiallib);
    }

    @Override
    public boolean existsByFidAndMateriallib(long fid, String materiallib)
    {
        return fangAnMaterialRepository.ExistsFanganMaterialByFidAndMateriallib(fid,materiallib).equals(BigInteger.ONE);
    }

    @Override
    public boolean existsByFid(long fid){
        return fangAnMaterialRepository.ExistsFanganMaterialByFid(fid).equals(BigInteger.ONE);
    }

    @Override
    public void deleteByFidAndMateriallib(long fid, String materiallib)
    {
        fangAnMaterialRepository.DeleteFanganMaterialByFidAndMateriallib(fid,materiallib);
    }
}
