package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.SensitiveWords;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface SensitiveWordsRepository extends JpaRepository<SensitiveWords,Integer> {
    List<SensitiveWords> findAllByType(String type);

    boolean existsByTypeAndWord(String type, String word);

    @Transactional(rollbackOn = Exception.class)
    void deleteByTypeAndWord(String type, String word);
}
