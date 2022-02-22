package com.sjtu.mts.Response;

import com.sjtu.mts.Entity.WarningRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WarningRecordResponse {
    int number;

    List<WarningRecord> warningRecordContent;
}
