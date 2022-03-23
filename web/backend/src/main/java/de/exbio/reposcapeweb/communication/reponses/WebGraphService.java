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
import de.exbio.reposcapeweb.tools.algorithms.Algorithm;
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
import java.util.concurrent.atomic.AtomicBoolean;
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
    private EnumMap<ToolService.Tool, Algorithm> algorithms;


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
        this.algorithms = toolService.getAlgorithms();

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
                    finalReq.attributes.get("edges").put(g.getEdge(k), k < 0 ? g.getCustomEdgeListAttributes(k) : edgeController.getListAttributes(k));
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
                Set<String> detailAttrs = DBConfig.getConfig().nodes.get(type).getDetailAttributes().stream().map(a -> a.name).collect(Collectors.toCollection(HashSet::new));
                List<String> allAttributes = Arrays.stream(nodeController.getAttributes(type)).filter(detailAttrs::contains).collect(Collectors.toList());
                try {
                    g.getCustomNodeAttributeTypes().get(type).forEach((attrName, attrType) -> {
                        if (!attributes.contains(attrName))
                            attributes.add(attrName);
                        if (!allAttributes.contains(attrName))
                            allAttributes.add(attrName);
                    });
                } catch (NullPointerException ignore) {
                }
                HashMap<String, String> attributeLabelMap = nodeController.getAttributeLabelMap(stringType);
                if (g.getCustomNodeAttributeLabels().containsKey(type))
                    attributeLabelMap.putAll(g.getCustomNodeAttributeLabels().get(type));
                finalList.addListAttributes("nodes", stringType, attributes.toArray(new String[]{}), attributeLabelMap);
                finalList.addAttributes("nodes", stringType, allAttributes.toArray(new String[]{}), attributeLabelMap);
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
                HashMap<String, String> attributeLabelMap = type < 0 ? g.getCustomEdgeAttributeLabels().get(type) : edgeController.getAttributeLabelMap(stringType);
                if (g.getCustomEdgeAttributeLabels().get(type) != null)
                    attributeLabelMap.putAll(g.getCustomEdgeAttributeLabels().get(type));
                finalList.addListAttributes("edges", stringType, attributes, attributeLabelMap);
                List<PairId> edges = edgeList.stream().map(e -> new PairId(e.getId1(), e.getId2())).collect(Collectors.toList());
                if (type < 0) {
                    String[] attributeArray = g.getCustomEdgeListAttributes(type);
                    finalList.addAttributes("edges", stringType, attributeArray, attributeLabelMap);

                    LinkedList<HashMap<String, Object>> attrMaps = edges.stream().map(p -> getCustomEdgeAttributeList(g, type, p)).collect(toCollection(LinkedList::new));
                    finalList.addEdges(stringType, attrMaps);
                    finalList.setTypes("edges", stringType, attributeArray, g.getCustomEdgeListAttributeTypes(type), g.areCustomListAttributeIds(type), g.getCustomEdgeAttributeTypes().get(type));
                } else {
                    Set<String> detailAttrs = DBConfig.getConfig().edges.get(type).getDetailAttributes().stream().map(a -> a.name).collect(Collectors.toCollection(HashSet::new));
                    String[] attributeArray = Arrays.stream(edgeController.getAttributes(type)).filter(detailAttrs::contains).collect(Collectors.toList()).toArray(new String[]{});
                    finalList.addAttributes("edges", stringType, attributeArray, attributeLabelMap);
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
                                    if (value != null)
                                        if (value instanceof String)
                                            distinctValues.get(attr).add((String) value);
                                    if (value instanceof Integer)
                                        distinctValues.get(attr).add(value + "");
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
                g.addCustomEdgeAttributeTypes(newId, basis.getCustomEdgeAttributeTypes(typeId), basis.getCustomEdgeAttributeLabels().get(typeId));
                g.addCustomEdgeAttribute(newId, basis.getCustomAttributes(typeId));
            } else {
                g.addEdges(typeId, basis.getEdges().get(typeId).stream().filter(e -> edgeIds.contains(e.getId1() + "-" + e.getId2())).collect(toCollection(LinkedList::new)));
            }
        });

        g.calculateDegrees();
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

        HashMap<Integer, Boolean> nodesExtendable = new HashMap<>();
        Graph finalG = g;

        HashMap<String, Object> nodeOptions = request.options.get("nodes");
        boolean drugsFilterElements = (boolean) nodeOptions.get("filterElementDrugs");
        boolean genesOnlyCoding = (boolean) nodeOptions.get("codingGenesOnly");
        boolean drugsApprovedOnly = (boolean) nodeOptions.get("approvedDrugsOnly");

        HashMap<String, Object> edgeOptions = request.options.get("edges");
        boolean drugTargetsWithAction = (boolean) edgeOptions.get("drugTargetsWithAction");
        boolean disorderParents = (boolean) edgeOptions.get("disorderParents");
        boolean extendPPI = (boolean) edgeOptions.get("extendPPI");
        boolean extendGGI = (boolean) edgeOptions.get("extendGGI");
        boolean experimentalInteraction = (boolean) edgeOptions.get("experimentalInteraction");
        double disorderAssociationCutoff = Double.parseDouble(edgeOptions.get("disorderAssociationCutoff").toString());

        HashSet<Integer> connectedNodes = new HashSet<>();

        request.nodes.forEach((k, v) -> {
            nodesExtendable.put(Graphs.getNode(k), true);
            NodeFilter nf = nodeController.getFilter(k);
            boolean filtered = false;
            if (v.ids != null) {
                filtered = true;
                nf = new NodeFilter(nf, v.ids);
                nodesExtendable.put(Graphs.getNode(k), false);
            } else if (v.filters != null) {
                for (Filter filter : v.filters) {
                    filtered = true;
                    nf = nf.apply(filter);
                    nodesExtendable.put(Graphs.getNode(k), false);
                }
            }

            if (k.equals("drug")) {
                if (drugsFilterElements) {
                    nf = new NodeFilter(nf, getElementFilteredDrugs(nf.toList(-1).stream().map(FilterEntry::getNodeId).collect(Collectors.toList())));
                }
                if (drugsApprovedOnly) {
                    nf = new NodeFilter(nf, getApprovedOnlyDrugs(nf.toList(-1).stream().map(FilterEntry::getNodeId).collect(Collectors.toList())));
                }
            }
            if (k.equals("gene") & genesOnlyCoding) {
                nf = new NodeFilter(nf, getOnlyCodingGenes(nf.toList(-1).stream().map(FilterEntry::getNodeId).collect(Collectors.toList())));
            }
            if (filtered) {
                connectedNodes.add(Graphs.getNode(k));
                finalG.saveNodeFilter(k, nf);
                HashMap<Integer, Node> ids = new HashMap<>();
                nf.toList(-1).forEach(entry -> {
                    ids.put(entry.getNodeId(), new Node(entry.getNodeId(), entry.getName()));
                });
                finalG.addNodes(Graphs.getNode(k), ids);
            }
        });

        HashSet<String> addedEdges = new HashSet<>();
        LinkedList<String> edgeList = new LinkedList<>();

        request.edges.keySet().stream().filter(e -> connectedNodes.contains(Graphs.getNodesfromEdge(Graphs.getEdge(e)).first) & connectedNodes.contains(Graphs.getNodesfromEdge(Graphs.getEdge(e)).second)).forEach(edgeList::add);
        request.edges.keySet().stream().filter(e -> !edgeList.contains(e)).filter(e -> connectedNodes.contains(Graphs.getNodesfromEdge(Graphs.getEdge(e)).first) | connectedNodes.contains(Graphs.getNodesfromEdge(Graphs.getEdge(e)).second)).forEach(edgeList::add);
        request.edges.keySet().stream().filter(e -> !edgeList.contains(e)).forEach(edgeList::add);


        while (!edgeList.isEmpty()) {
            String edgeName = edgeList.getFirst();
            addedEdges.add(edgeName);
            Pair<Integer, Integer> edgeNodes = Graphs.getNodesfromEdge(edgeName);
            if (!connectedNodes.contains(Graphs.getNodesfromEdge(Graphs.getEdge(edgeName)).first) & !connectedNodes.contains(Graphs.getNodesfromEdge(Graphs.getEdge(edgeName)).second)) {
                HashMap<Integer, HashSet<Integer>> nodes = new HashMap<>();
                nodes.put(edgeNodes.first, new HashSet<>());
                nodes.put(edgeNodes.second, new HashSet<>());
                LinkedList<Edge> edges = new LinkedList<>();
                edgeController.findAllIDs(Graphs.getEdge(edgeName)).forEach(edge -> {
                    edges.add(new Edge(edge));
                    nodes.get(edgeNodes.first).add(edge.getId1());
                    nodes.get(edgeNodes.second).add(edge.getId2());
                });
                finalG.addEdges(Graphs.getEdge(edgeName), edges);

                nodes.forEach((k, v) -> {
                    NodeFilter nf = new NodeFilter(nodeController.getFilter(Graphs.getNode(k)), v);
                    finalG.saveNodeFilter(Graphs.getNode(k), nf);
                    finalG.addNodes(k, nodeFilterToNode(nf));
                });
            } else {
                boolean extend = (extendPPI & edgeName.equals("ProteinProteinInteraction")) | (extendGGI & edgeName.equals("GeneGeneInteraction")) | edgeName.equals("DisorderHierarchy") | (!Objects.equals(edgeNodes.first, edgeNodes.second) & !(connectedNodes.contains(Graphs.getNodesfromEdge(Graphs.getEdge(edgeName)).first) & connectedNodes.contains(Graphs.getNodesfromEdge(Graphs.getEdge(edgeName)).second)));
                extendGraph(finalG, edgeName, !extend, false, drugTargetsWithAction, disorderAssociationCutoff, disorderParents, experimentalInteraction, "");

                if (request.connectedOnly) {
                    request.nodes.forEach((k, v) -> removeUnconnectedNodes(finalG, Graphs.getNode(k), null));
                }
            }
            LinkedList<String> nodes = new LinkedList<>(Arrays.asList(Graphs.getNode(edgeNodes.first), Graphs.getNode(edgeNodes.second)));
            nodes.forEach(nodeName -> {
                connectedNodes.add(Graphs.getNode(nodeName));
                FilterGroup req = request.nodes.get(nodeName);
                if (req.ids != null | (req.filters != null && req.filters.length > 0)) {
                    HashSet<Integer> nodeIds = new HashSet<>(finalG.getNodes().get(Graphs.getNode(nodeName)).keySet());
                    if (nodeName.equals("drug")) {
                        if (drugsFilterElements) {
                            getElementFilteredDrugs(nodeIds);
                        }
                        if (drugsApprovedOnly) {
                            getApprovedOnlyDrugs(nodeIds);
                        }
                    }
                    if (nodeName.equals("gene") & genesOnlyCoding) {
                        getOnlyCodingGenes(nodeIds);
                    }
                    if (nodeIds.size() < finalG.getNodes().get(Graphs.getNode(nodeName)).size()) {
                        NodeFilter nf = new NodeFilter(finalG.getNodeFilter(nodeName), nodeIds);
                        finalG.saveNodeFilter(nodeName, nf);
                        finalG.getNodes().put(Graphs.getNode(nodeName), nodeFilterToNode(nf));
                    }
                }
            });
            edgeList.clear();
            request.edges.keySet().stream().filter(e -> !addedEdges.contains(e)).filter(e -> connectedNodes.contains(Graphs.getNodesfromEdge(Graphs.getEdge(e)).first) & connectedNodes.contains(Graphs.getNodesfromEdge(Graphs.getEdge(e)).second)).forEach(edgeList::add);
            request.edges.keySet().stream().filter(e -> !addedEdges.contains(e)).filter(e -> !edgeList.contains(e)).filter(e -> connectedNodes.contains(Graphs.getNodesfromEdge(Graphs.getEdge(e)).first) | connectedNodes.contains(Graphs.getNodesfromEdge(Graphs.getEdge(e)).second)).forEach(edgeList::add);
            request.edges.keySet().stream().filter(e -> !addedEdges.contains(e)).filter(e -> !edgeList.contains(e)).forEach(edgeList::add);
        }

        return g;
    }

    public Suggestions getSuggestions(SuggestionRequest request) {
        NodeFilter nf = null;
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

        if (request.typeCount != null) {
            boolean sourceEqualsTargetType = request.name.equals(request.typeCount);
            NodeFilter finalNf = nf;
            LinkedList<Suggestion> keep = new LinkedList<>();
            suggestions.getSuggestions().forEach(suggestion -> {
                int count = sourceEqualsTargetType ? suggestion.getSize() : getConnectedNodesCount(request.name, request.typeCount, suggestion.getSId().contains("_") ? finalNf.getEntry(suggestion.getSId()).stream().map(FilterEntry::getNodeId).collect(Collectors.toSet()) : Collections.singletonList(Integer.parseInt(suggestion.getSId())));
                if (count > 0) {
                    suggestion.setTargetCount(count);
                    keep.add(suggestion);
                }
            });
            suggestions.setSuggestions(keep);
        }

        return suggestions;
    }

