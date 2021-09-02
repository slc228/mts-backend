package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAnTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface FangAnTemplateRepository extends JpaRepository<FangAnTemplate, Integer> {
    List<FangAnTemplate> findAllByFid(long fid);

    Boolean existsById(int id);

    FangAnTemplate findById(int id);

    @Transactional(rollbackOn = Exception.class)
    void deleteById(int id);
}
