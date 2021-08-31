package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.Dimension;

import java.util.List;

public interface DimensionDao {
    List<Dimension> findAllByKeyIn(List<String> keys);
}
