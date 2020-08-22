package de.exbio.reposcapeweb.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
public class TestDoc {

    @Id
    private String uid;
    private String name;

    public TestDoc(String name){
        this.uid = UUID.randomUUID().toString();
        this.name=name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
