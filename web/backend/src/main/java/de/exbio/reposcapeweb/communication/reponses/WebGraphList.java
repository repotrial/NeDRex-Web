package de.exbio.reposcapeweb.communication.reponses;

import java.util.HashMap;

public class WebGraphList {
    public String id;
    public HashMap<String,HashMap<String,String[]>> attributes;
    public HashMap<String,HashMap<String,String>> nodes;
    public HashMap<String,HashMap<String,String>> edges;


    public WebGraphList(String id){
        this.id = id;
        attributes = new HashMap<>();
        attributes.put("nodes",new HashMap<>());
        attributes.put("edges",new HashMap<>());
        nodes = new HashMap<>();
        edges = new HashMap<>();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, HashMap<String, String[]>> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, HashMap<String, String[]>> attributes) {
        this.attributes = attributes;
    }

    public HashMap<String, HashMap<String, String>> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<String, HashMap<String, String>> nodes) {
        this.nodes = nodes;
    }

    public HashMap<String, HashMap<String, String>> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<String, HashMap<String, String>> edges) {
        this.edges = edges;
    }
}
