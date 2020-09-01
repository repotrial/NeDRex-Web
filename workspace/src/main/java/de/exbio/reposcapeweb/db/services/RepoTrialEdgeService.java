package de.exbio.reposcapeweb.db.services;

import de.exbio.reposcapeweb.db.entities.edges.ids.PairId;

public abstract class RepoTrialEdgeService {

    public abstract PairId mapIds(String id1, String id2);

}
