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
import de.exbio.reposcapeweb.utils.WriterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class Bicon implements Algorithm{


    private Environment env;
    private AlgorithmUtils utils;
    private NodeController nodeController;

    private File executable;

    private Logger log = LoggerFactory.getLogger(Bicon.class);

    @Autowired
    public Bicon(Environment env, AlgorithmUtils utils, NodeController nodeController){
        this.env = env;
        this.nodeController = nodeController;
        this.utils = utils;
    }


    @Override
    public ToolService.Tool getEnum() {
        return ToolService.Tool.BICON;
    }

    @Override
    public void prepare() {
        log.info("Setting up BiCon");
        executable = new File(env.getProperty("path.tool.bicon"));
        log.info("BiCon path=" + executable.getAbsolutePath());
        if (!executable.exists()) {
            log.error("BiCon executable was not found in config. Please make sure it is referenced correctly!");
            new RuntimeException("Missing reference.");
        }
    }

    @Override
    public File[] interactionFiles(JobRequest request) {
        String tissue = "";
        if(request.tissue!=null && request.tissue.length()>0 && !request.tissue.equals("all"))
            tissue = "-"+request.tissue.replaceAll(" ","");
        return new File[]{new File(utils.dataDir, "gene_gene_interaction"+tissue+"_" + (request.experimentalOnly ? "exp" : "all") + ".pairs")};
    }

    @Override
    public String getResultSuffix() {
        return "txt";
    }

    @Override
    public String createCommand(File[] interactions, JobRequest request) {
        return "bicon " + executable.getAbsolutePath() + " exprFile " + interactions[0].getName() + " genes.txt " + request.getParams().get("lg_min") + " " + request.getParams().get("lg_max");
    }

    @Override
    public void prepareJobFiles(File tempDir, JobRequest req, Graph g, HashMap<Integer, Pair<String, String>> domainMap) {
        File exprFile = new File(tempDir, "exprFile");
        WriterUtils.writeTo(exprFile,req.getParams().get("exprData"));
    }

    @Override
    public void getResults(HashMap<Integer, HashMap<String, Object>> nodes, HashMap<Integer, HashMap<Integer, HashMap<String, Object>>> edges, File tempDir, Job j, HashMap<Integer, HashMap<String, Integer>> domainMaps) throws IOException {
        try (BufferedReader br = ReaderUtils.getBasicReader(new File(tempDir, "genes.txt"))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                nodes.put(domainMaps.get(Graphs.getNode("gene")).get(line), null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public File getResultFile(File tempDir) {
        return new File(tempDir, "genes.txt");
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
        Set<Integer> newNodeIDs = new HashSet<>(j.getResult().getNodes().keySet());
        allNodes.addAll(newNodeIDs);
        derived.addNodeMarks(nodeTypeId, newNodeIDs);
        j.setUpdate("" + (allNodes.size() - beforeCount));
        NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(nodeTypeId)), allNodes);
        derived.saveNodeFilter(Graphs.getNode(nodeTypeId), nf);
        derived.addNodes(nodeTypeId, nf.toList(-1).stream().map(e -> new Node(e.getNodeId(), e.getName())).collect(Collectors.toList()));
    }

    @Override
    public boolean usesExpressionInput() {
        return true;
    }
    @Override
    public boolean usesSeedInput() {
        return false;
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
    public boolean hasCustomEdges() {
        return false;
    }


    @Override
    public ProcessBuilder getExecutionEnvironment(String[] command) {
        return new ProcessBuilder(command);
    }

    @Override
    public int getNodeType(Job j) {
        return Graphs.getNode("gene");
    }
}
