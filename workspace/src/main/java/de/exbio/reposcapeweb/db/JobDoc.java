package de.exbio.reposcapeweb.db;

import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.Table;

//@Entity
//@Table(name="jobs")
public class JobDoc {

//    @Id
    private String id;
    private String doc;

    public String getId() {
        return id;
    }

    public JobDoc(){

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }
}
