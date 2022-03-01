package de.exbio.reposcapeweb.db.repositories.edges;

import de.exbio.reposcapeweb.db.entities.edges.GeneInteractsWithGene;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

public interface GeneInteractsWithGeneRepository extends PagingAndSortingRepository<GeneInteractsWithGene, PairId> {
    List<GeneInteractsWithGene> findGeneInteractsWithGeneByIdIn(Collection<PairId> list);
}
