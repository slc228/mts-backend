package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.BriefingFile;
import com.sjtu.mts.Repository.BriefingFileRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BriefingFileDaoImpl implements BriefingFileDao {
    private final BriefingFileRepository briefingFileRepository;

    public BriefingFileDaoImpl(BriefingFileRepository briefingFileRepository) {
        this.briefingFileRepository = briefingFileRepository;
    }

    @Override
    public void save(BriefingFile briefingFile)
    {
        briefingFileRepository.save(briefingFile);
    }

    @Override
    public List<BriefingFile> findAllByFid(long fid)
    {
        return briefingFileRepository.findAllByFid(fid);
    }

    @Override
    public boolean existsById(int id)
    {
        return briefingFileRepository.existsById(id);
    }

    @Override
    public BriefingFile findById(int id)
    {
        return briefingFileRepository.findById(id);
    }

    @Override
    public void deleteById(int id)
    {
        briefingFileRepository.deleteById(id);
    }
}
