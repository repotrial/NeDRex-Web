package de.exbio.reposcapeweb.communication.reponses;

import de.exbio.reposcapeweb.communication.cache.Edge;

import java.util.HashMap;
import java.util.HashSet;

public class SelectionResponse {
    private HashMap<String, HashMap<Integer, HashSet<Integer>>> edges;
    private HashMap<String, HashSet<Integer>> nodes;

    public SelectionResponse() {
        this.edges = new HashMap<>();
        this.nodes = new HashMap<>();
    }


    public HashMap<String, HashMap<Integer, HashSet<Integer>>> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<String, HashMap<Integer, HashSet<Integer>>> edges) {
        this.edges = edges;
    }

    public HashMap<String, HashSet<Integer>> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<String, HashSet<Integer>> nodes) {
        this.nodes = nodes;
    }

    public void addEdges(String type, HashSet<Edge> edges) {
        if (!this.edges.containsKey(type))
            this.edges.put(type, new HashMap<>());
        edges.forEach(e -> {
            if (!this.edges.get(type).containsKey(e.getId1()))
                this.edges.get(type).put(e.getId1(), new HashSet<>());
            this.edges.get(type).get(e.getId1()).add(e.getId2());
        });
    }

    public void addNodes(String type, HashSet<Integer> nodes) {
        if (!this.nodes.containsKey(type))
            this.nodes.put(type, nodes);
        else
            this.nodes.get(type).addAll(nodes);
    }
}
