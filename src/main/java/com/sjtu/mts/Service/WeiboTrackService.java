package com.sjtu.mts.Service;

import com.sjtu.mts.WeiboTrack.WeiboRepostTree;

public interface WeiboTrackService {
    public WeiboRepostTree trackWeibo(long fid, String startPublishedDay, String endPublishedDay);
}
