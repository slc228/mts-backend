package com.sjtu.mts.Dao;

import com.sjtu.mts.Entity.BriefingFile;
import com.sjtu.mts.Repository.BriefingFileRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Repository
public class BriefingFileDaoImpl implements BriefingFileDao {
    private final BriefingFileRepository briefingFileRepository;

    public BriefingFileDaoImpl(BriefingFileRepository briefingFileRepository) {
        this.briefingFileRepository = briefingFileRepository;
    }

    @Override
    public void InsertBriefingFile(long fid, String name, Date generatetime, byte[] pdf, byte[] word, byte[] excel, int percent)
    {
        briefingFileRepository.InsertBriefingFile(fid, name, generatetime, pdf, word, excel, percent);
    }

    @Override
    public void UpdateBriefingFile(int id, long fid, String name, Date generatetime, byte[] pdf, byte[] word, byte[] excel, int percent)
    {
        briefingFileRepository.UpdateBriefingFile(id, fid, name, generatetime, pdf, word, excel, percent);
    }

    @Override
    public List<BriefingFile> findAllByFidOrderByGeneratetimeDesc(long fid) {
        return briefingFileRepository.SelectBriefingFileByFidOrderByGeneratetimeDesc(fid);
    }

    @Override
    public List<BriefingFile> findAllByFid(long fid)
    {
        return briefingFileRepository.SelectBriefingFileByFid(fid);
    }

    @Override
    public boolean existsById(int id)
    {
        return briefingFileRepository.ExistsBriefingFileById(id).equals(BigInteger.ONE);
    }

    @Override
    public BriefingFile findById(int id)
    {
        return briefingFileRepository.SelectBriefingFileById(id);
    }

    @Override
    public void deleteById(int id)
    {
        briefingFileRepository.DeleteBriefingFileById(id);
    }
}
