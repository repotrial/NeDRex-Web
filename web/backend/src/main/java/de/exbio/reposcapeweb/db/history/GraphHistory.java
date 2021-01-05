package de.exbio.reposcapeweb.db.history;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.reponses.WebGraphInfo;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "histories")
public class GraphHistory {

    @Id
    @Column(name = "graph_id")
    private String graphId;
    private String name;
    private String userId;
    //    @ElementCollection(fetch = FetchType.EAGER)
    @Transient
    private List<String> starredList;
    private String starred;
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

    @Transient
    private String method = null;

    public GraphHistory() {
        derived = new LinkedList<>();
    }

    public GraphHistory(String userId, String graphId, WebGraphInfo graphInfo) {
        created = LocalDateTime.now();
        this.graphId = graphId;
        this.name = graphId;
        this.userId = userId;
        derived = new LinkedList<>();
        edgeMap = graphInfo.getEdges();
        nodeMap = graphInfo.getNodes();
        starredList = new LinkedList<>();
    }

    @JsonIgnore
    private void setStarred() {
        starred = StringUtils.listToString(starredList);
    }

    @JsonIgnore
    private LinkedList<String> getStarred() {
        return StringUtils.stringToList(starred);
    }

    public void setParent(GraphHistory parent) {
        this.parent = parent;
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
        out.put("name", name);
        if (jobState != null) {
            out.put("state", jobState.name());
            out.put("method", method);
        }
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

    public void setMethod(ToolService.Tool tool) {
        this.method = tool.name();
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public boolean isStarred(String uid) {
        if (starred == null)
            starredList = new LinkedList<>();
        else
            starredList = StringUtils.stringToList(starred);
        return starredList.contains(uid);
    }

    public void toggleStarred(String uid) {
        if (starredList == null)
            starredList = new LinkedList<>();
        if (isStarred(uid))
            starredList.remove(uid);
        else
            starredList.add(uid);
        setStarred();
    }

    public void setComment(String desc) {
        this.comment = desc;
    }
}
