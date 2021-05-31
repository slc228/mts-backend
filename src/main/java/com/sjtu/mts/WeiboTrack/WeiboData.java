package com.sjtu.mts.WeiboTrack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WeiboData {
    private String author;
    private String content;
    private String cflag;
    private String url;
    private String publishedDay;

    public boolean isSameWeibo(WeiboData another){
        if (!this.author.equals(another.author)){
            return false;
        }
        if (!this.content.equals(another.content)){
            return false;
        }
        return true;
    }
}
