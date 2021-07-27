package de.exbio.reposcapeweb.communication.reponses;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.cache.Edge;
import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.cache.Node;
import de.exbio.reposcapeweb.communication.controller.SocketController;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.requests.*;
import de.exbio.reposcapeweb.configs.DBConfig;
import de.exbio.reposcapeweb.configs.VisConfig;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.RepoTrialNode;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.entities.nodes.*;
import de.exbio.reposcapeweb.db.history.GraphHistory;
import de.exbio.reposcapeweb.db.history.HistoryController;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.filter.FilterEntry;
import de.exbio.reposcapeweb.filter.NodeFilter;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.*;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

@Service
public class WebGraphService {
    private Logger log = LoggerFactory.getLogger(WebGraphService.class);

    private final EdgeController edgeController;
    private final NodeController nodeController;
    private final HistoryController historyController;
    private final ObjectMapper objectMapper;
    private final DbCommunicationService dbCommunicationService;
    private final ToolService toolService;
    private final SocketController socketController;
    private HashMap<String, Graph> cache = new HashMap<>();
    private HashMap<String, String> userGraph = new HashMap<>();
    private HashMap<String, Object> colorMap;
    private HashSet<String> thumbnailGenerating = new HashSet<>();
    private HashSet<String> layoutGenerating = new HashSet<>();
    private HashSet<String> graphmlGenerating = new HashSet<>();
    private WebGraph metagraph = null;


    @Autowired
    public WebGraphService(
            NodeController nodeController,
            EdgeController edgeController,
            DbCommunicationService dbCommunicationService,
            ObjectMapper objectMapper,
            HistoryController historyController,
            ToolService toolService,
            SocketController socketController) {
        this.edgeController = edgeController;
        this.nodeController = nodeController;
        this.dbCommunicationService = dbCommunicationService;
        this.objectMapper = objectMapper;
        this.historyController = historyController;
        this.toolService = toolService;
        this.socketController = socketController;
    }

    public WebGraph getMetaGraph() {
        if (metagraph == null) {
            metagraph = new WebGraph("Metagraph", true, historyController.getGraphId());
            HashMap<String, Object> sourceIds = new HashMap<>();

            DBConfig.getConfig().nodes.forEach(node -> {
                metagraph.addNode(new WebNode(node.id, node.label, node.name, node.label));
                sourceIds.put(node.label, node.sourceId);
            });
            metagraph.getNodes().forEach(n -> metagraph.setWeight("nodes", n.group, nodeController.getNodeCount(n.group)));

            DBConfig.getConfig().edges.forEach(edge -> metagraph.addEdge(new WebEdge(Graphs.getNode(edge.source), Graphs.getNode(edge.target)).setLabel(edge.mapsTo).setTitle(edge.mapsTo).setDashes(!edge.original).setArrowHead(edge.directed)));
            metagraph.getEdges().forEach(e -> metagraph.setWeight("edges", e.label, edgeController.getEdgeCount(e.label)));

            metagraph.setColorMap(this.getColorMap(null));
            metagraph.setData(sourceIds);
            metagraph.setOptions(VisConfig.getConfig());
        }
        return metagraph;
    }

