package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.GeneAssociatedWithDisorder;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.GeneAssociatedWithDisorderRepository;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import de.exbio.reposcapeweb.db.services.nodes.GeneService;
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
public class GeneAssociatedWithDisorderService {

    private final Logger log = LoggerFactory.getLogger(GeneAssociatedWithDisorder.class);

    private final GeneAssociatedWithDisorderRepository geneAssociatedWithDisorderRepository;

    private final GeneService geneService;

    private final DisorderService disorderService;

    @Autowired
    public GeneAssociatedWithDisorderService(GeneService geneService, DisorderService disorderService, GeneAssociatedWithDisorderRepository geneAssociatedWithDisorderRepository) {
        this.geneAssociatedWithDisorderRepository = geneAssociatedWithDisorderRepository;
        this.disorderService = disorderService;
        this.geneService = geneService;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<PairId, GeneAssociatedWithDisorder>> updates) {

        if (updates == null)
            return false;

        if (updates.containsKey(UpdateOperation.Deletion))
            geneAssociatedWithDisorderRepository.deleteAll(geneAssociatedWithDisorderRepository.findGeneAssociatedWithDisorderByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet())));

        LinkedList<GeneAssociatedWithDisorder> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<PairId, GeneAssociatedWithDisorder> toUpdate = updates.get(UpdateOperation.Alteration);

            geneAssociatedWithDisorderRepository.findGeneAssociatedWithDisorderByIdIn(new HashSet<>(toUpdate.keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet()))).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryIds()));
                toSave.add(d);
            });
        }
        geneAssociatedWithDisorderRepository.saveAll(toSave);
        log.debug("Updated gene_associated_with_disorder table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }


    public PairId mapIds(Pair<String, String> ids) {
        return new PairId(geneService.map(ids.getFirst()), disorderService.map(ids.getSecond()));
    }
}
