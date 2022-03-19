package com.sjtu.mts.Dao;



import com.sjtu.mts.Entity.FangAnCloud;

import java.util.List;

public interface FangAnCloudDao {
    List<FangAnCloud> SelectFanganCloudByFid(long fid);

    void InsertFanganCloud(long fid, String cloudWord);
}
