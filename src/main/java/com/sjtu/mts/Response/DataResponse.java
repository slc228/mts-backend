package com.sjtu.mts.Response;

import com.sjtu.mts.Entity.Data;
import com.sjtu.mts.Entity.YuQing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DataResponse {
    long hitNumber;

    List<Data> dataContent;
}
