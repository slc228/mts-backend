package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.BriefingFile;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface BriefingFileRepository extends JpaRepository<BriefingFile,Integer> {
    List<BriefingFile> findAllByFidOrderByGeneratetimeDesc(long fid);

    List<BriefingFile> findAllByFid(long fid);

    BriefingFile findById(int id);

    boolean existsById(int id);

    @Transactional(rollbackOn = Exception.class)
    void deleteById(int id);
}
