package de.exbio.reposcapeweb.communication.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.exbio.reposcapeweb.communication.reponses.WebGraph;
import de.exbio.reposcapeweb.communication.reponses.WebGraphInfo;
import de.exbio.reposcapeweb.communication.reponses.WebGraphList;
import de.exbio.reposcapeweb.filter.NodeFilter;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Graph {

    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(Graph.class);
    private String id;
    @JsonIgnore
    private String parent;
    private HashMap<Integer, HashMap<Integer, Node>> nodes;
    // edgetype -> node1 -> node2 -> edge
//    private HashMap<Integer,HashMap<Integer,HashMap<Integer,Node>>> edges;
    private HashMap<Integer, LinkedList<Edge>> edges;
    private HashMap<Integer,String> customEdges;
    private HashMap<Integer,Pair<Integer,Integer>> customEdgeNodes;
    private HashMap<Integer,HashMap<Integer,HashMap<Integer,Integer>>> customEdgeWeights;
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>> customJaccardIndex;
    @JsonIgnore
    private WebGraph webgraph;
    @JsonIgnore
    private WebGraphList weblist;
    @JsonIgnore
    private HashMap<String, NodeFilter> nodeFilters;

    public Graph() {
        nodes = new HashMap<>();
        edges = new HashMap<>();
        nodeFilters = new HashMap<>();
        customEdges = new HashMap<>();
        customEdgeNodes = new HashMap<>();
        customEdgeWeights= new HashMap<>();
        customJaccardIndex=new HashMap<>();
    }

    public Graph(String id) {
        this();
        this.id = id;

    }

    public void setParent(String id){
        this.parent = id;
    }

    public String getParent() {
        return parent;
    }

    public WebGraph toWebGraph(HashMap<String,Object> colors) {
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
                Pair<Integer, Integer> ns = getNodesfromEdge(typeId);
                String pref1 = Graphs.getPrefix(ns.first);
                String pref2 = Graphs.getPrefix(ns.second);
                //TODO add directional?
                webgraph.addEdges(edges.stream().map(edge -> edge.toWebEdge().addPrefixes(pref1, pref2)).collect(Collectors.toSet()));
            });
        }
        webgraph.setColorMap(colors);
        return webgraph;
    }


    public void saveNodeFilter(String nodeName, NodeFilter nf) {
        this.nodeFilters.put(nodeName, nf);
        nf.getDistinctMap().forEach((type, filters) -> {
            filters.forEach((name, list) -> {
            });
        });
    }

    public NodeFilter getNodeFilter(String nodeName) {
        return this.nodeFilters.get(nodeName);
    }

    public WebGraphInfo toInfo() {
        WebGraphInfo info = new WebGraphInfo(id);
        this.edges.forEach((id, es) -> info.edges.put(getEdge(id), es.size()));
        this.nodes.forEach((id, ns) -> info.nodes.put(Graphs.getNode(id), ns.size()));
        return info;
    }

    public String getEdge(int id){
        return id<0 ? customEdges.get(id) : Graphs.getEdge(id);
    }

    public Integer getEdge(String id){
        try{
            return Graphs.getEdge(id);
        }catch (NullPointerException e) {
            AtomicReference<Integer> out = new AtomicReference<>();
            customEdges.forEach((k,v)->{
                if(v.equals(id))
                    out.set(k);
            });
            return out.get();
        }
    }

    public Pair<Integer,Integer> getNodesfromEdge(int id){
        return id<0 ? customEdgeNodes.get(id) : Graphs.getNodesfromEdge(id);
    }



    public WebGraphList toWebList() {
        return weblist;
    }

    public String getId() {
        return id;
    }

    public void addEdges(int edgeId, LinkedList<Edge> edges) {
        if (!this.edges.containsKey(edgeId))
            this.edges.put(edgeId, new LinkedList<>());
        this.edges.get(edgeId).addAll(edges);
    }

    public void addNodes(Integer typeId, Collection<Node> nodes) {
        if (!this.nodes.containsKey(typeId))
            this.nodes.put(typeId, new HashMap<>());
        nodes.forEach(n -> addNode(typeId, n));
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


    public HashMap<String, NodeFilter> getNodeFilters() {
        return nodeFilters;
    }


    public Graph clone(String id) {
        //TODO clone weblist and graph?
        Graph g = new Graph(id);
        getNodeFilters().forEach(g::saveNodeFilter);
        nodes.forEach((type, map) -> g.addNodes(type, map.values()));
        edges.forEach(g::addEdges);
        customEdgeNodes.forEach((k,v)->g.addCollapsedEdges(v.getFirst(),v.getSecond(),customEdges.get(k),new LinkedList<>()));
        g.setParent(this.id);
        customEdgeWeights.forEach((k,v)->g.addCollapsedWeights(g.getEdge(k),v));
        customJaccardIndex.forEach((k,v)->g.addCollapsedJaccardIndex(g.getEdge(k),v));
        return g;
    }

    public void addCollapsedEdges(int node1, int node2, String edgeName, LinkedList<Edge> edges) {
        int edgeId = (customEdges.size()+1)*-1;
        customEdgeNodes.put(edgeId, new Pair<>(node1,node2));
        customEdges.put(edgeId,edgeName);
        addEdges(edgeId,edges);
    }

    public HashMap<Integer, String> getCustomEdges() {
        return customEdges;
    }

    public HashMap<Integer, Pair<Integer, Integer>> getCustomEdgeNodes() {
        return customEdgeNodes;
    }

    public void addCollapsedWeights(String edgeName, HashMap<Integer, HashMap<Integer, Integer>> edgeWeights) {
        customEdgeWeights.put(getEdge(edgeName),edgeWeights);
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> getCustomEdgeWeights() {
        return customEdgeWeights;
    }


    public void addCollapsedJaccardIndex(String edgeName, HashMap<Integer, HashMap<Integer, Double>> jaccardIndex) {
        customJaccardIndex.put(getEdge(edgeName), jaccardIndex);
    }

    public HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>> getCustomJaccardIndex() {
        return customJaccardIndex;
    }
}
