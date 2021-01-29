package com.sjtu.mts.Repository;

import com.sjtu.mts.Entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, String> {
    @Query(value = "select codeid from tb_da_area where cityName like concat(?1,'%')", nativeQuery = true)
    List<Integer>  findCodeidByCityName(String cityName);

    @Query(value = "select distinct codeid from tb_da_area where parentid =?1", nativeQuery = true)
    List<Integer> findCodeidByParentid(int codeid);

    @Query(value = "select cityName from tb_da_area where codeid=?1", nativeQuery = true)
    String findCityByCodeid(int codeid);

    @Query(value = "select cityName from tb_da_area where codeid like concat(?1,'%')", nativeQuery = true)
    List<String> findCityNameByCodeid(int codeid);
}
