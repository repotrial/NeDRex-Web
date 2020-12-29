package de.exbio.reposcapeweb.configs.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.LinkedList;

public class EdgeConfig {
    public int id;
    public String name;
    public String label;
    public boolean original;
    public boolean directed;
    public String source;
    public String target;
    public boolean disabled=false;
    public String mapsTo;
    @JsonIgnore
    public File file;
    public LinkedList<AttributeConfig> attributes;
}
