package de.exbio.reposcapeweb.db.repositories.edges;

import de.exbio.reposcapeweb.db.entities.edges.ProteinEncodedBy;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProteinEncodedByRepository extends CrudRepository<ProteinEncodedBy, PairId> {
    List<ProteinEncodedBy> findProteinEncodedsByIdIn(Collection<PairId> list);
}
