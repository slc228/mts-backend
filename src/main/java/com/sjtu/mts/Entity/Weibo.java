package com.sjtu.mts.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Document(indexName = "weibo")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Weibo {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String userid;

    @Field(type = FieldType.Text)
    private String nickname;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Text)
    private String position;

    @Field(type = FieldType.Date)
    private String publishDay;

    @Field(type = FieldType.Integer)
    private Integer like;

    @Field(type = FieldType.Integer)
    private Integer transpond;

    @Field(type = FieldType.Integer)
    private Integer comment;

    @Field(type = FieldType.Text)
    private String tool;
}