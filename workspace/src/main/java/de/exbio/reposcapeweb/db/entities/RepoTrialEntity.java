package de.exbio.reposcapeweb.db.entities;

import de.exbio.reposcapeweb.db.entities.nodes.Drug;

import java.util.HashSet;

public interface RepoTrialEntity {

    HashSet<String> attributes = null;

    public static boolean validateFormat(HashSet<String> attributes) {
        return false;
    }

    public String getPrimaryId();


//    public String toTsv();
//
//    public String getHeader();

}
