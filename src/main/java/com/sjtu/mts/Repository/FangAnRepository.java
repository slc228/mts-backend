package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FangAnRepository extends JpaRepository<FangAn, Long> {

    List<FangAn> findAllByUsername(String username);

    //@Query(value = "select * from fangan where fanganname=?1", nativeQuery = true)
    List<FangAn> findAllByFangAnname(String fangAnName);
}
