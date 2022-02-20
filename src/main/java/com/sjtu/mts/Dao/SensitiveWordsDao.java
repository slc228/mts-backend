package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.SensitiveWords;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.List;

public interface SensitiveWordsDao {

    List<SensitiveWords> findAll();

//    SensitiveWords save(SensitiveWords sensitiveWords);

    void InsertSensitivewords(String type, String word);

    void UpdateSensitivewords(int id, String type, String word);

    List<SensitiveWords> findAllByType(String type);

    boolean existsByTypeAndWord(String type, String word);

    void deleteByTypeAndWord(String type, String word);
}
