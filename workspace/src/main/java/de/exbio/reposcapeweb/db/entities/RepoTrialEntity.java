package de.exbio.reposcapeweb.db.entities;

import java.util.HashSet;

public interface RepoTrialEntity {

    HashSet<String> attributes = null;

    public static boolean validateFormat(HashSet<String> attributes) {
        return false;
    }

    public String toTsv();

    public String getHeader();

}
