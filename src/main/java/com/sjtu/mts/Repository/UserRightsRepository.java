package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.UserRights;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

public interface UserRightsRepository extends JpaRepository<UserRights, String> {
    UserRights findByUsername(String username);

    @Transactional(rollbackOn = Exception.class)
    void  deleteByUsername(String username);
}
