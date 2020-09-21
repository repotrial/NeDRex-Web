package de.exbio.reposcapeweb.communication.reponses;

import de.exbio.reposcapeweb.communication.requests.Filter;
import de.exbio.reposcapeweb.communication.requests.GraphRequest;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.filter.NodeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WebGraphService {

    private final EdgeController edgeController;
    private final NodeController nodeController;

    @Autowired
    public WebGraphService(
            NodeController nodeController,
            EdgeController edgeController) {
        this.edgeController = edgeController;
        this.nodeController = nodeController;
    }

    public WebGraph getGraph(GraphRequest request) {
        WebGraph graph = new WebGraph("All requested", false);
        TreeMap<String, HashMap<Integer, WebNode>> nodeIds = new TreeMap<>();
        request.nodes.forEach((k, v) -> {
            String prefix = k.substring(0, 3) + "_";
            NodeFilter nf = nodeController.getFilter(k);
            if (v.filters != null)
                for (Filter filter : v.filters) {
                    nf.apply(filter);
                }
            HashMap<Integer, WebNode> ids = new HashMap<>();
            nodeIds.put(k, ids);
            nf.toList(-1).forEach(entry -> {
                ids.put(entry.getNodeId(), new WebNode(prefix, entry.getNodeId(), entry.getNodeId() + "", k));
            });
        });
        String[] nodes = nodeIds.keySet().toArray(new String[nodeIds.size()]);
        for (int i = 0; i < nodes.length; i++) {
            String prefixI = nodes[i].substring(0, 3) + "_";
            for (int j = 0; j < nodes.length; j++) {
                if (i > j)
                    continue;
                String prefixJ = nodes[j].substring(0, 3) + "_";
                String nodeI = nodes[i];
                String nodeJ = nodes[j];
                LinkedList<String> edgeNames = edgeController.mapEdgeName(nodeI, nodeJ);
                if(edgeNames==null) {
                    continue;
                }
                edgeNames.forEach(edgeName -> {
                    if (request.edges.containsKey(edgeName)) {
                        //TODO add edgeFilters
                        nodeIds.get(nodeI).forEach((k1, v1) -> {
                            nodeIds.get(nodeJ).forEach((k2, v2) -> {
                                //TODO chanche toGetEdges() for each direction -> iterate through results, set hasEdge true for the matching
                                if (edgeController.isEdge(edgeName, nodeI, nodeJ, k1, k2)) {
                                    v1.hasEdge = true;
                                    v2.hasEdge = true;
                                    graph.addEdge(new WebEdge(prefixI, k1, prefixJ, k2));
                                }
                            });
                        });
                    }
                });
            }
        }
        graph.addNodes(request.connectedOnly ?
                nodeIds.values().stream().map(HashMap::values).flatMap(Collection::stream).collect(Collectors.toSet())
                :
                nodeIds.values().stream().map(HashMap::values).flatMap(Collection::stream).filter(WebNode::hasEdge).collect(Collectors.toSet())
        );
        graph.drawDoubleCircular();
        return graph;
    }

    public WebGraph getNeuroDrugs() {
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
