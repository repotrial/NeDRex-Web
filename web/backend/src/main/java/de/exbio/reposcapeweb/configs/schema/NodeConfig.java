package de.exbio.reposcapeweb.configs.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.LinkedList;

public class NodeConfig {
    public int id;
    public String name;
    public String label;
    public String sourceId;
    public ColorConfig colors;
    public boolean disabled = false;
    @JsonIgnore
    public File file =null;
    public LinkedList<AttributeConfig> attributes;
}
