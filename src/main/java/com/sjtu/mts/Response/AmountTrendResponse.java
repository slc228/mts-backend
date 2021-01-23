package com.sjtu.mts.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AmountTrendResponse {
    List<String> timeRange;

    List<Long> totalAmountTrend;

    List<Long> fromTypeAmountTrend1;

    List<Long> fromTypeAmountTrend2;

    List<Long> fromTypeAmountTrend3;

    List<Long> fromTypeAmountTrend4;

    List<Long> fromTypeAmountTrend5;

    List<Long> fromTypeAmountTrend6;

    List<Long> fromTypeAmountTrend7;
}
