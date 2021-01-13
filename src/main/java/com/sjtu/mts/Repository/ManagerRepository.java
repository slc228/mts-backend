package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

public interface ManagerRepository extends JpaRepository<Manager, String> {
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
     * find manager by username
     *
     * @param username username
     * @return manager found
     */
    Manager findByUsername(String username);
    /**
     * find manager by phone
     *
     * @param phone phone
     * @return manager found
     */
    Manager findByPhone(String phone);

    /**
     * delete data from database by username
     *
     * @param username username
     */
    @Transactional(rollbackOn = Exception.class)
    void deleteByUsername(String username);
}
