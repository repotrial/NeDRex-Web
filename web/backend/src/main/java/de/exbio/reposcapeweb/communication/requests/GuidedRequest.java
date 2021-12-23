package de.exbio.reposcapeweb.communication.requests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class GuidedRequest {
    public String uid;
    public String sourceType;
    public String targetType;
    public HashSet<Integer> sources;
    public HashSet<Integer> targets;
    public LinkedList<HashMap<String,String>> path;
    public HashMap<String,HashMap<String,Object>> params;
    public Boolean excludeConnectors;
    public HashSet<Integer> connectors;
}
