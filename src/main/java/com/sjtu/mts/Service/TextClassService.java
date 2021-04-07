package com.sjtu.mts.Service;

import net.minidev.json.JSONArray;

public interface TextClassService {

    JSONArray textClass(long fid, String startPublishedDay, String endPublishedDay);
}
