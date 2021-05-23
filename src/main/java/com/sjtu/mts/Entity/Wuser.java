package com.sjtu.mts.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "weixinUser")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Wuser {
    @Field(type = FieldType.Text)
    private String username;
    private Integer commentNumber;
}
