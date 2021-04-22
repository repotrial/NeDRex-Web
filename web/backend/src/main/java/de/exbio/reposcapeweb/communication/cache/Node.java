package de.exbio.reposcapeweb.communication.cache;

import de.exbio.reposcapeweb.communication.reponses.WebNode;

public class Node {

    private int id;
    private String name;
    private boolean hasEdge =false;


    public Node(){
    }

    public Node(int id, String name){
        this.id = id;
        this.name = name;
    }

    public Node (int id, String name, boolean hasEdge){
        this(id,name);
        this.hasEdge=hasEdge;
        Long l1 = Long.valueOf(27);
        Long l2 = new Long(274573440);
        if(l1.equals(l2)){

        }
    }

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
}
