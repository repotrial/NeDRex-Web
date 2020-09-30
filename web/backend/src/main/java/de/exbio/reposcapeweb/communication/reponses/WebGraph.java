package de.exbio.reposcapeweb.communication.reponses;

import de.exbio.reposcapeweb.communication.controller.RequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class WebGraph {
    private final Logger log = LoggerFactory.getLogger(WebGraph.class);
    //TODO map with edge directions?
    boolean directed =false;
    String title;
    LinkedList<WebNode> nodes = new LinkedList<>();
    LinkedList<WebEdge> edges = new LinkedList<>();
    private int radiusFactor = 10;
//    int y = 1;
//    int x = 0;
    private int f = 10;

    public WebGraph(){
    }

    public WebGraph(String title, boolean directed){
        this.title = title;
        this.directed=directed;
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

    public LinkedList<WebEdge> getEdges() {
        return edges;
    }

    public void setEdges(LinkedList<WebEdge> edges) {
        this.edges = edges;
    }


    public void drawCircular(){

       drawCircular(nodes,radiusFactor);
    }

    private void drawCircular(List<WebNode> nodes,double radiusFactor){
        double rad = (2*Math.PI)/nodes.size();
        AtomicReference<Double> next = new AtomicReference<>(0.0);
        double radius = radiusFactor*nodes.size();
        nodes.forEach(n->{
//            n.setX(radius*Math.cos(next.get()));
//            n.setY(radius*Math.sin(next.get()));
            next.updateAndGet(v -> (v + rad));
        });
    }

    public void drawDoubleCircular(){
        log.debug("Layouting: start");
        LinkedList<WebNode> connectedNodes = new LinkedList<>();
        LinkedList<WebNode> isolatedNodes = new LinkedList<>();
        nodes.forEach(n -> {
            if(n.hasEdge)
                connectedNodes.add(n);
            else
                isolatedNodes.add(n);

        });
        drawCircular(connectedNodes,radiusFactor);
        drawCircular(isolatedNodes,radiusFactor*0.4);
        log.debug("Layouting: finished!");
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
}
