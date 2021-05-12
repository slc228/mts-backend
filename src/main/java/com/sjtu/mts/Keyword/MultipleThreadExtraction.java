package com.sjtu.mts.Keyword;

import com.hankcs.hanlp.HanLP;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultipleThreadExtraction {

    private List<List<String>> sum = new ArrayList<>();
    private CyclicBarrier barrier;//障栅集合点(同步器)
    private List<String> list;
    private int threadCounts;//使用的线程数

    public MultipleThreadExtraction(List<String> list, int threadCounts) {
        this.list = list;
        this.threadCounts = threadCounts;
    }

    /**
     * 获取List中所有整数的和
     *
     * @return
     */
    public List<List<String>> getIntegerSum() {
        ExecutorService exec = Executors.newFixedThreadPool(threadCounts);
        int len = list.size() / threadCounts;//平均分割List
        //List中的数量没有线程数多（很少存在）
        if (len == 0) {
            threadCounts = list.size();//采用一个线程处理List中的一个元素
            len = list.size() / threadCounts;//重新平均分割List
        }
        barrier = new CyclicBarrier(threadCounts + 1);
        for (int i = 0; i < threadCounts; i++) {
            //创建线程任务
            if (i == threadCounts - 1) {//最后一个线程承担剩下的所有元素的计算
                exec.execute(new SubKeywordExtractionTask(list.subList(i * len, list.size())));
            } else {
                exec.execute(new SubKeywordExtractionTask(list.subList(i * len, len * (i + 1) > list.size() ? list.size() : len * (i + 1))));
            }
        }
        try {
            barrier.await();//关键，使该线程在障栅处等待，直到所有的线程都到达障栅处
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + ":Interrupted");
        } catch (BrokenBarrierException e) {
            System.out.println(Thread.currentThread().getName() + ":BrokenBarrier");
        }
        exec.shutdown();
        return sum;
    }

    /**
     * 分割提取关键词的线程任务
     */
    public class SubKeywordExtractionTask implements Runnable {
        private List<String> subList;

        public SubKeywordExtractionTask(List<String> subList) {
            this.subList = subList;
        }

        public void run() {
            List<List<String>> subSum = new ArrayList<>();
            for (String i : subList) {
//                List<String> keywordList = HanLP.extractKeyword(i, 25);
//                List<String> keywordList = new TextRankKeyword().getKeyword("",i);
                List<String> keywordList = new Yake.KeywordExtractor().extract_keywords(i);
                subSum.add(keywordList);
            }
            synchronized (MultipleThreadExtraction.this) { //在LargeListIntegerSum对象上同步
                sum.addAll(subSum);
            }
            try {
                barrier.await();    //关键，使该线程在障栅处等待，直到所有的线程都到达障栅处
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + ":Interrupted");
            } catch (BrokenBarrierException e) {
                System.out.println(Thread.currentThread().getName() + ":BrokenBarrier");
            }
        }
    }
}
