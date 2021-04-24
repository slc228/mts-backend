package com.sjtu.mts.EventTrack;

import org.springframework.data.elasticsearch.core.query.Criteria;

import javax.persistence.criteria.CriteriaBuilder;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class IteratorNodeTool {
    Map<Integer, List<EventTreeNode>> pathMap = new HashMap<>(); //记录从根节点到所有结点的路径

    public void print(List<EventTreeNode> lst)//打印出路径
    {
        Iterator<EventTreeNode> it = lst.iterator();
        while (it.hasNext()) {
            EventTreeNode n = it.next();
            System.out.print(n.getClusterNum() + "-");
        }
        System.out.println();
    }

    public double compatibility(EventTreeNode a, EventTreeNode b) {
        List<BigDecimal> center_a = a.getCenter();
        List<BigDecimal> center_b = b.getCenter();
        BigDecimal temp1 = (center_a.get(0).subtract(center_b.get(0))).multiply((center_a.get(0).subtract(center_b.get(0))));
        BigDecimal temp2 = (center_a.get(1).subtract(center_b.get(1))).multiply((center_a.get(1).subtract(center_b.get(1))));
        BigDecimal temp3 = (center_a.get(2).subtract(center_b.get(2))).multiply((center_a.get(2).subtract(center_b.get(2))));
        BigDecimal temp4 = (center_a.get(3).subtract(center_b.get(3))).multiply((center_a.get(3).subtract(center_b.get(3))));
        BigDecimal temp5 = (center_a.get(4).subtract(center_b.get(4))).multiply((center_a.get(4).subtract(center_b.get(4))));
        return Math.sqrt((temp1.add(temp2).add(temp3).add(temp4).add(temp5)).doubleValue());
    }

    public double timePenalty(EventTreeNode a, EventTreeNode b) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date_a = sdf.parse(a.getTime());
            Date date_b = sdf.parse(b.getTime());
            long diff = date_a.getTime() - date_b.getTime();
            double days = diff / (1000.0 * 60 * 60 * 24);
            return Math.exp(days/365);
        } catch (ParseException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public void iteratorNode(EventTreeNode n, Stack<EventTreeNode> pathstack) {
        pathstack.push(n);//入栈
        List<EventTreeNode> childlist = n.getChildList();
        if (childlist.size() == 0)   //没有孩子 说明是叶子结点
        {
            List<EventTreeNode> lst = new ArrayList<>();
            Iterator<EventTreeNode> stackIt = pathstack.iterator();
            while (stackIt.hasNext()) {
                lst.add(stackIt.next());
            }
            pathMap.put(n.getClusterNum(), lst);//保存路径信息
        } else {
            List<EventTreeNode> lst = new ArrayList<>();
            Iterator<EventTreeNode> stackIt = pathstack.iterator();
            while (stackIt.hasNext()) {
                lst.add(stackIt.next());

            }
            pathMap.put(n.getClusterNum(), lst);//保存路径信息

            Iterator it = childlist.iterator();
            while (it.hasNext()) {
                EventTreeNode child = (EventTreeNode) it.next();
                iteratorNode(child, pathstack);//深度优先 进入递归
                pathstack.pop();//回溯时候出栈
            }
        }
    }

    public void addNewNodeToTree(EventTreeNode root, EventTreeNode newNode, double threshold) {
        System.out.println("Add " + newNode.getClusterNum() + " to tree");
        if (root.getChildList().size() == 0) {
            root.getChildList().add(newNode);
            return;
        }
        IteratorNodeTool tool = new IteratorNodeTool();
        for (EventTreeNode n : root.getChildList()) {
            Stack<EventTreeNode> pathstack = new Stack<>();
            tool.iteratorNode(n, pathstack);
        }

        double maxConnectionStrength = 0;
        EventTreeNode maxConnectionStrengthNode = root;

        for (Map.Entry<Integer, List<EventTreeNode>> entry : tool.pathMap.entrySet()) {
            EventTreeNode thisNode = entry.getValue().get(entry.getValue().size() - 1);
            entry.getValue().add(newNode);
            double sum = 0;
            double compatibility = 0;
            double timePenalty = 0;
            for (int i = 0; i < entry.getValue().size() - 1; i++) {
                sum += tool.compatibility(entry.getValue().get(i), entry.getValue().get(i + 1));
                if (i == entry.getValue().size() - 2) {
                    compatibility = tool.compatibility(entry.getValue().get(i), entry.getValue().get(i + 1));
                    timePenalty = tool.timePenalty(entry.getValue().get(i), entry.getValue().get(i + 1));
                }
            }
            tool.print(entry.getValue());
            double coherence = (1 / (double) (entry.getValue().size() - 1)) * sum;
            double connectionStrength = compatibility * coherence * timePenalty;
            System.out.println("compatibility: " + compatibility);
            System.out.println("coherence: " + coherence);
            System.out.println("time penalty: " + timePenalty);
            System.out.println("connection strength: " + connectionStrength);
            if (connectionStrength > maxConnectionStrength) {
                maxConnectionStrength = connectionStrength;
                maxConnectionStrengthNode = thisNode;
            }
        }
        if (maxConnectionStrength > threshold) {
            maxConnectionStrengthNode.getChildList().add(newNode);
        } else {
            root.getChildList().add(newNode);
        }
    }
}
