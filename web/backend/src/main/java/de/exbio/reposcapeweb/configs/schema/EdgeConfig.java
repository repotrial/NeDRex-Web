package de.exbio.reposcapeweb.configs.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class EdgeConfig {
    public int id;
    public String name;
    public String paginatedName;
    public String label;
    public boolean original;
    public boolean directed;
    public String source;
    public String target;
    public boolean disabled=false;
    public String mapsTo;
    @JsonIgnore
    public File file;
    public HashSet<String> databases;
    public LinkedList<AttributeConfig> attributes;

    public LinkedList<AttributeConfig> getDetailAttributes(){
        return attributes.stream().filter(a -> a.detail).distinct().collect(Collectors.toCollection(LinkedList::new));
    }
}
