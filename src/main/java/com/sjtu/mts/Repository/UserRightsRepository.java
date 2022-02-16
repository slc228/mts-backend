package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAnMaterial;
import com.sjtu.mts.Entity.UserRights;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;

public interface UserRightsRepository extends JpaRepository<UserRights, String> {
    UserRights findByUsername(String username);

    @Transactional(rollbackOn = Exception.class)
    void  deleteByUsername(String username);

    @Query(nativeQuery = true,value = "call usp_SelectUserRightsByUsername(:username)")
    UserRights SelectUserRightsByUsername(@Param("username") String username);

    @Procedure(procedureName="usp_InsertUserRights")
    void InsertUserRights(String username, boolean dataScreen, boolean schemeConfiguration,
                          boolean globalSearch, boolean analysis, boolean warning,
                          boolean briefing, boolean userRole, boolean sensitiveWords);

    @Procedure(procedureName="usp_UpdateUserRights")
    void UpdateUserRights(String username, boolean dataScreen, boolean schemeConfiguration,
                          boolean globalSearch, boolean analysis, boolean warning,
                          boolean briefing, boolean userRole, boolean sensitiveWords);

    @Procedure(procedureName="usp_DeleteUserRightsByUsername")
    void DeleteUserRightsByUsername(String username);
}
