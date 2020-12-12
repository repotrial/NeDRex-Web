package de.exbio.reposcapeweb.communication.jobs;

import java.util.HashMap;

public class JobResult {


    private HashMap<Integer, HashMap<String,Object>> nodes;
    private HashMap<Integer,HashMap<Integer, HashMap<String,Object>>> edges;


    public JobResult(){
        nodes = new HashMap<>();
        edges = new HashMap<>();
    }


    public HashMap<Integer, HashMap<String, Object>> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<Integer, HashMap<String, Object>> nodes) {
        this.nodes = nodes;
    }

    public HashMap<Integer, HashMap<Integer, HashMap<String, Object>>> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<Integer, HashMap<Integer, HashMap<String, Object>>> edges) {
        this.edges = edges;
    }
}
