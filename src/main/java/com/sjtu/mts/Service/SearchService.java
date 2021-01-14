package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.Entity.DataResponse;

import java.util.List;

public interface SearchService {

    public DataResponse Search(String keyword, String cflag, String startPublishedDay, String endPublishedDay,
                               String fromType, int page, int pageSize, int timeOrder);
}
