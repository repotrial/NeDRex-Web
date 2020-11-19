package de.exbio.reposcapeweb.communication.reponses;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.cache.Edge;
import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.cache.Node;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.requests.*;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.history.GraphHistory;
import de.exbio.reposcapeweb.db.history.HistoryController;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.filter.NodeFilter;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.StatUtils;
import de.exbio.reposcapeweb.utils.WriterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WebGraphService {
    private Logger log = LoggerFactory.getLogger(WebGraphService.class);

    private final EdgeController edgeController;
    private final NodeController nodeController;
    private final HistoryController historyController;
    private final ObjectMapper objectMapper;
    private final DbCommunicationService dbCommunicationService;
    private HashMap<String, Graph> cache = new HashMap<>();
    private HashMap<String, Object> colorMap;


    @Autowired
    public WebGraphService(
            NodeController nodeController,
            EdgeController edgeController,
            DbCommunicationService dbCommunicationService,
            ObjectMapper objectMapper,
            HistoryController historyController) {
        this.edgeController = edgeController;
        this.nodeController = nodeController;
        this.dbCommunicationService = dbCommunicationService;
        this.objectMapper = objectMapper;
        this.historyController = historyController;
    }

    public WebGraph getMetaGraph() {
        WebGraph graph = new WebGraph("Metagraph", true, historyController.getGraphId());

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

        graph.setColorMap(this.getColorMap(null));

        return graph;
    }

    public HashMap<String, Object> getColorMap(Collection<String> nodetypes) {
        if (nodetypes == null || colorMap == null) {
            //TODO read from config json

            colorMap = new HashMap<>();
            ArrayList<String[]> colors = new ArrayList<>(Arrays.asList(
                    new String[]{"drug", "#00CC96", "#b4cdcc"},
                    new String[]{"disorder", "#EF553B", "#ecd0cb"},
                    new String[]{"gene", "#636EFA", "#d6d9f8"},
                    new String[]{"protein", "#19d3f3", "#bcdfe5"},
                    new String[]{"pathway", "#fecb52", "#fae6c1"}));
            colors.forEach(c -> {
                HashMap<String, String> node = new HashMap<>();
                colorMap.put(c[0], node);
                node.put("main", c[1]);
                node.put("light", c[2]);
            });
        }
        if (nodetypes == null)
            nodetypes = new HashSet<>(colorMap.keySet());
        HashMap<String, Object> out = new HashMap<>();
        nodetypes.forEach(n -> out.put(n, colorMap.get(n)));
        return out;
    }

    public Graph getCachedGraph(String graphId) {
        if (graphId != null && !graphId.equals("null")) {
            if (!cache.containsKey(graphId))
                importGraph(graphId);
            return cache.get(graphId);
        }
        return null;
    }

    public WebGraphList getList(String id, CustomListRequest req) {
        Graph g = getCachedGraph(id);
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
                    finalReq.attributes.get("edges").put(g.getEdge(k), k < 0 ? new String[]{"id", "idOne", "idTwo", "weight", "jaccardIndex"} : edgeController.getListAttributes(k));
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
                finalList.setTypes("nodes", stringType, nodeController.getAttributes(type), nodeController.getAttributeTypes(type), nodeController.getIdAttributes(type));
            });
            log.debug("Converting edges from Graph to WebList for " + id);

            g.getEdges().forEach((type, edgeList) -> {
                String stringType = g.getEdge(type);
                if (!finalReq1.attributes.get("edges").containsKey(stringType))
                    return;

                String[] attributes = finalReq1.attributes.get("edges").get(stringType);
                HashSet<String> attrs = new HashSet<>(Arrays.asList(attributes));
                finalList.addListAttributes("edges", stringType, attributes);
                List<PairId> edges = edgeList.stream().map(e -> new PairId(e.getId1(), e.getId2())).collect(Collectors.toList());
                if (type < 0) {
                    Pair<Integer, Integer> nodeIds = g.getNodesfromEdge(type);
                    String[] attributeArray = new String[]{"id", "idOne", "idTwo", "memberOne", "memberTwo", "weight", "jaccardIndex"};
                    finalList.addAttributes("edges", stringType, attributeArray);
                    LinkedList<String> attrMaps = edges.stream().map(p -> getCustomEdgeAttributeList(g, type, p)).collect(Collectors.toCollection(LinkedList::new));
                    finalList.addEdges(stringType, attrMaps);
                    finalList.setTypes("edges", stringType, attributeArray, new String[]{"", "numeric", "numeric", "", "", "numeric", "numeric"}, new boolean[]{true, true, true, true, true, false, false});
                } else {
                    String[] attributeArray = edgeController.getAttributes(type);
                    finalList.addAttributes("edges", stringType, attributeArray);

                    LinkedList<String> attrMaps = edgeController.edgesToAttributeList(type, edges, attrs);
                    finalList.addEdges(stringType, attrMaps);
                    finalList.setTypes("edges", stringType, attributeArray, edgeController.getAttributeTypes(type), edgeController.getIdAttributes(type));

                }
            });


            if (!custom)
                getCachedGraph(id).setWebList(list);

        }
        return list;
    }

    public WebGraph getWebGraph(GraphRequest request) {
        Graph g = getGraph(request);
        return g.toWebGraph(getColorMap(g.getNodes().keySet().stream().map(Graphs::getNode).collect(Collectors.toSet())));

    }

    public WebGraphInfo updateGraph(UpdateRequest request) {
        Graph basis = getCachedGraph(request.id);
        Graph g = new Graph(historyController.getGraphId());
        g.setParent(request.id);
        request.nodes.forEach((type, ids) -> {
            if (ids.length == 0)
                return;
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
            g.saveNodeFilter(type, new NodeFilter(basis.getNodeFilter(type), Arrays.asList(ids)));
        });
        request.edges.forEach((type, ids) -> {
            if (ids.length == 0)
                return;
            int typeId = basis.getEdge(type);
            HashSet<String> edgeIds = new HashSet<>(Arrays.asList(ids));
            if (typeId < 0) {
                Pair<Integer, Integer> nodeIds = basis.getNodesfromEdge(typeId);
                g.addCollapsedEdges(nodeIds.getFirst(), nodeIds.getSecond(), type, basis.getEdges().get(typeId).stream().filter(e -> edgeIds.contains(e.getId1() + "-" + e.getId2())).collect(Collectors.toCollection(LinkedList::new)));
                g.addCollapsedWeights(type, filterIdMaps(basis.getCustomEdgeWeights().get(typeId), g.getNodes().get(nodeIds.first).keySet(), g.getNodes().get(nodeIds.second).keySet()));
                g.addCollapsedJaccardIndex(type, filterIdMaps(basis.getCustomJaccardIndex().get(typeId), g.getNodes().get(nodeIds.first).keySet(), g.getNodes().get(nodeIds.second).keySet()));
            } else {
                g.addEdges(typeId, basis.getEdges().get(typeId).stream().filter(e -> edgeIds.contains(e.getId1() + "-" + e.getId2())).collect(Collectors.toCollection(LinkedList::new)));
            }
        });

        cache.put(g.getId(), g);
        return g.toInfo();
    }

    private <T extends Number> HashMap<Integer, HashMap<Integer, T>> filterIdMaps(HashMap<Integer, HashMap<Integer, T>> filterMap, Set<Integer> idSet1, Set<Integer> idSet2) {
        HashMap<Integer, HashMap<Integer, T>> out = new HashMap<>();
        filterMap.forEach((k, v) -> {
            if (idSet1.contains(k))
                v.forEach((k2, value) -> {
                    if (idSet2.contains(k2)) {
                        if (!out.containsKey(k))
                            out.put(k, new HashMap<>());
                        out.get(k).put(k2, value);
                    }
                });
        });
        return out;
    }

    public WebGraph getWebGraph(String id) {
        Graph g = getCachedGraph(id);
        return g.toWebGraph(getColorMap(g.getNodes().keySet().stream().map(Graphs::getNode).collect(Collectors.toSet())));
    }

    public Graph getGraph(GraphRequest request) {
        Graph g = getCachedGraph(request.id);
        if (g != null)
            return g;
        else
            g = new Graph(historyController.getGraphId());

        dbCommunicationService.scheduleRead();


        cache.put(g.getId(), g);

        TreeMap<Integer, HashMap<Integer, Node>> nodeIds = new TreeMap<>();
        Graph finalG = g;
        request.nodes.forEach((k, v) -> {
            NodeFilter nf = nodeController.getFilter(k);
            if (v.filters != null)
                for (Filter filter : v.filters) {
                    nf = nf.apply(filter);
                }
            finalG.saveNodeFilter(k, nf);
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
                        finalG.addEdges(edgeId, edges);
                        if (request.nodes.get(Graphs.getNode(nodeI[0])).filters.length == 0)
                            connectedNodes.add(nodeI[0]);
                        if (request.nodes.get(Graphs.getNode(nodeJ[0])).filters.length == 0)
                            connectedNodes.add(nodeJ[0]);
                    }
                });
            }
        }
        if (request.connectedOnly) {
            nodeIds.forEach((type, nodeMap) -> nodeMap.entrySet().stream().filter(e -> e.getValue().hasEdge()).forEach(e -> finalG.addNode(type, e.getValue())));
        } else
            nodeIds.forEach((type, nodeMap) -> nodeMap.forEach((key, value) -> finalG.addNode(type, value)));

        return g;
    }

