package de.exbio.reposcapeweb.db.updates;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import de.exbio.reposcapeweb.utils.StringUtils;

import java.io.File;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.LinkedList;

public class Node implements Collection {

    private String name;
    private String key;
    private File file;

    public Node(String definition) {
        Iterator<String> list = StringUtils.split(definition, "\t").iterator();
        try {
            this.name = list.next();
            this.key = list.next();
        } catch (Exception e) {
            throw new InputMismatchException("Node specification with line: " + definition);
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
        this.file = file;
    }
}
