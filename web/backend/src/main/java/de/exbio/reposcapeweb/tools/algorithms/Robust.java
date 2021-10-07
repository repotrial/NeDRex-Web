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
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Robust implements Algorithm {

    private Environment env;
    private NodeController nodeController;
    private AlgorithmUtils utils;

    private Logger log = LoggerFactory.getLogger(Robust.class);

    private File executable;
    private File scriptDir;

    @Autowired
    public Robust(Environment env, NodeController nodeController, AlgorithmUtils utils) {
        this.env = env;
        this.nodeController = nodeController;
        this.utils = utils;
    }


    @Override
    public ToolService.Tool getEnum() {
        return ToolService.Tool.ROBUST;
    }

    @Override
    public void prepare() {
        log.info("Checking availability of robust");
        executable = new File(env.getProperty("path.tool.robust"));
        log.info("Robust path=" + executable.getAbsolutePath());
        if (!executable.exists()) {
            log.error("Robust executable was not found in config. Please make sure it is referenced correctly!");
            new RuntimeException("Missing reference.");
        } else {
            scriptDir = executable.getParentFile();
        }
    }

    @Override
    public File[] interactionFiles(JobRequest request) {
        return new File[]{(request.getParams().get("type").equals("gene") ? new File(utils.dataDir, "gene_gene_interaction_" + (request.experimentalOnly ? "exp" : "all") + ".pairs") : new File(utils.dataDir, "protein_protein_interaction_" + (request.experimentalOnly ? "exp" : "all") + ".pairs"))};
    }

    @Override
    public String createCommand(File[] interactions, JobRequest request) {
        return "robust robust.py " + interactions[0].getAbsolutePath() + " seeds.list result.csv 0.25 0.9 30 0.1";
    }

    @Override
    public void prepareJobFiles(File tempDir, JobRequest req, Graph g, HashMap<Integer, Pair<String, String>> domainMap) {
        File seed = new File(tempDir, "seeds.list");
        if (req.selection)
            utils.writeSeedFile(seed, req.nodes, domainMap, "", true);
        else
            utils.writeSeedFile(req.params.get("type"), seed, g, domainMap, "", true);
        for (File file : scriptDir.listFiles()) {
            try {
                if (file.isDirectory())
                    FileUtils.copyDirectory(file, new File(tempDir, file.getName()));
                else
                    Files.copy(file.toPath(), new File(tempDir, file.getName()).toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void getResults(HashMap<Integer, HashMap<String, Object>> nodes, HashMap<Integer, HashMap<Integer, HashMap<String, Object>>> edges, File tempDir, Job j, HashMap<Integer, HashMap<String, Integer>> domainMaps) throws IOException {
        File result = getResultFile(tempDir);
        HashMap<String, Integer> domainMap = domainMaps.get(getNodeType(j));
        try (BufferedReader br = ReaderUtils.getBasicReader(result)) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                LinkedList<String> entry = StringUtils.split(line, ",");
                HashMap<String, Object> attributes = new HashMap<>();
                nodes.put(domainMap.get(entry.get(0)), attributes);
                attributes.put("occs_abs", Integer.parseInt(entry.get(1)));
                attributes.put("occs_rel", Double.parseDouble(entry.get(2)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getResultFile(File tempDir) {
        return new File(tempDir, "result.csv");
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
        Set<Integer> newNodeIDs = j.getResult().getNodes().entrySet().stream().filter(e -> e.getValue() != null).map(Map.Entry::getKey).collect(Collectors.toSet());
        allNodes.addAll(newNodeIDs);
        derived.addNodeMarks(nodeTypeId, newNodeIDs);
        j.setUpdate("" + (allNodes.size() - beforeCount));
        NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(nodeTypeId)), allNodes);
        derived.saveNodeFilter(Graphs.getNode(nodeTypeId), nf);
        derived.addNodes(nodeTypeId, nf.toList(-1).stream().map(e -> new Node(e.getNodeId(), e.getName())).collect(Collectors.toList()));

        derived.addCustomNodeAttributeType(nodeTypeId, "occs_abs", "numeric");
        derived.addCustomNodeAttributeType(nodeTypeId, "occs_rel", "numeric");
        derived.addCustomNodeAttribute(nodeTypeId, j.getResult().getNodes());
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
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("LD_LIBRARY_PATH", env.getProperty("path.tool.python3-7.ldlib"));
        pb.environment().put("PYTHONPATH",env.getProperty("path.tool.python3-7.pythonpath"));
        pb.environment().put("PYTHON",env.getProperty("path.tool.python3-7.python"));
        pb.environment().put("PATH",System.getenv("PATH")+":"+env.getProperty("path.tool.python3-7.bin"));
        return pb;
    }
}
