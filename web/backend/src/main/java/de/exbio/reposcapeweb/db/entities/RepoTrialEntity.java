package de.exbio.reposcapeweb.db.entities;

import java.util.HashMap;
import java.util.HashSet;

public abstract class RepoTrialEntity{

    public HashSet<String> attributes = null;

    public abstract HashMap<String,Object> getAsMap();

    public abstract HashMap<String,Object> getAsMap(HashSet<String> attributes);

//    public static boolean validateFormat(HashSet<String> attributes) {
//        return false;
//    }





//    public String toTsv();
//
//    public String getHeader();

}
