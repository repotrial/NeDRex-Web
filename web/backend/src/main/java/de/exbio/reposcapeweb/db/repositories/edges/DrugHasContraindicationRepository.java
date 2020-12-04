package de.exbio.reposcapeweb.db.repositories.edges;

import de.exbio.reposcapeweb.db.entities.edges.DrugHasContraindication;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DrugHasContraindicationRepository extends CrudRepository<DrugHasContraindication, PairId> {
    List<DrugHasContraindication> findDrugHasIndicationsByIdIn(Collection<PairId> list);
}
