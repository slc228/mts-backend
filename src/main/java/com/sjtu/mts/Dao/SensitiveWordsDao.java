package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.SensitiveWords;

import java.util.List;

public interface SensitiveWordsDao {

    List<SensitiveWords> findAll();

    SensitiveWords save(SensitiveWords sensitiveWords);

    List<SensitiveWords> findAllByType(String type);

    boolean existsByTypeAndWord(String type, String word);

    void deleteByTypeAndWord(String type, String word);
}
