package de.exbio.reposcapeweb.db.history;

import java.util.HashMap;

public class GraphHistoryDetail {

    public String name;
    public String owner;
    public String method;

    public boolean starred;
    public boolean root;
    public String parentId;
    public String parentName;
    public String parentMethod;
    public HashMap<String,HashMap<String,String>> children;

}
