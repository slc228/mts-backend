package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.UserRights;
import org.springframework.data.jpa.repository.query.Procedure;

import javax.transaction.Transactional;

public interface UserRightsDao {
    void InsertUserRights(String username, boolean dataScreen, boolean schemeConfiguration,
                          boolean globalSearch, boolean analysis, boolean warning,
                          boolean briefing, boolean userRole, boolean sensitiveWords);

    void UpdateUserRights(String username, boolean dataScreen, boolean schemeConfiguration,
                          boolean globalSearch, boolean analysis, boolean warning,
                          boolean briefing, boolean userRole, boolean sensitiveWords);

    UserRights findByUsername(String username);

    void  deleteByUsername(String username);
}
