package de.exbio.reposcapeweb.tools.algorithms;

import de.exbio.reposcapeweb.communication.cache.Edge;
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
import de.exbio.reposcapeweb.utils.ProcessUtils;
import de.exbio.reposcapeweb.utils.ReaderUtils;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Domino implements Algorithm {

    private AlgorithmUtils utils;
    private Environment env;
    private NodeController nodeController;


    @Autowired
    public Domino(AlgorithmUtils utils, Environment env, NodeController nodeController) {
        this.utils = utils;
        this.env = env;
        this.nodeController = nodeController;
    }

    @Override
    public ToolService.Tool getEnum() {
        return ToolService.Tool.DOMINO;
    }


    //TODO all

    @Override
    public void prepare() {
    }

    @Override
    public File[] interactionFiles(JobRequest request) {
        String tissue = "";
        if(request.tissue!=null && request.tissue.length()>0&& !request.tissue.equals("all"))
            tissue = "-"+request.tissue.replaceAll(" ","");
        File interactions = (request.getParams().get("type").equals("gene") ? new File(utils.dataDir, "geneInteractsWithGene"+tissue+"_" + (request.experimentalOnly ? "exp" : "all") + ".sif") : new File(utils.dataDir, "proteinInteractsWithProtein"+tissue+"_" + (request.experimentalOnly ? "exp" : "all") + ".sif"));
        return new File[]{interactions, getIndexFile(interactions)};
    }

    @Override
    public String getResultSuffix() {
        return "txt";
    }

    @Override
    public String createCommand(File[] interactions, JobRequest request) {
        return "domino seeds.list " + interactions[0].getName() + " " + interactions[1].getName() + " result";
    }

    @Override
    public void prepareJobFiles(File tempDir, JobRequest req, Graph g, HashMap<Integer, Pair<String, String>> domainMap) {
        File seed = new File(tempDir, "seeds.list");
        if (req.selection)
            utils.writeSeedFile(seed, req.nodes, domainMap, "_", false);
        else
            utils.writeSeedFile(req.params.get("type"), seed, g, domainMap, "_", false);
    }

    @Override
    public void getResults(HashMap<Integer, HashMap<String, Object>> nodes, HashMap<Integer, HashMap<Integer, HashMap<String, Object>>> edges, File tempDir, Job j, HashMap<Integer, HashMap<String, Integer>> domainMaps) throws IOException {
        try (BufferedReader br = ReaderUtils.getBasicReader(getResultFile(tempDir))) {
            String line;
            while ((line = br.readLine()) != null) {
                LinkedList<Integer> entries = new LinkedList<>(StringUtils.split(line.substring(1, line.length() - 1), ",").stream().map(node -> Integer.parseInt(node.trim().substring(1))).collect(Collectors.toList()));
                for (int i = 0; i < entries.size(); i++) {
                    int node1 = entries.get(i);
                    nodes.put(node1, null);
                    for (int k = i + 1; k < entries.size(); k++) {
                        int node2 = entries.get(k);
                        if (!edges.containsKey(node1))
                            edges.put(node1, new HashMap<>());
                        HashMap<String, Object> e = new HashMap<>();
                        edges.get(node1).put(node2, e);
                        e.put("memberOne", nodeController.getDomainId(getNodeType(j),node1));
                        e.put("memberTwo", nodeController.getDomainId(getNodeType(j),node2));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getResultFile(File tempDir) {
        return new File(tempDir.getAbsolutePath() + "/result/seeds/", "modules.out");
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
        derived.addNodes(nodeTypeId, nf.toList(-1).stream().map(e -> new Node(e.getNodeId(), nodeController.getDomainId(nf.getType(),e.getNodeId()), e.getName())).collect(Collectors.toList()));

        LinkedList<Edge> edges = new LinkedList<>();
        j.getResult().getEdges().forEach((id1, map) -> {
            map.forEach((id2, attributes) -> {
                edges.add(new Edge(id1, id2));
            });
        });
        derived.addCustomEdge(nodeTypeId, nodeTypeId, "DOMINO_Interaction", edges);
        int eid = derived.getEdge("DOMINO_Interaction");
        derived.addCustomEdgeAttribute(eid, j.getResult().getEdges());
        derived.addCustomAttributeType(eid, "memberOne", "id");
        derived.addCustomAttributeLabel(eid,"memberOne","ID1");
        derived.addCustomAttributeType(eid, "memberTwo", "id");
        derived.addCustomAttributeLabel(eid,"memberTwo","ID2");
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

    public File getIndexFile(File sif) {
        if (sif == null)
            return new File(utils.getIndexDirectory(), "domino");
        return new File(getIndexFile(null), sif.getName() + ".sliced");
    }

    @Override
    public ProcessBuilder getExecutionEnvironment(String[] command) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("LD_LIBRARY_PATH", env.getProperty("path.tool.python3-7.ldlib"));
        pb.environment().put("PATH", System.getenv("PATH") + ":" + env.getProperty("path.tool.python3-7.bin"));
        return pb;
    }

    @Override
    public void createIndex() {
        File wd = getIndexFile(null);
        wd.mkdir();
        Arrays.stream(utils.dataDir.listFiles()).filter(file -> file.getName().endsWith(".sif")).forEach(sif -> {
            try {
                ProcessUtils.executeProcessWait(getExecutionEnvironment(new String[]{"slicer", "-n", sif.getAbsolutePath(), "-o", getIndexFile(sif).getAbsolutePath()}), false);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public boolean hasCustomEdges() {
        return true;
    }
}
