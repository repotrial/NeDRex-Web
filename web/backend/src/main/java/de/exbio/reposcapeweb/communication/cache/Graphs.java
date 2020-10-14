package de.exbio.reposcapeweb.communication.cache;

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
        String[] nodes = new String[]{"drug", "protein", "pathway", "gene", "disorder"};
        nodeToIdMap = new HashMap<>();
        idToNodeMap = new HashMap<>();
        idToNodePrefixMap = new HashMap<>();
        for (int i = 0; i < nodes.length; i++) {
            String name = nodes[i];
            nodeToIdMap.put(name, i);
            idToNodeMap.put(i, name);
            idToNodePrefixMap.put(i, name.substring(0, 3) + "_");
        }

        String[] edges = new String[]{"DrugHasTargetProtein", "DrugHasTargetGene", "ProteinInteractsWithProtein", "ProteinInPathway", "ProteinEncodedBy", "ProteinAssociatedWithDisorder", "GeneInteractsWithGene", "GeneAssociatedWithDisorder", "DisorderComorbidWithDisorder", "DisorderIsADisorder", "DrugHasIndication"};
        edgeToIdMap = new HashMap<>();
        idToEdgeMap = new HashMap<>();

        for (int i = 0; i < edges.length; i++) {
            String name = edges[i];
            edgeToIdMap.put(name, i);
            idToEdgeMap.put(i, name);
        }


        edge2node = new HashMap<>();

        edge2node.put(Graphs.getEdge("GeneAssociatedWithDisorder"), new Pair<>(Graphs.getNode("gene"), Graphs.getNode("disorder")));
        edge2node.put(Graphs.getEdge("DrugHasTargetGene"), new Pair<>(Graphs.getNode("drug"), Graphs.getNode("gene")));
        edge2node.put(Graphs.getEdge("ProteinEncodedBy"), new Pair<>(Graphs.getNode("protein"), Graphs.getNode("gene")));

        edge2node.put(Graphs.getEdge("DrugHasIndication"), new Pair<>(Graphs.getNode("drug"), Graphs.getNode("disorder")));
        edge2node.put(Graphs.getEdge("DrugHasTargetProtein"), new Pair<>(Graphs.getNode("drug"), Graphs.getNode("protein")));

        edge2node.put(Graphs.getEdge("ProteinInteractsWithProtein"), new Pair<>(Graphs.getNode("protein"), Graphs.getNode("protein")));
        edge2node.put(Graphs.getEdge("ProteinInPathway"), new Pair<>(Graphs.getNode("protein"), Graphs.getNode("pathway")));
        edge2node.put(Graphs.getEdge("ProteinAssociatedWithDisorder"), new Pair<>(Graphs.getNode("protein"), Graphs.getNode("disorder")));

        edge2node.put(Graphs.getEdge("DisorderIsADisorder"), new Pair<>(Graphs.getNode("disorder"), Graphs.getNode("disorder")));
        edge2node.put(Graphs.getEdge("DisorderComorbidWithDisorder"), new Pair<>(Graphs.getNode("disorder"), Graphs.getNode("disorder")));

        edge2node.put(Graphs.getEdge("GeneInteractsWithGene"),new Pair<>(Graphs.getNode("gene"),Graphs.getNode("gene")));

        nodes2edge = new HashMap<>();
        edge2node.forEach((k, v) -> {
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

    }


    public static String getNode(int id){
        return idToNodeMap.get(id);
    }

    public static int getNode(String name){
        return nodeToIdMap.get(name);
    }

    public static String getPrefix(int id){
        return idToNodePrefixMap.get(id);
    }


    public static int getEdge(String name){
        return edgeToIdMap.get(name);
    }

    public static String getEdge(int id){
        return idToEdgeMap.get(id);
    }

    public static LinkedList<Integer> getEdgesfromNodes(int node1, int node2) {
        try {
            return nodes2edge.get(node1).get(node2);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static boolean checkEdgeDirection(int edgeId, int node1, int node2){
        return edge2node.get(edgeId).getFirst().equals(node1) & edge2node.get(edgeId).getSecond().equals(node2);
    }

    public static Pair<Integer, Integer> getNodesfromEdge(int edge) {
        return edge2node.get(edge);
    }


}
