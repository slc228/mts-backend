package com.sjtu.mts.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Document(indexName = "weibouser")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class WeiboUser {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String nickname;
}