//    public WebGraph getNeuroDrugs() {
//        dbCommunicationService.scheduleRead();
//        WebGraph graph = new WebGraph("Neuro Disorer Drugs", true, historyController.getGraphId());
//        HashMap<Integer, WebNode> ids = new HashMap<>();
//        HashSet<Integer> drugs = new HashSet<>();
//        nodeController.filterDisorder().filterMatches(".*((neur)|(prion)|(brain)|(enceph)|(cogni)).*", -1).forEach(entry -> {
//            WebNode n = new WebNode("dis_", entry.getNodeId(), entry.getName(), "disorder");
//            ids.put(entry.getNodeId(), n);
//            n.setTitle(entry.getName());
//            HashSet<Integer> indications = edgeController.getDrugHasIndicationTo(entry.getNodeId());
//            graph.addNode(n);
//            try {
//                drugs.addAll(indications);
//                indications.forEach(drug -> {
//                    graph.addEdge(new WebEdge("dr_" + drug, "dis_" + entry.getNodeId()));
//                    n.hasEdge = true;
//                });
//            } catch (NullPointerException e) {
//            }
//        });
//        nodeController.findDrugs(drugs).forEach(d -> {
//            WebNode n = new WebNode("dr_", d.getId(), d.getDisplayName(), "drugs");
//            n.hasEdge = true;
//            n.setTitle(d.getDisplayName());
//            graph.addNode(n);
//        });
////        graph.drawDoubleCircular();
//        return graph;
//    }


