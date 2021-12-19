package de.exbio.reposcapeweb.db.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.jobs.JobController;
import de.exbio.reposcapeweb.communication.reponses.ConnectionGraph;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.FileUtils;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.print.attribute.standard.JobState;
import java.io.File;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class HistoryController {

    private Logger log = LoggerFactory.getLogger(HistoryController.class);

    private final HistoryRepository historyRepository;
    private final ObjectMapper objectMapper;
    private final Environment env;
    private final ToolService toolService;

    private HashMap<String, GraphHistory> graphMap;
    private HashMap<String, HashSet<String>> userMap;


    @Autowired
    public HistoryController(HistoryRepository historyRepository, ObjectMapper objectMapper, Environment env, ToolService toolService) {
        this.historyRepository = historyRepository;
        this.objectMapper = objectMapper;
        this.toolService = toolService;
        this.env = env;
    }


    public void importHistory() {
        graphMap = new HashMap<>();
        userMap = new HashMap<>();
        historyRepository.findAll().forEach(this::addHistory);
//        graphMap.values().forEach(h->{
//            GraphHistory parent = h.getParent();
//            if(parent!=null)
//                parent.addDerivate(h);
//        });
    }

    public GraphHistory save(GraphHistory history) {
        try {
            history.serialize(objectMapper.writeValueAsString(history.getEdgeMap()), objectMapper.writeValueAsString(history.getNodeMap()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (!graphMap.containsKey(history.getGraphId())) {
            addHistory(history);
        }
        return historyRepository.save(history);
    }

    public void addHistory(GraphHistory history) {
        try {
            HashMap<String, Integer> edges = objectMapper.readValue(history.getEdges(), HashMap.class);
            HashMap<String, Integer> nodes = objectMapper.readValue(history.getNodes(), HashMap.class);

            history.deserialize(edges, nodes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        graphMap.put(history.getGraphId(), history);
        if (!userMap.containsKey(history.getUserId()))
            userMap.put(history.getUserId(), new HashSet<>());
        userMap.get(history.getUserId()).add(history.getGraphId());
    }

    public GraphHistory getHistory(String gid) {
        return graphMap.get(gid);
    }

    public String getNewUser() {
        String uid = UUID.randomUUID().toString();
        while (userMap.containsKey(uid))
            uid = UUID.randomUUID().toString();
        userMap.put(uid, new HashSet<>());
        return uid;
    }

    public HashMap<String, Object> getUserHistory(String uid, HashMap<String, Pair<String, Pair<Job.JobState, ToolService.Tool>>> jobs) {
        HashMap<String, Object> out = new HashMap<>();
        out.put("uid", uid);
        if (!userMap.containsKey(uid))
            userMap.put(uid, new HashSet<>());
        HashMap<String, GraphHistory> map = new HashMap<>();
        userMap.get(uid).forEach(h -> {
            GraphHistory history = getHistory(h);
            while (history != null && !map.containsKey(history.getGraphId())) {
                map.put(history.getGraphId(), history);
                history = history.getParent();
            }

        });
        map.forEach((gid, history) -> {
            if (jobs.containsKey(gid)) {
                history.setJobState(jobs.get(gid).second.first);
                history.setMethod(jobs.get(gid).second.second);
            }
        });

        out.put("history", getHistoryTree(map.values(), uid));

        LinkedList<HashMap<String, Object>> list = map.values().stream().map(h -> h.toMap(false, uid)).sorted((history, t1) -> {
            long h1 = (long) history.get("created");
            long h2 = (long) t1.get("created");
            if (h1 == h2)
                return 0;
            return h1 < h2 ? 1 : -1;
        }).collect(Collectors.toCollection(LinkedList::new));


        out.put("chronology", list);
        return out;
    }

    public ArrayList<Object> getHistoryTree(Collection<GraphHistory> historyList, String uid) {
        ArrayList<Object> history = new ArrayList<>();

        historyList.forEach(v -> {
            if (v.getParent() == null)
                history.add(v.toMap(true, uid));
        });
        return history;
    }

    public String getGraphId() {
        String gid = UUID.randomUUID().toString();
        while (graphMap.containsKey(gid))
            gid = UUID.randomUUID().toString();
        return gid;
    }

    public GraphHistory saveDerivedHistory(String parentId, GraphHistory childHistory) {
        GraphHistory parent = getHistory(parentId);
        GraphHistory child = save(childHistory);
        save(parent);
        return child;
    }


    public File getGraphPath(String id) {
        String cachedir = env.getProperty("path.usr.cache");
        GraphHistory history = getHistory(id);
        try {
            return new File(cachedir, "users/" + history.getUserId() + "/graphs/" + id + ".json");
        } catch (NullPointerException e) {
            log.warn("Broken history request!");
        }
        return null;
    }

    public File getThumbnailPath(String id) {
        String cachedir = env.getProperty("path.usr.cache");
        GraphHistory history = getHistory(id);
        try {
            return new File(cachedir, "users/" + history.getUserId() + "/thumbnails/" + id + ".png");
        } catch (NullPointerException e) {
            log.warn("Broken history request!");
        }
        return null;
    }

    public File getLayoutPath(String id) {
        String cachedir = env.getProperty("path.usr.cache");
        GraphHistory history = getHistory(id);
        try {
            return new File(cachedir, "users/" + history.getUserId() + "/layouts/" + id + ".tsv");
        } catch (NullPointerException e) {
            log.warn("Broken history request!");
        }
        return null;
    }

    public File getTriLayoutPath(String id) {
        String cachedir = env.getProperty("path.usr.cache");
        GraphHistory history = getHistory(id);
        try {
            return new File(cachedir, "users/" + history.getUserId() + "/layouts/" + id + "_tripartite.tsv");
        } catch (NullPointerException e) {
            log.warn("Broken history request!");
        }
        return null;
    }

    public File getJobPath(Job j) {
        String cachedir = env.getProperty("path.usr.cache");
        String suffix = toolService.getAlgorithms().get(j.getMethod()).getResultSuffix();
        if (toolService.getAlgorithms().get(j.getMethod()).hasMultipleResultFiles()) {
            return new File(cachedir, "users/" + j.getUserId() + "/jobs/" + j.getJobId() + "_result"+(suffix.equals("zip") ? "" :("_"+suffix) )+ ".zip");
        } else
            return new File(cachedir, "users/" + j.getUserId() + "/jobs/" + j.getJobId() +  "_result"+(suffix.equals("txt") ? "" :("_"+suffix) )+ ".txt");
    }

    public String validateUser(String userId) {
        if (userId == null) {
            userId = getNewUser();
        }
        return userId;
    }

    public GraphHistoryDetail getDetailedHistory(String uid, Graph g, ConnectionGraph connectionGraph, HashMap<String, Pair<String, Pair<Job.JobState, ToolService.Tool>>> jobs, File thumbnail) {
        GraphHistoryDetail details = new GraphHistoryDetail();
        GraphHistory history = getHistory(g.getId());
        details.name = history.getName();
        details.starred = history.isStarred(uid);
        details.owner = history.getUserId();
        details.comment = history.getComment() != null ? history.getComment() : "";

        g.getNodes().forEach((key, ns) -> details.counts.get("nodes").put(Graphs.getNode(key), ns.size()));
        g.getEdges().forEach((key, es) -> details.counts.get("edges").put(g.getEdge(key), es.size()));

        details.entityGraph = connectionGraph;

        if (jobs.containsKey(g.getId())) {
            details.method = jobs.get(g.getId()).getSecond().getSecond().name();
            details.jobid = jobs.get(g.getId()).getFirst();
        }

        GraphHistory parent = history.getParent();
        details.root = parent == null;
        if (!details.root) {
            details.parentId = parent.getGraphId();
            details.parentName = parent.getName();
            if (jobs.containsKey(details.parentId))
                details.parentMethod = jobs.get(details.parentId).second.second.name();
        }


        details.children = new HashMap<>();

        history.getDerived().forEach(h -> {
            String id = h.getGraphId();
            details.children.put(id, new HashMap<>());
            details.children.get(id).put("name", getHistory(id).getName());
            if (jobs.containsKey(id)) {
                details.children.get(id).put("method", jobs.get(id).second.second.name());
            }
        });
        details.thumbnailReady = thumbnail.exists();
        return details;
    }

    public void saveName(String gid, String name) {
        GraphHistory history = getHistory(gid);
        history.setName(name);
        save(history);
    }

    public void saveDescription(String gid, String desc) {
        GraphHistory history = getHistory(gid);
        history.setComment(desc);
        save(history);
    }

    public void toggleStarring(String gid, String uid) {
        GraphHistory history = getHistory(gid);
        history.toggleStarred(uid);
        historyRepository.save(history);
    }

    public void remove(String gid) {
        GraphHistory g = graphMap.get(gid);
        if (g != null) {
            int idx = -1;
            if (g.getParent() != null) {
                for (int i = 0; i < g.getParent().getDerived().size(); i++) {
                    if (g.getParent().getDerived().get(i).getGraphId().equals(gid))
                        idx = i;
                }
                g.getParent().getDerived().remove(idx);
            }
            g.getDerived().forEach(child -> {
                if (g.getParent() != null) {
                    g.getParent().addDerivate(child);
                }
                child.setParent(g.getParent());
            });
            historyRepository.saveAll(g.getDerived());
        }
        File cached = getGraphPath(gid);
        if (cached != null) {
            cached.delete();
            if (Arrays.asList(cached.getParentFile().list()).isEmpty())
                FileUtils.deleteDirectory(cached.getParentFile());
        }
        userMap.forEach((u, m) -> m.remove(gid));
        if (g != null) {
            historyRepository.delete(g);
        }
        graphMap.remove(gid);
    }
}
