package de.exbio.reposcapeweb.communication.requests;

import java.util.HashMap;

public class GraphRequest {

    public String uid;
    public String id = null;
    public HashMap<String,FilterGroup> nodes;
    public HashMap<String,FilterGroup> edges;
    public boolean connectedOnly = true;
    public HashMap<String,Boolean> interactions;
}
