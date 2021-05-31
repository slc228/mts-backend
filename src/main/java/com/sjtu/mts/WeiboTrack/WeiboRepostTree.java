package com.sjtu.mts.WeiboTrack;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WeiboRepostTree {
    private Set<WeiboRepostTree> children = new LinkedHashSet<>(); // LinkedHashSet preserves insertion order
//    @JsonIgnore
    private WeiboData data;
    private String name;

    public WeiboRepostTree(WeiboData data) {
        this.data = data;
        this.name = data.getAuthor();
    }

    public WeiboRepostTree findChild(WeiboData data) {
        for (WeiboRepostTree child: children ) {
            if (child.data.isSameWeibo(data)) {
                return child;
            }
        }
        return addChild(new WeiboRepostTree(data));
    }

    public WeiboRepostTree findChildAndAddInfo(WeiboData data) {
        for (WeiboRepostTree child: children ) {
            if (child.data.isSameWeibo(data)) {
                if (child.data.getCflag().equals("0")){
                    child.data.setCflag(data.getCflag());
                    child.data.setPublishedDay(data.getPublishedDay());
                    child.data.setUrl(data.getUrl());
                }
                return child;
            }
        }
        return addChild(new WeiboRepostTree(data));
    }

    public WeiboRepostTree addChild(WeiboRepostTree child) {
        children.add(child);
        return child;
    }
}
