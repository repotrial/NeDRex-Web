package de.exbio.reposcapeweb.db.services.edges;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.db.entities.edges.DisorderIsADisorder;
import de.exbio.reposcapeweb.db.entities.edges.DrugHasIndication;
import de.exbio.reposcapeweb.db.entities.edges.GeneAssociatedWithDisorder;
import de.exbio.reposcapeweb.db.entities.edges.ProteinAssociatedWithDisorder;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.DrugHasIndicationRepository;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DrugHasIndicationService {

    private final Logger log = LoggerFactory.getLogger(DrugHasIndicationService.class);

    private final DrugHasIndicationRepository drugHasIndicationRepository;

    private final DrugService drugService;

    private final DisorderService disorderService;

    private final boolean directed = true;
    private final HashMap<Integer, HashSet<PairId>> edgesTo = new HashMap<>();
    private final HashMap<Integer, HashSet<PairId>> edgesFrom = new HashMap<>();

    @Autowired
    public DrugHasIndicationService(DrugService drugService, DisorderService disorderService, DrugHasIndicationRepository drugHasIndicationRepository) {
        this.drugService = drugService;
        this.drugHasIndicationRepository = drugHasIndicationRepository;
        this.disorderService = disorderService;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<PairId, DrugHasIndication>> updates) {

        if (updates == null)
            return false;

        if (updates.containsKey(UpdateOperation.Deletion))
            drugHasIndicationRepository.deleteAll(drugHasIndicationRepository.findDrugHasIndicationsByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet())));

        LinkedList<DrugHasIndication> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<PairId, DrugHasIndication> toUpdate = updates.get(UpdateOperation.Alteration);

            drugHasIndicationRepository.findDrugHasIndicationsByIdIn(new HashSet<>(toUpdate.keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet()))).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryIds()));
                toSave.add(d);
            });
        }
        drugHasIndicationRepository.saveAll(toSave);
        log.debug("Updated drug_has_indication table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }

    public Iterable<DrugHasIndication> findAll() {
        return drugHasIndicationRepository.findAll();
    }

    public void importEdges() {
        findAll().forEach(edge -> {
            importEdge(edge.getPrimaryIds());
        });
    }

    private void importEdge(PairId edge) {
        if (!edgesFrom.containsKey(edge.getId1()))
            edgesFrom.put(edge.getId1(), new HashSet<>());
        edgesFrom.get(edge.getId1()).add(edge);

        if (!edgesTo.containsKey(edge.getId2()))
            edgesTo.put(edge.getId2(), new HashSet<>());
        edgesTo.get(edge.getId2()).add(edge);
    }

    public boolean isEdgeTo(PairId edge) {
        try {
            return edgesTo.get(edge.getId1()).contains(edge);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isEdgeTo(int id1, int id2) {
       return isEdgeTo(new PairId(id1,id2));
    }


    public boolean isEdgeFrom(PairId edge) {
        try {
            return edgesFrom.get(edge.getId1()).contains(edge);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isEdgeFrom(int id1, int id2) {
       return isEdgeFrom(new PairId(id1,id2));
    }

    public HashSet<PairId> getEdgesTo(Integer id) {
        return edgesTo.get(id);
    }

    public HashSet<PairId> getEdgesFrom(Integer id) {
        return edgesFrom.get(id);
    }


    public PairId mapIds(Pair<String, String> ids) {
        return new PairId(drugService.map(ids.getFirst()), disorderService.map(ids.getSecond()));
    }

    public List<DrugHasIndication> getEntries(List<PairId> ids) {
        return drugHasIndicationRepository.findDrugHasIndicationsByIdIn(ids);
    }

    public Optional<DrugHasIndication> find(PairId id) {
        return drugHasIndicationRepository.findById(id);
    }

    public DrugHasIndication setDomainIds(DrugHasIndication item) {
        item.setSourceDomainId(drugService.map(item.getPrimaryIds().getId1()));
        item.setTargetDomainId(disorderService.map(item.getPrimaryIds().getId2()));
        item.setNodeNames(drugService.getName(item.getPrimaryIds().getId1()),disorderService.getName(item.getPrimaryIds().getId2()));
        return item;
    }

    public boolean isDirected() {
        return directed;
    }

    public Long getCount() {
        return drugHasIndicationRepository.count();
    }

}
