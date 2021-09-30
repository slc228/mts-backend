package com.sjtu.mts.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Document(indexName = "yuqing_version2")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Data {
    @Id
    @Field(type = FieldType.Text)
    private String id;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Text)
    private String cflag;

    @Field(type = FieldType.Date)
    private String publishedDay;

    @Field(type = FieldType.Keyword)
    private String resource;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String webpageUrl;

    @Field(type = FieldType.Text)
    private String fromType;

    @Field(type = FieldType.Date)
    private String captureTime;

    @Field(type = FieldType.Date)
    private String published;

    @Field(type = FieldType.Long)
    private Long _version_;

    @Field(type = FieldType.Text)
    private String tag;

    @Field(type = FieldType.Text)
    private String emotion;

    @Field(type = FieldType.Text)
    private String sensitiveType;
}

