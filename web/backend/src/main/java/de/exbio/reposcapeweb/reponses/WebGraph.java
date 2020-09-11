package de.exbio.reposcapeweb.reponses;

import java.util.LinkedList;

public class WebGraph {
    String message;
    LinkedList<WebNode> nodes = new LinkedList<>();

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
}
