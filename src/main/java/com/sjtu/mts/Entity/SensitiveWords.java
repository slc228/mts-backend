package com.sjtu.mts.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sensitivewords")
public class SensitiveWords {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "type")
    private String type;

    @Column(name = "word")
    private String word;

    public SensitiveWords(){}
    public SensitiveWords(
            String type,
            String word
    ){
        this.type=type;
        this.word=word;
    }

    public Integer getId() {
        return id;
    }

    public String getType(){return type;}

    public void setType(String type){this.type=type;}

    public String getWord(){return word;}

    public void setWord(String word){this.word=word;}
}
