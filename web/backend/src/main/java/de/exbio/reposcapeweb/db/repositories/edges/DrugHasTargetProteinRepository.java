package de.exbio.reposcapeweb.db.repositories.edges;

import de.exbio.reposcapeweb.db.entities.edges.DrugHasTargetProtein;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DrugHasTargetProteinRepository extends CrudRepository<DrugHasTargetProtein, PairId> {
    List<DrugHasTargetProtein> findDrugHasTargetsByIdIn(Collection<PairId> list);
}
