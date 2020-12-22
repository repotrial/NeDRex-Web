package de.exbio.reposcapeweb.configs.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.HashMap;

public class NodeConfig {
    public int id;
    public String name;
    public String label;
    public ColorConfig colors;
    public boolean disabled = false;
    @JsonIgnore
    public File file =null;
    public HashMap<String,AttributeConfig> attributes;
}
