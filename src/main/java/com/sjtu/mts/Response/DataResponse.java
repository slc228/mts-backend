package com.sjtu.mts.Response;

import com.sjtu.mts.Entity.Data;

import java.util.List;

public class DataResponse {
    long hitNumber;

    List<Data> dataContent;

    public long getHitNumber() {
        return hitNumber;
    }

    public void setHitNumber(long hitNumber) {
        this.hitNumber = hitNumber;
    }

    public List<Data> getDataContent() {
        return dataContent;
    }

    public void setDataContent(List<Data> dataContent) {
        this.dataContent = dataContent;
    }
}
