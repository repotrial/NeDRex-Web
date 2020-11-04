package de.exbio.reposcapeweb.db.history;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends CrudRepository<GraphHistory,String>{


}
