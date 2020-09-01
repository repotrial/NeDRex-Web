package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.DrugHasTarget;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.DrugHasTargetRepository;
import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import de.exbio.reposcapeweb.db.services.nodes.ProteinService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

@Service
public class DrugHasTargetService {
    private final Logger log = LoggerFactory.getLogger(DrugHasTargetService.class);

    private final DrugHasTargetRepository drugHasTargetRepository;

    private final DrugService drugService;

    private final ProteinService proteinService;

    @Autowired
    public DrugHasTargetService(DrugService drugService,  ProteinService proteinService, DrugHasTargetRepository drugHasTargetRepository){
        this.drugService=drugService;
        this.drugHasTargetRepository=drugHasTargetRepository;
        this.proteinService=proteinService;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<PairId, DrugHasTarget>> updates) {

        if (updates == null)
            return false;

        if (updates.containsKey(UpdateOperation.Deletion))
            drugHasTargetRepository.deleteAll(drugHasTargetRepository.findDrugHasTargetsByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o->(PairId)o).collect(Collectors.toSet())));

        LinkedList<DrugHasTarget> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<PairId, DrugHasTarget> toUpdate = updates.get(UpdateOperation.Alteration);

            drugHasTargetRepository.findDrugHasTargetsByIdIn(new HashSet<>(toUpdate.keySet().stream().map(o->(PairId)o).collect(Collectors.toSet()))).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryIds()));
                toSave.add(d);
            });
        }
        drugHasTargetRepository.saveAll(toSave);
        log.debug("Updated drug_has_target table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }


    public PairId mapIds(Pair<String,String> ids) {
        return new PairId(drugService.map(ids.getFirst()),proteinService.map(ids.getSecond()));
    }
}
