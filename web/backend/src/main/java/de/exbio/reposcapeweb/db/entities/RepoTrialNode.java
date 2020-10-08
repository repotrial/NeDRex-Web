package de.exbio.reposcapeweb.db.entities;

import de.exbio.reposcapeweb.filter.FilterEntry;
import de.exbio.reposcapeweb.filter.FilterKey;
import de.exbio.reposcapeweb.filter.FilterType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class RepoTrialNode extends RepoTrialEntity {

    public abstract int getId();

    public abstract String getPrimaryId();

    public abstract String getUniqueId();

    public abstract EnumMap<FilterType, Map<FilterKey, FilterEntry>> toUniqueFilter();

    public abstract EnumMap<FilterType, Map<FilterKey, FilterEntry>> toDistinctFilter();


}
