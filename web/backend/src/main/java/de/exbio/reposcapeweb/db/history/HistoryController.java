package de.exbio.reposcapeweb.db.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

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

    public HashMap<String, Object> getUserHistory(String uid) {
        HashMap<String, Object> out = new HashMap<>();
        if (uid.equals("null")) {
            uid = getNewUser();
        }
        out.put("uid", uid);
        if(!userMap.containsKey(uid))
            userMap.put(uid,new HashSet<>());
        ArrayList<GraphHistory> list = new ArrayList<>(userMap.get(uid).size());
        userMap.get(uid).forEach(h -> list.add(getHistory(h)));
        out.put("history", list);
        return out;
    }

    public String getGraphId() {
        String gid = UUID.randomUUID().toString();
        while (graphMap.containsKey(gid))
            gid = UUID.randomUUID().toString();
        return gid;
    }

    public GraphHistory setDerivedHistory(String parentId, GraphHistory childHistory) {
        GraphHistory parent = getHistory(parentId);
        GraphHistory child = save(childHistory);
        parent.addDerivate(child);
        save(child);
        save(parent);
        return child;
    }


    public File getGraphPath(String id) {
        String cachedir = env.getProperty("path.db.cache");
        GraphHistory history = getHistory(id);
        return new File(cachedir, "graphs/"+history.getUserId() + "/" + id + ".json");
    }
}
