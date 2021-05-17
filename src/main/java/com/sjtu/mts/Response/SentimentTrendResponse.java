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
public class SentimentTrendResponse {
    List<String> timeRange;

    List<Long> totalAmountTrend;

    List<Long> happyTrend;

    List<Long> surpriseTrend;

    List<Long> sadTrend;

    List<Long> angryTrend;

    List<Long> fearTrend;

    List<Long> neutralTrend;
}

