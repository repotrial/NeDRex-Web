package de.exbio.reposcapeweb.communication.reponses;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.cache.Edge;
import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.cache.Node;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.requests.*;
import de.exbio.reposcapeweb.configs.DBConfig;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.history.GraphHistory;
import de.exbio.reposcapeweb.db.history.HistoryController;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.filter.NodeFilter;
import de.exbio.reposcapeweb.tools.ToolService;
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
        graph.getNodes().forEach(n -> graph.setWeight("nodes", n.group, nodeController.getNodeCount(n.group)));

        graph.addEdge(new WebEdge(1, 2, "DrugHasTargetProtein").setTitle("DrugHasTargetProtein"));
        graph.addEdge(new WebEdge(1, 4, "DrugHasTargetGene").setDashes(true).setTitle("DrugHasTargetGene"));
        graph.addEdge(new WebEdge(2, 2, "ProteinInteractsWithProtein").setTitle("ProteinInteractsWithProtein"));
        graph.addEdge(new WebEdge(2, 3, "ProteinInPathway").setTitle("ProteinInPathway"));
        graph.addEdge(new WebEdge(2, 4, "ProteinEncodedBy").setTitle("ProteinEncodedBy"));
        graph.addEdge(new WebEdge(2, 5, "ProteinAssociatedWithDisorder").setDashes(true).setTitle("ProteinAssociatedWithDisorder"));
        graph.addEdge(new WebEdge(4, 4, "GeneInteractsWithGene").setDashes(true).setTitle("GeneInteractsWithGene"));
        graph.addEdge(new WebEdge(4, 5, "GeneAssociatedWithDisorder").setTitle("GeneAssociatedWithDisorder"));
