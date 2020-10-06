package de.exbio.reposcapeweb.db.repositories.edges;

import de.exbio.reposcapeweb.db.entities.edges.GeneInteractsWithGene;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface GeneInteractsWithGeneRepository extends CrudRepository<GeneInteractsWithGene, PairId> {
    List<GeneInteractsWithGene> findGeneInteractsWithGeneByIdIn(Collection<PairId> list);
}
