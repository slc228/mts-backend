package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.FangAnWeiboUser;

import java.util.List;
public interface FangAnWeiboUserDAO {
    FangAnWeiboUser save(FangAnWeiboUser fanganWeiboUser);
    List<FangAnWeiboUser> findAllByFid(long fid);
    List<FangAnWeiboUser> findAll();

    List<FangAnWeiboUser> findAllByFidAndWeibousernickname(long fid,String weibousernickname);

    FangAnWeiboUser findByFidAndWeibouserid(long fid,String weibouserid);

    Boolean existsByFidAndWeibouserid(long fid, String weibouserid);

    void  deleteByFidAndWeibousernickname(long fid,String weibouser);
}
