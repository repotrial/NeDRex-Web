package de.exbio.reposcapeweb.communication.requests;

import java.util.HashMap;
import java.util.LinkedList;

public class GuidedRequest {
    public String uid;
    public String sourceType;
    public String targetType;
    public LinkedList<Integer> sources;
    public LinkedList<Integer> targets;
    public LinkedList<HashMap<String,String>> path;
    public HashMap<String,HashMap<String,Object>> params;
}
