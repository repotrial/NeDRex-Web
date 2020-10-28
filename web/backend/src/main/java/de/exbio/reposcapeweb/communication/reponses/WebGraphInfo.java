package de.exbio.reposcapeweb.communication.reponses;

import java.util.HashMap;

public class WebGraphInfo {
    public String id;
    public HashMap<String,Integer> nodes;
    public HashMap<String,Integer> edges;

    public WebGraphInfo(String id){
        this.id = id;
        nodes = new HashMap<>();
        edges = new HashMap<>();
    }
}