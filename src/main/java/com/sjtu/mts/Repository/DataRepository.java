package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface DataRepository extends ElasticsearchRepository<Data, Long> {
    List<Data> findByCflag(String cflag);

}
