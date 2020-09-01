package de.exbio.reposcapeweb.db.entities.nodes;

import de.exbio.reposcapeweb.db.entities.RepoTrialEntity;
import de.exbio.reposcapeweb.db.entities.edges.ids.PairId;
import de.exbio.reposcapeweb.utils.Pair;

public abstract class RepoTrialEdge extends RepoTrialEntity {

    public abstract PairId getPrimaryIds();

    public abstract void setId(PairId id);

    public abstract Pair<String,String> getIdsToMap();
}