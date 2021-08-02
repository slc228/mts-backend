package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnTemplate;
import com.sjtu.mts.Entity.FangAnWeiboUser;
import com.sjtu.mts.Repository.FangAnTemplateRepository;
import com.sjtu.mts.Repository.FanganWeiboUserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FangAnTemplateDAOImpl implements FangAnTemplateDAO {
    private final FangAnTemplateRepository fangAnTemplateRepository;

    public FangAnTemplateDAOImpl(FangAnTemplateRepository fangAnTemplateRepository) {
        this.fangAnTemplateRepository = fangAnTemplateRepository;
    }

    @Override
    public FangAnTemplate save(FangAnTemplate fangAnTemplate){
        return fangAnTemplateRepository.save(fangAnTemplate);
    };

    @Override
    public List<FangAnTemplate> findAllByFid(long fid)
    {
        return fangAnTemplateRepository.findAllByFid(fid);
    };

    @Override
    public Boolean existsById(int id)
    {
        return fangAnTemplateRepository.existsById(id);
    };

    @Override
    public void deleteById(int id)
    {
        fangAnTemplateRepository.deleteById(id);
    };
}
