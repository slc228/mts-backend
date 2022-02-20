package com.sjtu.mts.Repository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

import com.sjtu.mts.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;

//public interface UserRepository extends CrudRepository<User, Integer> {
//
//}
//        JpaRepository，它继承自PagingAndSortingRepository，而PagingAndSortingRepository又继承自CrudRepository。
//        每个都有自己的功能：
//
//        CrudRepository提供CRUD的功能。
//        PagingAndSortingRepository提供分页和排序功能
//        JpaRepository提供JPA相关的方法，如刷新持久化数据、批量删除。

public interface UserRepository extends JpaRepository<User, String> {


    /**
     * whether exists username
     *
     * @param username username
     * @return whether exists username
     */
    Boolean existsByUsername(String username);

    /**
     * whether exists phone
     *
     * @param phone phone
     * @return whether exists phone
     */
    Boolean existsByPhone(String phone);

    /**
     * find user by username
     *
     * @param username username
     * @return user found
     */
    User findByUsername(String username);

    /**
     * find user by phone
     *
     * @param phone phone
     * @return user found
     */
    User findByPhone(String phone);

    /**
     * delete data from database by username
     *
     * @param username username
     */
    @Transactional(rollbackOn = Exception.class)
    void deleteByUsername(String username);

    /**
     * find all user information contains the username
     * @param username username
     * @return all user that contains the username
     */
    List<User> findAllByUsernameContains(String username);

    @Query(nativeQuery = true,value = "call usp_SelectUser()")
    List<User> SelectUser();

    @Query(nativeQuery = true,value = "call usp_SelectUserByUsername(:username)")
    User SelectUserByUsername(@Param("username") String username);

    @Query(nativeQuery = true,value = "call usp_SelectUserByPhone(:phone)")
    User SelectUserByPhone(@Param("phone") String phone);

    @Query(nativeQuery = true,value = "call usp_ExistsUserByPhone(:phone)")
    BigInteger ExistsUserByPhone(@Param("phone") String phone);

    @Query(nativeQuery = true,value = "call usp_ExistsUserByUsername(:username)")
    BigInteger ExistsUserByUsername(@Param("username") String username);

    @Procedure(procedureName="usp_InsertUser")
    void InsertUser(String username, String password, String phone, String email, int projectNum,
                    String validDate, String role, int state, String eventLimiter, String sensitiveLimiter);

    @Query(nativeQuery = true,value = "call usp_UpdateUserEventLimiterByUsername(:username,:eventLimiter)")
    void UpdateUserEventLimiterByUsername(@Param("username") String username,@Param("eventLimiter") String eventLimiter);

    @Query(nativeQuery = true,value = "call usp_UpdateUserSensitiveLimiterByUsername(:username,:sensitiveLimiter)")
    void UpdateUserSensitiveLimiterByUsername(@Param("username") String username,@Param("sensitiveLimiter") String sensitiveLimiter);

    @Procedure(procedureName="usp_UpdateUserByUsername")
    void UpdateUserByUsername(String username, String password, String phone, String email, int projectNum,
                              String validDate, String role, int state, String eventLimiter, String sensitiveLimiter);

    @Procedure(procedureName="usp_DeleteUserByUsername")
    void DeleteUserByUsername(String username);
}
