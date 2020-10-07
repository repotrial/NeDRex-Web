package de.exbio.reposcapeweb.communication.reponses;

import de.exbio.reposcapeweb.ReposcapewebApplication;
import de.exbio.reposcapeweb.communication.requests.Filter;
import de.exbio.reposcapeweb.communication.requests.GraphRequest;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.filter.NodeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WebGraphService {
    private Logger log = LoggerFactory.getLogger(WebGraphService.class);

    private final EdgeController edgeController;
    private final NodeController nodeController;
    private final DbCommunicationService dbCommunicationService;
    private HashMap<String,WebGraph> cache = new HashMap<>();


    @Autowired
    public WebGraphService(
            NodeController nodeController,
            EdgeController edgeController,
            DbCommunicationService dbCommunicationService) {
        this.edgeController = edgeController;
        this.nodeController = nodeController;
        this.dbCommunicationService=dbCommunicationService;
    }

    public WebGraph getMetaGraph(){
        WebGraph graph = new WebGraph("Metagraph",true);

        graph.addNode(new WebNode(1,"Drug","drug","Drug"));
        graph.addNode(new WebNode(2,"Protein","protein","Protein"));
        graph.addNode(new WebNode(3,"Pathway","pathway","Pathway"));
        graph.addNode(new WebNode(4,"Gene","gene","Gene"));
        graph.addNode(new WebNode(5,"Disorder","disorder","Disorder"));


        graph.addEdge(new WebEdge(1,2,"DrugHasTargetProtein"));
        graph.addEdge(new WebEdge(1,4,"DrugHasTargetGene").setDashes(true));
        graph.addEdge(new WebEdge(2,2,"ProteinInteractsWithProtein"));
        graph.addEdge(new WebEdge(2,3,"ProteinInPathway"));
        graph.addEdge(new WebEdge(2,4,"ProteinEncodedBy"));
        graph.addEdge(new WebEdge(2,5,"ProteinAssociatedWithDisorder").setDashes(true));
        graph.addEdge(new WebEdge(4,4,"GeneInteractsWithGene").setDashes(true));
        graph.addEdge(new WebEdge(4,5,"GeneAssociatedWithDisorder"));
        graph.addEdge(new WebEdge(5,5,"DisorderComorbidWithDisorder"));
        graph.addEdge(new WebEdge(5,5,"DisorderIsADisorder"));
        graph.addEdge(new WebEdge(1,5,"DrugHasIndication"));

        return graph;
    }

    public WebGraph getCachedGraph(String graphId){
        return cache.get(graphId);
    }

    public WebGraphList getListFromGraph(String id){
        return getListFromGraph(getCachedGraph(id));
    }

    public WebGraphList getListFromGraph(WebGraph graph){
        dbCommunicationService.scheduleRead();


        //TODO write conversion from graph to list -> maybe cache graph differently
        return new WebGraphList(graph.id);
    }

    public WebGraph getGraph(GraphRequest request) {
        dbCommunicationService.scheduleRead();
        WebGraph graph = new WebGraph("All requested", false);
        cache.put(graph.id,graph);
        TreeMap<String, HashMap<Integer, WebNode>> nodeIds = new TreeMap<>();
        request.nodes.forEach((k, v) -> {
            String prefix = k.substring(0, 3) + "_";
            NodeFilter nf = nodeController.getFilter(k);
            if (v.filters != null)
                for (Filter filter : v.filters) {
                    nf = nf.apply(filter);
                }
            HashMap<Integer, WebNode> ids = new HashMap<>();
            nodeIds.put(k, ids);
            nf.toList(-1).forEach(entry -> {
                ids.put(entry.getNodeId(), new WebNode(prefix, entry.getNodeId(), entry.getNodeId() + "", entry.getName(), k));
            });
        });
        String[] nodes = nodeIds.keySet().toArray(new String[nodeIds.size()]);
        HashSet<String> conenctedNodes = new HashSet<>();
        for (int i = 0; i < nodes.length; i++) {
            String prefixI = nodes[i].substring(0, 3) + "_";
            for (int j = 0; j < nodes.length; j++) {
                if (i > j)
                    continue;
                String prefixJ = nodes[j].substring(0, 3) + "_";
                String nodeI = nodes[i];
                String nodeJ = nodes[j];
                LinkedList<String> edgeNames = edgeController.mapEdgeName(nodeI, nodeJ);
                if (edgeNames == null) {
                    log.debug("Edge for " + nodeI + " & " + nodeJ + " do not exist!");
                    continue;
                }
                edgeNames.forEach(edgeName -> {
                    if (request.edges.containsKey(edgeName)) {
                        //TODO add edgeFilters
                        nodeIds.get(nodeI).forEach((k1, v1) -> {
                            try {
                                if (request.connectedOnly & conenctedNodes.contains(nodeI) & !v1.hasEdge)
                                    return;

                                edgeController.getEdges(edgeName, nodeI, k1).forEach(t -> {
                                    try {
                                        if (request.connectedOnly & conenctedNodes.contains(nodeJ) & !nodeIds.get(nodeJ).get(t).hasEdge)
                                            return;
                                        nodeIds.get(nodeJ).get(t).hasEdge = true;
                                        v1.hasEdge = true;
                                        graph.addEdge(new WebEdge(prefixI, k1, prefixJ, t));
                                    } catch (NullPointerException ignore) {

                                    }
                                });
                            } catch (NullPointerException ignore) {
                            }
                        });
                        if (request.nodes.get(nodeI).filters.length == 0)
                            conenctedNodes.add(nodeI);
                        if (request.nodes.get(nodeJ).filters.length == 0)
                            conenctedNodes.add(nodeJ);
                    }
                });
            }
        }
        graph.addNodes(request.connectedOnly ?
                nodeIds.values().stream().map(HashMap::values).flatMap(Collection::stream).filter(WebNode::hasEdge).collect(Collectors.toSet())
                :
                nodeIds.values().stream().map(HashMap::values).flatMap(Collection::stream).collect(Collectors.toSet())
        );
//        graph.drawDoubleCircular();
        return graph;
    }

    public WebGraph getNeuroDrugs() {
        dbCommunicationService.scheduleRead();
        WebGraph graph = new WebGraph("Neuro Disorer Drugs", true);
        HashMap<Integer, WebNode> ids = new HashMap<>();
        HashSet<Integer> drugs = new HashSet<>();
        nodeController.filterDisorder().filterMatches(".*((neur)|(prion)|(brain)|(enceph)|(cogni)).*", -1).forEach(entry -> {
            WebNode n = new WebNode("dis_", entry.getNodeId(), entry.getName(), "disorder");
            ids.put(entry.getNodeId(), n);
            n.setTitle(entry.getName());
            HashSet<Integer> indications = edgeController.getDrugHasIndicationTo(entry.getNodeId());
            graph.addNode(n);
            try {
                drugs.addAll(indications);
                indications.forEach(drug -> {
                    graph.addEdge(new WebEdge("dr_" + drug, "dis_" + entry.getNodeId()));
                    n.hasEdge = true;
                });
            } catch (NullPointerException e) {
            }
        });
        nodeController.findDrugs(drugs).forEach(d -> {
            WebNode n = new WebNode("dr_", d.getId(), d.getDisplayName(), "drugs");
            n.hasEdge = true;
            n.setTitle(d.getDisplayName());
            graph.addNode(n);
        });
        graph.drawDoubleCircular();
        return graph;
    }


    public WebGraph getCancerComorbidity() {
        dbCommunicationService.scheduleRead();
        WebGraph graph = new WebGraph("Cancer comorbidity Network", false);
        HashMap<Integer, WebNode> ids = new HashMap<>();
        nodeController.filterDisorder().filterMatches(".*(cancer|tumor|tumour|carc).*", -1).forEach(entry -> {
            WebNode n = new WebNode(entry.getNodeId(), entry.getNodeId() + "", "disorder");
            ids.put(entry.getNodeId(), n);
            n.setTitle(entry.getName());
        });

        ids.keySet().forEach(id1 -> ids.keySet().forEach(id2 -> {
            if (edgeController.isDisorderComorbidWithDisorder(id1, id2)) {
                graph.addEdge(new WebEdge(id1, id2));
                ids.get(id1).hasEdge = true;
                ids.get(id2).hasEdge = true;
            }
        }));

        ids.values().forEach(graph::addNode);
        graph.drawDoubleCircular();
        return graph;
    }


}
