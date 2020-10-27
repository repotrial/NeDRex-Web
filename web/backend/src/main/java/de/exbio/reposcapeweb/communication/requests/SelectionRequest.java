package de.exbio.reposcapeweb.communication.requests;

import de.exbio.reposcapeweb.communication.cache.Edge;

import java.util.HashMap;

public class SelectionRequest {
    public String gid;
    public boolean extend= false;
    public boolean node = false;
    public boolean edge= false;
    public HashMap<String,Integer[]> nodes;
    public HashMap<String, Edge[]> edges;


}
