package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, String> {

    List<SensitiveWord>  findAll();
}
