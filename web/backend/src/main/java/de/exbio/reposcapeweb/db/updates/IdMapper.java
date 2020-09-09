package de.exbio.reposcapeweb.db.updates;

import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.utils.Pair;

public interface IdMapper {

    public abstract PairId mapIds(Pair<String, String> ids);
}
