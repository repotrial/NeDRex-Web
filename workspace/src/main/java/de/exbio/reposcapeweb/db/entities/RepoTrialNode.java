package de.exbio.reposcapeweb.db.entities;

import de.exbio.reposcapeweb.db.entities.RepoTrialEntity;
import de.exbio.reposcapeweb.filter.FilterEntry;
import de.exbio.reposcapeweb.filter.FilterKey;

import java.util.HashMap;
import java.util.Map;

public abstract class RepoTrialNode extends RepoTrialEntity {

    public abstract String getPrimaryId();

//    public abstract HashMap<Integer,String> getIdToDomainMap();
//
//    public abstract HashMap<String,Integer> getDomainToIdMap();

    public abstract String getUniqueId();

    public abstract Map<FilterKey, FilterEntry> toFilter();
}
