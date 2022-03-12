package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.UserUUidQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;

public interface UserUUidQueueRepository extends JpaRepository<UserUUidQueue, String> {

    @Query(nativeQuery = true,value = "call usp_SelectUserUUidByUuid(:uuid)")
    UserUUidQueue SelectUserUUidByUuid(@Param("uuid") String uuid);

    @Procedure(procedureName="usp_InsertUserUuid")
    void InsertUserUuid(String uuid, Timestamp createdTime);
}
