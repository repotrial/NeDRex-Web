package de.exbio.reposcapeweb.communication.requests;

import java.util.LinkedList;

public class EdgeRequest {
    public String type;
    public LinkedList<Integer> ids;
    public Boolean exp = true;
    public String tissue;
}
