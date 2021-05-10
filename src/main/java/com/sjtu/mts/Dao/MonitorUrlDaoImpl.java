package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.MonitorUrl;
import com.sjtu.mts.Repository.MonitorUrlRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MonitorUrlDaoImpl implements MonitorUrlDao {

    private final MonitorUrlRepository monitorUrlRepository;

    public MonitorUrlDaoImpl(MonitorUrlRepository monitorUrlRepository){
        this.monitorUrlRepository = monitorUrlRepository;
    }
    @Override
    public  MonitorUrl save(MonitorUrl monitorUrl){
        return monitorUrlRepository.save(monitorUrl);
    }
    @Override
    public List<MonitorUrl> findAll(){
        return monitorUrlRepository.findAll();
    }

    @Override
    public void deleteById(long id){
        monitorUrlRepository.deleteById(id);
    }
}
