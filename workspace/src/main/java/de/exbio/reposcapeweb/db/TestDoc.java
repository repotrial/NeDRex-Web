package de.exbio.reposcapeweb.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name="tests")
public class TestDoc {

    @Id
    private String id;
    private String doc;

    public TestDoc(){
    }

    public TestDoc(String doc){
        this.id = UUID.randomUUID().toString();
        this.doc=doc;
    }

    public TestDoc(String id, String doc){
        this.id = id;
        this.doc = doc;
    }

    public String getId() {
        return id;
    }

    public void setUid(String id) {
        this.id = id;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String name) {
        this.doc = doc;
    }
}
