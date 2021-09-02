package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.SensitiveWords;
import com.sjtu.mts.Repository.FangAnMaterialRepository;
import com.sjtu.mts.Repository.SensitiveWordsRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SensitiveWordsDaoImpl implements SensitiveWordsDao{
    private final SensitiveWordsRepository sensitiveWordsRepository;

    public SensitiveWordsDaoImpl(SensitiveWordsRepository sensitiveWordsRepository) {
        this.sensitiveWordsRepository = sensitiveWordsRepository;
    }

    @Override
    public List<SensitiveWords> findAll() {
        return sensitiveWordsRepository.findAll();
    }

    @Override
    public SensitiveWords save(SensitiveWords sensitiveWords) {
        return sensitiveWordsRepository.save(sensitiveWords);
    }

    @Override
    public List<SensitiveWords> findAllByType(String type) {
        return sensitiveWordsRepository.findAllByType(type);
    }

    @Override
    public boolean existsByTypeAndWord(String type, String word) {
        return sensitiveWordsRepository.existsByTypeAndWord(type,word);
    }

    @Override
    public void deleteByTypeAndWord(String type, String word) {
        sensitiveWordsRepository.deleteByTypeAndWord(type,word);
    }
}
