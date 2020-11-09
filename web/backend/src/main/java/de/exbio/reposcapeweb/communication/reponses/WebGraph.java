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
    LinkedList<WebNode> nodes = new LinkedList<>();
    LinkedList<WebEdge> edges = new LinkedList<>();
    HashMap<String,Object> colorMap;

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

    public LinkedList<WebNode> getNodes() {
        return nodes;
    }

    public void setNodes(LinkedList<WebNode> nodes) {
        this.nodes = nodes;
    }

    public void addEdge(WebEdge e){
        edges.add(e);
    }

    public void addEdges(Collection<WebEdge> edges){
        this.edges.addAll(edges);
    }

    public LinkedList<WebEdge> getEdges() {
        return edges;
    }

    public void setEdges(LinkedList<WebEdge> edges) {
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
}
