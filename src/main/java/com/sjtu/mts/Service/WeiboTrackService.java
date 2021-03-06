package com.sjtu.mts.Service;

import com.sjtu.mts.WeiboTrack.WeiboRepostTree;

public interface WeiboTrackService {
    public WeiboRepostTree trackWeibo(String keyword, String startPublishedDay, String endPublishedDay);
}