    public HashMap<String, Object> getColorMap(Collection<String> nodetypes) {
        if (nodetypes == null || colorMap == null) {
            colorMap = new HashMap<>();
            DBConfig.getConfig().nodes.forEach(node -> {
                HashMap<String, String> n = new HashMap<>();
                colorMap.put(node.name, n);
                n.put("main", node.colors.main);
                n.put("light", node.colors.light);
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
        if (g == null)
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
                finalList.addListAttributes("nodes", stringType, attributes.toArray(new String[]{}), nodeController.getAttributeLabelMap(stringType));
                finalList.addAttributes("nodes", stringType, allAttributes.toArray(new String[]{}), nodeController.getAttributeLabelMap(stringType));
                finalList.addNodes(stringType, nodeController.nodesToAttributeList(type, nodeMap.keySet(), new HashSet<>(attributes), g.getCustomNodeAttributes().get(type)));
                finalList.setTypes("nodes", stringType, nodeController.getAttributes(type), nodeController.getAttributeTypes(type), nodeController.getIdAttributes(type), g.getCustomNodeAttributeTypes().get(type));

                try {
                    HashMap<String, HashSet<String>> distinctValues = new HashMap<>();
                    HashSet<String> distinctAttrs = DBConfig.getDistinctAttributes("node", type);
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
                finalList.addListAttributes("edges", stringType, attributes, edgeController.getAttributeLabelMap(stringType));
                List<PairId> edges = edgeList.stream().map(e -> new PairId(e.getId1(), e.getId2())).collect(Collectors.toList());
                if (type < 0) {
                    String[] attributeArray = g.getCustomListAttributes(type);
                    finalList.addAttributes("edges", stringType, attributeArray, edgeController.getAttributeLabelMap(stringType));

                    LinkedList<HashMap<String, Object>> attrMaps = edges.stream().map(p -> getCustomEdgeAttributeList(g, type, p)).collect(toCollection(LinkedList::new));
                    finalList.addEdges(stringType, attrMaps);
                    finalList.setTypes("edges", stringType, attributeArray, g.getCustomListAttributeTypes(type), g.areCustomListAttributeIds(type), g.getCustomEdgeAttributeTypes().get(type));
                } else {
                    String[] attributeArray = edgeController.getAttributes(type);
                    finalList.addAttributes("edges", stringType, attributeArray, edgeController.getAttributeLabelMap(stringType));
                    LinkedList<HashMap<String, Object>> attrMaps = edgeController.edgesToAttributeList(type, edges, attrs);
                    finalList.addEdges(stringType, attrMaps);
                    finalList.setTypes("edges", stringType, attributeArray, edgeController.isExperimental(type), edgeController.getIdAttributes(type), g.getCustomEdgeAttributeTypes().get(type));
                    try {
                        HashMap<String, HashSet<String>> distinctValues = new HashMap<>();
                        HashSet<String> distinctAttrs = DBConfig.getDistinctAttributes("edge", type);
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
                g.setWebList(list);

        }
        return list;
    }

    public WebGraph getWebGraph(GraphRequest request) {
        Graph g = getGraph(request);
        return getWebGraph(g.getId());
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
                g.addCustomEdge(nodeIds.getFirst(), nodeIds.getSecond(), type, basis.getEdges().get(typeId).stream().filter(e -> edgeIds.contains(e.getId1() + "-" + e.getId2())).collect(toCollection(LinkedList::new)));
                int newId = g.getEdge(basis.getEdge(typeId));
                g.addCustomEdgeAttributeTypes(newId, basis.getCustomAttributeTypes(typeId));
                g.addCustomEdgeAttribute(newId, basis.getCustomAttributes(typeId));
            } else {
                g.addEdges(typeId, basis.getEdges().get(typeId).stream().filter(e -> edgeIds.contains(e.getId1() + "-" + e.getId2())).collect(toCollection(LinkedList::new)));
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
        if (g == null)
            return null;
        WebGraph wg = g.getWebgraph();
        if (wg == null)
            return g.toWebGraph(getColorMap(g.getNodes().keySet().stream().map(Graphs::getNode).collect(Collectors.toSet())), getLayout(g, historyController.getLayoutPath(id)));
        return wg;
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
        Integer[] nodes = nodeIds.keySet().toArray(new Integer[0]);
        HashSet<Integer> connectedNodes = new HashSet<>();
        for (int l = 0; l < 2; l++) {
            for (int i = 0; i < nodes.length; i++) {
                for (int j = i; j < nodes.length; j++) {
                    if ((l == 0 && i == j) || (l != 0 && i != j))
                        continue;
                    final int[] nodeI = {nodes[i]};
                    final int[] nodeJ = {nodes[j]};

                    LinkedList<Integer> edgeIds = Graphs.getEdgesfromNodes(nodeI[0], nodeJ[0]);
                    if (edgeIds == null) {
                        continue;
                    }
                    boolean loop = nodeI[0] == nodeJ[0];
                    HashSet<Integer> externalNodes = new HashSet<>();
                    edgeIds.forEach(edgeId -> {
                        boolean internalOnly = loop && request.edges.containsKey(Graphs.getEdge(edgeId)) && ((HashMap<String, Object>) request.edges.get(Graphs.getEdge(edgeId))).containsKey("filters") && (boolean) ((HashMap<String, Object>) request.edges.get(Graphs.getEdge(edgeId)).get("filters")).get("internalOnly");
                        if (!Graphs.checkEdgeDirection(edgeId, nodeI[0], nodeJ[0])) {
                            int tmp = nodeI[0];
                            nodeI[0] = nodeJ[0];
                            nodeJ[0] = tmp;
                        }
                        boolean experimental = (request.interactions.containsKey(Graphs.getEdge(edgeId)) && !request.interactions.get(Graphs.getEdge(edgeId)));
                        if (request.edges.containsKey(Graphs.getEdge(edgeId))) {
                            LinkedList<Edge> edges = new LinkedList<>();

                            nodeIds.get(nodeI[0]).forEach((id1, v1) -> {
                                try {
                                    if (request.connectedOnly & connectedNodes.contains(nodeI[0]) & !v1.hasEdge()) {
                                        return;
                                    }
                                    edgeController.getEdges(edgeId, nodeI[0], id1, false).forEach(id -> {
                                        try {
                                            if ((!loop | internalOnly) && ((request.connectedOnly & connectedNodes.contains(nodeJ[0]) & !nodeIds.get(nodeJ[0]).get(id.getId2()).hasEdge()) | (experimental && !edgeController.isExperimental(edgeId, id.getId1(), id.getId2()))))
                                                return;
                                            if (!loop || (internalOnly && nodeIds.get(nodeJ[0]).containsKey(id.getId2()) && nodeIds.get(nodeJ[0]).containsKey(id.getId1()))) {
                                                nodeIds.get(nodeJ[0]).get(v1.getId() == id.getId1() ? id.getId2() : id.getId1()).setHasEdge(true);
                                                v1.setHasEdge(true);
                                                edges.add(new Edge(id.getId1(), id.getId2()));
                                            } else if (!internalOnly) {
                                                int nonNodeId = v1.getId() == id.getId1() ? id.getId2() : id.getId1();
                                                if (!nodeIds.get(nodeJ[0]).containsKey(nonNodeId)) {
                                                    externalNodes.add(nonNodeId);
                                                } else {
                                                    nodeIds.get(nodeJ[0]).get(nonNodeId).setHasEdge(true);
                                                }
                                                v1.setHasEdge(true);
                                                edges.add(new Edge(id.getId1(), id.getId2()));
                                            }
                                        } catch (NullPointerException ignore) {
                                        }
                                    });
                                } catch (NullPointerException ignore) {
                                }
                            });
                            finalG.addEdges(edgeId, edges);
                            if (loop && !internalOnly) {
                                HashSet<Integer> newNodes = new HashSet<>(nodeIds.get(nodeI[0]).keySet());
                                newNodes.addAll(externalNodes);
                                NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(nodeI[0])), newNodes);
                                finalG.saveNodeFilter(Graphs.getNode(nodeI[0]), nf);
                                HashMap<Integer, Node> ids = nodeIds.get(nodeI[0]);
                                nf.toList(-1).stream().filter(entry -> !ids.containsKey(entry.getNodeId())).forEach(entry -> ids.put(entry.getNodeId(), new Node(entry.getNodeId(), entry.getName(), true)));
                            }
                            if (request.nodes.get(Graphs.getNode(nodeI[0])).filters.length == 0)
                                connectedNodes.add(nodeI[0]);
                            if (request.nodes.get(Graphs.getNode(nodeJ[0])).filters.length == 0)
                                connectedNodes.add(nodeJ[0]);
                        }
                    });

                }
            }
        }
        //TODO update filter if only connected are used
        if (request.connectedOnly) {
            nodeIds.forEach((type, nodeMap) -> nodeMap.entrySet().stream().filter(e -> e.getValue().hasEdge()).forEach(e -> finalG.addNode(type, e.getValue())));
            finalG.getNodes().forEach((nid, nids) -> {
                NodeFilter nf = new NodeFilter(finalG.getNodeFilter(Graphs.getNode(nid)), nids.keySet());
                finalG.saveNodeFilter(Graphs.getNode(nid), nf);
            });
        } else
            nodeIds.forEach((type, nodeMap) -> nodeMap.forEach((key, value) -> finalG.addNode(type, value)));

        return g;
    }

    public Suggestions getSuggestions(SuggestionRequest request) {
        NodeFilter nf;
        Suggestions suggestions;
        String query = StringUtils.normalize(request.query);
        if (request.gid == null) {
            suggestions = new Suggestions(null, query);
            nf = nodeController.getFilter(request.name);
            suggestions.setDistinct(nf.distinctContains(query), nf.getDistinctID2Keys());
            suggestions.setUnique(nf.uniqueContains(query), nf.getUniqueID2Keys());
        } else {
            Graph graph = getCachedGraph(request.gid);
            suggestions = new Suggestions(request.gid, query);

            if (request.type.equals("nodes")) {
                nf = graph.getNodeFilter(request.name);
                HashSet<Integer> ids = new HashSet<>(graph.getNodes().get(Graphs.getNode(request.name)).keySet());
                suggestions.setDistinct(nf.distinctContains(query), nf.getDistinctID2Keys(), ids);
                suggestions.setUnique(nf.uniqueContains(query), nf.getUniqueID2Keys(), ids);
            }
        }
        return suggestions;
    }

    public Collection<Integer> getSuggestionEntry(String gid, String nodeName, String sid) {

        return (gid == null ? nodeController.getFilter(nodeName) : getCachedGraph(gid).getNodeFilter(nodeName)).getEntry(sid).stream().map(FilterEntry::getNodeId).collect(Collectors.toSet());
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

    public void extendGraph(Graph g, HashSet<String> requestedEdges, HashSet<String> inducedEdges, HashSet<String> switched) {
        while (!requestedEdges.isEmpty()) {
            HashSet<String> added = new HashSet<>();
            requestedEdges.forEach(e -> {
                added.add(e);
                extendGraph(g, e, inducedEdges.contains(e), switched.contains(e));
            });
            if (added.isEmpty())
                break;
            requestedEdges.removeAll(added);
        }
    }

    public void extendGraph(Graph g, String e, boolean induced, boolean switched) {
        int edgeId = g.getEdge(e);
        boolean extend = !induced;
        Pair<Integer, Integer> nodeIds = g.getNodesfromEdge(edgeId);
        LinkedList<Edge> edges = new LinkedList<>();
        HashSet<Integer> nodes = new HashSet<>();

        if (g.getNodes().containsKey(nodeIds.first) & nodeIds.first.equals(nodeIds.second)) {
            g.getNodes().get(nodeIds.first).keySet().forEach(nodeId1 -> {
                try {
                    edgeController.getEdges(edgeId, nodeIds.first, nodeId1, switched).forEach(id -> {
                        Set<Integer> existing = g.getNodes().get(nodeIds.first).keySet();
                        boolean existing1 = existing.contains(id.getId1());
                        boolean existing2 = existing.contains(id.getId2());
                        if ((existing1 & existing2) | extend)
                            edges.add(!edgeController.getDirection(edgeId) & id.getId1() > id.getId2() ? new Edge(id.flipIds()) : new Edge(id));
                        if (!existing1 & extend)
                            nodes.add(id.getId1());
                        if (!existing2 & extend)
                            nodes.add(id.getId2());
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
                    edgeController.getEdges(edgeId, nodeIds.first, nodeId1, switched).forEach(id -> {
                        if (g.getNodes().get(nodeIds.second).containsKey(id.getId2()) && g.getNodes().get(nodeIds.first).containsKey(id.getId1()))
                            edges.add(new Edge(id));
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
                    HashSet<PairId> add = new HashSet<>(edgeController.getEdges(edgeId, node1, nodeId1, switched));
                    if (node1 == nodeIds.getFirst())
                        add.forEach(id -> {
                                    edges.add(new Edge(id));
                                    nodes.add(id.getId2());
                                }
                        );
                    else
                        add.forEach(id -> {
                                    edges.add(new Edge(id));
                                    nodes.add(id.getId1());
                                }
                        );
                } catch (NullPointerException ignore) {
                }
            });


            int node2 = adding[i];
            NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(node2)), nodes);
            g.saveNodeFilter(Graphs.getNode(node2), nf);

            nodeMap.put(node2, new HashMap<>());
            nf.toList(-1).forEach(entry -> nodeMap.get(node2).put(entry.getNodeId(), new Node(entry.getNodeId(), entry.getName())));
        }
        nodeMap.forEach(g::addNodes);
        g.addEdges(edgeId, edges);
    }

    public WebGraphInfo getExtension(ExtensionRequest request) {
        Graph g = getCachedGraph(request.gid).clone(historyController.getGraphId());
        cache.put(g.getId(), g);
        HashSet<String> inducedEdges = new HashSet<>(Arrays.asList(request.induced));
        HashSet<String> requestedEdges = new HashSet<>(Arrays.asList(request.edges));
        HashSet<String> switched = new HashSet<>(Arrays.asList(request.switchDirection));
        extendGraph(g, requestedEdges, inducedEdges, switched);
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
        collapseGraph(g, Graphs.getNode(request.node), g.getEdge(request.edge1), g.getEdge(request.edge2), request.edgeName, request.self, request.keep);
        return g.toInfo();
    }

    public void collapseGraph(Graph g, int collapseNode, int edge1, int edge2, String name, boolean self, boolean keep) {
        Pair<Integer, Integer> nodes1 = g.getNodesfromEdge(edge1);
        HashMap<Integer, HashSet<Integer>> edgeMap1 = prepareEdgeMap(g.getEdges().get(edge1), nodes1.first == collapseNode);
        LinkedList<Edge> edges = new LinkedList<>();
        int startNodeId = nodes1.getFirst() == collapseNode ? nodes1.getSecond() : nodes1.getFirst();
        int targetNodeId;
        HashMap<Integer, HashMap<Integer, Object>> edgeWeights = new HashMap<>();
        HashMap<Integer, HashMap<Integer, Object>> jaccardIndex = new HashMap<>();
        if (self) {
            //TODO problems?
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
                                    jaccardIndex.get(startNode).put(targetNode, 1);
                                }
                            })
                    )
            );
        } else {
            Pair<Integer, Integer> nodes2 = g.getNodesfromEdge(edge2);
            targetNodeId = nodes2.getFirst() == collapseNode ? nodes2.getSecond() : nodes2.getFirst();
            HashMap<Integer, HashSet<Integer>> edgeMap2 = prepareEdgeMap(g.getEdges().get(edge2), nodes2.first == collapseNode);
            HashMap<Integer, HashSet<Integer>> jaccardSet1 = new HashMap<>();
            HashMap<Integer, HashSet<Integer>> jaccardSet2 = new HashMap<>();
            edgeMap1.forEach((middle, startNodes) -> {
                try {
                    HashSet<Integer> targetNodes = edgeMap2.get(middle);
                    targetNodes.forEach(targetNode ->
                            startNodes.forEach(startNode -> {
                                int weight = 1;
                                if (!startNode.equals(targetNode)) {
                                    if (!jaccardSet1.containsKey(startNode))
                                        jaccardSet1.put(startNode, new HashSet<>());
                                    jaccardSet1.get(startNode).add(middle);
                                    if (!jaccardSet2.containsKey(targetNode))
                                        jaccardSet2.put(targetNode, new HashSet<>());
                                    jaccardSet2.get(targetNode).add(middle);

                                    if (!edgeWeights.containsKey(startNode) || !edgeWeights.get(startNode).containsKey(targetNode))
                                        edges.add(new Edge(startNode, targetNode));

                                    if (edgeWeights.containsKey(startNode) && edgeWeights.get(startNode).containsKey(targetNode))
                                        weight += (int) edgeWeights.get(startNode).get(targetNode);

                                    if (!edgeWeights.containsKey(startNode)) {
                                        edgeWeights.put(startNode, new HashMap<>());
                                    }
                                    edgeWeights.get(startNode).put(targetNode, weight);
                                }
                            })
                    );
                } catch (NullPointerException ignore) {
                }
            });
            edges.forEach(e -> {
                if (!jaccardIndex.containsKey(e.getId1()))
                    jaccardIndex.put(e.getId1(), new HashMap<>());
                jaccardIndex.get(e.getId1()).put(e.getId2(), StatUtils.calculateJaccardIndex(jaccardSet1.get(e.getId1()), jaccardSet2.get(e.getId2())));
            });


            if (!keep)
                g.getEdges().remove(edge2);

        }
        g.addCustomEdge(startNodeId, targetNodeId, name, edges);
        int eid = g.getEdge(name);
        g.addCustomEdgeAttribute(eid, "Weight", edgeWeights);
        g.addCustomAttributeType(eid, "Weight", "numeric");
        g.addCustomEdgeAttribute(eid, "JaccardIndex", jaccardIndex);
        g.addCustomAttributeType(eid, "JaccardIndex", "numeric");
        if (!keep) {
            g.getEdges().remove(edge1);
            HashSet<Integer> otherConnectedEdges = new HashSet<>();
            g.getEdges().keySet().forEach(e -> {
                Pair<Integer, Integer> nodeIDs = g.getNodesfromEdge(e);
                if (nodeIDs.getFirst() == collapseNode || nodeIDs.second == collapseNode)
                    otherConnectedEdges.add(e);
            });
            if (otherConnectedEdges.isEmpty())
                g.getNodes().remove(collapseNode);
        }
    }

    public String[] getCustomEdgeAttributes(Graph g, int edgeId) {
        LinkedList<String> list = new LinkedList<>(Arrays.asList("ID", "IDOne", "IDTwo", "Node1", "Node2", "MemberOne", "MemberTwo"));
        list.addAll(g.getCustomAttributeTypes(edgeId).keySet());
        list.add("Type");
        return list.toArray(new String[]{});
    }

    public HashMap<String, Object> getCustomEdgeAttributeList(Graph graph, int edgeId, PairId p) {
        Pair<Integer, Integer> nodeIds = graph.getNodesfromEdge(edgeId);
        HashMap<String, Object> as = new HashMap<>();
        as.put("ID", p.getId1() + "-" + p.getId2());
        as.put("IDOne", p.getId1());
        as.put("IDTwo", p.getId2());
        as.put("Node1", nodeController.getName(nodeIds.first, p.getId1()));
        as.put("Node2", nodeController.getName(nodeIds.second, p.getId2()));
        as.put("MemberOne", nodeController.getDomainId(nodeIds.first, p.getId1()));
        as.put("MemberTwo", nodeController.getDomainId(nodeIds.second, p.getId2()));
        ArrayList<String> order = new ArrayList<>(Arrays.asList("ID", "IDOne", "IDTwo", "Node1", "Node2", "MemberOne", "MemberTwo"));
        graph.getCustomAttributeTypes(edgeId).keySet().forEach(attrName -> {
            as.put(attrName, graph.getCustomAttributes(edgeId).get(p.getId1()).get(p.getId2()).get(attrName));
            order.add(attrName);
        });
        order.add("Type");
        as.put("Type", graph.getEdge(edgeId));
        as.put("order", order);
        return as;
    }

    public ConnectionGraph getConnectionGraph(String gid) {
        Graph g = getCachedGraph(gid);
        if (g == null)
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
            HashMap<String, Object> details = new HashMap<>();
            edgeController.edgeToAttributeList(edgeId, p).forEach((k, v) -> details.put(edgeController.getAttributeLabelMap(name).get(k), v));
            details.put("order", Arrays.stream(edgeController.getAttributes(edgeId)).map(a -> edgeController.getAttributeLabelMap(name).get(a)).collect(Collectors.toList()));
            details.put("Type", EdgeController.edgeLabel2NameMap.get(details.get("Type").toString()));
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
        cacheCleanup(uid, gid);
        exportGraph(g);
    }

    public void removeTempDir(String gid) {
        File wd = getGraphWD(gid);
        if (wd.exists()) {
            try {
                FileUtils.deleteDirectory(wd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cacheCleanup(String uid, String gid) {
        if (userGraph.containsKey(uid)) {
            String oldId = userGraph.get(uid);
            cache.remove(oldId);
            removeTempDir(oldId);
        }
        userGraph.put(uid, gid);
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

        if (j.getMethod() == ToolService.Tool.CENTRALITY | j.getMethod() == ToolService.Tool.TRUSTRANK)
            filterDrugsMap(nodeTypeId, j);


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
            Set<Integer> newNodeIDs = (j.getMethod().equals(ToolService.Tool.BICON) ? j.getResult().getNodes().entrySet().stream() : j.getResult().getNodes().entrySet().stream().filter(e -> e.getValue() != null)).map(Map.Entry::getKey).collect(Collectors.toSet());
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
                int otherTypeId = nodeTypeId;


                derived.addCustomNodeAttributeType(otherTypeId, "score", "numeric");
                HashMap<Integer, HashMap<String, Object>> idMap = new HashMap<>();
                j.getResult().getNodes().forEach((k, v) -> {
                    if (newNodeIDs.contains(k))
                        idMap.put(k, v);
                });
                derived.addCustomNodeAttribute(otherTypeId, idMap);
                nodeTypeId = Graphs.getNode(j.getTarget());
                derived.saveNodeFilter(j.getTarget(), g.getNodeFilter(j.getTarget()));
                derived.addNodes(nodeTypeId, g.getNodes().get(nodeTypeId));

                Set<Integer> seedIds = j.getResult().getNodes().entrySet().stream().filter(e -> e.getValue() != null).map(Map.Entry::getKey).collect(Collectors.toSet());
                derived.addNodeMarks(nodeTypeId, seedIds);
                updateEdges(derived, j, otherTypeId);
            }
            updateEdges(derived, j, nodeTypeId);
        } else {


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
            derived.addCustomEdge(nodeTypeId, nodeTypeId, "MuST_Interaction", edges);
            int eid = derived.getEdge("MuST_Interaction");
            derived.addCustomEdgeAttribute(eid, j.getResult().getEdges());
            derived.addCustomAttributeType(eid, "participation_number", "numeric");
            derived.addCustomAttributeType(eid, "memberOne", "id");
            derived.addCustomAttributeType(eid, "memberTwo", "id");

        }
        AtomicInteger size = new AtomicInteger();
        derived.getNodes().forEach((k, v) -> size.addAndGet(v.size()));
        derived.getEdges().forEach((k, v) -> size.addAndGet(v.size()));
        if (size.get() < 100_000) {
            cache.put(derived.getId(), derived);
            j.setDerivedGraph(derived.getId());
            addGraphToHistory(j.getUserId(), derived.getId());
        } else
            j.setStatus(Job.JobState.LIMITED);
    }

    private void filterDrugsMap(int nodeTypeId, Job j) {
        int topX = Integer.parseInt(j.getParams().get("topX"));
        boolean elements = j.getParams().get("elements").startsWith("t");
        HashMap<Integer, HashMap<String, Object>> newNodes = j.getResult().getNodes();
        LinkedList<Map.Entry<Integer, HashMap<String, Object>>> sortedEntries = new LinkedList<>(newNodes.entrySet().stream().sorted(Comparator.comparingDouble(e -> ((double) e.getValue().get("score")))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)).entrySet());
        newNodes.clear();
        Collections.reverse(sortedEntries);
        for (Map.Entry<Integer, HashMap<String, Object>> e : sortedEntries) {
            if (!elements && ((Drug) nodeController.getNode(Graphs.getNode(nodeTypeId), e.getKey())).getDrugCategories().contains("Elements"))
                continue;
            newNodes.put(e.getKey(), e.getValue());
            if (newNodes.size() >= topX)
                break;
        }
        j.getResult().setNodes(newNodes);
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
            int typeId1 = nodeTypeId;
            int typeId2 = Graphs.getNode(j.getTarget());
            List<Integer> edgeIds = Graphs.getEdgesfromNodes(typeId1, typeId2);
            for (int edgeId : edgeIds) {
                if (!g.getEdges().containsKey(edgeId))
                    g.getEdges().put(edgeId, new LinkedList<>());
                HashMap<Integer, HashSet<Integer>> edges = new HashMap<>();
                g.getNodes().get(typeId1).keySet().forEach(n -> {
                    try {
                        edgeController.getEdges(edgeId, typeId1, n, false).stream().filter(e -> g.getNodes().get(typeId2).containsKey(e.getId2())).forEach(e -> {
                            int n2 = e.getId2();
                            if (n < n2) {
                                if (!expOnly | edgeController.isExperimental(edgeId, n, n2)) {
                                    if (!edges.containsKey(n))
                                        edges.put(n, new HashSet<>());
                                    edges.get(n).add(n2);
                                }
                            } else {
                                if (!expOnly | edgeController.isExperimental(edgeId, n2, n)) {
                                    if (!edges.containsKey(n2))
                                        edges.put(n2, new HashSet<>());
                                    edges.get(n2).add(n);
                                }
                            }
                        });
                    } catch (NullPointerException ignore) {
                    }
                });
                //Remove duplicates
                g.getEdges().get(edgeId).forEach(e -> {
                    if (edges.containsKey(e.getId1()))
                        edges.get(e.getId1()).remove(e.getId2());
                });
                edges.forEach((id1, l) -> l.forEach(id2 -> g.getEdges().get(edgeId).add(new Edge(id1, id2))));
            }
        }
    }

    public File getDownload(String gid) {
        File wd = getGraphWD(gid);
        File graphml = new File(wd, gid + ".graphml");

        if (!graphml.exists()) {
            if (!graphmlGenerating.contains(gid)) {
                graphmlGenerating.add(gid);
                wd.getParentFile().mkdirs();
                CustomListRequest req = new CustomListRequest();
                req.id = gid;
                req.attributes = new HashMap<>();
                Graph g = getCachedGraph(gid);

                g.getNodes().keySet().forEach(type -> {
                    if (!req.attributes.containsKey("nodes"))
                        req.attributes.put("nodes", new HashMap<>());
                    req.attributes.get("nodes").put(Graphs.getNode(type), nodeController.getAttributes(type));
                });

                g.getEdges().keySet().forEach(type -> {
                    if (!req.attributes.containsKey("edges"))
                        req.attributes.put("edges", new HashMap<>());
                    req.attributes.get("edges").put(g.getEdge(type), type < 0 ? getCustomEdgeAttributes(g, type) : edgeController.getAttributes(type));
                });

                WebGraphList list = getList(gid, req);
                writeGraphListToFS(list, wd, req);
                toolService.createGraphmlFromFS(wd, graphml);

                graphmlGenerating.remove(gid);
            } else {
                while (graphmlGenerating.contains(gid)) {
                }
            }

        }
        return graphml;
    }

    private void writeGraphListToFS(WebGraphList list, File wd, CustomListRequest req) {
        File nodes = new File(wd, "nodes");
        nodes.mkdirs();
        list.getNodes().forEach((type, values) -> {
            try (BufferedWriter bw = WriterUtils.getBasicWriter(new File(nodes, type + ".tsv"))) {
                String[] order = req.attributes.get("nodes").get(type);
                StringBuilder header = new StringBuilder("#");
                Arrays.stream(order).forEach(attr -> header.append(attr).append("\t"));
                bw.write(header.substring(0, header.length() - 1) + "\n");
                StringBuilder line = new StringBuilder("");
                for (HashMap<String, Object> map : values) {
                    line.setLength(0);
                    Arrays.stream(order).forEach(key -> line.append(escapeStrings(map.get(key) == null ? "NA" : map.get(key).toString())).append("\t"));
                    bw.write(line.substring(0, line.length() - 1) + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        File edges = new File(wd, "edges");
        edges.mkdirs();
        list.getEdges().forEach((type, values) -> {
            try (BufferedWriter bw = WriterUtils.getBasicWriter(new File(edges, type + ".tsv"))) {
                String[] order = req.attributes.get("edges").get(type);
                StringBuilder header = new StringBuilder("#");
                Arrays.stream(order).forEach(attr -> header.append(attr).append("\t"));
                bw.write(header.substring(0, header.length() - 1) + "\n");
                StringBuilder line = new StringBuilder("");
                for (HashMap<String, Object> map : values) {
                    line.setLength(0);
                    Arrays.stream(order).forEach(key -> {
                        line.append(map.get(key)).append("\t");
                    });
                    bw.write(line.substring(0, line.length() - 1) + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private File getGraphWD(String gid) {
        return new File("/tmp/nedrex_graph-" + gid);
    }

    private String escapeStrings(String in) {
        if (in.contains("\r"))
            log.warn("Detected windows line endings: " + in);
        String out = StringUtils.replaceAll(in, '\t', "\\t");
        out = StringUtils.replaceAll(out, '\n', "\\n");
        return out;
    }

    public void remove(String gid) {
        removeTempDir(gid);
        cache.remove(gid);
        AtomicReference<String> user = new AtomicReference<>(null);
        userGraph.forEach((u, g) -> {
            if (gid.equals(g))
                user.set(u);
        });
        if (user.get() != null)
            userGraph.remove(user.get());
        File thumb = historyController.getThumbnailPath(gid);
        if (thumb != null && thumb.exists())
            thumb.delete();
        historyController.remove(gid);
    }

    public File getThumbnail(String gid) {
        return historyController.getThumbnailPath(gid);
    }

    @Async
    public void createThumbnail(String gid, File thumb) {
        if (thumb.exists() || thumbnailGenerating.contains(gid))
            return;
        thumbnailGenerating.add(gid);
        toolService.createThumbnail(getDownload(gid), thumb);
        socketController.setThumbnailReady(gid);
        thumbnailGenerating.remove(gid);
        removeTempDir(gid);
    }

    public HashMap<Integer, HashMap<Integer, Point2D>> getLayout(Graph g, File lay) {
        if (!lay.exists()) {
            lay.getParentFile().mkdirs();
            if (layoutGenerating.contains(g.getId())) {
                while (layoutGenerating.contains(g.getId())) {
                }
            } else {
                layoutGenerating.add(g.getId());
                toolService.createLayout(getDownload(g.getId()), lay);
                layoutGenerating.remove(g.getId());
            }
        }
        HashMap<Integer, HashMap<Integer, Point2D>> coords = new HashMap<>();
        try (BufferedReader br = ReaderUtils.getBasicReader(lay)) {
            String line;
            while ((line = br.readLine()) != null) {
                LinkedList<String> l = StringUtils.split(line, "\t");
                if (l.getFirst().length() == 0)
                    continue;
                int typeId = Graphs.getNode(l.getFirst());
                int nodeid = nodeController.getId(l.getFirst(), l.get(1));
                if (!coords.containsKey(typeId))
                    coords.put(typeId, new HashMap<>());
                coords.get(typeId).put(nodeid, new Point2D.Double(Double.parseDouble(l.get(2)), Double.parseDouble(l.get(3))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        removeTempDir(g.getId());
        return coords;
    }

    public LinkedList<Object> mapIdsToItemList(String type, LinkedList<String> file) {
        LinkedList<Object> out = new LinkedList<>();

        List<Integer> ids = new LinkedList<>();
        file.forEach(id -> {
            try {
                if (!id.contains("."))
                    id = DBConfig.getConfig().nodes.get(Graphs.getNode(type)).sourceId.toLowerCase() + "." + id;
                ids.add(nodeController.getId(type, id));
            } catch (NullPointerException ignore) {
                try {
                    if (id.contains("\t")) {
                        id = StringUtils.split(id, "\t").get(0);
                        ids.add(nodeController.getId(type, id));
                    } else {
                        id = StringUtils.split(id, ",").get(0);
                        ids.add(nodeController.getId(type, id));
                    }
                } catch (NullPointerException ignore2) {
                }
            }
        });
        nodeController.findByIds(type, ids).forEach(o -> {
            RepoTrialNode n = null;
            switch (type) {
                case "gene" -> n = (Gene) o;
                case "protein" -> n = (Protein) o;
                case "pathway" -> n = (Pathway) o;
                case "disorder" -> n = (Disorder) o;
                case "drug" -> n = (Drug) o;
            }
            out.add(n.getAsMap(new HashSet<>(Arrays.asList("id", "displayName", "primaryDomainId"))));
        });
        return out;
    }

    public LinkedList<Object> getConnectedNodes(String sourceType, String targetType, Collection<Integer> sourceIds) {
        LinkedList<Integer> edgeIds = Graphs.getEdgesfromNodes(Graphs.getNode(sourceType), Graphs.getNode(targetType));
        HashMap<Integer, HashMap<String, Object>> out = new HashMap<>();
        int edgeId = edgeIds.getFirst();
        sourceIds.forEach(sourceId -> {
            try {
                edgeController.getEdges(edgeId, Graphs.getNode(sourceType), sourceId, false).forEach(e -> {
                    int n = Graphs.getNode(sourceType) == Graphs.getNodesfromEdge(edgeId).first ? e.getId2() : e.getId1();
                    try {
                        HashMap<String, Object> edgeEntries = edgeController.edgeToAttributeList(edgeId, e);
                        HashSet<String> sources = new HashSet<>();
                        if (edgeEntries.containsKey("databases"))
                            sources.addAll((List<String>) edgeEntries.get("databases"));
                        else if (edgeEntries.containsKey("assertedBy"))
                            sources.addAll((List<String>) edgeEntries.get("assertedBy"));

                        if (!out.containsKey(n)) {
                            HashMap<String, Object> nodeEntries = nodeController.getNode(targetType, n).getAsMap(new HashSet<>(Arrays.asList("id", "displayName", "primaryDomainId")));
                            nodeEntries.put("sourceDBs", sources);
                            out.put(n, nodeEntries);
                        } else {
                            sources.addAll((HashSet<String>) out.get(n).get("sources"));
                            out.get(n).put("sourceDBs", sources);
                        }


                    } catch (NullPointerException ignore) {
                    }
                });
            } catch (NullPointerException ignore) {
            }
        });
        return new LinkedList<>(out.values());

    }

    public HashMap<Integer, Node> nodeFilterToNode(NodeFilter nf) {
        HashMap<Integer, Node> nodes = new HashMap<>();
        nf.toList(-1).forEach(entry -> {
            nodes.put(entry.getNodeId(), new Node(entry.getNodeId(), entry.getName()));
        });
        return nodes;
    }

    public Graph createGraphFromIds(String type, List<Integer> nodes, String uid) {
        Graph g = new Graph(historyController.getGraphId());
        NodeFilter nf = new NodeFilter(nodeController.getFilter(type), nodes);
        g.saveNodeFilter(type, nf);
        g.addNodes(Graphs.getNode(type), nf.toList(-1).stream().map(e -> new Node(e.getNodeId(), e.getName())).collect(Collectors.toList()));
        cache.put(g.getId(), g);
        addGraphToHistory(uid, g.getId());
        return g;
    }

    public WebGraphInfo getGuidedGraph(GuidedRequest request) {
        Graph g = new Graph(historyController.getGraphId());
        cache.put(g.getId(), g);
        int sourceTypeId = Graphs.getNode(request.sourceType);

        NodeFilter nfs = new NodeFilter(nodeController.getFilter(request.sourceType), request.sources);
        g.addNodes(sourceTypeId, nodeFilterToNode(nfs));
        g.saveNodeFilter(request.sourceType, nfs);

        for (int p = 0; p < request.path.size(); p++) {
            boolean endDefined = p == request.path.size() - 1 & request.targets.size() > 0;
            if (endDefined) {
                int targetTypeId = Graphs.getNode(request.targetType);
                NodeFilter nft = new NodeFilter(nodeController.getFilter(request.targetType), request.targets);
                g.addNodes(targetTypeId, nodeFilterToNode(nft));
                g.saveNodeFilter(request.targetType, nft);
            }
            extendGraph(g, request.path.get(p).get("label"), endDefined, false);
        }

        if (!(boolean) request.params.get("general").get("keep")) {
            String edgeName = request.params.get("general").get("name").toString();
            for (int p = 0; p < request.path.size() - 1; p++) {
                int edge2 = Graphs.getEdge(request.path.get(p + 1).get("label"));
                Pair<Integer, Integer> nodes;
                int edge1;
                if (p == 0) {
                    edge1 = Graphs.getEdge(request.path.get(p).get("label"));
                } else {
                    edge1 = g.getEdge(edgeName);
                }
                nodes = g.getNodesfromEdge(edge1);
                collapseGraph(g, nodes.first == sourceTypeId ? nodes.second : nodes.first, edge1, edge2, edgeName, false, false);
            }
        }

        addGraphToHistory(request.uid, g.getId());


        return g.toInfo();
    }


    public String cloneGraph(String gid, String uid, String parent) {
        Graph g = getCachedGraph(gid);
        String cloneId = historyController.getGraphId();
        Graph clone = g.clone(cloneId);
        clone.setParent(parent);
        cache.put(cloneId, clone);
        addGraphToHistory(uid, cloneId);
        return cloneId;
    }

    public String getTableDownload(String gid, String type, String name, LinkedList<String> attributes) {
        Graph g = getCachedGraph(gid);
        return type.equals("nodes") ? getNodeTableDownload(g, name, attributes) : getEdgeTableDownload(g, name, attributes);
    }

    private String getEdgeTableDownload(Graph g, String name, LinkedList<String> attributes){
        StringBuilder table = new StringBuilder();
        for (String a : attributes)
            table.append(table.length() == 0 ? "#" : "\t").append(a).append(1).append("\t").append(a).append(2);
        table.append("\n");
        Pair<Integer,Integer> ids = g.getNodesfromEdge(g.getEdge(name));
        g.getEdges().get(g.getEdge(name)).forEach(e->{
            HashMap<String,Object> node1 = nodeController.nodeToAttributeList(ids.first,e.getId1());
            HashMap<String,Object> node2 = nodeController.nodeToAttributeList(ids.second,e.getId2());
            StringBuilder line = new StringBuilder();
            attributes.forEach(a->line.append(line.length() == 0 ? "" : "\t").append(node1.get(a)).append("\t").append(node2.get(a)).append(2));
            table.append(line).append("\n");
        });
        return table.toString();
    }

    private String getNodeTableDownload(Graph g, String name, LinkedList<String> attributes) {
        StringBuilder table = new StringBuilder();
        for (String a : attributes)
            table.append(table.length() == 0 ? "#" : "\t").append(a);
        table.append("\n");
        HashSet<Integer> ids = g.getNodes().get(Graphs.getNode(name)).values().stream().map(Node::getId).collect(toCollection(HashSet::new));
        nodeController.nodesToAttributeList(Graphs.getNode(name),ids,new HashSet<>(attributes),null).forEach(n->{
            StringBuilder line = new StringBuilder();
            attributes.forEach(a->{
                line.append(line.length()==0 ? "":"\t").append(n.get(a));
            });
            table.append(line).append("\n");
        });
        return table.toString();
    }
}
