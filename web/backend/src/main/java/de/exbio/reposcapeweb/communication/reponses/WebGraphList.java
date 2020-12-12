package de.exbio.reposcapeweb.communication.reponses;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.util.stream.Collectors;

public class WebGraphList {
    public String id;
    public HashMap<String, HashMap<String, LinkedList<WebAttribute>>> attributes;
    public HashMap<String, LinkedList<HashMap<String, Object>>> nodes;
    //    @JsonIgnore
    public HashMap<String, LinkedList<HashMap<String, Object>>> edges;


    public WebGraphList(String id) {
        this.id = id;
        attributes = new HashMap<>();
        attributes.put("nodes", new HashMap<>());
        attributes.put("edges", new HashMap<>());
        nodes = new HashMap<>();
        edges = new HashMap<>();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, HashMap<String, LinkedList<WebAttribute>>> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, HashMap<String, LinkedList<WebAttribute>>> attributes) {
        this.attributes = attributes;
    }

    public HashMap<String, LinkedList<HashMap<String, Object>>> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<String, LinkedList<HashMap<String, Object>>> nodes) {
        this.nodes = nodes;
    }

    public HashMap<String, LinkedList<HashMap<String, Object>>> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<String, LinkedList<HashMap<String, Object>>> edges) {
        this.edges = edges;
    }

    public void addNode(String type, HashMap<String, Object> entry) {
        if (!nodes.containsKey(type))
            nodes.put(type, new LinkedList<>());
        nodes.get(type).add(entry);
    }

    public void addEdge(String type, HashMap<String, Object> entry) {
        if (!edges.containsKey(type))
            edges.put(type, new LinkedList<>());
        edges.get(type).add(entry);
    }

    public void addAttributes(String entity, String type, String[] attributes) {
        if (!this.attributes.get(entity).containsKey(type))
            this.attributes.get(entity).put(type, new LinkedList<>());
        Set<String> names = this.attributes.get(entity).get(type).stream().map(a -> a.name).collect(Collectors.toSet());
        Arrays.stream(attributes).forEach(a -> {
            if (!names.contains(a))
                this.attributes.get(entity).get(type).add(new WebAttribute(a));
        });
    }

    public void addListAttributes(String entity, String type, String[] attributes) {
        this.addAttributes(entity, type, attributes);
        this.setListAttributes(entity, type, attributes);
    }

    public void setListAttributes(String entity, String type, String[] attributes) {
        HashSet<String> attrSet = new HashSet<>(Arrays.asList(attributes));
        this.attributes.get(entity).get(type).stream().filter(s -> attrSet.contains(s.name)).forEach(WebAttribute::isListAttribute);
    }

    public void setSentAttributes(String entity, String type, String[] attributes) {
        HashSet<String> attrSet = new HashSet<>(Arrays.asList(attributes));
        this.attributes.get(entity).get(type).stream().filter(s -> attrSet.contains(s.name)).forEach(WebAttribute::isSent);
    }

    public void setDistinctAttributes(String entity, String type, HashMap<String, HashSet<String>> valueMap) {
        this.attributes.get(entity).get(type).forEach(a -> {
            if (valueMap.containsKey(a.name))
                a.setValues(valueMap.get(a.name).toArray(new String[valueMap.get(a.name).size()]));
        });
    }

    public void addEdges(String type, LinkedList<HashMap<String, Object>> edgesToAttributeList) {
        if (!edges.containsKey(type))
            edges.put(type, edgesToAttributeList);
        else
            edges.get(type).addAll(edgesToAttributeList);
    }

    public void addNodes(String type, LinkedList<HashMap<String, Object>> nodesToAttributeList) {
        if (!nodes.containsKey(type))
            nodes.put(type, nodesToAttributeList);
        else
            nodes.get(type).addAll(nodesToAttributeList);
    }

    public void setTypes(String type, String name, String[] attributes, String[] types, Boolean[] ids) {
        ArrayList<String> names = new ArrayList<>(Arrays.asList(attributes));
        this.attributes.get(type).get(name).forEach(a -> {
            int idx = names.indexOf(a.name);
            String t = types[idx];
            if (t.equals("numeric"))
                a.isNumeric();
            if (t.equals("array"))
                a.isArray();
            if (ids[idx])
                a.isId();
        });
    }

    private class WebAttribute {
        public String name;
        public boolean list;
        public boolean sent;
        public boolean numeric;
        public boolean array;
        public boolean id;
        public String[] values;

        public WebAttribute(String name) {
            this.name = name;
        }

        public void isListAttribute() {
            this.list = true;
            isSent();
        }

        public void isNumeric() {
            this.numeric = true;
        }

        public void isSent() {
            this.sent = true;
        }

        public void isArray() {
            this.array = true;
        }

        public void isId() {
            this.id = true;
        }

        public void setValues(String[] values) {
            this.values = values;
        }
    }

}
