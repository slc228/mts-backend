package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.UserRights;

import javax.transaction.Transactional;

public interface UserRightsDao {
    UserRights save(UserRights userRights);

    UserRights findByUsername(String username);

    void  deleteByUsername(String username);
}
