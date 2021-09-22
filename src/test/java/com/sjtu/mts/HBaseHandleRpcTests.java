package com.sjtu.mts;

import com.sjtu.mts.Entity.YuQing;
import com.sjtu.mts.rpc.HBaseHandleRpc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HBaseHandleRpcTests {

    @Autowired
    private HBaseHandleRpc hBaseHandleRpc;

    @Test
    void simpleTest() {
        YuQing yuQing = hBaseHandleRpc.GetYuqing("https://www.xiaohongshu.com/discovery/item/61432561000000000102ba54");
        System.out.println(yuQing.toString());
    }
}
