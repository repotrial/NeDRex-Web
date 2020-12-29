package de.exbio.reposcapeweb.communication.cache;

import de.exbio.reposcapeweb.configs.DBConfig;
import de.exbio.reposcapeweb.configs.schema.Config;
import de.exbio.reposcapeweb.utils.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class Graphs {

    private static HashMap<String, Integer> edgeToIdMap;
    private static HashMap<String, Integer> nodeToIdMap;
    private static HashMap<Integer, String> idToEdgeMap;
    private static HashMap<Integer, String> idToNodeMap;
    private static HashMap<Integer, String> idToNodePrefixMap;

    private static HashMap<Integer, HashMap<Integer, LinkedList<Integer>>> nodes2edge;
    private static HashMap<Integer, Pair<Integer, Integer>> edge2node;


    public static void setUp() {
        Config config = DBConfig.getConfig();


        nodeToIdMap = new HashMap<>();
        idToNodeMap = new HashMap<>();
        idToNodePrefixMap = new HashMap<>();


        config.nodes.forEach(node -> {
            int id = node.id;
            String name = node.name;
            nodeToIdMap.put(name, id);
            idToNodeMap.put(id, name);
            idToNodePrefixMap.put(id, name.substring(0, 3) + "_");
        });

        edgeToIdMap = new HashMap<>();
        idToEdgeMap = new HashMap<>();

        config.edges.forEach(edge -> {
            int id = edge.id;
            String name = edge.mapsTo;
            edgeToIdMap.put(name, id);
            idToEdgeMap.put(id, name);
        });


        HashMap<Integer, Pair<Integer, Integer>> edge2node = new HashMap<>();

        config.edges.forEach(edge -> edge2node.put(edge.id, new Pair<>(Graphs.getNode(edge.source), Graphs.getNode(edge.target))));

        Graphs.mapSetUp(edge2node);


    }

    public static void mapSetUp(HashMap<Integer, Pair<Integer, Integer>> edge2node){
        Graphs.edge2node=edge2node;
        nodes2edge = new HashMap<>();
        edge2node.forEach((k, v) ->

        {
            if (!nodes2edge.containsKey(v.first))
                nodes2edge.put(v.first, new HashMap<>());
            if (!nodes2edge.get(v.first).containsKey(v.second))
                nodes2edge.get(v.first).put(v.second, new LinkedList<>(Collections.singletonList(k)));
            else if (!v.first.equals(v.second) | !nodes2edge.get(v.first).get(v.second).contains(k))
                nodes2edge.get(v.first).get(v.second).add(k);

            if (!nodes2edge.containsKey(v.second))
                nodes2edge.put(v.second, new HashMap<>());
            if (!nodes2edge.get(v.second).containsKey(v.first))
                nodes2edge.get(v.second).put(v.first, new LinkedList<>(Collections.singletonList(k)));
            else if (!v.first.equals(v.second))
                nodes2edge.get(v.second).get(v.first).add(k);
        });
    };


    public static String getNode(int id) {
        return idToNodeMap.get(id);
    }

    public static int getNode(String name) {
        return nodeToIdMap.get(name);
    }

    public static String getPrefix(int id) {
        return idToNodePrefixMap.get(id);
    }


    public static Integer getEdge(String name) {
        return edgeToIdMap.get(name);
    }

    public static String getEdge(int id) {
        return idToEdgeMap.get(id);
    }

    public static LinkedList<Integer> getEdgesfromNodes(int node1, int node2) {
        try {
            return nodes2edge.get(node1).get(node2);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static boolean checkEdgeDirection(int edgeId, int node1, int node2) {
        return edge2node.get(edgeId).getFirst().equals(node1) & edge2node.get(edgeId).getSecond().equals(node2);
    }

    public static Pair<Integer, Integer> getNodesfromEdge(int edge) {
        return edge2node.get(edge);
    }

    public static Pair<Integer, Integer> getNodesfromEdge(String edge) {
        return edge2node.get(Graphs.getEdge(edge));
    }


}
