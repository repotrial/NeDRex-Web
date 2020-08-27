package de.exbio.reposcapeweb.db.updates;

import de.exbio.reposcapeweb.utils.StringUtils;

import java.io.File;
import java.util.InputMismatchException;
import java.util.Iterator;

public class Edge implements Collection {

    private String name;
    private String key1;
    private String key2;
    private String map1;
    private String map2;
    private File file;

    public Edge(String definition) {
        Iterator<String> list = StringUtils.split(definition, "\t").iterator();
        try {
            this.name = list.next();
            this.key1 = list.next();
            this.key2 = list.next();
            this.map1 = list.next();
            this.map2 = list.next();
        } catch (Exception e) {
            throw new InputMismatchException("Edge specification with line: " + definition);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(File file) {
        this.file=file;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public String getMap1() {
        return map1;
    }

    public void setMap1(String map1) {
        this.map1 = map1;
    }

    public String getMap2() {
        return map2;
    }

    public void setMap2(String map2) {
        this.map2 = map2;
    }
}
