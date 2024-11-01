package de.exbio.reposcapeweb.communication.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.exbio.reposcapeweb.communication.reponses.WebNode;

public class Node {

    private int id;
    private String sourceID =null;
    private String name;
    private boolean hasEdge =false;
    private int degree = 0;


    public Node(){
    }

    public Node(int id, String sourceID, String name){
        this.id = id;
        this.sourceID=sourceID;
        this.name = name;
    }

//    public Node (int id,String sourceID,  String name, boolean hasEdge){
//        this(id,sourceID, name);
//        this.hasEdge=hasEdge;
//    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean hasEdge() {
        return hasEdge;
    }

    public void setHasEdge(boolean hasEdge) {
        this.hasEdge = hasEdge;
    }

    public WebNode toWebNode(){
        return new WebNode(id,name,hasEdge);
    }

    public String getName() {
        return name;
    }

    public boolean isHasEdge() {
        return hasEdge;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public int getDegree() {
        return degree;
    }
//
//    public void setDegree(int degree) {
//        this.degree = degree;
//    }

    public void addDegree() {
        this.degree++;
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String domainId) {
        this.sourceID = domainId;
    }
}
