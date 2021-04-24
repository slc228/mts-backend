package com.sjtu.mts.Service;

import com.sjtu.mts.EventTrack.EventTreeNode;

public interface EventTrackService {
    public EventTreeNode getEventTree(long fid, String startPublishedDay, String endPublishedDay);
}
