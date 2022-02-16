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
    public void InsertUserRights(String username, boolean dataScreen, boolean schemeConfiguration,
                                 boolean globalSearch, boolean analysis, boolean warning,
                                 boolean briefing, boolean userRole, boolean sensitiveWords)
    {
        userRightsRepository.InsertUserRights(username, dataScreen, schemeConfiguration, globalSearch, analysis, warning, briefing, userRole, sensitiveWords);
    }

    @Override
    public void UpdateUserRights(String username, boolean dataScreen, boolean schemeConfiguration,
                          boolean globalSearch, boolean analysis, boolean warning,
                          boolean briefing, boolean userRole, boolean sensitiveWords)
    {
        userRightsRepository.UpdateUserRights(username, dataScreen, schemeConfiguration, globalSearch, analysis, warning, briefing, userRole, sensitiveWords);
    }

    @Override
    public UserRights findByUsername(String username)
    {
        return userRightsRepository.SelectUserRightsByUsername(username);
    }

    @Override
    public void  deleteByUsername(String username)
    {
        userRightsRepository.DeleteUserRightsByUsername(username);
    }
}
