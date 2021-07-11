package com.sjtu.mts.Response;

import com.sjtu.mts.Entity.hotArticle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HotArticleResponse {
    long hitNumber;

    List<hotArticle> hotArticleContent;
}