//    public LinkedList<Integer> getQuickExample(String nodeName, int nr) {
//        HashSet<Integer> ids = new HashSet<>();
//        switch (nr) {
//            case 0 -> ids.addAll(nodeController.filterDisorder().distinctMatches(FilterType.UMBRELLA_DISORDER, "alzheimer disease").entrySet().stream().findFirst().get().getValue().stream().map(FilterEntry::getNodeId).collect(Collectors.toSet()));
//            case 1 -> ids.addAll(nodeController.filterDrugs().distinctMatches(FilterType.CATEGORY, "breast cancer resistance protein inhibitors").entrySet().stream().findFirst().get().getValue().stream().map(FilterEntry::getNodeId).collect(Collectors.toSet()));
//            case 2 -> ids.addAll(nodeController.filterGenes().matches("(pten|brca1|brca2)").toList(-1).stream().filter(i -> !i.getName().startsWith("entrez")).map(FilterEntry::getNodeId).collect(Collectors.toSet()));
//        }
//        return new LinkedList(ids);
//    }

    public Collection<Integer> getSuggestionEntry(String gid, String nodeName, String sid) {

        return (gid == null ? nodeController.getFilter(nodeName) : getCachedGraph(gid).getNodeFilter(nodeName)).getEntry(sid).stream().map(FilterEntry::getNodeId).collect(Collectors.toSet());
    }

    public HashMap<String, Object> getDisorderHierarchy(String sid) {
        HashMap<String, Object> graph = new HashMap<>();
        HashMap<Integer, WebNode> nodes = new HashMap<>();
        LinkedList<WebEdge> edges = new LinkedList<>();
        Disorder root = nodeController.findDisorder(nodeController.getFilter("disorder").getEntry(sid).stream().findFirst().get().getNodeId());
        nodes.put(root.getId(), new WebNode(root.getId(), root.getDisplayName(), "disorder"));
        getDisorderChildren(root.getId(), nodes, edges);
        graph.put("nodes", new LinkedList(nodes.values()));
        graph.put("edges", edges);
        return graph;
    }

    public void getDisorderChildren(Integer id, HashMap<Integer, WebNode> nodes, LinkedList<WebEdge> edges) {
        try {
            this.edgeController.getDisorderIsADisorderChildren(id).forEach(edge -> {
                if (edge.getId1() == edge.getId2())
                    return;
                int childId = id == edge.getId1() ? edge.getId2() : edge.getId1();
                Disorder d = nodeController.findDisorder(childId);
                nodes.put(d.getId(), new WebNode(d.getId(), d.getDisplayName(), "disorder"));
                edges.add(new WebEdge(edge));
                getDisorderChildren(childId, nodes, edges);
            });
        } catch (NullPointerException ignore) {
        }
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
                extendGraph(g, e, inducedEdges.contains(e), switched.contains(e), false, null, switched.contains(e) && e.equals("DisorderHierarchy"), false, "");
            });
            if (added.isEmpty())
                break;
            requestedEdges.removeAll(added);
        }
    }

    public Graph extendGraph(String gid, String e, boolean endDefined, boolean switched, boolean drugTargetActionFilter, Double disorderGenomeAssociationCutoff, boolean getDisorderParents, boolean experimentalInteractionsOnly, String tissueFilter) {
        return this.extendGraph(getCachedGraph(gid), e, endDefined, switched, drugTargetActionFilter, disorderGenomeAssociationCutoff, getDisorderParents, experimentalInteractionsOnly, tissueFilter);
    }

    public Graph extendGraph(Graph g, String e, boolean endDefined, boolean switched, boolean drugTargetActionFilter, Double disorderGenomeAssociationCutoff, boolean getDisorderParents, boolean experimentalInteractionsOnly, String tissueFilter) {
        int edgeId = g.getEdge(e);
        boolean extend = !endDefined;
        Pair<Integer, Integer> nodeIds = g.getNodesfromEdge(edgeId);
        LinkedList<Edge> edges = new LinkedList<>();
        HashSet<Integer> nodes = new HashSet<>();
        boolean isTissueFilter = tissueFilter != null && this.edgeController.getTissueIDMap().containsKey(tissueFilter);
        Integer tissueId = null;
        if (isTissueFilter)
            tissueId = this.edgeController.getTissueIDMap().get(tissueFilter);
        if (g.getNodes().containsKey(nodeIds.first) & nodeIds.first.equals(nodeIds.second)) {
            Integer finalTissueId = tissueId;
            g.getNodes().get(nodeIds.first).keySet().forEach(nodeId1 -> {
                try {
                    HashSet<PairId> edgeIds;
                    if (e.equals("DisorderHierarchy")) {
                        edgeIds = getDisorderParents ? edgeController.getDisorderIsADisorderParents(nodeId1) : edgeController.getDisorderIsADisorderChildren(nodeId1);
                    } else {
                        edgeIds = edgeController.getEdges(edgeId, nodeIds.first, nodeId1, switched);
                    }
                    if (edgeIds == null || edgeIds.isEmpty())
                        return;
                    if (isTissueFilter & (e.equals("GeneGeneInteraction") | e.equals("ProteinProteinInteraction"))) {
                        edgeIds.removeAll(edgeIds.stream().filter(p -> !edgeController.isTissue(edgeId, p.getId1(), p.getId2(), finalTissueId)).collect(Collectors.toSet()));
                    }
                    if (experimentalInteractionsOnly & (e.equals("GeneGeneInteraction") | e.equals("ProteinProteinInteraction"))) {
                        edgeIds.removeAll(edgeIds.stream().filter(p -> !edgeController.isExperimental(edgeId, p.getId1(), p.getId2())).collect(Collectors.toSet()));
                    }

                    if (drugTargetActionFilter) {
                        if (e.equals("DrugTargetGene"))
                            edgeController.findAllDrugHasTargetGene(edgeIds).forEach(edge -> {
                                if (edge.getActions().size() == 0) edgeIds.remove(edge.getPrimaryIds());
                            });
                        if (e.equals("DrugTargetProtein"))
                            edgeController.findAllDrugHasTargetProtein(edgeIds).forEach(edge -> {
                                if (edge.getActions().size() == 0) edgeIds.remove(edge.getPrimaryIds());
                            });
                    }
                    if (disorderGenomeAssociationCutoff != null && disorderGenomeAssociationCutoff > 0) {
                        if (e.equals("GeneAssociatedWithDisorder"))
                            edgeController.findAllGeneAssociatedWithDisorder(edgeIds).forEach(edge -> {
                                if (edge.getScore() < disorderGenomeAssociationCutoff)
                                    edgeIds.remove(edge.getPrimaryIds());
                            });
                        if (e.equals("ProteinAssociatedWithDisorder"))
                            edgeController.findAllProteinAssociatedWithDisorder(edgeIds).forEach(edge -> {
                                if (edge.getScore() < disorderGenomeAssociationCutoff)
                                    edgeIds.remove(edge.getPrimaryIds());
                            });
                    }
                    edgeIds.forEach(id -> {
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
            return g;
        }
        int[] existing = new int[]{g.getNodes().containsKey(nodeIds.first) ? nodeIds.first : nodeIds.second};
        int[] adding = new int[]{(existing[0] == nodeIds.first ? nodeIds.second : nodeIds.first)};
        if (g.getNodes().containsKey(nodeIds.first) & g.getNodes().containsKey(nodeIds.second)) {
            existing = new int[]{existing[0], adding[0]};
            adding = new int[]{adding[0], existing[0]};
            g.getNodes().get(nodeIds.first).keySet().forEach(nodeId1 -> {
                try {
                    HashSet<PairId> edgeIds = edgeController.getEdges(edgeId, nodeIds.first, nodeId1, switched);
                    if (edgeIds == null || edgeIds.isEmpty())
                        return;
                    if (drugTargetActionFilter) {
                        if (e.equals("DrugTargetGene"))
                            edgeController.findAllDrugHasTargetGene(edgeIds).forEach(edge -> {
                                if (edge.getActions().size() == 0) edgeIds.remove(edge.getPrimaryIds());
                            });
                        if (e.equals("DrugTargetProtein"))
                            edgeController.findAllDrugHasTargetProtein(edgeIds).forEach(edge -> {
                                if (edge.getActions().size() == 0) edgeIds.remove(edge.getPrimaryIds());
                            });
                    }
                    if (disorderGenomeAssociationCutoff != null && disorderGenomeAssociationCutoff > 0) {
                        if (e.equals("GeneAssociatedWithDisorder"))
                            edgeController.findAllGeneAssociatedWithDisorder(edgeIds).forEach(edge -> {
                                if (edge.getScore() < disorderGenomeAssociationCutoff)
                                    edgeIds.remove(edge.getPrimaryIds());
                            });
                        if (e.equals("ProteinAssociatedWithDisorder"))
                            edgeController.findAllProteinAssociatedWithDisorder(edgeIds).forEach(edge -> {
                                if (edge.getScore() < disorderGenomeAssociationCutoff)
                                    edgeIds.remove(edge.getPrimaryIds());
                            });
                    }
                    edgeIds.forEach(id -> {
                        if (g.getNodes().get(nodeIds.second).containsKey(id.getId2()) && g.getNodes().get(nodeIds.first).containsKey(id.getId1()))
                            edges.add(new Edge(id));
                    });
                } catch (NullPointerException ignore) {
                }
            });
            if (!extend) {
                g.addEdges(edgeId, edges);
                return g;
            }
        }
        HashMap<Integer, HashMap<Integer, Node>> nodeMap = new HashMap<>();
        for (int i = 0; i < existing.length; i++) {
            int node1 = existing[i];
            g.getNodes().get(node1).keySet().forEach(nodeId1 -> {
                try {
                    HashSet<PairId> edgeIds = new HashSet<>(edgeController.getEdges(edgeId, node1, nodeId1, switched));
                    if (edgeIds.isEmpty())
                        return;
                    if (drugTargetActionFilter) {
                        if (e.equals("DrugTargetGene"))
                            edgeController.findAllDrugHasTargetGene(edgeIds).forEach(edge -> {
                                if (edge.getActions().size() == 0) edgeIds.remove(edge.getPrimaryIds());
                            });
                        if (e.equals("DrugTargetProtein"))
                            edgeController.findAllDrugHasTargetProtein(edgeIds).forEach(edge -> {
                                if (edge.getActions().size() == 0) edgeIds.remove(edge.getPrimaryIds());
                            });
                    }
                    if (disorderGenomeAssociationCutoff != null && disorderGenomeAssociationCutoff > 0) {
                        if (e.equals("GeneAssociatedWithDisorder"))
                            edgeController.findAllGeneAssociatedWithDisorder(edgeIds).forEach(edge -> {
                                if (edge.getScore() < disorderGenomeAssociationCutoff)
                                    edgeIds.remove(edge.getPrimaryIds());
                            });
                        if (e.equals("ProteinAssociatedWithDisorder"))
                            edgeController.findAllProteinAssociatedWithDisorder(edgeIds).forEach(edge -> {
                                if (edge.getScore() < disorderGenomeAssociationCutoff)
                                    edgeIds.remove(edge.getPrimaryIds());
                            });
                    }

                    if (node1 == nodeIds.getFirst())
                        edgeIds.forEach(id -> {
                                    edges.add(new Edge(id));
                                    nodes.add(id.getId2());
                                }
                        );
                    else
                        edgeIds.forEach(id -> {
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
        return g;
    }

    public WebGraphInfo getExtension(ExtensionRequest request) {
        Graph g = getCachedGraph(request.gid).clone(historyController.getGraphId());
        cache.put(g.getId(), g);
        HashSet<String> inducedEdges = new HashSet<>(Arrays.asList(request.induced));
        HashSet<String> requestedEdges = new HashSet<>(Arrays.asList(request.edges));
        HashSet<String> switched = new HashSet<>(Arrays.asList(request.switchDirection));
        extendGraph(g, requestedEdges, inducedEdges, switched);
        g.calculateDegrees();
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
        g.calculateDegrees();
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
        g.addCustomAttributeLabel(eid, "Weight", "Weight");
        g.addCustomEdgeAttribute(eid, "JaccardIndex", jaccardIndex);
        g.addCustomAttributeType(eid, "JaccardIndex", "numeric");
        g.addCustomAttributeLabel(eid, "JaccardIndex", "Jaccard Index");
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
        list.addAll(g.getCustomEdgeAttributeTypes(edgeId).keySet());
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
        graph.getCustomEdgeAttributeTypes(edgeId).keySet().forEach(attrName -> {
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
            //TODO just add detailAttributeList as static to edges
            HashMap<String, Object> details = new HashMap<>();
            edgeController.edgeToAttributeList(edgeId, p, new HashSet<>(Arrays.asList(edgeController.getDetailAttributes(edgeId)))).forEach((k, v) -> details.put(edgeController.getAttributeLabelMap(name).get(k), v));
            details.put("order", edgeController.getDetailLabels(edgeId));
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

    public void removeTempDir(File wd) {
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

    public void applyJob(Job j, boolean basisIsJob) {

        Graph g = getCachedGraph(j.getBasisGraph());
        Graph derived;

        Algorithm algorithm = algorithms.get(j.getMethod());

        int nodeTypeId = algorithm.getNodeType(j);

        if (nodeTypeId == Graphs.getNode("drug"))
            filterDrugsMap(nodeTypeId, j);

        boolean integrateOriginalGraph = algorithm.integrateOriginalGraph(j);

        if (!integrateOriginalGraph) {
            derived = new Graph(historyController.getGraphId());
            derived.setParent(g.getId());
        } else
            derived = g.clone(historyController.getGraphId());

        algorithm.createGraph(derived, j, nodeTypeId, g);

        if (algorithm.getEnum().equals(ToolService.Tool.BICON)) {
            extendGraph(derived, "GeneGeneInteraction", true, false, false, 0.0, false, Boolean.parseBoolean(j.getParams().get("experimentalOnly")), j.getParams().get("tissue"));
        } else {
            //TODO maybe as an option
            //if (!algorithm.hasCustomEdges())
            updateEdges(derived, j, nodeTypeId);
        }
        derived.calculateDegrees();
        AtomicInteger size = new AtomicInteger();
        derived.getNodes().forEach((k, v) -> size.addAndGet(v.size()));
        derived.getEdges().forEach((k, v) -> size.addAndGet(v.size()));
        if (size.get() < 100_000) {
            if (!basisIsJob)
                derived.setParent(null);
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
            HashSet<String> cats = new HashSet<>(((Drug) nodeController.getNode(Graphs.getNode(nodeTypeId), e.getKey())).getDrugCategories());
            if (!elements && (cats.contains("Elements") | cats.contains("Metal Cations") | cats.contains("Zinc Compounds") | cats.contains("Metals") | cats.contains("Mineral Supplements") | cats.contains("Minerals")))
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
        boolean isDrug = nodeTypeId == Graphs.getNode("drug");
        String tissueFilter = j.getParams().get("tissue");
        boolean isTissueFilter = tissueFilter != null && edgeController.getTissueIDMap().containsKey(tissueFilter);
        Integer tissueID = null;
        if (isTissueFilter)
            tissueID = edgeController.getTissueIDMap().get(tissueFilter);
        if (!isDrug && j.getParams().containsKey("addInteractions") && j.getParams().get("addInteractions").equals("true")) {
            boolean expOnly = j.getParams().containsKey("experimentalOnly") && j.getParams().get("experimentalOnly").equals("true");
            int typeId1 = nodeTypeId;
            int typeId2 = Graphs.getNode(j.getTarget());
            List<Integer> edgeIds = Graphs.getEdgesfromNodes(typeId1, typeId2);
            for (int edgeId : edgeIds) {
                if (!g.getEdges().containsKey(edgeId))
                    g.getEdges().put(edgeId, new LinkedList<>());
                HashMap<Integer, HashSet<Integer>> edges = new HashMap<>();
                Integer finalTissueID = tissueID;
                g.getNodes().get(typeId1).keySet().forEach(n -> {
                    try {
                        edgeController.getEdges(edgeId, typeId1, n, false).stream().filter(e -> g.getNodes().get(typeId2).containsKey(e.getId2())).forEach(e -> {
                            int n2 = e.getId2();
                            if (n < n2) {
                                if ((!expOnly | edgeController.isExperimental(edgeId, n, n2)) && (!isTissueFilter | edgeController.isTissue(edgeId, n, n2, finalTissueID))) {
                                    if (!edges.containsKey(n))
                                        edges.put(n, new HashSet<>());
                                    edges.get(n).add(n2);
                                }
                            } else {
                                if ((!expOnly | edgeController.isExperimental(edgeId, n2, n)) && (!isTissueFilter | edgeController.isTissue(edgeId, n2, n, finalTissueID))) {
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

        if (isDrug && j.getParams().containsKey("addInteractions") && j.getParams().get("addInteractions").equals("true")) {
            int typeId1 = nodeTypeId;
            int typeId2 = Graphs.getNode(j.getTarget());
            List<Integer> edgeIds = Graphs.getEdgesfromNodes(typeId1, typeId2);
            for (int edgeId : edgeIds) {
                if (!g.getEdges().containsKey(edgeId))
                    g.getEdges().put(edgeId, new LinkedList<>());
                if (!Graphs.checkEdgeDirection(edgeId, typeId1, typeId2)) {
                    int tmp = typeId1;
                    typeId1 = typeId2;
                    typeId2 = tmp;
                }
                HashMap<Integer, HashSet<Integer>> edges = new HashMap<>();
                int finalTypeId = typeId1;
                int finalTypeId1 = typeId2;
                g.getNodes().get(typeId1).keySet().forEach(n -> {
                    try {
                        edgeController.getEdges(edgeId, finalTypeId, n, false).stream().filter(e -> g.getNodes().get(finalTypeId1).containsKey(e.getId2())).forEach(e -> {
                            int n2 = e.getId2();
                            if (!edges.containsKey(n))
                                edges.put(n, new HashSet<>());
                            edges.get(n).add(n2);
                        });
                    } catch (NullPointerException ignore) {
                    }
                });
                edges.forEach((id1, l) -> l.forEach(id2 -> g.getEdges().get(edgeId).add(new Edge(id1, id2))));
            }
        }
    }

    public File getDownload(String gid) {
        return getDownload(gid, getGraphWD(gid), false);
    }

    public File getDownload(String gid, boolean basic) {
        return getDownload(gid, getGraphWD(gid), basic);
    }

    public File getDownload(String gid, File wd, boolean basic) {
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
                    req.attributes.get("nodes").put(Graphs.getNode(type), basic ? new String[]{"primaryDomainId", "id", "type"} : nodeController.getAttributes(type));
                });

                g.getEdges().keySet().forEach(type -> {
                    if (!req.attributes.containsKey("edges"))
                        req.attributes.put("edges", new HashMap<>());
                    if (basic && type >= 0) {
                        HashSet<String> basicAttributes = new HashSet<>(Arrays.asList("sourceDomainId", "targetDomainId", "type", "memberOne", "memberTwo"));
                        req.attributes.get("edges").put(g.getEdge(type), Arrays.stream(edgeController.getAttributes(type)).filter(basicAttributes::contains).collect(Collectors.toList()).toArray(new String[]{}));
                    } else
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
        File layout = historyController.getLayoutPath(gid);
        if (layout != null && layout.exists())
            layout.delete();
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
        toolService.createThumbnail(getDownload(gid, true), thumb);
        socketController.setThumbnailReady(gid);
        thumbnailGenerating.remove(gid);
        removeTempDir(gid);
    }

    public HashMap<Integer, HashMap<Integer, Point2D>> getLayout(Graph g) {
        return getLayout(g, historyController.getLayoutPath(g.getId()));
    }

    public HashMap<Integer, HashMap<Integer, Point2D>> getLayout(Graph g, File lay) {
        if (!lay.exists()) {
            lay.getParentFile().mkdirs();
            if (layoutGenerating.contains(g.getId())) {
                while (layoutGenerating.contains(g.getId())) {
                }
            } else {
                layoutGenerating.add(g.getId());
                if (!this.getThumbnail(g.getId()).exists()) {
                    thumbnailGenerating.add(g.getId());
                    toolService.createLayoutAndThumbnail(getDownload(g.getId(), true), lay, getThumbnail(g.getId()));
                    thumbnailGenerating.remove(g.getId());
                    socketController.setThumbnailReady(g.getId());
                } else {
                    toolService.createLayout(getDownload(g.getId(), true), lay);
                }
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

    public LinkedList<Object> mapDomainIdsToItemList(String type, Collection<String> file) {
        LinkedList<Object> out = new LinkedList<>();

        List<Integer> ids = new LinkedList<>();
        file.forEach(id -> {
            id = id.strip();
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

    public LinkedList<Object> mapIdsToItemList(String type, List<Integer> ids, List<String> attributes) {
        LinkedList<Object> out = new LinkedList<>();
        nodeController.findByIds(type, ids).forEach(o -> {
            RepoTrialNode n = null;
            switch (type) {
                case "gene" -> n = (Gene) o;
                case "protein" -> n = (Protein) o;
                case "pathway" -> n = (Pathway) o;
                case "disorder" -> n = (Disorder) o;
                case "drug" -> n = (Drug) o;
            }
            out.add(n.getAsMap(new HashSet<>(attributes == null ? Arrays.asList("id", "displayName", "primaryDomainId") : attributes)));
        });
        return out;
    }

    public Integer getConnectedNodesCount(String sourceType, String targetType, Collection<Integer> sourceIds) {
        LinkedList<Integer> edgeIds = Graphs.getEdgesfromNodes(Graphs.getNode(sourceType), Graphs.getNode(targetType));
        HashSet<Integer> nodeList = new HashSet<>();
        int edgeId = edgeIds.getFirst();
        sourceIds.forEach(sourceId -> {
            try {
                edgeController.getEdges(edgeId, Graphs.getNode(sourceType), sourceId, false).forEach(e -> nodeList.add(Graphs.getNode(sourceType) == Graphs.getNodesfromEdge(edgeId).first ? e.getId2() : e.getId1()));
            } catch (NullPointerException ignore) {
            }
        });
        return nodeList.size();

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
                            ((List<String>) edgeEntries.get("databases")).forEach(s -> sources.add(sourceType + ":" + s));
                        else if (edgeEntries.containsKey("assertedBy"))
                            ((List<String>) edgeEntries.get("assertedBy")).forEach(s -> sources.add(sourceType + ":" + s));
                        else
                            DBConfig.getConfig().getEdge(edgeId).databases.forEach(s -> sources.add(sourceType + ":" + s));

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

    public HashSet<Integer> getElementFilteredDrugs(Collection<Integer> drugs) {
        HashSet<Integer> out = new HashSet<>();
        for (Drug d : nodeController.findDrugs(drugs)) {
            HashSet<String> cats = new HashSet<>(d.getDrugCategories());
            if ((cats.contains("Elements") | cats.contains("Metal Cations") | cats.contains("Zinc Compounds") | cats.contains("Metals") | cats.contains("Mineral Supplements") | cats.contains("Minerals")))
                continue;
            out.add(d.getId());
        }
        return out;
    }

    public HashSet<Integer> getApprovedOnlyDrugs(Collection<Integer> drugs) {
        HashSet<Integer> out = new HashSet<>();
        for (Drug d : nodeController.findDrugs(drugs)) {
            if (d.getDrugGroups().contains("approved"))
                out.add(d.getId());
        }
        return out;
    }

    public HashSet<Integer> getOnlyCodingGenes(Collection<Integer> genes) {
        HashSet<Integer> out = new HashSet<>();
        for (Gene g : nodeController.findGenes(genes)) {
            if (g.getGeneType().equals("protein-coding"))
                out.add(g.getId());
        }
        return out;
    }

    public WebGraphInfo getGuidedGraph(GuidedRequest request) {
        Graph g = new Graph(historyController.getGraphId());
        cache.put(g.getId(), g);
        int sourceTypeId = Graphs.getNode(request.sourceType);
        int targetTypeId = Graphs.getNode(request.targetType);

        Collection<Integer> sids = request.sources;
        boolean elementFilter = (boolean) request.params.get("nodes").get("filterElementDrugs");
        boolean approvedFilter = (boolean) request.params.get("nodes").get("approvedDrugsOnly");
        boolean codingFilter = (boolean) request.params.get("nodes").get("codingGenesOnly");
        boolean fullPaths = (boolean) request.params.get("general").get("removePartial");

        boolean expInteractions = (boolean) request.params.get("edges").get("experimentalInteraction");
        boolean drugTargetWithAction = (boolean) request.params.get("edges").get("drugTargetsWithAction");
        double disorderAssociationCutoff = Double.parseDouble(request.params.get("edges").get("disorderAssociationCutoff").toString());
        boolean getDisorderParents = (boolean) request.params.get("edges").get("disorderParents");

        if (request.sourceType.equals("drug") & (elementFilter | approvedFilter)) {
            sids = elementFilter ? getElementFilteredDrugs(sids) : sids;
            sids = approvedFilter ? getApprovedOnlyDrugs(sids) : sids;
        }
        if (request.sourceType.equals("gene") & (codingFilter))
            sids = getOnlyCodingGenes(sids);

        NodeFilter nfs = new NodeFilter(nodeController.getFilter(request.sourceType), sids);
        g.addNodes(sourceTypeId, nodeFilterToNode(nfs));
        g.saveNodeFilter(request.sourceType, nfs);

        for (int p = 0; p < request.path.size(); p++) {
            boolean secondPath = p == request.path.size() - 1;
            boolean endDefined = request.targets.size() > 0;
            if (secondPath & endDefined) {
                NodeFilter nft;
                Collection<Integer> tids = request.targets;
                if (request.targetType.equals("drug") & (elementFilter | approvedFilter)) {
                    tids = elementFilter ? getElementFilteredDrugs(tids) : tids;
                    tids = approvedFilter ? getApprovedOnlyDrugs(tids) : tids;
                }
                if (request.targetType.equals("gene") & codingFilter) {
                    tids = getOnlyCodingGenes(tids);
                }
                nft = new NodeFilter(nodeController.getFilter(request.targetType), tids);
                g.addNodes(targetTypeId, nodeFilterToNode(nft));
                g.saveNodeFilter(request.targetType, nft);
            }
            String edgeName = request.path.get(p).get("label");
            boolean drugTargetFilter = drugTargetWithAction && (edgeName.equals("DrugTargetGene") | edgeName.equals("DrugTargetProtein"));
            boolean interactionFilter = expInteractions && (edgeName.equals("GeneGeneInteraction") | edgeName.equals("ProteinProteinInteraction"));
            this.extendGraph(g, edgeName, endDefined, false, drugTargetFilter, disorderAssociationCutoff, getDisorderParents, interactionFilter, "");

            String connector = request.path.get(p).get("connector");
            if (connector != null) {
                HashMap<Integer, Node> nodes = g.getNodes().get(Graphs.getNode(connector));
                HashSet<Integer> ids = new HashSet<>(nodes.keySet());
                if (request.connectors != null) {
                    if (request.excludeConnectors) {
                        request.connectors.forEach(ids::remove);
                    } else {
                        ids = request.connectors;
                    }
                }

                if (connector.equals("drug") && (elementFilter || approvedFilter)) {
                    ids = elementFilter ? getElementFilteredDrugs(nodes.keySet()) : ids;
                    ids = approvedFilter ? getApprovedOnlyDrugs(ids) : ids;
                }
                if (connector.equals("gene") & codingFilter) {
                    ids = getOnlyCodingGenes(ids);
                }
                if (ids.size() < nodes.size()) {
                    NodeFilter nf = new NodeFilter(nodeController.getFilter(connector), ids);
                    g.getNodes().put(Graphs.getNode(connector), nodeFilterToNode(nf));
                    g.saveNodeFilter(connector, nf);
                    removeUnconnectedEdges(g, edgeName);
                }
                if (secondPath && fullPaths) {
                    HashSet<Integer> targets;
                    if (endDefined)
                        targets = request.targets;
                    else {
                        targets = new HashSet<>(g.getNodes().get(Graphs.getNode(request.targetType)).keySet());
                        targets.removeAll(request.sources);
                    }
                    removeNonConnectingNodes(g, Graphs.getNode(connector), request.sources, Graphs.getNode(request.sourceType), targets, Graphs.getNode(request.targetType));
                }
            }
            if (secondPath && !endDefined) {
                this.removeUnconnectedNodes(g, Graphs.getNode(request.targetType), request.targetType.equals(request.sourceType) ? request.sources : null);
            }
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
        g.calculateDegrees();
        addGraphToHistory(request.uid, g.getId());
        if (request.path.size() > 1 && (boolean) request.params.get("general").get("keep")) {
            List<Integer> targets;
            if (request.targets != null && request.targets.size() > 0)
                targets = new LinkedList<>(request.targets);
            else {
                targets = new LinkedList<>(g.getNodes().get(Graphs.getNode(request.targetType)).keySet());
                targets.removeAll(sids);
            }
            List<Integer> sources = new LinkedList<>(sids);
            sources.sort(Comparator.comparingInt(o -> g.getNodes().get(sourceTypeId).get(o).getDegree()).reversed());
            targets.sort(Comparator.comparingInt(o -> g.getNodes().get(targetTypeId).get(o).getDegree()).reversed());
            this.createTripartiteLayout(g, historyController.getTriLayoutPath(g.getId()), sourceTypeId, sources, targetTypeId, targets);
        }
        return g.toInfo();
    }


    public void createTripartiteLayout(Graph g, File lay, int sourceTypeId, Collection<Integer> sources, int targetTypeId, Collection<Integer> targets) {
        if (!lay.exists()) {
            File wd = new File(getGraphWD(g.getId()).getAbsolutePath() + "_tri");
            lay.getParentFile().mkdirs();
            File sourceFile = new File(wd, "sources.tsv");
            WriterUtils.writeTo(sourceFile, sources.stream().map(s -> nodeController.getDomainId(sourceTypeId, s) + "\t" + g.getNodes().get(sourceTypeId).get(s).getDegree() + "\n").collect(Collectors.joining()));
            File targetFile = new File(wd, "targets.tsv");
            WriterUtils.writeTo(targetFile, targets.stream().map(s -> nodeController.getDomainId(targetTypeId, s) + "\t" + g.getNodes().get(targetTypeId).get(s).getDegree() + "\n").collect(Collectors.joining()));
            toolService.createTripartiteLayout(getDownload(g.getId(), wd, true), lay, sourceFile, targetFile);
            removeTempDir(wd);
        }
    }

    public HashMap<Integer, HashMap<Integer, Point2D>> getTripartiteLayout(Graph g) {
        File lay = historyController.getTriLayoutPath(g.getId());
        HashMap<Integer, HashMap<Integer, Point2D>> coords = new HashMap<>();
        if (!lay.exists()) {
            return coords;
        }

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
        return coords;
    }

    private void removeUnconnectedEdges(Graph g, String edgeName) {
        removeUnconnectedEdges(g, g.getEdge(edgeName));
    }

    private void removeUnconnectedEdges(Graph g, Integer edgeId) {
        HashSet<Edge> rem = new HashSet<>();
        Pair<Integer, Integer> nodes = g.getNodesfromEdge(edgeId);
        Set<Integer> set1 = g.getNodes().get(nodes.first).keySet();
        Set<Integer> set2 = g.getNodes().get(nodes.second).keySet();
        g.getEdges().get(edgeId).forEach(e -> {
            if (!set1.contains(e.getId1()) | !set2.contains(e.getId2()))
                rem.add(e);
        });
        g.getEdges().get(edgeId).removeAll(rem);
    }

    private void removeUnconnectedNodes(Graph g, int node, HashSet<Integer> except) {
        if (!g.getNodes().containsKey(node))
            return;
        HashSet<Integer> ids = new HashSet<>(g.getNodes().get(node).keySet());
        if (except != null)
            ids.removeAll(except);
        g.getEdges().forEach((key, vals) -> {
            Pair<Integer, Integer> edge = Graphs.getNodesfromEdge(key);
            if (edge.first == node) {
                vals.forEach(e -> ids.remove(e.getId1()));
            }
            if (edge.second == node) {
                vals.forEach(e -> ids.remove(e.getId2()));
            }
        });
        if (!ids.isEmpty()) {
            NodeFilter nf = g.getNodeFilter(Graphs.getNode(node));
            nf.removeByNodeIds(ids);
            g.getNodes().put(node, nodeFilterToNode(nf));
            g.saveNodeFilter(Graphs.getNode(node), nf);
        }
    }

    private void removeNonConnectingNodes(Graph g, int connectType, Collection<Integer> set1, int type1, Collection<Integer> set2, int type2) {
        if (!g.getNodes().containsKey(connectType))
            return;
        HashSet<Integer> set1Connectors = new HashSet<>();
        HashSet<Integer> set2Connectors = new HashSet<>();
        HashMap<Integer, HashMap<Integer, HashSet<Edge>>> connectorEdges = new HashMap<>();
        g.getEdges().forEach((key, vals) -> {
            HashMap<Integer, HashSet<Edge>> edges = new HashMap<>();
            connectorEdges.put(key, edges);
            Pair<Integer, Integer> edge = Graphs.getNodesfromEdge(key);
            if (edge.first == connectType) {
                if (edge.second == type1)
                    vals.stream().filter(e -> set1.contains(e.getId2())).forEach(e -> {
                        set1Connectors.add(e.getId1());
                        if (!edges.containsKey(e.getId1()))
                            edges.put(e.getId1(), new HashSet<>());
                        edges.get(e.getId1()).add(e);
                    });
                if (edge.second == type2)
                    vals.stream().filter(e -> set2.contains(e.getId2())).forEach(e -> {
                        set2Connectors.add(e.getId1());
                        if (!edges.containsKey(e.getId1()))
                            edges.put(e.getId1(), new HashSet<>());
                        edges.get(e.getId1()).add(e);
                    });
            }
            if (edge.second == connectType) {
                if (edge.first == type1)
                    vals.stream().filter(e -> set1.contains(e.getId1())).forEach(e -> {
                        set1Connectors.add(e.getId2());
                        if (!edges.containsKey(e.getId2()))
                            edges.put(e.getId2(), new HashSet<>());
                        edges.get(e.getId2()).add(e);
                    });
                if (edge.first == type2)
                    vals.stream().filter(e -> set2.contains(e.getId1())).forEach(e -> {
                        set2Connectors.add(e.getId2());
                        if (!edges.containsKey(e.getId2()))
                            edges.put(e.getId2(), new HashSet<>());
                        edges.get(e.getId2()).add(e);
                    });
            }
        });

        HashSet<Integer> connectors = set1Connectors.stream().filter(set2Connectors::contains).collect(Collectors.toCollection(HashSet::new));
        set1Connectors.removeAll(connectors);
        set2Connectors.removeAll(connectors);
        set1Connectors.addAll(set2Connectors);
        if (!set1Connectors.isEmpty()) {
            NodeFilter nf = g.getNodeFilter(Graphs.getNode(connectType));
            nf.removeByNodeIds(set1Connectors);
            g.getNodes().put(connectType, nodeFilterToNode(nf));
            g.saveNodeFilter(Graphs.getNode(connectType), nf);
            connectorEdges.forEach((type, vals) ->
                    g.getEdges().get(type).removeAll(vals.entrySet().stream().filter(e -> set1Connectors.contains(e.getKey())).map(Map.Entry::getValue).flatMap(Collection::stream).collect(Collectors.toSet())));
        }
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

    private String getEdgeTableDownload(Graph g, String name, LinkedList<String> attributes) {
        StringBuilder table = new StringBuilder();
        HashSet<String> edgeAttributes = new HashSet<>();
        int edgeIdx = g.getEdge(name);
        boolean custom = edgeIdx < 0;
        HashMap<String, String> edgeAttributeMap = !custom ? edgeController.getAttributeLabelMap(name) : g.getCustomEdgeAttributeLabels().get(edgeIdx);
        HashMap<String, String> nodeAttributes = new HashMap<>();
        for (String a : attributes) {
            table.append(table.length() == 0 ? "#" : "\t");
            if (edgeAttributeMap.containsKey(a)) {
                edgeAttributes.add(a);
                table.append(edgeAttributeMap.get(a) != null ? edgeAttributeMap.get(a) : a);
            } else {
                switch (a) {
                    case "node1", "Node1" -> nodeAttributes.put(a, "displayName_1");
                    case "node2", "Node2" -> nodeAttributes.put(a, "displayName_2");
                    case "sourceId" -> nodeAttributes.put(a, "id_1");
                    case "targetId" -> nodeAttributes.put(a, "id_2");
                    case "sourceDomainId" -> nodeAttributes.put(a, "primaryDomainId_1");
                    case "targetDomainId" -> nodeAttributes.put(a, "primaryDomainId_2");
                    default -> nodeAttributes.put(a, a);
                }
                if (nodeAttributes.get(a).equals(a)) {
                    table.append(a).append(1).append("\t").append(a).append(2);
                } else {
                    table.append(a);
                }
            }
        }
        table.append("\n");

        Pair<Integer, Integer> ids = g.getNodesfromEdge(g.getEdge(name));
        g.getEdges().get(g.getEdge(name)).forEach(e -> {

            HashMap<String, Object> node1 = nodeController.nodeToAttributeList(ids.first, e.getId1());
            HashMap<String, Object> node2 = nodeController.nodeToAttributeList(ids.second, e.getId2());
            HashMap<String, Object> edge = custom ? g.getCustomEdgeAttributes().get(edgeIdx).get(e.getId1()).get(e.getId2()) : edgeController.edgeToAttributeList(g.getEdge(name), new PairId(e.getId1(), e.getId2()), edgeAttributes);
            StringBuilder line = new StringBuilder();
            attributes.forEach(a -> {
                line.append(line.length() == 0 ? "" : "\t");
                if (edgeAttributes.contains(a)) {
                    line.append(edge.get(a));

                } else {
                    a = nodeAttributes.get(a);
                    if (a.contains("_")) {
                        LinkedList<String> spl = StringUtils.splitFirst(a, '_');
                        line.append((spl.get(1).equals("1") ? node1 : node2).get(spl.get(0)));
                    } else {
                        line.append(node1.get(a)).append("\t").append(node2.get(a));
                    }
                }
            });
            table.append(line).append("\n");
        });
        return table.toString();
    }

    private String getNodeTableDownload(Graph g, String name, LinkedList<String> attributes) {
        StringBuilder table = new StringBuilder();
        HashMap<String, String> labelMap = nodeController.getAttributeLabelMap(name);
        HashMap<String, HashMap<Integer, Object>> customAttributes = g.getCustomNodeAttributes().containsKey(Graphs.getNode(name)) ? g.getCustomNodeAttributes().get(Graphs.getNode(name)) : new HashMap<>();

        for (String a : attributes) {
            table.append(table.length() == 0 ? "#" : "\t").append(labelMap.containsKey(a) ? labelMap.get(a) : g.getCustomNodeAttributeLabels().get(Graphs.getNode(name)).get(a));
        }
        table.append("\n");
        HashSet<Integer> ids = g.getNodes().get(Graphs.getNode(name)).values().stream().map(Node::getId).collect(toCollection(HashSet::new));


        ids.forEach(id -> {
            HashMap<String, Object> attrMap = nodeController.nodeToAttributeList(Graphs.getNode(name), id, new HashSet<>(attributes));
            StringBuilder line = new StringBuilder();
            attributes.forEach(a -> {
                Object value = attrMap.get(a);
                if (value == null && !attrMap.containsKey(a) && customAttributes.containsKey(a))
                    value = customAttributes.get(a).get(id);
                line.append(line.length() == 0 ? "" : "\t").append(value);
            });
            table.append(line).append("\n");
        });
        return table.toString();
    }

    public LinkedList<Object> getDirectNodes(Collection<Integer> ids, HashMap<String, Object> request) {
        return getDirectNodes(ids, request.get("sourceType").toString());
    }

    public LinkedList<Object> getDirectNodes(Collection<Integer> ids, String sourceType) {
        HashSet<Integer> addedIds = new HashSet<>();
        LinkedList<Object> nodes = new LinkedList<>();
        AtomicReference<LinkedList<String>> edgeSource = new AtomicReference<>(null);
        ids.forEach(n -> {
            if (addedIds.add(n)) {
                HashMap<String, Object> attrs = nodeController.getNode(sourceType, n).getAsMap();
                HashMap<String, Object> node = new HashMap<>();
                node.put("id", attrs.get("id"));
                node.put("displayName", attrs.get("displayName"));
                node.put("primaryDomainId", attrs.get("primaryDomainId"));
                if (sourceType.equals("disorder")) {

                    if (edgeSource.get() != null)
                        node.put("sourceDBs", edgeSource);
                    else {
                        edgeSource.set(new LinkedList<>());
                        DBConfig.getConfig().getEdge(Graphs.getEdge("DisorderHierarchy")).databases.forEach(s -> edgeSource.get().add(sourceType + ":" + s));
                        node.put("sourceDBs", edgeSource);
                    }
                } else {
                    node.put("sourceDBs", Collections.singletonList(DBConfig.getConfig().nodes.get(Graphs.getNode(sourceType)).sourceId));
                }
                nodes.add(node);
            }
        });
        return nodes;
    }

    public HashSet<Integer> filterInteracting(String type, HashSet<Integer> ids) {
        HashSet<Integer> out = new HashSet<>();
        int nodeId = Graphs.getNode(type);
        LinkedList<Integer> edgeIds = Graphs.getEdgesfromNodes(nodeId, nodeId);
        if (edgeIds == null)
            return out;
        int edgeId = edgeIds.get(0);

        ids.forEach(node -> {
            AtomicBoolean interacting = new AtomicBoolean(false);
            try {
                edgeController.getEdges(edgeId, nodeId, node, false).forEach(edge -> {
                    if (edge.getId1() != edge.getId2() && edgeController.isExperimental(edgeId, edge.getId1(), edge.getId2()))
                        interacting.set(true);
                });
                if (!interacting.get())
                    edgeController.getEdges(edgeId, nodeId, node, true).forEach(edge -> {
                        if (edge.getId1() != edge.getId2() && edgeController.isExperimental(edgeId, edge.getId1(), edge.getId2()))
                            interacting.set(true);
                    });
            } catch (NullPointerException ignore) {
            }
            if (interacting.get())
                out.add(node);
        });
        return out;
    }

    public String getThumbnailState(String gid) {
        File thumb = getThumbnail(gid);
        if (thumb.exists())
            return "ready";
        if (thumbnailGenerating.contains(gid))
            return "running";
        return "not requested";
    }

    public Boolean isLayoutReady(String gid) {
        return !layoutGenerating.contains(gid) && historyController.getLayoutPath(gid).exists();
    }

    public Object loadLayout(String gid, String type) {
        Graph g = getCachedGraph(gid);
        if (type.equals("default"))
            return getLayout(g);
        if (type.equals("tripartite"))
            return getTripartiteLayout(g);
        return new HashMap<>();
    }

    public Object getInteractionEdges(String type, LinkedList<Integer> ids, Boolean exp, String tissue) {
        String edgeName = type.equals("gene") ? "GeneGeneInteraction" : "ProteinProteinInteraction";
        HashSet<Integer> existingIds = new HashSet<>(ids);
        int edgeId = Graphs.getEdge(edgeName);
        int nodeId = Graphs.getNode(type);
        HashSet<PairId> edges = new HashSet<>();
        boolean allTissues = tissue == null || !edgeController.getTissueIDMap().containsKey(tissue);
        int tissueId = -1;
        if (!allTissues)
            tissueId = edgeController.getTissueIDMap().get(tissue);
        int finalTissueId = tissueId;
        ids.forEach(node -> {
            try {
                edgeController.getEdges(edgeId, nodeId, node, false).forEach(pairid -> {
                    if (existingIds.contains(pairid.getId2()))
                        if ((!exp || edgeController.isExperimental(edgeId, pairid.getId1(), pairid.getId2())) && (allTissues || edgeController.isTissue(edgeId, pairid.getId1(), pairid.getId2(), finalTissueId)))
                            edges.add(pairid);
                });
            } catch (NullPointerException ignore) {

            }
        });
        Graph g = new Graph();
        if (type.equals("gene")) {
            nodeController.findGenes(existingIds).forEach(n -> g.addNode(nodeId, new Node(n.getId(), n.getDisplayName())));
        } else
            nodeController.findProteins(existingIds).forEach(n -> g.addNode(nodeId, new Node(n.getId(), n.getDisplayName())));

        g.addEdges(edgeId, edges.stream().map(Edge::new).collect(Collectors.toList()));
        return g.toWebGraph(getColorMap(g.getNodes().keySet().stream().map(Graphs::getNode).collect(Collectors.toSet())), null);
    }

    public HashSet<Integer> getSeeds(String type, Object marks, Graph g) {
        HashSet<Integer> targets = new HashSet<>((Collection<Integer>) marks);
        return g.getNodes().get(Graphs.getNode(type)).keySet().stream().filter(n -> !targets.contains(n)).collect(Collectors.toCollection(HashSet::new));
    }

    public void recreateJobData(JobResponse jr) {
        if (jr.derivedGraph != null) {
            Graph g = getCachedGraph(jr.derivedGraph);
            if (jr.goal == Job.JobGoal.DRUG_PRIORITIZATION) {
                jr.seeds = new HashSet<>(g.getNodes().get(Graphs.getNode(jr.target)).keySet());
            } else {
                HashMap<String, HashMap<Integer, Object>> marks = g.getMarks();
                if (marks.get("nodes") != null) {
                    if (jr.goal == Job.JobGoal.MODULE_IDENTIFICATION | jr.goal == Job.JobGoal.DRUG_REPURPOSING) {
                        jr.seeds = getSeeds(jr.target, marks.get("nodes").get(Graphs.getNode(jr.target)), g);
                    }
//                    if () {
//                        Graph parent = getCachedGraph(jr.basisGraph);
//                        String type = jr.target;
//                        jr.seeds = getSeeds(type, parent.getMarks().get("nodes").get(Graphs.getNode(type)), parent);
//                    }
                }
            }
        } else if (jr.state != Job.JobState.DONE) {

            if (jr.goal == Job.JobGoal.MODULE_IDENTIFICATION || jr.goal == Job.JobGoal.DRUG_PRIORITIZATION || (jr.goal == Job.JobGoal.DRUG_REPURPOSING && jr.basisGraph != null)) {
                Graph parent = getCachedGraph(jr.basisGraph);
                jr.seeds = new HashSet<>(parent.getNodes().get(Graphs.getNode(jr.target)).keySet());
            } else if (jr.goal == Job.JobGoal.DRUG_REPURPOSING) {
                jr.type = "mi";
                if (getCachedGraph(jr.parentGraph) != null)
                    jr.seeds = new HashSet<>(getCachedGraph(jr.parentGraph).getNodes().get(Graphs.getNode(jr.target)).keySet());
            }
        }
    }

    public void addInteractionsToDPResult(String gid, String edgeType, boolean experimentalOnly, String tissue, Job j) {
        Graph g =extendGraph(gid,edgeType, true, false, false, 0.0, false, experimentalOnly, tissue);
        g.calculateDegrees();
        AtomicInteger size = new AtomicInteger();
        g.getNodes().forEach((k, v) -> size.addAndGet(v.size()));
        g.getEdges().forEach((k, v) -> size.addAndGet(v.size()));
        if (size.get() < 100_000) {
            cache.put(g.getId(), g);
            j.setDerivedGraph(g.getId());
            addGraphToHistory(j.getUserId(), g.getId());
        } else
            j.setStatus(Job.JobState.LIMITED);
    }
}
