package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.DisorderComorbidWithDisorder;
import de.exbio.reposcapeweb.db.entities.edges.DisorderIsADisorder;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.DisorderComorbidWithDisorderRepository;
import de.exbio.reposcapeweb.db.repositories.edges.DisorderIsADisorderRepository;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
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
public class DisorderIsADisorderService {

    private final Logger log = LoggerFactory.getLogger(DisorderIsADisorder.class);

    private final DisorderIsADisorderRepository disorderIsADisorderRepository;

    private final DisorderService disorderService;

    @Autowired
    public DisorderIsADisorderService(DisorderService disorderService, DisorderIsADisorderRepository disorderIsADisorderRepository){
        this.disorderService=disorderService;
        this.disorderIsADisorderRepository=disorderIsADisorderRepository;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<Object, DisorderIsADisorder>> updates) {

        if (updates == null)
            return false;

        if (updates.containsKey(UpdateOperation.Deletion))
            disorderIsADisorderRepository.deleteAll(disorderIsADisorderRepository.findDisorderIsADisorderrByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o->(PairId)o).collect(Collectors.toSet())));

        LinkedList<DisorderIsADisorder> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<Object, DisorderIsADisorder> toUpdate = updates.get(UpdateOperation.Alteration);

            disorderIsADisorderRepository.findDisorderIsADisorderrByIdIn(new HashSet<>(toUpdate.keySet().stream().map(o->(PairId)o).collect(Collectors.toSet()))).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryIds()));
                toSave.add(d);
            });
        }
        disorderIsADisorderRepository.saveAll(toSave);
        log.debug("Updated disorder table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }


    public PairId mapIds(Pair<String,String> ids) {
        return new PairId(disorderService.map(ids.getFirst()),disorderService.map(ids.getSecond()));
    }
}
