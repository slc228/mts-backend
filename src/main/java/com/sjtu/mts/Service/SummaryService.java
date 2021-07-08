package com.sjtu.mts.Service;

import net.minidev.json.JSONObject;

public interface SummaryService {
    JSONObject multiSummary(long fid, String startPublishedDay, String endPublishedDay);

    JSONObject singleSummary(String document);
}
