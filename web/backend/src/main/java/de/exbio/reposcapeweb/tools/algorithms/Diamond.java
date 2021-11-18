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
public class Diamond implements Algorithm {

    private final ToolService.Tool id = ToolService.Tool.DIAMOND;
    private final String name = "diamond";
    private File executable;

    private Environment env;
    private AlgorithmUtils utils;
    private NodeController nodeController;

    private Logger log = LoggerFactory.getLogger(Diamond.class);


    @Autowired
    public Diamond(Environment env, AlgorithmUtils utils, NodeController nodeController) {
        this.env = env;
        this.nodeController = nodeController;
        this.utils = utils;
    }

    @Override
    public String createCommand(File[] interactions, JobRequest request) {
        return "diamond " +
                executable.getAbsolutePath() + " " +
                interactions[0].getName() + " " +
                "seeds.list " +
                request.getParams().get("n") + " " + request.getParams().get("alpha");
    }

    public File[] interactionFiles(JobRequest request) {
        return new File[]{(request.getParams().get("type").equals("gene") ? new File(utils.dataDir, "gene_gene_interaction_" + (request.experimentalOnly ? "exp" : "all") + ".pairs") : new File(utils.dataDir, "protein_protein_interaction_" + (request.experimentalOnly ? "exp" : "all") + ".pairs"))};
    }


    @Override
    public ToolService.Tool getEnum() {
        return id;
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

        derived.addCustomNodeAttributeType(nodeTypeId, "rank", "numeric", "Rank");
        derived.addCustomNodeAttributeType(nodeTypeId, "p_hyper", "numeric", "P-Val (hyper)");
        derived.addCustomNodeAttribute(nodeTypeId, j.getResult().getNodes());
    }

    @Override
    public int getNodeType(Job j) {
        return Graphs.getNode(j.getTarget());
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
    public boolean hasMultipleResultFiles() {
        return false;
    }

    @Override
    public void createIndex() {

    }

    @Override
    public void setTreads(Job j, int max) {

    }

    public void prepare() {
        log.info("Setting up DIAMOnD");
        executable = new File(env.getProperty("path.tool.diamond"));
        log.info("Diamond path=" + executable.getAbsolutePath());
        if (!executable.exists()) {
            log.error("DIAMOnD executable was not found in config. Please make sure it is referenced correctly!");
            new RuntimeException("Missing reference.");
        }
    }

    @Override
    public ProcessBuilder getExecutionEnvironment(String[] command) {
        return new ProcessBuilder(command);
    }

    public void prepareJobFiles(File wd, JobRequest req, Graph g, HashMap<Integer, Pair<String, String>> domainMap) {
        File seed = new File(wd, "seeds.list");
        if (req.selection)
            utils.writeSeedFile(seed, req.nodes, domainMap,"",true);
        else
            utils.writeSeedFile(req.params.get("type"), seed, g, domainMap,"",true);
    }

    @Override
    public boolean hasCustomEdges() {
        return false;
    }

    public void getResults(HashMap<Integer, HashMap<String, Object>> nodes, HashMap<Integer, HashMap<Integer, HashMap<String, Object>>> edges, File wd, Job j, HashMap<Integer, HashMap<String, Integer>> domainMaps) {
        for (File f : wd.listFiles()) {
            if (f.getName().endsWith(".txt")) {
                nodes.putAll(readResults(f, Double.parseDouble(j.getParams().get("pcutoff")), domainMaps.get(Graphs.getNode(j.getTarget()))));
            }
        }
    }

    private HashMap<Integer, HashMap<String, Object>> readResults(File f, double cutoff, HashMap<String, Integer> domainMap) {
        HashMap<Integer, HashMap<String, Object>> results = new HashMap<>();
        try {
            BufferedReader br = ReaderUtils.getBasicReader(f);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.charAt(0) == '#')
                    continue;
                LinkedList<String> attrs = StringUtils.split(line, "\t");
                double p_val = Double.parseDouble(attrs.get(2));
                if (p_val < cutoff) {
                    int id = domainMap.get(attrs.get(1));
                    results.put(id, new HashMap<>());
                    results.get(id).put("p_hyper", p_val);
                    results.get(id).put("rank", Integer.parseInt(attrs.get(0)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    public File getResultFile(File wd) {
        for (File f : wd.listFiles()) {
            if (f.getName().endsWith(".txt")) {
                return f;
            }
        }
        return null;
    }
}
