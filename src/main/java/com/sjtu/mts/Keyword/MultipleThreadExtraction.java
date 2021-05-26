package com.sjtu.mts.Keyword;

import com.hankcs.hanlp.HanLP;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.sjtu.mts.Keyword.Wrapper.min;

public class MultipleThreadExtraction {

    private List<List<String>> sum = new ArrayList<>();
    private CyclicBarrier barrier;//障栅集合点(同步器)
    private List<String> list;
    private int threadCounts;//使用的线程数
    private String extractMethod;

    public MultipleThreadExtraction(List<String> list, int threadCounts, String extractMethod) {
        this.list = list;
        this.threadCounts = threadCounts;
        this.extractMethod = extractMethod;
    }

    public String getExtractMethod() {
        return extractMethod;
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
                if (getExtractMethod().equals("tfidf")){
                    List<String> keywordList = HanLP.extractKeyword(i, 25);
                    subSum.add(keywordList);
                }
                if (getExtractMethod().equals("textrank")){
                    List<String> keywordList = new TextRankKeyword().getKeyword("",i);
                    subSum.add(keywordList);
                }
                else {
                    List<String> keywordList = new Yake.KeywordExtractor().extract_keywords(i);
                    subSum.add(keywordList);
                }
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

    // For test
    public static void main(String[] args)
    {
        List<String> extractlist = new ArrayList<>();
        extractlist.add("我们是共产主义接班人");
        extractlist.add("毛主席是我国最伟大的领导人之一。");
        MultipleThreadExtraction countListIntegerSum=new MultipleThreadExtraction(extractlist,8, "tfidf");

        int keywordNumber = 25;
        long start = System.currentTimeMillis();
        List<List<String>> sum=countListIntegerSum.getIntegerSum();
        Map<String, Integer> wordScore = new HashMap<>();
        for (List<String> singleDocList : sum)
        {
            for (int i=0; i<singleDocList.size(); i++){
                if (!wordScore.containsKey(singleDocList.get(i))){
                    wordScore.put(singleDocList.get(i), 0);
                }
                wordScore.put(singleDocList.get(i),wordScore.get(singleDocList.get(i))+(singleDocList.size()-i));
            }
        }
        List<Map.Entry<String, Integer>> keywordList = new ArrayList<Map.Entry<String, Integer>>(wordScore.entrySet());
        Collections.sort(keywordList, new Comparator<Map.Entry<String, Integer>>()
        {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
            {
                return Integer.compare(0, o1.getValue() - o2.getValue());
            }
        });

        List<KeywordResponse> keywords = new ArrayList<>();
        for(int i=0; i<min(keywordNumber, keywordList.size()); i++)
        {
            String name = keywordList.get(i).getKey().replace(" ", "");
            Integer value = keywordList.get(i).getValue();
            keywords.add(new KeywordResponse(name, value));
        }
        for (KeywordResponse kr : keywords){
            System.out.println(kr.name);
        }
        long end = System.currentTimeMillis();
        System.out.println("关键词提取耗时：" + (end-start) + "ms");

    }
}
