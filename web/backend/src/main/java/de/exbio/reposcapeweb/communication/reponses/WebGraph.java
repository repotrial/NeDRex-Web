package de.exbio.reposcapeweb.communication.reponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class WebGraph {
    private final Logger log = LoggerFactory.getLogger(WebGraph.class);
    //TODO map with edge directions?
    boolean directed =false;
    String id;
    String title;
    HashSet<WebNode> nodes = new HashSet<>();
    HashSet<WebEdge> edges = new HashSet<>();
    HashMap<String,Object> colorMap;
    HashMap<String,HashMap<String,Long>> weights = new HashMap<>();

    public WebGraph(String id){
        this.id = id;
    }

    public WebGraph(String title, boolean directed, String id){
        this.id= id;
        this.title = title;
        this.directed=directed;
    }

    public void setColorMap(HashMap<String, Object> colorMap) {
        this.colorMap = colorMap;
    }

    public void addNode(WebNode node){
        nodes.add(node);
    }

    public HashSet<WebNode> getNodes() {
        return nodes;
    }

    public void setNodes(HashSet<WebNode> nodes) {
        this.nodes = nodes;
    }

    public void addEdge(WebEdge e){
        edges.add(e);
    }

    public void addEdges(Collection<WebEdge> edges){
        this.edges.addAll(edges);
    }

    public HashSet<WebEdge> getEdges() {
        return edges;
    }

    public void setEdges(HashSet<WebEdge> edges) {
        this.edges = edges;
    }

    public HashMap<String, Object> getColorMap() {
        return colorMap;
    }


    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public void addNodes(Collection<WebNode> nodes) {
        this.nodes.addAll(nodes);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWeight(String type, String entity, Long weight) {
        if(!weights.containsKey(type))
            weights.put(type,new HashMap<>());
        weights.get(type).put(entity,weight);
    }

    public HashMap<String, HashMap<String, Long>> getWeights() {
        return weights;
    }
}
