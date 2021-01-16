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

    List<Long> fromType1AmountTrend;

    List<Long> fromType2AmountTrend;

    List<Long> fromType3AmountTrend;

    List<Long> fromType4AmountTrend;

    List<Long> fromType5AmountTrend;

    List<Long> fromType6AmountTrend;

    List<Long> fromType7AmountTrend;
}
