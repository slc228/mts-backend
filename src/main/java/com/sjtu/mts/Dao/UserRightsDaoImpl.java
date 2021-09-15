package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.UserRights;
import com.sjtu.mts.Repository.AreaRepository;
import com.sjtu.mts.Repository.FangAnRepository;
import com.sjtu.mts.Repository.UserRightsRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserRightsDaoImpl implements UserRightsDao {
    private final UserRightsRepository userRightsRepository;

    public UserRightsDaoImpl(UserRightsRepository userRightsRepository) {
        this.userRightsRepository = userRightsRepository;
    }

    @Override
    public UserRights save(UserRights userRights)
    {
        return userRightsRepository.save(userRights);
    }

    @Override
    public UserRights findByUsername(String username)
    {
        return userRightsRepository.findByUsername(username);
    }

    @Override
    public void  deleteByUsername(String username)
    {
        userRightsRepository.deleteByUsername(username);
    }
}
