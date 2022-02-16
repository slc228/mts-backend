package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.SensitiveWords;
import com.sjtu.mts.Repository.FangAnMaterialRepository;
import com.sjtu.mts.Repository.SensitiveWordsRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public class SensitiveWordsDaoImpl implements SensitiveWordsDao{
    private final SensitiveWordsRepository sensitiveWordsRepository;

    public SensitiveWordsDaoImpl(SensitiveWordsRepository sensitiveWordsRepository) {
        this.sensitiveWordsRepository = sensitiveWordsRepository;
    }

    @Override
    public List<SensitiveWords> findAll() {
        return sensitiveWordsRepository.SelectSensitivewords();
    }

    @Override
    public void InsertSensitivewords(String type, String word)
    {
        sensitiveWordsRepository.InsertSensitivewords(type, word);
    }

    @Override
    public void UpdateSensitivewords(int id, String type, String word)
    {
        sensitiveWordsRepository.UpdateSensitivewords(id, type, word);
    }

    @Override
    public List<SensitiveWords> findAllByType(String type) {
        return sensitiveWordsRepository.SelectSensitivewordsByType(type);
    }

    @Override
    public boolean existsByTypeAndWord(String type, String word) {
        return sensitiveWordsRepository.ExistsSensitivewordsByTypeAndWord(type,word).equals(BigInteger.ONE);
    }

    @Override
    public void deleteByTypeAndWord(String type, String word) {
        sensitiveWordsRepository.DeleteSensitivewordsByTypeAndWord(type,word);
    }
}
