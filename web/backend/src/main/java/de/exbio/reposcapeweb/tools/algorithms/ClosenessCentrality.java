package de.exbio.reposcapeweb.tools.algorithms;

import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.cache.Node;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.jobs.JobRequest;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.filter.NodeFilter;
import de.exbio.reposcapeweb.tools.AlgorithmUtils;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.ReaderUtils;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClosenessCentrality implements Algorithm {

    private Environment env;
    private AlgorithmUtils utils;
    private NodeController nodeController;
    private File executable;

    private Logger log = LoggerFactory.getLogger(ClosenessCentrality.class);

    @Autowired
    public ClosenessCentrality(Environment env, AlgorithmUtils utils, NodeController nodeController) {
        this.env = env;
        this.utils = utils;
        this.nodeController = nodeController;
    }

    @Override
    public boolean integrateOriginalGraph(Job j) {
        return !j.getParams().containsKey("nodesOnly") || j.getParams().get("nodesOnly").equals("false");
    }

    @Override
    public void createGraph(Graph derived, Job j, int nodeTypeId, Graph g) {
        HashSet<Integer> allNodes = new HashSet<>();
        if (g.getNodes().containsKey(nodeTypeId))
            allNodes.addAll(g.getNodes().get(nodeTypeId).keySet());
        int beforeCount = allNodes.size();
        Set<Integer> newNodeIDs = j.getResult().getNodes().entrySet().stream().filter(e -> e.getValue() != null).map(Map.Entry::getKey).collect(Collectors.toSet());
        allNodes.addAll(newNodeIDs);
        derived.addNodeMarks(nodeTypeId, newNodeIDs);
        j.setUpdate("" + (allNodes.size() - beforeCount));
        NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(nodeTypeId)), allNodes);
        derived.saveNodeFilter(Graphs.getNode(nodeTypeId), nf);
        derived.addNodes(nodeTypeId, nf.toList(-1).stream().map(e -> new Node(e.getNodeId(), e.getName())).collect(Collectors.toList()));

        int otherTypeId = nodeTypeId;

        derived.addCustomNodeAttributeType(otherTypeId, "score", "numeric", "Score");
        HashMap<Integer, HashMap<String, Object>> idMap = new HashMap<>();
        j.getResult().getNodes().forEach((k, v) -> {
            if (newNodeIDs.contains(k))
                idMap.put(k, v);
        });
        derived.addCustomNodeAttribute(otherTypeId, idMap);
        nodeTypeId = Graphs.getNode(j.getTarget());
        derived.saveNodeFilter(j.getTarget(), g.getNodeFilter(j.getTarget()));
        derived.addNodes(nodeTypeId, g.getNodes().get(nodeTypeId));

        Set<Integer> seedIds = j.getResult().getNodes().entrySet().stream().filter(e -> e.getValue() != null).map(Map.Entry::getKey).collect(Collectors.toSet());
        derived.addNodeMarks(nodeTypeId, seedIds);
    }

    @Override
    public ToolService.Tool getEnum() {
        return ToolService.Tool.CENTRALITY;
    }

    @Override
    public int getNodeType(Job j) {
        return Graphs.getNode("drug");
    }

    @Override
    public void prepare() {
        log.info("Setting up Centrality");
        executable = new File(env.getProperty("path.tool.centrality"));
        log.info("Centrality path=" + executable.getAbsolutePath());
        if (!executable.exists()) {
            log.error("Centrality executable was not found in config. Please make sure it is referenced correctly!");
            new RuntimeException("Missing reference.");
        }
    }

    @Override
    public File[] interactionFiles(JobRequest request) {
        return new File[]{request.getParams().get("type").equals("gene") ? new File(utils.dataDir, "ranking_files/GGDr_" + (request.experimentalOnly ? "exp" : "all") + ".gt") : new File(utils.dataDir, "ranking_files/PPDr_all.gt")};
    }

    @Override
    public String getResultSuffix() {
        return "txt";
    }

    @Override
    public boolean usesExpressionInput() {
        return false;
    }

    @Override
    public boolean usesSeedInput() {
        return true;
    }

    @Override
    public void setTreads(Job j, int max) {

    }

    @Override
    public boolean hasMultipleResultFiles() {
        return false;
    }

    @Override
    public void createIndex() {

    }

    @Override
    public String createCommand(File[] interactions, JobRequest request) {
        return "centrality " + executable.getAbsolutePath() + " " +
                interactions[0].getName() +
                " seeds.list" +
                (request.getParams().get("direct").charAt(0) == 't' ? " Y" : " N") +
                (request.getParams().get("approved").charAt(0) == 't' ? " Y" : " N");
    }

    @Override
    public void prepareJobFiles(File tempDir, JobRequest req, Graph g, HashMap<Integer, Pair<String, String>> domainMap) {
        File seed = new File(tempDir, "seeds.list");
        if (req.selection)
            utils.writeSeedFile(seed, req.nodes, domainMap, "", true);
        else
            utils.writeSeedFile(req.params.get("type"), seed, g, domainMap, "", true);
    }

    @Override
    public boolean hasCustomEdges() {
        return false;
    }


    @Override
    public ProcessBuilder getExecutionEnvironment(String[] command) {
        return new ProcessBuilder(command);
    }

    @Override
    public void getResults(HashMap<Integer, HashMap<String, Object>> nodes, HashMap<Integer, HashMap<Integer, HashMap<String, Object>>> edges, File tempDir, Job j, HashMap<Integer, HashMap<String, Integer>> domainMaps) throws IOException {
        for (File f : tempDir.listFiles()) {
            if (!f.getName().equals("seeds.list") && !f.getName().endsWith(".gt")) {
                nodes.putAll(readResults(f, domainMaps, j.getTarget()));
            }
        }
    }

    @Override
    public File getResultFile(File tempDir) {
        for (File f : tempDir.listFiles()) {
            if (!f.getName().equals("seeds.list") && !f.getName().endsWith(".gt")) {
                return f;
            }
        }
        return null;
    }

    private HashMap<Integer, HashMap<String, Object>> readResults(File f, HashMap<Integer, HashMap<String, Integer>> domainMap, String target) {
        HashMap<Integer, HashMap<String, Object>> out = new HashMap<>();
        HashMap<String, Integer> drugMap = domainMap.get(Graphs.getNode("drug"));
        try (BufferedReader br = ReaderUtils.getBasicReader(f)) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                LinkedList<String> split = StringUtils.split(line, "\t");
                int id = drugMap.get(split.getFirst());
                double score = Double.parseDouble(split.get(1));
                if (score > 0.0) {
                    out.put(id, new HashMap<>());
                    out.get(id).put("score", score);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }
}
