package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.Data;

import java.util.List;

public interface SearchService {

    public List<Data> Search(String keyword, String cflag, String startPublishedDay, String endPublishedDay, String fromType);
}
