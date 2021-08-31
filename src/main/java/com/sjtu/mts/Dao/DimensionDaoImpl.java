package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.Dimension;
import com.sjtu.mts.Repository.DimensionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DimensionDaoImpl implements DimensionDao {
    private final DimensionRepository dimensionRepository;

    public DimensionDaoImpl(DimensionRepository dimensionRepository) {
        this.dimensionRepository = dimensionRepository;
    }

    @Override
    public List<Dimension> findAllByKeyIn(List<String> keys)
    {
        return dimensionRepository.findAllByKeyIn(keys);
    }
}
