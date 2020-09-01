package de.exbio.reposcapeweb.db.repositories.edges;

import de.exbio.reposcapeweb.db.entities.edges.DisorderIsADisorder;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DisorderIsADisorderRepository extends CrudRepository<DisorderIsADisorder, PairId> {
    List<DisorderIsADisorder> findDisorderIsADisorderrByIdIn(Collection<PairId> list);
}
