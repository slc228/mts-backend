package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAnWeiboUser;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface FanganWeiboUserRepository extends JpaRepository<FangAnWeiboUser, Integer> {

    List<FangAnWeiboUser> findAllByFid(long fid);

    List<FangAnWeiboUser> findAllByFidAndWeibousernickname(long fid, String weibousernickname);

    Boolean existsByFidAndWeibouserid(long fid, String weibouserid);

    @Transactional(rollbackOn = Exception.class)
    void deleteByFidAndWeibousernickname(long fid, String weibousernickname);
}
