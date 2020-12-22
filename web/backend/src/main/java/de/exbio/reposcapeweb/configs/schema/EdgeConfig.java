package de.exbio.reposcapeweb.configs.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.HashMap;

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
    public HashMap<String,AttributeConfig> attributes;
}
