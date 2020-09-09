package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.ProteinEncodedBy;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.ProteinEncodedByRepository;
import de.exbio.reposcapeweb.db.services.nodes.GeneService;
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
public class ProteinEncodedByService {

    private final Logger log = LoggerFactory.getLogger(ProteinEncodedByService.class);

    private final ProteinEncodedByRepository proteinEncodedByRepository;

    private final ProteinService proteinService;
    private final GeneService geneService;


    @Autowired
    public ProteinEncodedByService(GeneService geneService, ProteinService proteinService, ProteinEncodedByRepository proteinEncodedByRepository) {
        this.proteinEncodedByRepository = proteinEncodedByRepository;
        this.geneService = geneService;
        this.proteinService = proteinService;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<PairId, ProteinEncodedBy>> updates) {

        if (updates == null)
            return false;

        if (updates.containsKey(UpdateOperation.Deletion))
            proteinEncodedByRepository.deleteAll(proteinEncodedByRepository.findProteinEncodedsByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet())));

        LinkedList<ProteinEncodedBy> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<PairId, ProteinEncodedBy> toUpdate = updates.get(UpdateOperation.Alteration);

            proteinEncodedByRepository.findProteinEncodedsByIdIn(new HashSet<>(toUpdate.keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet()))).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryIds()));
                toSave.add(d);
            });
        }
        proteinEncodedByRepository.saveAll(toSave);
        log.debug("Updated protein_encoded_by table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }


    public PairId mapIds(Pair<String, String> ids) {
        return new PairId(proteinService.map(ids.getFirst()), geneService.map(ids.getSecond()));
    }

}
