package de.exbio.reposcapeweb.db.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.exbio.reposcapeweb.communication.reponses.WebGraphInfo;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name="histories")
public class GraphHistory {

    @Id
    @Column(name="graph_id")
    private String graphId;
    private String userId;
    private LocalDateTime created;
    @ManyToOne
    @JoinColumn(name="graph_id", insertable = false, updatable = false)
    @JsonIgnore
    private GraphHistory parent;
    @OneToMany(mappedBy="parent",fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GraphHistory> derived;
    private String comment;

    private String edges;
    private String nodes;

    @Transient
    private HashMap<String,Integer> edgeMap;
    @Transient
    private HashMap<String,Integer> nodeMap;

    public GraphHistory(){
        derived = new LinkedList<>();
    }

    public GraphHistory(String userId, String graphId, WebGraphInfo graphInfo){
        created = LocalDateTime.now();
        this.graphId = graphId;
        this.userId = userId;
        derived=new LinkedList<>();
        edgeMap = graphInfo.getEdges();
        nodeMap = graphInfo.getNodes();
    }

    public GraphHistory(String userId, String graphId, WebGraphInfo graphInfo, GraphHistory parent){
        this(userId,graphId,graphInfo);
        parent.addDerivate(this);
        this.parent=parent;
    }

    public void addDerivate(GraphHistory childHistory){
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

    public void serialize(String edges, String nodes){
        this.edges = edges;
        this.nodes = nodes;
    }

    public void deserialize(HashMap<String,Integer> edges, HashMap<String,Integer> nodes){
        this.edgeMap = edges;
        this.nodeMap = nodes;
    }

    public String getComment() {
        return comment;
    }
}
