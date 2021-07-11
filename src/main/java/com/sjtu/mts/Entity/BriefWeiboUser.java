package com.sjtu.mts.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "brief_weibo_user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BriefWeiboUser {
    @Field(type = FieldType.Text)
    private String uri;

    @Field(type = FieldType.Text)
    private String nickname;
}
