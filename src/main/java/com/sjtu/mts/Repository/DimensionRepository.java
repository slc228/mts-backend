package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.Dimension;
import com.sjtu.mts.Entity.FangAn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DimensionRepository extends JpaRepository<Dimension, Integer> {
    List<Dimension> findAllByKeyIn(List<String> keys);

    @Query(nativeQuery = true,value = "call usp_SelectDimensionByKeyIn(:keys)")
    List<Dimension> SelectDimensionByKeyIn(@Param("keys") String keys);
}
