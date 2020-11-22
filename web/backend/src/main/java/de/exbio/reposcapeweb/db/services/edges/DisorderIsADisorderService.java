package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.DisorderComorbidWithDisorder;
import de.exbio.reposcapeweb.db.entities.edges.DisorderIsADisorder;
import de.exbio.reposcapeweb.db.entities.edges.GeneAssociatedWithDisorder;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.DisorderIsADisorderRepository;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DisorderIsADisorderService {

    private final Logger log = LoggerFactory.getLogger(DisorderIsADisorderService.class);

    private final DisorderIsADisorderRepository disorderIsADisorderRepository;
    private final boolean directed = false;
    private final HashMap<Integer, HashMap<Integer, Boolean>> edges = new HashMap<>();

    private final DisorderService disorderService;

    @Autowired
    public DisorderIsADisorderService(DisorderService disorderService, DisorderIsADisorderRepository disorderIsADisorderRepository) {
        this.disorderService = disorderService;
        this.disorderIsADisorderRepository = disorderIsADisorderRepository;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<PairId, DisorderIsADisorder>> updates) {

        if (updates == null)
            return false;

        if (updates.containsKey(UpdateOperation.Deletion))
            disorderIsADisorderRepository.deleteAll(disorderIsADisorderRepository.findDisorderIsADisordersByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet())));


        LinkedList<DisorderIsADisorder> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<PairId, DisorderIsADisorder> toUpdate = updates.get(UpdateOperation.Alteration);

            disorderIsADisorderRepository.findDisorderIsADisordersByIdIn(new HashSet<>(toUpdate.keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet()))).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryIds()));
                toSave.add(d);
            });
        }
        disorderIsADisorderRepository.saveAll(toSave);
        log.debug("Updated disorder_is_a_disorder table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }

    public Iterable<DisorderIsADisorder> findAll() {
        return disorderIsADisorderRepository.findAll();
    }

    public void importEdges() {
        findAll().forEach(edge -> {
            importEdge(edge.getPrimaryIds());
        });
    }

    private void importEdge(PairId edge) {
        if (!edges.containsKey(edge.getId1()))
            edges.put(edge.getId1(), new HashMap<>());
        edges.get(edge.getId1()).put(edge.getId2(), true);

        if (!edges.containsKey(edge.getId2()))
            edges.put(edge.getId2(), new HashMap<>());
        edges.get(edge.getId2()).put(edge.getId1(), !directed);
    }

    public boolean isEdge(PairId edge) {
        return isEdge(edge.getId1(), edge.getId2());
    }

    public boolean isEdge(int id1, int id2) {
        try {
            return edges.get(id1).get(id2);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public HashSet<Integer> getEdges(int id) {
        return edges.get(id).entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toCollection(HashSet::new));
    }


    public PairId mapIds(Pair<String, String> ids) {
        return new PairId(disorderService.map(ids.getFirst()), disorderService.map(ids.getSecond()));
    }

    public List<DisorderIsADisorder> getEntries(Collection<PairId> ids) {
        return disorderIsADisorderRepository.findDisorderIsADisordersByIdIn(ids);
    }

    public Optional<DisorderIsADisorder> find(PairId id) {
        return disorderIsADisorderRepository.findById(id);
    }

    public DisorderIsADisorder setDomainIds(DisorderIsADisorder item) {
        item.setSourceDomainId(disorderService.map(item.getPrimaryIds().getId1()));
        item.setTargetDomainId(disorderService.map(item.getPrimaryIds().getId2()));
        item.setNodeNames(disorderService.getName(item.getPrimaryIds().getId1()), disorderService.getName(item.getPrimaryIds().getId2()));

        return item;
    }

    public boolean isDirected() {
        return directed;
    }

}
