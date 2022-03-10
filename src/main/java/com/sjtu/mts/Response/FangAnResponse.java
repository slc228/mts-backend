package com.sjtu.mts.Response;

import com.sjtu.mts.Entity.FangAn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FangAnResponse {
    long hitNumber;

    List<FangAn> FangAnContent;
}
