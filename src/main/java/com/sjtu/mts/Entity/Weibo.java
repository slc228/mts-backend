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
    private String weiboid;

    @Field(type = FieldType.Text)
    private String userid;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Text)
    private String article_url;

    @Field(type = FieldType.Text)
    private String original_pictures;

    @Field(type = FieldType.Text)
    private String retweet_pictures;

    @Field(type = FieldType.Boolean)
    private Boolean original;

    @Field(type = FieldType.Text)
    private String video_url;

    @Field(type = FieldType.Text)
    private String publish_place;

    @Field(type = FieldType.Date)
    private String publish_time;

    @Field(type = FieldType.Text)
    private String publish_tool;

    @Field(type = FieldType.Integer)
    private Integer up_num;

    @Field(type = FieldType.Integer)
    private Integer retweet_num;

    @Field(type = FieldType.Integer)
    private Integer comment_num;
}