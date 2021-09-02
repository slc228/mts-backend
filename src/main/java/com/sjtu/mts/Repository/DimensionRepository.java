package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DimensionRepository extends JpaRepository<Dimension, Integer> {
    List<Dimension> findAllByKeyIn(List<String> keys);
}
