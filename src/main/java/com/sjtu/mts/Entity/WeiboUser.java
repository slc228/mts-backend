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
    private String userid;

    @Field(type = FieldType.Text)
    private String nickname;

    @Field(type = FieldType.Text)
    private String user_avatar;

    @Field(type = FieldType.Text)
    private String tags;

    @Field(type = FieldType.Text)
    private String gender;

    @Field(type = FieldType.Text)
    private String location;

    @Field(type = FieldType.Text)
    private String birthday;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Text)
    private String verified_reason;

    @Field(type = FieldType.Text)
    private String talent;

    @Field(type = FieldType.Text)
    private String education;

    @Field(type = FieldType.Text)
    private String work;

    @Field(type = FieldType.Text)
    private Integer weibo_num;

    @Field(type = FieldType.Text)
    private Integer following;

    @Field(type = FieldType.Text)
    private Integer followers;
}