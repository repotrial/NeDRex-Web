package de.exbio.reposcapeweb.communication.reponses;

import java.util.HashMap;

public class ConnectionGraph {
    private HashMap<Integer, HashMap<String,Object>> edges = new HashMap<>();
    private HashMap<Integer, HashMap<String, Object>> nodes = new HashMap<>();

    public void addEdge(String name, int id, boolean directed, int node1, int node2){
        HashMap<String,Object> edge = new HashMap<>();
        edges.put(id,edge);
        edge.put("id",id);
        edge.put("name",name);
        edge.put("directed",directed);
        edge.put("node1", node1);
        edge.put("node2",node2);
    }

    public void addNode(String name, int id){
        HashMap<String, Object> node = new HashMap<>();
        nodes.put(id,node);
        node.put("id",id);
        node.put("name",name);
    }

    public HashMap<Integer, HashMap<String, Object>> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<Integer, HashMap<String, Object>> edges) {
        this.edges = edges;
    }

    public HashMap<Integer, HashMap<String, Object>> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<Integer, HashMap<String, Object>> nodes) {
        this.nodes = nodes;
    }
}
