package de.exbio.reposcapeweb.db.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.jobs.JobController;
import de.exbio.reposcapeweb.tools.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.print.attribute.standard.JobState;
import java.io.File;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistoryController {

    private final HistoryRepository historyRepository;
    private final ObjectMapper objectMapper;
    private final Environment env;

    private HashMap<String, GraphHistory> graphMap;
    private HashMap<String, HashSet<String>> userMap;


    @Autowired
    public HistoryController(HistoryRepository historyRepository, ObjectMapper objectMapper, Environment env) {
        this.historyRepository = historyRepository;
        this.objectMapper = objectMapper;
        this.env = env;
    }


    public void importHistory() {
        graphMap = new HashMap<>();
        userMap = new HashMap<>();
        historyRepository.findAll().forEach(this::addHistory);
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

    public HashMap<String, Object> getUserHistory(String uid, HashMap<String, Job.JobState> jobs) {
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
            if (jobs.containsKey(gid))
                history.setJobState(jobs.get(gid));
        });

        out.put("history", getHistoryTree(map.values()));

        LinkedList<HashMap<String, Object>> list = map.values().stream().map(h -> h.toMap(false)).sorted((history, t1) -> {
            long h1 = (long) history.get("created");
            long h2 = (long) t1.get("created");
            if (h1 == h2)
                return 0;
            return h1 < h2 ? 1 : -1;
        }).collect(Collectors.toCollection(LinkedList::new));


        out.put("chronology", list);
        return out;
    }

    public ArrayList<Object> getHistoryTree(Collection<GraphHistory> historyList) {
        ArrayList<Object> history = new ArrayList<>();

        historyList.forEach(v -> {
            if (v.getParent() == null)
                history.add(v.toMap(true));
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
        String cachedir = env.getProperty("path.db.cache");
        GraphHistory history = getHistory(id);
        return new File(cachedir, "users/" + history.getUserId() + "/graphs/" + id + ".json");
    }

    public File getJobPath(Job j) {
        String cachedir = env.getProperty("path.db.cache");
        if (j.getMethod().equals(ToolService.Tool.MUST))
            return new File(cachedir, "users/" + j.getUserId() + "/jobs/" + j.getJobId() + "_result.zip");
        else
            return new File(cachedir, "users/" + j.getUserId() + "/jobs/" + j.getJobId() + "_result.txt");
    }

    public String validateUser(String userId) {
        if (userId == null) {
            userId = getNewUser();
        }
        return userId;
    }
}
