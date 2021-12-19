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
import de.exbio.reposcapeweb.utils.ReaderUtils;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.zeroturnaround.zip.ZipUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;

@Service
public class Must implements Algorithm{

    private Environment env;
    private AlgorithmUtils utils;
    private NodeController nodeController;
    private File executable;

    private Logger log = LoggerFactory.getLogger(Must.class);

    @Autowired
    public Must(Environment env, AlgorithmUtils utils, NodeController nodeController){
        this.env = env;
        this.utils = utils;
        this.nodeController = nodeController;
    }

    @Override
    public ToolService.Tool getEnum() {
        return ToolService.Tool.MUST;
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
        j.setUpdate("" + j.getResult().getNodes().size());
        NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(nodeTypeId)), j.getResult().getNodes().keySet());
        derived.saveNodeFilter(Graphs.getNode(nodeTypeId), nf);
        derived.addNodes(nodeTypeId, nf.toList(-1).stream().map(e -> new Node(e.getNodeId(), e.getName())).collect(Collectors.toList()));
        LinkedList<Edge> edges = new LinkedList<>();
        j.getResult().getEdges().forEach((id1, map) -> {
            map.forEach((id2, attributes) -> {
                edges.add(new Edge(id1, id2));
            });
        });
        derived.addNodeMarks(nodeTypeId, derived.getNodes().get(nodeTypeId).keySet().stream().filter(n->!j.getSeeds().contains(n)).collect(Collectors.toList()));
        derived.addCustomEdge(nodeTypeId, nodeTypeId, "MuST_Interaction", edges);
        int eid = derived.getEdge("MuST_Interaction");
        derived.addCustomEdgeAttribute(eid, j.getResult().getEdges());
        derived.addCustomAttributeType(eid, "participation_number", "numeric");
        derived.addCustomAttributeType(eid, "memberOne", "id");
        derived.addCustomAttributeType(eid, "memberTwo", "id");
    }


    @Override
    public void prepare() {
        log.info("Setting up Must");
        executable = new File(env.getProperty("path.tool.must"));
        log.info("Must path=" + executable.getAbsolutePath());
        if (!executable.exists()) {
            log.error("Must executable was not found in config. Please make sure it is referenced correctly!");
            new RuntimeException("Missing reference.");
        }
    }

    @Override
    public File[] interactionFiles(JobRequest request) {
        return new File[]{(request.getParams().get("type").equals("gene") ? new File(utils.dataDir, "gene_gene_interaction_" + (request.experimentalOnly ? "exp" : "all") + ".pairs") : new File(utils.dataDir, "protein_protein_interaction_" + (request.experimentalOnly ? "exp" : "all") + ".pairs"))};
    }

    @Override
    public String createCommand(File[] interactions, JobRequest request) {
        return "must " +
                executable.getAbsolutePath() + " " +
                interactions[0].getName() + " " +
                "seeds.list edges.list nodes.list " +
                request.getParams().get("penalty") + " " +
                request.getParams().get("maxit") + (request.getParams().get("multiple").equals("true") ? " " + request.getParams().get("trees") : "");

    }

    @Override
    public void prepareJobFiles(File tempDir, JobRequest req, Graph g, HashMap<Integer, Pair<String, String>> domainMap) {
        File seed = new File(tempDir, "seeds.list");
        if (req.selection)
            utils.writeSeedFile( seed, req.nodes, domainMap,"",true);
        else
            utils.writeSeedFile(req.params.get("type"), seed, g, domainMap,"",true);
    }

    @Override
    public ProcessBuilder getExecutionEnvironment(String[] command) {
        return new ProcessBuilder(command);
    }

    @Override
    public void getResults(HashMap<Integer, HashMap<String, Object>> nodes, HashMap<Integer, HashMap<Integer, HashMap<String, Object>>> edges, File tempDir, Job j, HashMap<Integer, HashMap<String, Integer>> domainMaps) {
        HashMap<String, Integer> domainMap = domainMaps.get(Graphs.getNode(j.getTarget()));
        try (BufferedReader br = ReaderUtils.getBasicReader(new File(tempDir, "nodes.list"))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                LinkedList<String> split = StringUtils.split(line, "\t");
                int id = domainMaps.get(Graphs.getNode(j.getTarget())).get(split.get(0));
                nodes.put(id, new HashMap<>());
                nodes.get(id).put("participation_number", split.get(1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedReader br = ReaderUtils.getBasicReader(new File(tempDir, "edges.list"))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                LinkedList<String> split = StringUtils.split(line, "\t");
                int id1 = domainMap.get(split.get(0));
                int id2 = domainMap.get(split.get(1));
                if (id2 < id1) {
                    int tmp = id1;
                    id1 = id2;
                    id2 = tmp;
                }
                if (!edges.containsKey(id1))
                    edges.put(id1, new HashMap<>());
                if (!edges.get(id1).containsKey(id2))
                    edges.get(id1).put(id2, new HashMap<>());

                HashMap<String, Object> e = edges.get(id1).get(id2);

                e.put("memberOne", split.get(0));
                e.put("memberTwo", split.get(1));
                e.put("participation_number", Integer.parseInt(split.get(2)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasCustomEdges() {
        return true;
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
    public void setTreads(Job j, int treads) {
        j.setCommand(j.getCommand()+" "+treads);
    }

    @Override
    public boolean hasMultipleResultFiles() {
        return true;
    }

    @Override
    public void createIndex() {

    }

    @Override
    public File getResultFile(File tmp) {
        File result = null;
        Arrays.stream(tmp.listFiles()).forEach(f -> {
            if (!f.getName().equals("edges.list") && !f.getName().equals("nodes.list"))
                f.delete();
        });

        if (tmp.list().length > 0) {
            result = new File(tmp, "results.zip");
            ZipUtil.packEntries(tmp.listFiles(), result);
        }
        return result;
    }
}
