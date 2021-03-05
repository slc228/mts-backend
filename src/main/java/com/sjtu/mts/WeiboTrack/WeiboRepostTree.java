package com.sjtu.mts.WeiboTrack;

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
    private WeiboData data;

    public WeiboRepostTree(WeiboData data) {
        this.data = data;
    }

    WeiboRepostTree findChild(WeiboData data) {
        for (WeiboRepostTree child: children ) {
            if (child.data.isSameWeibo(data)) {
                return child;
            }
        }
        return addChild(new WeiboRepostTree(data));
    }

    WeiboRepostTree addChild(WeiboRepostTree child) {
        children.add(child);
        return child;
    }
}
