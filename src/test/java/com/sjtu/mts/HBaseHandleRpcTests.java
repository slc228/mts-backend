package com.sjtu.mts;

import com.sjtu.mts.Dao.ElasticSearchDao;
import com.sjtu.mts.Entity.YuQing;
import com.sjtu.mts.Expression.ElasticSearchExpression;
import com.sjtu.mts.rpc.HBaseHandleRpc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class HBaseHandleRpcTests {

    @Autowired
    private HBaseHandleRpc hBaseHandleRpc;

    @Autowired
    private ElasticSearchDao elasticSearchDao;

    @Test
    void simpleTest() {
        YuQing yuQing = hBaseHandleRpc.GetYuqing("https://www.xiaohongshu.com/discovery/item/61432561000000000102ba54");
        System.out.println(yuQing.toString());
    }

    @Test
    void findByTitleAndContentTest() {
        ElasticSearchExpression expression = new ElasticSearchExpression();
        expression.JoinTitleAndContentCriteria(new ArrayList<>(Arrays.asList("台山", "核电"))).Finished();
        List<YuQing> yuQingList = elasticSearchDao.findByExpression(expression);
        System.out.println(yuQingList);
    }

    @Test
    void findByPublishedDayTest() {
        ElasticSearchExpression expression = new ElasticSearchExpression();
        expression.JoinPublishedDayCriteria("2021-09-28 01:00:00", "2021-09-29 01:00:00").Finished();
        List<YuQing> yuQingList = elasticSearchDao.findByExpression(expression);
        System.out.println(yuQingList);
    }

    @Test
    void findByFidTest() {
        ElasticSearchExpression expression = new ElasticSearchExpression();
        expression.JoinFidCriteria(41).Finished();
        List<YuQing> yuQingList = elasticSearchDao.findByExpression(expression);
        System.out.println(yuQingList);
    }

    @Test
    void findByKeywordTest() {
        ElasticSearchExpression expression = new ElasticSearchExpression();
        expression.JoinKeywordCriteria("台山").Finished();
        List<YuQing> yuQingList = elasticSearchDao.findByExpression(expression);
        System.out.println(yuQingList);
    }

    @Test
    void findBySenderTest() {
        ElasticSearchExpression expression = new ElasticSearchExpression();
        expression.JoinSenderCriteria("测试").Finished();
        List<YuQing> yuQingList = elasticSearchDao.findByExpression(expression);
        System.out.println(yuQingList);
    }

    @Test
    void findByResourceTest() {
        ElasticSearchExpression expression = new ElasticSearchExpression();
        expression.JoinResourceCriteria("凤凰网").Finished();
        List<YuQing> yuQingList = elasticSearchDao.findByExpression(expression);
        System.out.println(yuQingList);
    }

    @Test
    void findByTagTest() {
        ElasticSearchExpression expression = new ElasticSearchExpression();
        expression.JoinTagCriteria("社会").Finished();
        List<YuQing> yuQingList = elasticSearchDao.findByExpression(expression);
        System.out.println(yuQingList);
    }

    @Test
    void findByEmotionTest() {
        ElasticSearchExpression expression = new ElasticSearchExpression();
        expression.JoinEmotionCriteria("neutral").Finished();
        List<YuQing> yuQingList = elasticSearchDao.findByExpression(expression);
        System.out.println(yuQingList);
    }

    @Test
    void findBySensitiveTypeTest() {
        ElasticSearchExpression expression = new ElasticSearchExpression();
        expression.JoinSensitiveTypeCriteria("政治敏感 ").Finished();
        List<YuQing> yuQingList = elasticSearchDao.findByExpression(expression);
        System.out.println(yuQingList);
    }

    @Test
    void findByMultiCriteriaTest() {
        ElasticSearchExpression expression = new ElasticSearchExpression();
        expression
                .JoinTitleAndContentCriteria(new ArrayList<>(Arrays.asList("台山", "核电")))
                .JoinPublishedDayCriteria("2021-09-28 01:00:00", "2021-09-29 01:00:00")
                .Finished();
        List<YuQing> yuQingList = elasticSearchDao.findByExpression(expression);
        System.out.println(yuQingList);
    }
}
