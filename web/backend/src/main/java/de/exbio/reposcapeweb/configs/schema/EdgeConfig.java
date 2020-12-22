package de.exbio.reposcapeweb.configs.schema;

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
    public HashMap<String,AttributeConfig> attributes;
}
