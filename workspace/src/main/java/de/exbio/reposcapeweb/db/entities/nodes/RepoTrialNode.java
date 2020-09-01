package de.exbio.reposcapeweb.db.entities.nodes;

import de.exbio.reposcapeweb.db.entities.RepoTrialEntity;

import java.util.HashMap;

public abstract class RepoTrialNode extends RepoTrialEntity {

    public abstract String getPrimaryId();

//    public abstract HashMap<Integer,String> getIdToDomainMap();
//
//    public abstract HashMap<String,Integer> getDomainToIdMap();

    public abstract String getUniqueId();
}
