package com.sjtu.mts.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SentimentCountResponse {
    long happy;

    long surprise;

    long sad;

    long angry;

    long fear;

    long neutral;
}

