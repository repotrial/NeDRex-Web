package de.exbio.reposcapeweb.filter;

import java.util.Objects;

public class FilterEntry implements Comparable<FilterEntry> {

    private String name;
    private FilterType type;
    private int nodeId;


    public FilterEntry(String name, FilterType type, int node) {
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

    public FilterType getType() {
        return type;
    }

    public void setType(FilterType type) {
        this.type = type;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public int compareTo(FilterEntry other) {
        int name = this.name.compareTo(other.name);
        if (name != 0)
            return name;
        int type = this.type.compareTo(other.type);
        if (type != 0)
            return type;
        return this.getNodeId() - other.getNodeId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterEntry that = (FilterEntry) o;
        return nodeId == that.nodeId &&
                name.equals(that.name) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, nodeId);
    }

    public enum FilterTypes {
        NAME, ALIAS, ALTERNATIVE, SYNONYM;
    }

    @Override
    public String toString() {
        return name + "\t" + type + "\t" + nodeId;
    }


}
