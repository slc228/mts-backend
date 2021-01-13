package com.sjtu.mts.Repository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

import com.sjtu.mts.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
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
}
