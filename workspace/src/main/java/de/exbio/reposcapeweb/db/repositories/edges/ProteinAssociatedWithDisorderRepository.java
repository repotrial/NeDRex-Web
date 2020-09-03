package de.exbio.reposcapeweb.db.repositories.edges;

import de.exbio.reposcapeweb.db.entities.edges.ProteinAssociatedWithDisorder;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProteinAssociatedWithDisorderRepository extends CrudRepository<ProteinAssociatedWithDisorder, PairId> {
    List<ProteinAssociatedWithDisorder> findProteinAssociatedWithDisorderByIdIn(Collection<PairId> list);
}
