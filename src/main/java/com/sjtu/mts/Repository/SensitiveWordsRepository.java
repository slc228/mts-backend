package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.FangAnTemplate;
import com.sjtu.mts.Entity.SensitiveWords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public interface SensitiveWordsRepository extends JpaRepository<SensitiveWords,Integer> {
    List<SensitiveWords> findAllByType(String type);

    boolean existsByTypeAndWord(String type, String word);

    @Transactional(rollbackOn = Exception.class)
    void deleteByTypeAndWord(String type, String word);

    @Query(nativeQuery = true,value = "call usp_SelectSensitivewords()")
    List<SensitiveWords> SelectSensitivewords();

    @Query(nativeQuery = true,value = "call usp_SelectSensitivewordsByType(:type)")
    List<SensitiveWords> SelectSensitivewordsByType(@Param("type") String type);

    @Query(nativeQuery = true,value = "call usp_ExistsSensitivewordsByTypeAndWord(:type,:word)")
    BigInteger ExistsSensitivewordsByTypeAndWord(@Param("type") String type, @Param("word") String word);

    @Procedure(procedureName="usp_InsertSensitivewords")
    void InsertSensitivewords(String type, String word);

    @Procedure(procedureName="usp_UpdateSensitivewords")
    void UpdateSensitivewords(int id, String type, String word);

    @Procedure(procedureName="usp_DeleteSensitivewordsByTypeAndWord")
    void DeleteSensitivewordsByTypeAndWord(String type, String word);
}
