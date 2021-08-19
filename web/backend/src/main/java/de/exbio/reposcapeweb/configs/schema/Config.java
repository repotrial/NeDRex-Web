package de.exbio.reposcapeweb.configs.schema;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class Config {
    public LinkedList<NodeConfig> nodes;
    public LinkedList<EdgeConfig> edges;


    public EdgeConfig getEdge(int id){
        return edges.stream().filter(e->e.id==id).collect(Collectors.toList()).get(0);
    }
}
