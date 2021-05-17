package com.sjtu.mts.Entity;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 敏感词库初始化2
 *
 * @author Fu Yongrui
 *
 */
public class SensitiveWordUtil2 {
    /**
     * 敏感词集合
     */
    public static HashMap sensitiveWordMap;

    /**
     * 初始化敏感词库
     *
     * @param sensitiveWordSet 敏感词库
     */
    public static synchronized void init(Set<String> sensitiveWordSet) {
        //初始化敏感词容器，减少扩容操作
        sensitiveWordMap = new HashMap(sensitiveWordSet.size());
        for (String sensitiveWord : sensitiveWordSet) {
            sensitiveWordMap.put(sensitiveWord, sensitiveWord);
        }
    }
    /**
     * 判断文字是否包含敏感字符
     *
     * @param txt 文字
     * @return 若包含返回true，否则返回false
     */
    public static boolean contains(String txt) throws Exception {
        boolean flag = false;
        List<String> wordList =segment(txt);
        for (String word : wordList) {
            if (sensitiveWordMap.get(word) != null) {
                return true;
            }
        }
        return flag;
    }
    /**
     * 获取文字中的敏感词
     *
     * @param txt 文字
     * @return
     */
    public static JSONArray  getSensitiveWord(String txt) throws IOException {
        JSONArray result = new JSONArray();


        List<String> wordList = segment(txt);
        for (String word : wordList) {
            if (sensitiveWordMap.get(word) != null) {
                //将检测出的敏感词放入到集合中
                JSONObject object = new JSONObject();
                object.put("sw",word);
                result.appendElement(object);
            }
        }
        return result;
    }

    /**
     * 对语句进行分词
     *
     * @param text 语句
     * @return 分词后的集合
     * @throws IOException
     */
    private static List segment(String text) throws IOException {
        List<Term> list= HanLP.segment(text);
        List<String> list1 = new LinkedList<>();
        for (Term term:list){
            list1.add(term.word);
        }
        System.out.println(list1);
        return list1;
    }
}
