package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnWeiboUser;
import com.sjtu.mts.Repository.FanganWeiboUserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FangAnWeiboUserDAOImpl implements FangAnWeiboUserDAO{
    private final FanganWeiboUserRepository fanganWeiboUserRepository;

    public FangAnWeiboUserDAOImpl(FanganWeiboUserRepository fanganWeiboUserRepository) {
        this.fanganWeiboUserRepository = fanganWeiboUserRepository;
    }
    @Override
    public FangAnWeiboUser save(FangAnWeiboUser fanganWeiboUser){
        return fanganWeiboUserRepository.save(fanganWeiboUser);
    };

    @Override
    public List<FangAnWeiboUser> findAllByFid(long fid){
      return fanganWeiboUserRepository.findAllByFid(fid);
    };

    @Override
    public List<FangAnWeiboUser> findAll() {
        return fanganWeiboUserRepository.findAll();
    }

    @Override
    public List<FangAnWeiboUser> findAllByFidAndWeibousernickname(long fid,String weibousernickname){
        return fanganWeiboUserRepository.findAllByFidAndWeibousernickname(fid,weibousernickname);
    };

    @Override
    public void  deleteByFidAndWeibousernickname(long fid,String weibouser){
        fanganWeiboUserRepository.deleteByFidAndWeibousernickname(fid,weibouser);
    };

    @Override
    public Boolean existsByFidAndWeibouserid(long fid, String weibouserid){
        return fanganWeiboUserRepository.existsByFidAndWeibouserid(fid,weibouserid);
    };
}
