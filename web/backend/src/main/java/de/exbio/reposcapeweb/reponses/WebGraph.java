package de.exbio.reposcapeweb.reponses;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class WebGraph {
    String message;
    LinkedList<WebNode> nodes = new LinkedList<>();
    LinkedList<WebEdge> edges = new LinkedList<>();
    int radiusFactor = 20;
//    int y = 1;
//    int x = 0;
    int f = 10;

    public WebGraph(){
    }

    public WebGraph(String message){
        setMessage(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
            n.setX(radius*Math.cos(next.get()));
            n.setY(radius*Math.sin(next.get()));
            next.updateAndGet(v -> (v + rad));
        });
    }

    public void drawDoubleCircular(){
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
    }
}
