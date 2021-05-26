package com.sjtu.mts.Service;

import com.sjtu.mts.Entity.Cluster;
import com.sjtu.mts.EventTrack.EventTreeNode;
import com.sjtu.mts.EventTrack.IteratorNodeTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventTrackServiceImpl implements EventTrackService{
    @Autowired
    private TextClassService textClassService;

    @Override
    public EventTreeNode getEventTree(long fid, String startPublishedDay, String endPublishedDay){
        List<Cluster> res = textClassService.clusteringData(fid, startPublishedDay, endPublishedDay);
        EventTreeNode root = new EventTreeNode(0, 0,
                "null", "Rootnode", null, null, new ArrayList<>());
        IteratorNodeTool tool = new IteratorNodeTool();
        for (Cluster cluster : res){
            tool.addNewNodeToTree(root, new EventTreeNode(cluster), 0.1);
        }
        return root;
    }
}
