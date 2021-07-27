package de.exbio.reposcapeweb.communication.requests;

import java.util.LinkedList;

public class TableDownloadRequest {
    public String gid;
    public String type;
    public String name;
    public LinkedList<String> attributes;
}
