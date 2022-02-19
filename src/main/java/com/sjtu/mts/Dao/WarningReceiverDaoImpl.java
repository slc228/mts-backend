package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.WarningReceiver;
import com.sjtu.mts.Repository.UserRightsRepository;
import com.sjtu.mts.Repository.WarningReceiverRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WarningReceiverDaoImpl implements WarningReceiverDao {
    private WarningReceiverRepository warningReceiverRepository;

    public WarningReceiverDaoImpl(WarningReceiverRepository warningReceiverRepository) {
        this.warningReceiverRepository = warningReceiverRepository;
    }

    @Override
    public List<WarningReceiver> findAllByFid(long fid) {
        return warningReceiverRepository.findAllByFid(fid);
    }

    @Override
    public boolean existsByFid(long fid) {
        return warningReceiverRepository.existsByFid(fid);
    }

    @Override
    public boolean existsByName(String name) {
        return warningReceiverRepository.existsByName(name);
    }

    @Override
    public boolean existsById(int id) {
        return warningReceiverRepository.existsById(id);
    }

    @Override
    public boolean existsByFidAndName(long fid, String name) {
        return warningReceiverRepository.existsByFidAndName(fid, name);
    }

    @Override
    public void deleteById(int id) {
        warningReceiverRepository.deleteById(id);
    }

    @Override
    public void save(WarningReceiver warningReceiver) {
        warningReceiverRepository.save(warningReceiver);
    }
}
