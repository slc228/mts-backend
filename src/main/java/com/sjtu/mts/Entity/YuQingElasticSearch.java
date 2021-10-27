package com.sjtu.mts.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "yuqing_version2")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class YuQingElasticSearch {
    @Id
    @Field(type = FieldType.Text)
    private String id;

    @Field(type = FieldType.Text)
    private String yuqing_url;

    @Field(type = FieldType.Integer)
    private int fid;

    @Field(type = FieldType.Text)
    private String keyword;

    @Field(type = FieldType.Text)
    private String sender;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Keyword)
    private String resource;

    @Field(type = FieldType.Date)
    private String publishedDay;

    @Field(type = FieldType.Keyword)
    private String tag;

    @Field(type = FieldType.Keyword)
    private String emotion;

    @Field(type = FieldType.Keyword)
    private String sensitiveType;


    public void setFid(Integer fid) {
        this.fid = fid;
    }
    public void setId(String id) {
        this.id = id;
    }
}
