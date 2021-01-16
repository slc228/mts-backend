package com.sjtu.mts.Service;

import com.sjtu.mts.Response.DataResponse;
import com.sjtu.mts.Response.ResourceCountResponse;

public interface SearchService {

    public DataResponse Search(String keyword, String cflag, String startPublishedDay, String endPublishedDay,
                               String fromType, int page, int pageSize, int timeOrder);

    public ResourceCountResponse globalSearchResourceCount(String keyword, String startPublishedDay,
                                                           String endPublishedDay);
}
