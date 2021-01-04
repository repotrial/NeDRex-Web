package de.exbio.reposcapeweb.db.history;

import de.exbio.reposcapeweb.communication.reponses.ConnectionGraph;

import java.util.HashMap;

public class GraphHistoryDetail {

    public String name;
    public String owner;
    public String method;
    public String comment;
    public HashMap<String,HashMap<String,Integer>> counts;
    public ConnectionGraph entityGraph;

    public boolean starred;
    public boolean root;
    public String parentId;
    public String parentName;
    public String parentMethod;
    public HashMap<String,HashMap<String,String>> children;

    public GraphHistoryDetail(){
        counts = new HashMap<>();
        counts.put("nodes",new HashMap<>());
        counts.put("edges",new HashMap<>());
    }

}
