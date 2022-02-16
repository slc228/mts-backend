package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnTemplate;
import com.sjtu.mts.Entity.FangAnWeiboUser;
import com.sjtu.mts.Repository.FangAnTemplateRepository;
import com.sjtu.mts.Repository.FanganWeiboUserRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Repository
public class FangAnTemplateDAOImpl implements FangAnTemplateDAO {
    private final FangAnTemplateRepository fangAnTemplateRepository;

    public FangAnTemplateDAOImpl(FangAnTemplateRepository fangAnTemplateRepository) {
        this.fangAnTemplateRepository = fangAnTemplateRepository;
    }


    @Override
    public void InsertFanganTemplate(long fid, String title, String version, String institution, Date time, String keylist, String text)
    {
        fangAnTemplateRepository.InsertFanganTemplate(fid,title,version,institution,time,keylist,text);
    }

    @Override
    public void UpdateFanganTemplate(int id, long fid, String title, String version, String institution, Date time, String keylist, String text)
    {
        fangAnTemplateRepository.UpdateFanganTemplate(id,fid,title,version,institution,time,keylist,text);
    }

    @Override
    public List<FangAnTemplate> findAllByFid(long fid)
    {
        return fangAnTemplateRepository.SelectFanganTemplateByFid(fid);
    };

    @Override
    public FangAnTemplate findById(int id)
    {
        return fangAnTemplateRepository.SelectFanganTemplateById(id);
    }

    @Override
    public Boolean existsById(int id)
    {
        return fangAnTemplateRepository.ExistsFanganTemplateById(id).equals(BigInteger.ONE);
    };

    @Override
    public void deleteById(int id)
    {
        fangAnTemplateRepository.DeleteFanganTemplateById(id);
    };
}
