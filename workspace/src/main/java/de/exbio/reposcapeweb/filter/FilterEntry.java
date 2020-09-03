package de.exbio.reposcapeweb.filter;

public class FilterEntry {

    private String name;
    private FilterTypes type;
    private int nodeId;


    public FilterEntry(String name, FilterTypes type, int node) {
        this.name = name;
        this.type = type;
        this.nodeId = node;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FilterTypes getType() {
        return type;
    }

    public void setType(FilterTypes type) {
        this.type = type;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public enum FilterTypes {
        NAME, ALIAS, ALTERNATIVE,SYNONYM;
    }

    @Override
    public String toString() {
        return name + "\t" + type + "\t" + nodeId;
    }
}