//        graph.addEdge(new WebEdge(5, 5, "DisorderComorbidWithDisorder").setTitle("DisorderComorbidWithDisorder"));
        graph.addEdge(new WebEdge(5, 5, "DisorderIsSubtypeOfDisorder").setTitle("DisorderIsSubtypeOfDisorder"));
        graph.addEdge(new WebEdge(1, 5, "DrugHasIndication").setTitle("DrugHasIndication"));
        graph.addEdge(new WebEdge(1, 5, "DrugHasContraindication").setTitle("DrugHasContraindication"));
        graph.getEdges().forEach(e -> graph.setWeight("edges", e.label, edgeController.getEdgeCount(e.label)));


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
        if(g==null)
            return null;
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
                    finalReq.attributes.get("edges").put(g.getEdge(k), k < 0 ? g.getCustomListAttributes(k) : edgeController.getListAttributes(k));
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
                try {
                    finalList.addMarks("nodes", stringType, g.getMarks().get("nodes").get(type));
                } catch (NullPointerException ignore) {
                }
                LinkedList<String> attributes = new LinkedList<>(Arrays.asList(finalReq1.attributes.get("nodes").get(stringType)));
                List<String> allAttributes = new LinkedList<>(Arrays.asList(nodeController.getAttributes(type)));
                try {
                    g.getCustomNodeAttributeTypes().get(type).forEach((attrName, attrType) -> {
                        if (!attributes.contains(attrName))
                            attributes.add(attrName);
                        if (!allAttributes.contains(attrName))
                            allAttributes.add(attrName);
                    });
                } catch (NullPointerException ignore) {
                }
                finalList.addListAttributes("nodes", stringType, attributes.toArray(new String[]{}));
                finalList.addAttributes("nodes", stringType, allAttributes.toArray(new String[]{}));
                finalList.addNodes(stringType, nodeController.nodesToAttributeList(type, nodeMap.keySet(), new HashSet<>(attributes), g.getCustomNodeAttributes().get(type)));
                finalList.setTypes("nodes", stringType, nodeController.getAttributes(type), nodeController.getAttributeTypes(type), nodeController.getIdAttributes(type), g.getCustomNodeAttributeTypes().get(type));

                try {
                    HashMap<String, HashSet<String>> distinctValues = new HashMap<>();
                    HashSet<String> distinctAttrs = DBConfig.getDistinctAttributes("node", stringType);
                    distinctAttrs.forEach(a -> distinctValues.put(a, new HashSet<>()));
                    finalList.getNodes().get(stringType).forEach(attrs -> {
                        distinctAttrs.forEach(attr -> {
                            Object value = attrs.get(attr);
                            try {
                                if (value instanceof String)
                                    distinctValues.get(attr).add((String) value);
                                else
                                    distinctValues.get(attr).addAll((LinkedList<String>) value);
                            } catch (NullPointerException ignore) {
                            }
                        });
                    });
                    finalList.setDistinctAttributes("nodes", stringType, distinctValues);
                } catch (NullPointerException e) {
                }
            });
            log.debug("Converting edges from Graph to WebList for " + id);

            g.getEdges().forEach((type, edgeList) -> {
                String stringType = g.getEdge(type);
                if (!finalReq1.attributes.get("edges").containsKey(stringType))
                    return;
                try {
                    finalList.addMarks("edges", stringType, g.getMarks().get("edges").get(type));
                } catch (NullPointerException ignore) {
                }
                String[] attributes = finalReq1.attributes.get("edges").get(stringType);
                HashSet<String> attrs = new HashSet<>(Arrays.asList(attributes));
                finalList.addListAttributes("edges", stringType, attributes);
                List<PairId> edges = edgeList.stream().map(e -> new PairId(e.getId1(), e.getId2())).collect(Collectors.toList());
                if (type < 0) {
                    Pair<Integer, Integer> nodeIds = g.getNodesfromEdge(type);
                    String[] attributeArray = g.getCustomListAttributes(type);
                    finalList.addAttributes("edges", stringType, attributeArray);
                    LinkedList<HashMap<String, Object>> attrMaps = edges.stream().map(p -> getCustomEdgeAttributeList(g, type, p)).collect(Collectors.toCollection(LinkedList::new));
                    finalList.addEdges(stringType, attrMaps);
                    finalList.setTypes("edges", stringType, attributeArray, g.getCustomListAttributeTypes(type), g.areCustomListAttributeIds(type), g.getCustomNodeAttributeTypes().get(type));
                } else {
                    String[] attributeArray = edgeController.getAttributes(type);
                    finalList.addAttributes("edges", stringType, attributeArray);

                    LinkedList<HashMap<String, Object>> attrMaps = edgeController.edgesToAttributeList(type, edges, attrs);
                    finalList.addEdges(stringType, attrMaps);
                    finalList.setTypes("edges", stringType, attributeArray, edgeController.isExperimental(type), edgeController.getIdAttributes(type), g.getCustomNodeAttributeTypes().get(type));
                    try {
                        HashMap<String, HashSet<String>> distinctValues = new HashMap<>();
                        HashSet<String> distinctAttrs = DBConfig.getDistinctAttributes("edge", stringType);
                        distinctAttrs.forEach(a -> distinctValues.put(a, new HashSet<>()));
                        finalList.getEdges().get(stringType).forEach(edgeAttrs -> {
                            distinctAttrs.forEach(attr -> {
                                Object value = edgeAttrs.get(attr);
                                try {
                                    if (value instanceof String)
                                        distinctValues.get(attr).add((String) value);
                                    else
                                        distinctValues.get(attr).addAll((LinkedList<String>) value);
                                } catch (NullPointerException ignore) {
                                }
                            });
                        });
                        finalList.setDistinctAttributes("edges", stringType, distinctValues);
                    } catch (NullPointerException e) {
                    }

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
        //TODO rewrite with clone?
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
                int newId = g.getEdge(basis.getEdge(typeId));
                g.addCustomEdgeAttributeTypes(newId, basis.getCustomAttributeTypes(typeId));
                g.addCustomEdgeAttribute(newId, basis.getCustomAttributes(typeId));
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
        if(g==null)
            return null;
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
                    boolean experimental = (request.interactions.containsKey(Graphs.getEdge(edgeId)) && !request.interactions.get(Graphs.getEdge(edgeId)));
                    if (request.edges.containsKey(Graphs.getEdge(edgeId))) {
                        LinkedList<Edge> edges = new LinkedList<>();

                        boolean expOnly = experimental;
                        nodeIds.get(nodeI[0]).forEach((id1, v1) -> {
                            try {
                                if (request.connectedOnly & connectedNodes.contains(nodeI[0]) & !v1.hasEdge()) {
                                    return;
                                }
                                edgeController.getEdges(edgeId, nodeI[0], id1).forEach(id2 -> {
                                    try {
                                        if ((request.connectedOnly & connectedNodes.contains(nodeJ[0]) & !nodeIds.get(nodeJ[0]).get(id2).hasEdge()) | (expOnly && !edgeController.isExperimental(edgeId, id1, id2)))
                                            return;
                                        nodeIds.get(nodeJ[0]).get(id2).setHasEdge(true);
                                        v1.setHasEdge(true);
                                        edges.add(new Edge(id1, id2));
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
        HashSet<String> inducedEdges = new HashSet<>(Arrays.asList(request.induced));
        HashSet<String> requestedEdges = new HashSet<>(Arrays.asList(request.edges));
        while (!requestedEdges.isEmpty()) {
            HashSet<String> added = new HashSet<>();
            requestedEdges.forEach(e -> {
                int edgeId = g.getEdge(e);
                boolean extend = !inducedEdges.contains(e);
                Pair<Integer, Integer> nodeIds = g.getNodesfromEdge(edgeId);
                added.add(e);
                LinkedList<Edge> edges = new LinkedList<>();
                HashSet<Integer> nodes = new HashSet<>();

                if (g.getNodes().containsKey(nodeIds.first) & nodeIds.first.equals(nodeIds.second)) {
                    g.getNodes().get(nodeIds.first).keySet().forEach(nodeId1 -> {
                        try {
                            edgeController.getEdges(edgeId, nodeIds.first, nodeId1).forEach(nodeId2 -> {
                                if (g.getNodes().get(nodeIds.second).containsKey(nodeId2) | extend)
                                    edges.add(!edgeController.getDirection(edgeId) & nodeId1 > nodeId2 ? new Edge(nodeId2, nodeId1) : new Edge(nodeId1, nodeId2));
                                if (!g.getNodes().get(nodeIds.second).containsKey(nodeId2) & extend) {
                                    nodes.add(nodeId2);
                                }
                            });
                        } catch (NullPointerException ignore) {
                        }

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
                    if (extend)
                        g.addNodes(nodeIds.first, nodeMap);
                    g.addEdges(edgeId, edges);
                    return;
                }
                int[] existing = new int[]{g.getNodes().containsKey(nodeIds.first) ? nodeIds.first : nodeIds.second};
                int[] adding = new int[]{(existing[0] == nodeIds.first ? nodeIds.second : nodeIds.first)};
                if (g.getNodes().containsKey(nodeIds.first) & g.getNodes().containsKey(nodeIds.second)) {
                    existing = new int[]{existing[0], adding[0]};
                    adding = new int[]{adding[0], existing[0]};
                    g.getNodes().get(nodeIds.first).keySet().forEach(nodeId1 -> {
                        try {
                            edgeController.getEdges(edgeId, nodeIds.first, nodeId1).forEach(nodeId2 -> {
                                if (g.getNodes().get(nodeIds.second).containsKey(nodeId2))
                                    edges.add(new Edge(nodeId1, nodeId2));
                            });
                        } catch (NullPointerException ignore) {
                        }
                    });
                    if (!extend) {
                        g.addEdges(edgeId, edges);
                        return;
                    }
                }

                HashMap<Integer, HashMap<Integer, Node>> nodeMap = new HashMap<>();
                for (int i = 0; i < existing.length; i++) {
                    int node1 = existing[i];
                    g.getNodes().get(node1).keySet().forEach(nodeId1 -> {
                        try {
                            HashSet<Integer> add = edgeController.getEdges(edgeId, node1, nodeId1);
                            nodes.addAll(add);
                            if (node1 == nodeIds.getFirst())
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


                    int node2 = adding[i];
                    NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(node2)), nodes);
                    g.saveNodeFilter(Graphs.getNode(node2), nf);

//                    HashMap<Integer, Node> nodeMap = new HashMap<>();
                    nodeMap.put(node2, new HashMap<>());
                    nf.toList(-1).forEach(entry -> nodeMap.get(node2).put(entry.getNodeId(), new Node(entry.getNodeId(), entry.getName())));
                }
                nodeMap.forEach(g::addNodes);
//                }
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
        HashMap<Integer, HashMap<Integer, Object>> edgeWeights = new HashMap<>();
        HashMap<Integer, HashMap<Integer, Object>> jaccardIndex = new HashMap<>();
        if (request.self) {
            targetNodeId = startNodeId;
            edgeMap1.forEach((middle, startNodes) ->
                    startNodes.forEach(targetNode ->
                            startNodes.forEach(startNode -> {
                                int weight = 1;
                                if (!startNode.equals(targetNode)) {
                                    if (edgeWeights.containsKey(startNode) && edgeWeights.get(startNode).containsKey(targetNode))
                                        weight += (int) edgeWeights.get(startNode).get(targetNode);
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
                                        weight += (int) edgeWeights.get(startNode).get(targetNode);
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
        int eid = g.getEdge(request.edgeName);
        g.addCustomEdgeAttribute(eid, "weight", edgeWeights);
        g.addCustomAttributeType(eid, "weight", "numeric");
        g.addCustomEdgeAttribute(eid, "jaccardIndex", jaccardIndex);
        g.addCustomAttributeType(eid, "jaccardIndex", "numeric");
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

    public HashMap<String, Object> getCustomEdgeAttributeList(Graph graph, int edgeId, PairId p) {
        Pair<Integer, Integer> nodeIds = graph.getNodesfromEdge(edgeId);
        HashMap<String, Object> as = new HashMap<>();
        as.put("id", p.getId1() + "-" + p.getId2());
        as.put("idOne", p.getId1());
        as.put("idTwo", p.getId2());
        as.put("node1", nodeController.getName(nodeIds.first, p.getId1()));
        as.put("node2", nodeController.getName(nodeIds.second, p.getId2()));
        as.put("memberOne", nodeController.getDomainId(nodeIds.first, p.getId1()));
        as.put("memberTwo", nodeController.getDomainId(nodeIds.second, p.getId2()));
        ArrayList<String> order = new ArrayList<>(Arrays.asList("id", "idOne", "idTwo", "node1", "node2", "memberOne", "memberTwo"));
        graph.getCustomAttributeTypes(edgeId).keySet().forEach(attrName -> {
            as.put(attrName, graph.getCustomAttributes(edgeId).get(p.getId1()).get(p.getId2()).get(attrName));
            order.add(attrName);
        });
        order.add("type");
        as.put("type", graph.getEdge(edgeId));
        as.put("order", order);
        return as;
    }

    public ConnectionGraph getConnectionGraph(String gid) {
        Graph g = getCachedGraph(gid);
        if(g==null)
            return null;
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

    public HashMap<String, Object> getEdgeDetails(String gid, String name, PairId p) {
        Graph graph = getCachedGraph(gid);
        int edgeId = graph.getEdge(name);
        if (edgeId < 0)
            return getCustomEdgeAttributeList(graph, edgeId, p);
        else {
            HashMap<String, Object> details = edgeController.edgeToAttributeList(edgeId, p);
            details.put("order", edgeController.getAttributes(edgeId));
            return details;
        }
    }

    public void addGraphToHistory(String uid, String gid) {
        Graph g = cache.get(gid);
        GraphHistory history;
        if (g.getParent() == null) {
            history = new GraphHistory(uid, g.getId(), g.toInfo());
            historyController.save(history);
        } else {
            history = new GraphHistory(uid, g.getId(), g.toInfo(), historyController.getHistory(g.getParent()));
            historyController.saveDerivedHistory(g.getParent(), history);
            cache.remove(gid);
        }

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
            File o = historyController.getGraphPath(gid);
            if (o == null)
                return;
            Graph g = objectMapper.readValue(o, Graph.class);
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

    public void applyModuleJob(Job j) {
        int nodeTypeId = Graphs.getNode((j.getMethod().equals(ToolService.Tool.CENTRALITY) | j.getMethod().equals(ToolService.Tool.TRUSTRANK) ? "drug" : j.getMethod().equals(ToolService.Tool.BICON) ? "gene" : j.getTarget()));

        boolean onlyKeepResult = j.getParams().containsKey("nodesOnly") && j.getParams().get("nodesOnly").equals("true");

        Graph g = getCachedGraph(j.getBasisGraph());
        Graph derived;
        if (j.getMethod().equals(ToolService.Tool.MUST) | onlyKeepResult) {
            derived = new Graph(historyController.getGraphId());
            derived.setParent(g.getId());
        } else
            derived = g.clone(historyController.getGraphId());

        if (j.getMethod() != ToolService.Tool.MUST) {
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

            if (j.getMethod().equals(ToolService.Tool.DIAMOND)) {
                derived.addCustomNodeAttributeType(nodeTypeId, "rank", "numeric");
                derived.addCustomNodeAttributeType(nodeTypeId, "p_hyper", "numeric");
                derived.addCustomNodeAttribute(nodeTypeId, j.getResult().getNodes());
            }

            if (j.getMethod().equals(ToolService.Tool.TRUSTRANK) || j.getMethod().equals(ToolService.Tool.CENTRALITY)) {
                derived.addCustomNodeAttributeType(nodeTypeId, "score", "numeric");
                HashMap<Integer, HashMap<String, Object>> idMap = new HashMap<>();
                j.getResult().getNodes().forEach((k, v) -> {
                    if (newNodeIDs.contains(k))
                        idMap.put(k, v);
                });
                derived.addCustomNodeAttribute(nodeTypeId, idMap);
                nodeTypeId = Graphs.getNode(j.getTarget());
                derived.saveNodeFilter(j.getTarget(), g.getNodeFilter(j.getTarget()));
                derived.addNodes(nodeTypeId, g.getNodes().get(nodeTypeId));


                Set<Integer> seedIds = j.getResult().getNodes().entrySet().stream().filter(e -> e.getValue() != null).map(Map.Entry::getKey).collect(Collectors.toSet());
                derived.addNodeMarks(nodeTypeId, seedIds);
            }
            updateEdges(derived, j, nodeTypeId);
        } else {
            j.setUpdate("" + (j.getResult().getNodes().size()));
            NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(nodeTypeId)), j.getResult().getNodes().keySet());
            derived.saveNodeFilter(Graphs.getNode(nodeTypeId), nf);
            derived.addNodes(nodeTypeId, nf.toList(-1).stream().map(e -> new Node(e.getNodeId(), e.getName())).collect(Collectors.toList()));
            LinkedList<Edge> edges = new LinkedList<>();
            j.getResult().getEdges().forEach((id1, map) -> {
                map.forEach((id2, attributes) -> {
                    edges.add(new Edge(id1, id2));
                });
            });
            derived.addCollapsedEdges(nodeTypeId, nodeTypeId, "MuST_Interaction", edges);
            int eid = derived.getEdge("MuST_Interaction");
            derived.addCustomEdgeAttribute(eid, j.getResult().getEdges());
            derived.addCustomAttributeType(eid, "participation_number", "numeric");

        }
        cache.put(derived.getId(), derived);
        j.setDerivedGraph(derived.getId());
        addGraphToHistory(j.getUserId(), derived.getId());
    }

    private void updateEdges(Graph g, Job j, Integer nodeTypeId) {
        HashMap<Integer, LinkedList<Edge>> keeping = new HashMap<>();
        g.getEdges().forEach((k, v) -> {
            if (g.getNodesfromEdge(k).first.equals(nodeTypeId)) {
                if (!keeping.containsKey(k))
                    keeping.put(k, new LinkedList<>());
                keeping.get(k).addAll(v.stream().filter(e -> g.getNodes().get(nodeTypeId).containsKey(e.getId1())).collect(Collectors.toList()));
            }
            if (g.getNodesfromEdge(k).second.equals(nodeTypeId)) {
                if (!keeping.containsKey(k))
                    keeping.put(k, new LinkedList<>());
                keeping.get(k).addAll(v.stream().filter(e -> g.getNodes().get(nodeTypeId).containsKey(e.getId2())).collect(Collectors.toList()));
            }
        });
        keeping.forEach((k, v) -> {
            if (v.size() == 0)
                g.getEdges().remove(k);
            else
                g.getEdges().put(k, v);
        });

        if (j.getParams().containsKey("addInteractions") && j.getParams().get("addInteractions").equals("true")) {
            boolean expOnly = j.getParams().containsKey("experimentalOnly") && j.getParams().get("experimentalOnly").equals("true");
            int typeId1 = Graphs.getNode(j.getMethod().equals(ToolService.Tool.TRUSTRANK) | j.getMethod().equals(ToolService.Tool.CENTRALITY) ? "drug" : j.getTarget());
            int typeId2 = Graphs.getNode(j.getTarget());
            List<Integer> edgeIds = Graphs.getEdgesfromNodes(typeId1, typeId2);
            for (int edgeId : edgeIds) {
                if (!g.getEdges().containsKey(edgeId))
                    g.getEdges().put(edgeId, new LinkedList<>());
                HashMap<Integer, HashSet<Integer>> edges = new HashMap<>();
                g.getNodes().get(typeId1).keySet().forEach(n -> {
                    try {
                        edgeController.getEdges(edgeId, typeId1, n).stream().filter(e -> g.getNodes().get(typeId2).containsKey(e)).forEach(e -> {

                            if (n < e) {
                                if (!expOnly | (expOnly & edgeController.isExperimental(edgeId, n, e))) {
                                    if (!edges.containsKey(n))
                                        edges.put(n, new HashSet<>());
                                    edges.get(n).add(e);
                                }
                            } else {
                                if (!expOnly | (expOnly & edgeController.isExperimental(edgeId, e, n))) {
                                    if (!edges.containsKey(e))
                                        edges.put(e, new HashSet<>());
                                    edges.get(e).add(n);
                                }
                            }
                        });
                    } catch (NullPointerException ignore) {
                    }
                });
                g.getEdges().get(edgeId).forEach(e -> {
                    if (edges.containsKey(e.getId1()) && edges.get(e.getId2()).contains(e.getId2()))
                        edges.get(e.getId1()).remove(e.getId2());
                });
                edges.forEach((id1, l) -> l.forEach(id2 -> g.getEdges().get(edgeId).add(new Edge(id1, id2))));
            }
        }


    }

}