//    public WebGraph getCancerComorbidity() {
//        dbCommunicationService.scheduleRead();
//        WebGraph graph = new WebGraph("Cancer comorbidity Network", false, historyController.getGraphId());
//        HashMap<Integer, WebNode> ids = new HashMap<>();
//        nodeController.filterDisorder().filterMatches(".*(cancer|tumor|tumour|carc).*", -1).forEach(entry -> {
//            WebNode n = new WebNode(entry.getNodeId(), entry.getNodeId() + "", "disorder");
//            ids.put(entry.getNodeId(), n);
//            n.setTitle(entry.getName());
//        });
//
//        ids.keySet().forEach(id1 -> ids.keySet().forEach(id2 -> {
//            if (edgeController.isDisorderComorbidWithDisorder(id1, id2)) {
//                graph.addEdge(new WebEdge(id1, id2));
//                ids.get(id1).hasEdge = true;
//                ids.get(id2).hasEdge = true;
//            }
//        }));
//
//        ids.values().forEach(graph::addNode);
////        graph.drawDoubleCircular();
//        return graph;
//    }

    public Suggestions getSuggestions(SuggestionRequest request) {
        Graph graph = getCachedGraph(request.gid);
        Suggestions suggestions = new Suggestions(request.gid, request.query);

        if (request.type.equals("nodes")) {
            NodeFilter nf = graph.getNodeFilter(request.name).contains(request.query);
            HashSet<Integer> ids = new HashSet<>(graph.getNodes().get(Graphs.getNode(request.name)).keySet());
            suggestions.setDistinct(nf.getDistinctMap(), ids);
            suggestions.setUnique(nf.getUniqueMap(), ids);
        } else {

        }


        return suggestions;
    }

    public SelectionResponse getSelection(SelectionRequest request) {
        SelectionResponse selection = new SelectionResponse();
        Graph graph = getCachedGraph(request.gid);

        if (request.edge) {
            getEdgeSelection(graph, selection, request);
        }
        if (request.node) {
            getNodeSelection(graph, selection, request);
        }
        return selection;
    }

    private void getEdgeSelection(Graph graph, SelectionResponse selection, SelectionRequest request) {
        HashMap<Integer, HashSet<Integer>> seed = new HashMap<>();
        request.nodes.forEach((k, v) -> seed.put(Graphs.getNode(k), new HashSet<>(Arrays.asList(v))));
        request.edges.keySet().forEach(k -> {
            int eid = graph.getEdge(k);
            HashSet<Edge> edges = new HashSet<>();
            Pair<Integer, Integer> nodeIds = graph.getNodesfromEdge(eid);
            HashSet<Integer> ids1 = seed.get(nodeIds.first);
            HashSet<Integer> ids2 = seed.get(nodeIds.second);
            HashSet<Integer> nodes1 = new HashSet<>();
            HashSet<Integer> nodes2 = new HashSet<>();

            graph.getEdges().get(eid).forEach(e -> {
                boolean cont1 = ids1.contains(e.getId1());
                boolean cont2 = ids2.contains(e.getId2());
                if ((cont1 & cont2)) {
                    edges.add(new Edge(e.getId1(), e.getId2()));
                } else if (request.extend) {
                    if (!cont1 & cont2) {
                        if (seed.containsKey(nodeIds.first))
                            nodes1.add(e.getId1());
                        edges.add(new Edge(e.getId1(), e.getId2()));
                    } else if (cont1) {
                        if (seed.containsKey(nodeIds.second))
                            nodes2.add(e.getId2());
                        edges.add(new Edge(e.getId1(), e.getId2()));
                    }
                }

            });
            selection.addEdges(k, edges);
            selection.addNodes(Graphs.getNode(nodeIds.first), nodes1);
            selection.addNodes(Graphs.getNode(nodeIds.second), nodes2);
        });
    }

    private void getNodeSelection(Graph graph, SelectionResponse selection, SelectionRequest request) {
        HashMap<Integer, HashMap<Integer, HashSet<Integer>>> seed = new HashMap<>();
        request.edges.forEach((k, v) -> {
            HashMap<Integer, HashSet<Integer>> edges = new HashMap<>();
            seed.put(graph.getEdge(k), edges);
            Arrays.stream(v).forEach(e -> {
                if (!edges.containsKey(e.getId1()))
                    edges.put(e.getId1(), new HashSet<>());
                edges.get(e.getId1()).add(e.getId2());
            });

        });
        request.edges.keySet().forEach(k -> {
            int eid = graph.getEdge(k);
            HashSet<Edge> edges = new HashSet<>();
            Pair<Integer, Integer> nodeIds = graph.getNodesfromEdge(eid);
            HashMap<Integer, HashSet<Integer>> eids = seed.get(eid);
            HashSet<Integer> nodes1 = new HashSet<>();
            HashSet<Integer> nodes2 = new HashSet<>();

            HashMap<Integer, Node> ids1 = graph.getNodes().get(nodeIds.first);
            HashMap<Integer, Node> ids2 = graph.getNodes().get(nodeIds.second);

            eids.forEach((n1, ns) -> {
                boolean cont1 = ids1.containsKey(n1);
                ns.forEach(n2 -> {
                    boolean cont2 = ids2.containsKey(n2);

                    if ((cont1 & cont2)) {
                        if (request.nodes.containsKey(Graphs.getNode(nodeIds.first)))
                            nodes1.add(n1);
                        if (request.nodes.containsKey(Graphs.getNode(nodeIds.second)))
                            nodes2.add(n2);
                    }
                });
            });
            selection.addEdges(k, edges);
            selection.addNodes(Graphs.getNode(nodeIds.first), nodes1);
            selection.addNodes(Graphs.getNode(nodeIds.second), nodes2);
        });
    }

    public WebGraphInfo getExtension(ExtensionRequest request) {
        Graph g = getCachedGraph(request.gid).clone(historyController.getGraphId());
        cache.put(g.getId(), g);
        HashSet<String> requestedEdges = new HashSet<>(Arrays.asList(request.edges));
        while (!requestedEdges.isEmpty()) {
            HashSet<String> added = new HashSet<>();
            requestedEdges.forEach(e -> {
                int edgeId = g.getEdge(e);
                Pair<Integer, Integer> nodeIds = g.getNodesfromEdge(edgeId);
                if (!g.getNodes().containsKey(nodeIds.first) & !g.getNodes().containsKey(nodeIds.second))
                    return;
                added.add(e);
                LinkedList<Edge> edges = new LinkedList<>();
                HashSet<Integer> nodes = new HashSet<>();
                if (g.getNodes().containsKey(nodeIds.first) & g.getNodes().containsKey(nodeIds.second) && !nodeIds.first.equals(nodeIds.second)) {
                    g.getNodes().get(nodeIds.first).keySet().forEach(nodeId1 -> {

                        try {
                            if (!nodeIds.first.equals(nodeIds.second))
                                edgeController.getEdges(edgeId, nodeIds.first, nodeId1).forEach(nodeId2 -> {
                                    if (g.getNodes().get(nodeIds.second).containsKey(nodeId2))
                                        edges.add(new Edge(nodeId1, nodeId2));
                                });
                        } catch (NullPointerException ignore) {
                        }
                    });

                } else if (nodeIds.first.equals(nodeIds.second)) {
                    g.getNodes().get(nodeIds.first).keySet().forEach(nodeId1 -> {
                        edgeController.getEdges(edgeId, nodeIds.first, nodeId1).forEach(nodeId2 -> {
                            edges.add(!edgeController.getDirection(edgeId) & nodeId1 > nodeId2 ? new Edge(nodeId2, nodeId1) : new Edge(nodeId1, nodeId2));
                            if (!g.getNodes().get(nodeIds.second).containsKey(nodeId2)) {
                                nodes.add(nodeId2);
                            }
                        });

                    });
                    HashSet<Integer> allNodes = new HashSet<>(nodes);
                    allNodes.addAll(g.getNodes().get(nodeIds.first).keySet());
                    NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(nodeIds.first)), allNodes);
                    g.saveNodeFilter(Graphs.getNode(nodeIds.first), nf);

                    HashMap<Integer, Node> nodeMap = g.getNodes().get(nodeIds.first);
                    nf.toList(-1).forEach(entry -> {
                        if (!nodeMap.containsKey(entry.getNodeId()))
                            nodeMap.put(entry.getNodeId(), new Node(entry.getNodeId(), entry.getName()));
                    });
                } else {

                    int existing = g.getNodes().containsKey(nodeIds.first) ? nodeIds.first : nodeIds.second;
                    int adding = existing == nodeIds.first ? nodeIds.second : nodeIds.first;

                    g.getNodes().get(existing).keySet().forEach(nodeId1 -> {
                        try {
                            HashSet<Integer> add = edgeController.getEdges(edgeId, existing, nodeId1);
                            nodes.addAll(add);
                            if (existing == nodeIds.getFirst())
                                add.forEach(nodeId2 ->
                                        edges.add(new Edge(nodeId1, nodeId2))
                                );
                            else
                                add.forEach(nodeId2 ->
                                        edges.add(new Edge(nodeId2, nodeId1))
                                );
                        } catch (NullPointerException ignore) {
                        }
                    });

                    NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(adding)), nodes);
                    g.saveNodeFilter(Graphs.getNode(adding), nf);

                    HashMap<Integer, Node> nodeMap = new HashMap<>();
                    nf.toList(-1).forEach(entry -> nodeMap.put(entry.getNodeId(), new Node(entry.getNodeId(), entry.getName())));

                    g.addNodes(adding, nodeMap);
                }
                g.addEdges(edgeId, edges);
            });
            if (added.isEmpty())
                break;
            requestedEdges.removeAll(added);
        }
        return g.toInfo();
    }

    private HashMap<Integer, HashSet<Integer>> prepareEdgeMap(Collection<Edge> edges, boolean firstNodeIsKey) {
        HashMap<Integer, HashSet<Integer>> edgeMap = new HashMap<>();
        if (firstNodeIsKey)
            edges.forEach(e -> {
                if (!edgeMap.containsKey(e.getId1()))
                    edgeMap.put(e.getId1(), new HashSet<>());
                edgeMap.get(e.getId1()).add(e.getId2());
            });
        else
            edges.forEach(e -> {
                if (!edgeMap.containsKey(e.getId2()))
                    edgeMap.put(e.getId2(), new HashSet<>());
                edgeMap.get(e.getId2()).add(e.getId1());
            });
        return edgeMap;
    }

    public WebGraphInfo getCollapsedGraph(CollapseRequest request) {
        Graph g = getCachedGraph(request.gid).clone(historyController.getGraphId());
        cache.put(g.getId(), g);
        int collapseNode = Graphs.getNode(request.node);
        int edgeId1 = g.getEdge(request.edge1);
        Pair<Integer, Integer> nodes1 = g.getNodesfromEdge(g.getEdge(request.edge1));
        HashMap<Integer, HashSet<Integer>> edgeMap1 = prepareEdgeMap(g.getEdges().get(edgeId1), nodes1.first == collapseNode);
        LinkedList<Edge> edges = new LinkedList<>();
        int startNodeId = nodes1.getFirst() == collapseNode ? nodes1.getSecond() : nodes1.getFirst();
        int targetNodeId;
        HashMap<Integer, HashMap<Integer, Integer>> edgeWeights = new HashMap<>();
        HashMap<Integer, HashMap<Integer, Double>> jaccardIndex = new HashMap<>();
        if (request.self) {
            targetNodeId = startNodeId;
            edgeMap1.forEach((middle, startNodes) ->
                    startNodes.forEach(targetNode ->
                            startNodes.forEach(startNode -> {
                                int weight = 1;
                                if (!startNode.equals(targetNode)) {
                                    if (edgeWeights.containsKey(startNode) && edgeWeights.get(startNode).containsKey(targetNode))
                                        weight += edgeWeights.get(startNode).get(targetNode);
                                    if (!edgeWeights.containsKey(startNode)) {
                                        edgeWeights.put(startNode, new HashMap<>());
                                        jaccardIndex.put(startNode, new HashMap<>());
                                    }
                                    if (!edgeWeights.containsKey(startNode) || !edgeWeights.get(startNode).containsKey(targetNode))
                                        edges.add(new Edge(startNode, targetNode));
                                    edgeWeights.get(startNode).put(targetNode, weight);
                                    jaccardIndex.get(startNode).put(targetNode, StatUtils.calculateJaccardIndex(startNodes, startNodes));
                                }
                            })


                    )
            );
        } else {

            int edgeId2 = g.getEdge(request.edge2);
            Pair<Integer, Integer> nodes2 = g.getNodesfromEdge(g.getEdge(request.edge2));
            targetNodeId = nodes2.getFirst() == collapseNode ? nodes2.getSecond() : nodes2.getFirst();
            HashMap<Integer, HashSet<Integer>> edgeMap2 = prepareEdgeMap(g.getEdges().get(edgeId2), nodes2.first == collapseNode);

            edgeMap1.forEach((middle, startNodes) -> {
                try {
                    HashSet<Integer> targetNodes = edgeMap2.get(middle);
                    targetNodes.forEach(targetNode ->
                            startNodes.forEach(startNode -> {
                                int weight = 1;
                                if (!startNode.equals(targetNode)) {
                                    if (edgeWeights.containsKey(startNode) && edgeWeights.get(startNode).containsKey(targetNode))
                                        weight += edgeWeights.get(startNode).get(targetNode);
                                    if (!edgeWeights.containsKey(startNode)) {
                                        edgeWeights.put(startNode, new HashMap<>());
                                        jaccardIndex.put(startNode, new HashMap<>());
                                    }
                                    if (!edgeWeights.containsKey(startNode) || !edgeWeights.get(startNode).containsKey(targetNode))
                                        edges.add(new Edge(startNode, targetNode));
                                    edgeWeights.get(startNode).put(targetNode, weight);
                                    jaccardIndex.get(startNode).put(targetNode, StatUtils.calculateJaccardIndex(startNodes, targetNodes));
                                }
                            })
                    );
                } catch (NullPointerException ignore) {
                }
            });
            if (!request.keep)
                g.getEdges().remove(edgeId2);
        }
        g.addCollapsedEdges(startNodeId, targetNodeId, request.edgeName, edges);
        g.addCollapsedWeights(request.edgeName, edgeWeights);
        g.addCollapsedJaccardIndex(request.edgeName, jaccardIndex);
        if (!request.keep) {
            g.getEdges().remove(edgeId1);
            HashSet<Integer> otherConnectedEdges = new HashSet<>();
            g.getEdges().keySet().forEach(e -> {
                Pair<Integer, Integer> nodeIDs = g.getNodesfromEdge(e);
                if (nodeIDs.getFirst() == collapseNode || nodeIDs.second == collapseNode)
                    otherConnectedEdges.add(e);
            });
            if (otherConnectedEdges.isEmpty())
                g.getNodes().remove(collapseNode);
        }
        return g.toInfo();
    }

    public String getCustomEdgeAttributeList(Graph graph, int edgeId, PairId p) {
        Pair<Integer, Integer> nodeIds = graph.getNodesfromEdge(edgeId);
        HashMap<String, Object> as = new HashMap<>();
        as.put("id", p.getId1() + "-" + p.getId2());
        as.put("idOne", p.getId1());
        as.put("idTwo", p.getId2());
        as.put("memberOne", nodeController.getDomainId(nodeIds.first, p.getId1()));
        as.put("memberTwo", nodeController.getDomainId(nodeIds.second, p.getId2()));
        as.put("weight", graph.getCustomEdgeWeights().get(edgeId).get(p.getId1()).get(p.getId2()));
        as.put("jaccardIndex", graph.getCustomJaccardIndex().get(edgeId).get(p.getId1()).get(p.getId2()));
        try {
            return objectMapper.writeValueAsString(as);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ConnectionGraph getConnectionGraph(String gid) {
        Graph g = getCachedGraph(gid);
        ConnectionGraph cg = new ConnectionGraph();
        g.getEdges().keySet().forEach(eid -> {
            //TODO get real direction
            Pair<Integer, Integer> nodeIds = g.getNodesfromEdge(eid);
            boolean direction = false;
            if (eid > -1)
                direction = edgeController.getDirection(eid);
            cg.addEdge(g.getEdge(eid), eid, direction, nodeIds.getFirst(), nodeIds.getSecond());
        });
        g.getNodes().keySet().forEach(nid -> {
            cg.addNode(Graphs.getNode(nid), nid);
        });
        return cg;
    }

    public String getEdgeDetails(String gid, String name, PairId p) {
        Graph graph = getCachedGraph(gid);
        int edgeId = graph.getEdge(name);
        if (edgeId < 0)
            return getCustomEdgeAttributeList(graph, edgeId, p);
        else {
            try {
                return objectMapper.writeValueAsString(edgeController.edgeToAttributeList(edgeId, p));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void addGraphToHistory(String uid, String gid) {
        Graph g = cache.get(gid);
        GraphHistory history;
        if (g.getParent() == null) {
            history = new GraphHistory(uid, g.getId(), g.toInfo());
        } else {
            history = new GraphHistory(uid, g.getId(), g.toInfo(), historyController.getHistory(g.getParent()));
            cache.remove(gid);
        }
        historyController.save(history);
        exportGraph(g);
    }


    public void exportGraph(Graph g) {
        File out = historyController.getGraphPath(g.getId());
        try {
            WriterUtils.writeTo(out, objectMapper.writeValueAsString(g));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void importGraph(String gid) {
        try {
            log.debug("Importing Graph with id=" + gid);
            Graph g = objectMapper.readValue(historyController.getGraphPath(gid), Graph.class);
            restoreNodeFilters(g);
            this.cache.put(gid, g);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restoreNodeFilters(Graph g) {
        g.getNodes().forEach((k, v) -> {
            String name = Graphs.getNode(k);
            g.saveNodeFilter(name, new NodeFilter(nodeController.getFilter(name), v.keySet()));
        });
    }

    public void applyModuleJob(Job j, Set<Integer> moduleIds) {
        Graph g = getCachedGraph(j.getBasisGraph());
        Graph derived = g.clone(historyController.getGraphId());
        cache.put(derived.getId(),derived);
        if(j.getRequest().algorithm.equals("diamond")){
            int nodeTypeId = Graphs.getNode(j.getRequest().params.get("type"));
            HashSet<Integer> allNodes = new HashSet<>(derived.getNodes().get(nodeTypeId).keySet());
            allNodes.addAll(moduleIds);
            NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(nodeTypeId)),allNodes);
            derived.saveNodeFilter(Graphs.getNode(nodeTypeId),nf);
            derived.addNodes(nodeTypeId,nf.toList(-1).stream().map(e->new Node(e.getNodeId(),e.getName())).collect(Collectors.toList()));
        }
        j.setDerivedGraph(derived.getId());
        addGraphToHistory(j.getUserId(),derived.getId());
    }
}
