package de.exbio.reposcapeweb.db.repositories.edges;

import de.exbio.reposcapeweb.db.entities.edges.ProteinInteractsWithProtein;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProteinInteractsWithProteinRepository extends PagingAndSortingRepository<ProteinInteractsWithProtein, PairId> {
    List<ProteinInteractsWithProtein> findProteinInteractsWithProteinsByIdIn(Collection<PairId> list);

}
