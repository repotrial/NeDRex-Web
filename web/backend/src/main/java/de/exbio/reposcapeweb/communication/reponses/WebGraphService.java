package de.exbio.reposcapeweb.communication.reponses;

import de.exbio.reposcapeweb.communication.cache.Edge;
import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.cache.Node;
import de.exbio.reposcapeweb.communication.requests.*;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.filter.NodeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class WebGraphService {
    private Logger log = LoggerFactory.getLogger(WebGraphService.class);

    private final EdgeController edgeController;
    private final NodeController nodeController;
    private final DbCommunicationService dbCommunicationService;
    private HashMap<String, Graph> cache = new HashMap<>();


    @Autowired
    public WebGraphService(
            NodeController nodeController,
            EdgeController edgeController,
            DbCommunicationService dbCommunicationService) {
        this.edgeController = edgeController;
        this.nodeController = nodeController;
        this.dbCommunicationService = dbCommunicationService;
    }

    public WebGraph getMetaGraph() {
        WebGraph graph = new WebGraph("Metagraph", true);

        graph.addNode(new WebNode(1, "Drug", "drug", "Drug"));
        graph.addNode(new WebNode(2, "Protein", "protein", "Protein"));
        graph.addNode(new WebNode(3, "Pathway", "pathway", "Pathway"));
        graph.addNode(new WebNode(4, "Gene", "gene", "Gene"));
        graph.addNode(new WebNode(5, "Disorder", "disorder", "Disorder"));


        graph.addEdge(new WebEdge(1, 2, "DrugHasTargetProtein"));
        graph.addEdge(new WebEdge(1, 4, "DrugHasTargetGene").setDashes(true));
        graph.addEdge(new WebEdge(2, 2, "ProteinInteractsWithProtein"));
        graph.addEdge(new WebEdge(2, 3, "ProteinInPathway"));
        graph.addEdge(new WebEdge(2, 4, "ProteinEncodedBy"));
        graph.addEdge(new WebEdge(2, 5, "ProteinAssociatedWithDisorder").setDashes(true));
        graph.addEdge(new WebEdge(4, 4, "GeneInteractsWithGene").setDashes(true));
        graph.addEdge(new WebEdge(4, 5, "GeneAssociatedWithDisorder"));
        graph.addEdge(new WebEdge(5, 5, "DisorderComorbidWithDisorder"));
        graph.addEdge(new WebEdge(5, 5, "DisorderIsADisorder"));
        graph.addEdge(new WebEdge(1, 5, "DrugHasIndication"));

        return graph;
    }

    public WebGraph getCachedGraph(String graphId) {
        return cache.get(graphId).toWebGraph();
    }

    public WebGraphList getList(String id, CustomListRequest req) {
        Graph g = cache.get(id);


        WebGraphList list = g.toWebList();

        boolean custom = req != null;
        if (list == null || custom) {
            if (req == null) {
                req = new CustomListRequest();
                req.attributes = new HashMap<>();
                CustomListRequest finalReq = req;
                g.getNodes().keySet().forEach(k -> {
                    if (!finalReq.attributes.containsKey("nodes"))
                        finalReq.attributes.put("nodes", new HashMap<>());
                    finalReq.attributes.get("nodes").put(Graphs.getNode(k), nodeController.getListAttributes(k));
                });
                g.getEdges().keySet().forEach(k -> {
                    if (!finalReq.attributes.containsKey("edges"))
                        finalReq.attributes.put("edges", new HashMap<>());
                    finalReq.attributes.get("edges").put(Graphs.getEdge(k), edgeController.getListAttributes(k));
                });
            }
            log.debug("Converting nodes from Graph to WebList for " + id);
            list = new WebGraphList(id);
            WebGraphList finalList = list;
            CustomListRequest finalReq1 = req;
            g.getNodes().forEach((type, nodeMap) -> {
                String stringType = Graphs.getNode(type);
                if (!finalReq1.attributes.get("nodes").containsKey(stringType))
                    return;
                String[] attributes = finalReq1.attributes.get("nodes").get(stringType);
                finalList.addListAttributes("nodes", stringType, attributes);
                finalList.addAttributes("nodes", stringType, nodeController.getAttributes(type));
                finalList.addNodes(stringType, nodeController.nodesToAttributeList(type, nodeMap.keySet(), new HashSet<>(Arrays.asList(attributes))));
                finalList.setTypes("nodes", stringType, nodeController.getAttributes(type), nodeController.getAttributeTypes(type));

            });
            log.debug("Converting edges from Graph to WebList for " + id);

            g.getEdges().forEach((type, edgeList) -> {
                String stringType = Graphs.getEdge(type);
                if (!finalReq1.attributes.get("edges").containsKey(stringType))
                    return;

                String[] attributes = finalReq1.attributes.get("edges").get(stringType);
                finalList.addListAttributes("edges", stringType, attributes);
                finalList.addAttributes("edges", stringType, edgeController.getAttributes(type));
                HashSet<String> attrs = new HashSet<>(Arrays.asList(attributes));

                List<PairId> edges = edgeList.stream().map(e -> new PairId(e.getId1(), e.getId2())).collect(Collectors.toList());


                LinkedList<String> attrMaps = edgeController.edgesToAttributeList(type, edges, attrs);
                finalList.addEdges(stringType, attrMaps);
                finalList.setTypes("edges", stringType, edgeController.getAttributes(type), edgeController.getAttributeTypes(type));
            });


            if (!custom)
                cache.get(id).setWebList(list);
        }
        return list;
    }

    public WebGraph getWebGraph(GraphRequest request) {
        return getGraph(request).toWebGraph();
    }

    public WebGraphInfo updateGraph(UpdateRequest request) {
        Graph basis = cache.get(request.id);
        Graph g = new Graph();
        request.nodes.forEach((type, ids) -> {
            int typeId = Graphs.getNode(type);
            HashSet<Integer> nodeIds = new HashSet<>(Arrays.asList(ids));
            g.addNodes(typeId,
                    basis
                            .getNodes()
                            .get(typeId)
                            .entrySet()
                            .stream().filter(e ->
                            nodeIds.contains(e.getKey()))
                            .map(Map.Entry::getValue)
                            .collect(Collectors.toList()));
        });
        request.edges.forEach((type, ids) -> {
            int typeId = Graphs.getEdge(type);
            HashSet<String> edgeIds = new HashSet<>(Arrays.asList(ids));
            g.addEdges(typeId, basis.getEdges().get(typeId).stream().filter(e -> edgeIds.contains(e.getId1() + "-" + e.getId2())).collect(Collectors.toCollection(LinkedList::new)));
        });

        cache.put(g.getId(), g);
        return g.toInfo();
    }

    public WebGraph getWebGraph(String id) {
        return cache.get(id).toWebGraph();
    }

    public Graph getGraph(GraphRequest request) {
        if (request.id != null && cache.containsKey(request.id))
            return cache.get(request.id);

        dbCommunicationService.scheduleRead();


        Graph g = request.id != null ? new Graph(request.id) : new Graph();


        cache.put(g.getId(), g);

        TreeMap<Integer, HashMap<Integer, Node>> nodeIds = new TreeMap<>();
        request.nodes.forEach((k, v) -> {
            NodeFilter nf = nodeController.getFilter(k);
            if (v.filters != null)
                for (Filter filter : v.filters) {
                    nf = nf.apply(filter);
                }
            g.saveNodeFilter(k, nf);
            HashMap<Integer, Node> ids = new HashMap<>();
            nodeIds.put(Graphs.getNode(k), ids);

            nf.toList(-1).forEach(entry -> {
                ids.put(entry.getNodeId(), new Node(entry.getNodeId(), entry.getName()));
            });
        });
        Integer[] nodes = nodeIds.keySet().toArray(new Integer[nodeIds.size()]);
        HashSet<Integer> connectedNodes = new HashSet<>();
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes.length; j++) {
                if (i > j)
                    continue;
                final int[] nodeI = {nodes[i]};
                final int[] nodeJ = {nodes[j]};

                LinkedList<Integer> edgeIds = Graphs.getEdgesfromNodes(nodeI[0], nodeJ[0]);
                if (edgeIds == null) {
                    log.debug("Edge for " + nodeI[0] + " & " + nodeJ[0] + " do not exist!");
                    continue;
                }
                edgeIds.forEach(edgeId -> {
                    //TODO edge id mapping in frontend
                    if (request.edges.containsKey(Graphs.getEdge(edgeId))) {
                        //TODO add edgeFilters
                        LinkedList<Edge> edges = new LinkedList<>();

                        nodeIds.get(nodeI[0]).forEach((k1, v1) -> {
                            try {
                                if (request.connectedOnly & connectedNodes.contains(nodeI[0]) & !v1.hasEdge()) {
                                    return;
                                }

                                edgeController.getEdges(edgeId, nodeI[0], k1).forEach(t -> {
                                    try {
                                        if (request.connectedOnly & connectedNodes.contains(nodeJ[0]) & !nodeIds.get(nodeJ[0]).get(t).hasEdge())
                                            return;
                                        nodeIds.get(nodeJ[0]).get(t).setHasEdge(true);
                                        v1.setHasEdge(true);
                                        edges.add(new Edge(k1, t));
                                    } catch (NullPointerException ignore) {
                                    }
                                });
                            } catch (NullPointerException ignore) {
                            }
                        });
                        g.addEdges(edgeId, edges);
                        if (request.nodes.get(Graphs.getNode(nodeI[0])).filters.length == 0)
                            connectedNodes.add(nodeI[0]);
                        if (request.nodes.get(Graphs.getNode(nodeJ[0])).filters.length == 0)
                            connectedNodes.add(nodeJ[0]);
                    }
                });
            }
        }
        if (request.connectedOnly) {
            nodeIds.forEach((type, nodeMap) -> nodeMap.entrySet().stream().filter(e -> e.getValue().hasEdge()).forEach(e -> g.addNode(type, e.getValue())));
        }
        else
            nodeIds.forEach((type, nodeMap) -> nodeMap.forEach((key, value) -> g.addNode(type, value)));
        return g;
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

    public Suggestions getSuggestions(SuggestionRequest request) {
        Graph graph = cache.get(request.gid);
        Suggestions suggestions = new Suggestions(request.gid, request.query);

        if (request.type.equals("nodes")) {
            NodeFilter nf = graph.getNodeFilter(request.name).contains(request.query);
            HashSet<Integer> ids = new HashSet<>(graph.getNodes().get(Graphs.getNode(request.name)).keySet());
            suggestions.setDistinct(nf.getDistinctMap(), ids);
            suggestions.setUnique(nf.getUniqueMap(),ids);
        } else {

        }


        return suggestions;
    }
}
