package de.exbio.reposcapeweb.configs.schema;

import java.util.HashMap;

public class NodeConfig {
    public int id;
    public String name;
    public String label;
    public ColorConfig colors;
    public boolean disabled = false;
    public HashMap<String,AttributeConfig> attributes;
}
