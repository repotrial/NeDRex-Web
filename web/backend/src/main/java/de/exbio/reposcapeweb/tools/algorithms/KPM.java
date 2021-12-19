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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KPM implements Algorithm {

    private Environment env;
    private NodeController nodeController;
    private AlgorithmUtils utils;

    private Logger log = LoggerFactory.getLogger(KPM.class);

    private File executable;
    private File scriptDir;

    @Autowired
    public KPM(Environment env, NodeController nodeController, AlgorithmUtils utils) {
        this.env = env;
        this.nodeController = nodeController;
        this.utils = utils;
    }


    @Override
    public ToolService.Tool getEnum() {
        return ToolService.Tool.KPM;
    }

    @Override
    public void prepare() {
        log.info("Checking availability of KPM");
        executable = new File(env.getProperty("path.tool.kpm"));
        log.info("KPM path=" + executable.getAbsolutePath());
        if (!executable.exists()) {
            log.error("KPM executable was not found in config. Please make sure it is referenced correctly!");
            new RuntimeException("Missing reference.");
        } else {
            scriptDir = executable.getParentFile();
        }
    }

    @Override
    public File[] interactionFiles(JobRequest request) {
        File interactions = (request.getParams().get("type").equals("gene") ? new File(utils.dataDir, "geneInteractsWithGene_" + (request.experimentalOnly ? "exp" : "all") + ".sif") : new File(utils.dataDir, "proteinInteractsWithProtein_" + (request.experimentalOnly ? "exp" : "all") + ".sif"));
        File config = new File(utils.scriptDir, "kpm.properties");
        return new File[]{interactions, config, executable};
    }

    @Override
    public String getResultSuffix() {
        return "txt";
    }


    @Override
    public String createCommand(File[] interactions, JobRequest request) {
        return "kpm KPM-5.jar " + interactions[0].getName() + " seeds.matrix 0 " + request.getParams().get("k");
    }

    @Override
    public void prepareJobFiles(File tempDir, JobRequest req, Graph g, HashMap<Integer, Pair<String, String>> domainMap) {
        File seed = new File(tempDir, "seeds.matrix");
        if (req.selection)
            utils.writeSeedFile(seed, req.nodes, domainMap, "_", "\t1", false);
        else
            utils.writeSeedFile(req.params.get("type"), seed, g, domainMap, "_", "\t1", false);
    }

    @Override
    public void getResults(HashMap<Integer, HashMap<String, Object>> nodes, HashMap<Integer, HashMap<Integer, HashMap<String, Object>>> edges, File tempDir, Job j, HashMap<Integer, HashMap<String, Integer>> domainMaps) throws IOException {
        File result = getResultFile(tempDir);
        try (BufferedReader br = ReaderUtils.getBasicReader(result)) {
            String line;
            boolean node_part = false;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("NODES"))
                    node_part = true;
                if (line.startsWith("EDGES"))
                    node_part = false;
                if (node_part && line.charAt(0) == '_')
                    nodes.put(Integer.parseInt(StringUtils.split(line.strip(), '\t').get(0).substring(1)), null);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            throw new IOException();
        }
    }

    @Override
    public File getResultFile(File tempDir) {
        File results = new File(tempDir, "results");
        results = results.listFiles()[0];
        results = results.listFiles()[0];
        for (File file : results.listFiles()) {
            if (file.getName().startsWith("pathways") & !file.getName().startsWith("pathways_stats"))
                return file;
        }
        return null;
    }

    @Override
    public int getNodeType(Job j) {
        return Graphs.getNode(j.getTarget());
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
        derived.addNodeMarks(nodeTypeId, newNodeIDs.stream().filter(n->!j.getSeeds().contains(n)).collect(Collectors.toList()));
        j.setUpdate("" + (allNodes.size() - beforeCount));
        NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(nodeTypeId)), allNodes);
        derived.saveNodeFilter(Graphs.getNode(nodeTypeId), nf);
        derived.addNodes(nodeTypeId, nf.toList(-1).stream().map(e -> new Node(e.getNodeId(), e.getName())).collect(Collectors.toList()));
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
    public boolean hasCustomEdges() {
        return false;
    }

    @Override
    public ProcessBuilder getExecutionEnvironment(String[] command) {
        return new ProcessBuilder(command);
    }
}
