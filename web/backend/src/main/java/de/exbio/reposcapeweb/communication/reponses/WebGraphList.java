package de.exbio.reposcapeweb.communication.reponses;

import java.util.HashMap;
import java.util.LinkedList;

public class WebGraphList {
    public String id;
    public HashMap<String, HashMap<String, String[]>> attributes;
    public HashMap<String, LinkedList<HashMap<String, String>>> nodes;
    public HashMap<String, LinkedList<HashMap<String, String>>> edges;


    public WebGraphList(String id) {
        this.id = id;
        attributes = new HashMap<>();
        attributes.put("nodes", new HashMap<>());
        attributes.put("edges", new HashMap<>());
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

    public HashMap<String, LinkedList<HashMap<String, String>>> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<String, LinkedList<HashMap<String, String>>> nodes) {
        this.nodes = nodes;
    }

    public HashMap<String, LinkedList<HashMap<String, String>>> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<String, LinkedList<HashMap<String, String>>> edges) {
        this.edges = edges;
    }

    public void addNode(String type, HashMap<String, String> entry) {
        if (!nodes.containsKey(type))
            nodes.put(type, new LinkedList<>());
        nodes.get(type).add(entry);
    }

    public void addEdge(String type, HashMap<String,String> entry){
        if(!edges.containsKey(type))
            edges.put(type,new LinkedList<>());
        edges.get(type).add(entry);
    }

    public void addAttributes(String entity, String type, String[] attributes){
        this.attributes.get(entity).put(type,attributes);
    }

    public void addEdges(String type, LinkedList<HashMap<String, String>> edgesToAttributeList) {
        if(!edges.containsKey(type))
            edges.put(type,edgesToAttributeList);
        else
            edges.get(type).addAll(edgesToAttributeList);
    }

    public void addNodes(String type, LinkedList<HashMap<String, String>> nodesToAttributeList) {
        if(!nodes.containsKey(type))
            nodes.put(type,nodesToAttributeList);
        else
            nodes.get(type).addAll(nodesToAttributeList);
    }


}
