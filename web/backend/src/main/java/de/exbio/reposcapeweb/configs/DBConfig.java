package de.exbio.reposcapeweb.configs;

import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.configs.schema.Config;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.ReaderUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class DBConfig {

    private static File confFile;
    private static Config config;

    private static HashMap<String, HashMap<String, HashSet<String>>> distinctValues;


    public static HashSet<String> getDistinctAttributes(String entity, String type) {
        if (distinctValues == null) {
            distinctValues = new HashMap<>();
            distinctValues.put("node", new HashMap<>());
            distinctValues.put("edge", new HashMap<>());
            distinctValues.get("node").put("drug", new HashSet<>(Arrays.asList("drugGroups", "drugCategories", "_cls")));
            distinctValues.get("edge").put("ProteinInteractsWithProtein", new HashSet<>(Arrays.asList("methods", "evidenceTypes", "databases")));
        }

        return distinctValues.get(entity).get(type);
    }


    public static void importConfig(File conf, Config object) {

        confFile = conf;
        config = object;
        importGraphSetup();
    }

    private static void importGraphSetup() {

        HashMap<Integer, String> nodeIds = new HashMap<>();
        config.nodes.forEach(node -> {
            nodeIds.put(node.id, node.name);
        });

        HashMap<Integer, String> edgeIds = new HashMap<>();
        config.edges.forEach(edge -> {
            edgeIds.put(edge.id, edge.name);
        });

        Graphs.setUp(nodeIds, edgeIds);

        HashMap<Integer, Pair<Integer, Integer>> edge2node = new HashMap<>();

        config.edges.forEach(edge -> {
            if (!edge.disabled)
                edge2node.put(edge.id, new Pair<>(Graphs.getNode(edge.source), Graphs.getNode(edge.target)));
        });

        Graphs.mapSetUp(edge2node);

    }

}
