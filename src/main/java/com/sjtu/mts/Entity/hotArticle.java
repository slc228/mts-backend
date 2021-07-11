package com.sjtu.mts.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Document(indexName = "hotarticle")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class hotArticle {
    @Field(type = FieldType.Date)
    private String publishedDay;

    @Field(type = FieldType.Text)
    private String resource;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String url;

    @Field(type = FieldType.Text)
    private String heat;
}