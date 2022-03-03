package com.sjtu.mts.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Document(indexName = "video")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Video {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String image;

    @Field(type = FieldType.Text)
    private String url;

    @Field(type = FieldType.Keyword)
    private String resource;

    @Field(type = FieldType.Date)
    private String publishedDate;
}
