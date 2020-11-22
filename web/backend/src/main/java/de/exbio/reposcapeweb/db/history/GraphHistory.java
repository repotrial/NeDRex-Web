package de.exbio.reposcapeweb.db.history;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.reponses.WebGraphInfo;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "histories")
public class GraphHistory {

    @Id
    @Column(name = "graph_id")
    private String graphId;
    private String userId;
    private LocalDateTime created;
    @ManyToOne
    private GraphHistory parent;
    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<GraphHistory> derived;
    private String comment;

    private String edges;
    private String nodes;

    @Transient
    private HashMap<String, Integer> edgeMap;
    @Transient
    private HashMap<String, Integer> nodeMap;

    @Transient
    private Job.JobState jobState = null;

    public GraphHistory() {
        derived = new LinkedList<>();
    }

    public GraphHistory(String userId, String graphId, WebGraphInfo graphInfo) {
        created = LocalDateTime.now();
        this.graphId = graphId;
        this.userId = userId;
        derived = new LinkedList<>();
        edgeMap = graphInfo.getEdges();
        nodeMap = graphInfo.getNodes();
    }

    public GraphHistory(String userId, String graphId, WebGraphInfo graphInfo, GraphHistory parent) {
        this(userId, graphId, graphInfo);
        parent.addDerivate(this);
        this.parent = parent;
    }

    public void addDerivate(GraphHistory childHistory) {
        derived.add(childHistory);
    }

    public String getGraphId() {
        return graphId;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public GraphHistory getParent() {
        return parent;
    }

    @JsonGetter("parent")
    public String getParentId() {
        return parent != null ? parent.getParentId() : null;
    }

    public List<GraphHistory> getDerived() {
        return derived;
    }

    public String getEdges() {
        return edges;
    }

    public String getNodes() {
        return nodes;
    }

    public HashMap<String, Integer> getEdgeMap() {
        return edgeMap;
    }

    public HashMap<String, Integer> getNodeMap() {
        return nodeMap;
    }

    public void serialize(String edges, String nodes) {
        this.edges = edges;
        this.nodes = nodes;
    }

    public void deserialize(HashMap<String, Integer> edges, HashMap<String, Integer> nodes) {
        this.edgeMap = edges;
        this.nodeMap = nodes;
    }

    public String getComment() {
        return comment;
    }

    public HashMap<String, Object> toMap(boolean cascade) {
        HashMap<String, Object> out = new HashMap<>();
        out.put("id", getGraphId());
        out.put("edges", edgeMap);
        out.put("user", userId);
        out.put("nodes", nodeMap);
        out.put("created", created.toEpochSecond(ZoneOffset.ofTotalSeconds(0)));
        out.put("comment", comment);
        if(jobState!=null)
            out.put("state",jobState.name());
        if (cascade) {
            ArrayList<Object> children = derived.stream().map(g -> g.toMap(true)).collect(Collectors.toCollection(ArrayList::new));
            out.put("children", children);
        }
        return out;
    }

    public Job.JobState getJobState() {
        return jobState;
    }

    public void setJobState(Job.JobState jobState) {
        this.jobState = jobState;
    }
}
