package com.sjtu.mts.Response;

import com.sjtu.mts.Entity.Video;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VideoResponse {
    long hitNumber;

    List<Video> dataContent;
}
