package de.exbio.reposcapeweb.communication.cache;

import de.exbio.reposcapeweb.communication.controller.RequestController;
import de.exbio.reposcapeweb.communication.reponses.WebGraph;
import de.exbio.reposcapeweb.communication.reponses.WebGraphInfo;
import de.exbio.reposcapeweb.communication.reponses.WebGraphList;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

public class Graph {

    private final Logger log = LoggerFactory.getLogger(Graph.class);
    private String id;
    private HashMap<Integer, HashMap<Integer, Node>> nodes;
    // edgetype -> node1 -> node2 -> edge
//    private HashMap<Integer,HashMap<Integer,HashMap<Integer,Node>>> edges;
    private HashMap<Integer, LinkedList<Edge>> edges;
    private WebGraph webgraph;
    private WebGraphList weblist;

    public Graph() {
        this(UUID.randomUUID().toString());
    }

    public Graph(String id) {
        this.id = id;
        nodes = new HashMap<>();
        edges = new HashMap<>();
    }

    public WebGraph toWebGraph() {
        if (webgraph == null) {
            int nodeCount = nodes.values().stream().mapToInt(HashMap::size).sum();
            int edgeCount = edges.values().stream().mapToInt(LinkedList::size).sum();
            log.info("Converting Graph " + id + " (nodes:" + nodeCount + ", edges:" + edgeCount + ") to webgraph: ");
            webgraph = new WebGraph(id);
            nodes.forEach((typeId, nodeMap) -> {
                String prefix = Graphs.getPrefix(typeId);
                String group = Graphs.getNode(typeId);
                webgraph.addNodes(nodeMap.values().stream().map(node -> node.toWebNode().setPrefix(prefix).setGroup(group)).collect(Collectors.toSet()));
            });
            edges.forEach((typeId, edges) -> {
                Pair<Integer, Integer> ns = Graphs.getNodesfromEdge(typeId);
                String pref1 = Graphs.getPrefix(ns.first);
                String pref2 = Graphs.getPrefix(ns.second);
                //TODO add directional?
                webgraph.addEdges(edges.stream().map(edge -> edge.toWebEdge().addPrefixes(pref1, pref2)).collect(Collectors.toSet()));
            });

        }
        return webgraph;
    }

    public WebGraphInfo toInfo(){
        WebGraphInfo info = new WebGraphInfo(id);
        this.edges.forEach((id,es)->info.edges.put(Graphs.getEdge(id),es.size()));
        this.nodes.forEach((id,ns)->info.nodes.put(Graphs.getNode(id),ns.size()));
        return info;
    }


    public WebGraphList toWebList() {
        return weblist;
    }

    public String getId() {
        return id;
    }

    public void addEdges(int edgeId, LinkedList<Edge> edges) {
        if (!this.edges.containsKey(edgeId)) {
            this.edges.put(edgeId, edges);
        } else
            this.edges.get(edgeId).addAll(edges);
    }

    public void addNodes(Integer typeId, HashMap<Integer, Node> nodeMap) {
        if (!this.nodes.containsKey(typeId)) {
            this.nodes.put(typeId, nodeMap);
        } else
            this.nodes.get(typeId).putAll(nodeMap);
    }

    public void addNode(Integer typeId, Node node) {
        if (!this.nodes.containsKey(typeId))
            this.nodes.put(typeId, new HashMap<>());
        this.nodes.get(typeId).put(node.getId(), node);
    }

    public void setWebList(WebGraphList list) {
        this.weblist = list;
    }

    public HashMap<Integer, HashMap<Integer, Node>> getNodes() {
        return nodes;
    }

    public HashMap<Integer, LinkedList<Edge>> getEdges() {
        return edges;
    }
}
