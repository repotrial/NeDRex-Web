package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.DisorderComorbidWithDisorder;
import de.exbio.reposcapeweb.db.entities.edges.GeneAssociatedWithDisorder;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.DisorderComorbidWithDisorderRepository;
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
public class DisorderComorbidWithDisorderService {
    private final Logger log = LoggerFactory.getLogger(DisorderComorbidWithDisorderService.class);

    private final DisorderComorbidWithDisorderRepository disorderComorbidWithDisorderRepository;

    private final DisorderService disorderService;
    //TODO set to directed value of config
    private final boolean directed = false;
    private final HashMap<Integer, HashMap<PairId, Boolean>> edges = new HashMap<>();

    @Autowired
    public DisorderComorbidWithDisorderService(DisorderService disorderService, DisorderComorbidWithDisorderRepository disorderComorbidWithDisorderRepository) {
        this.disorderService = disorderService;
        this.disorderComorbidWithDisorderRepository = disorderComorbidWithDisorderRepository;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<PairId, DisorderComorbidWithDisorder>> updates) {

        if (updates == null)
            return false;

        if (updates.containsKey(UpdateOperation.Deletion))
            disorderComorbidWithDisorderRepository.deleteAll(disorderComorbidWithDisorderRepository.findDisorderComorbidWithDisordersByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o -> o.getId1()>o.getId2()?o.flipIds():o).collect(Collectors.toSet())));

        LinkedList<DisorderComorbidWithDisorder> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        toSave.forEach(dd->{
            PairId p = dd.getPrimaryIds();
            if(p.getId1()>p.getId2())
                dd.flipIds();
        });
        int insertCount = toSave.size();

        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<PairId, DisorderComorbidWithDisorder> toUpdate = updates.get(UpdateOperation.Alteration);


            HashSet<PairId> batch = new HashSet<>();
            toUpdate.keySet().forEach(p -> {
                batch.add(p);
                if (batch.size() > 1_000) {
                    disorderComorbidWithDisorderRepository.findDisorderComorbidWithDisordersByIdIn(batch).forEach(d -> {
                        d.setValues(toUpdate.get(d.getPrimaryIds()));
                        toSave.add(d);
                    });
                    batch.clear();
                }
            });
            if (!batch.isEmpty()) {
                disorderComorbidWithDisorderRepository.findDisorderComorbidWithDisordersByIdIn(batch).forEach(d -> {
                    d.setValues(toUpdate.get(d.getPrimaryIds()));
                    toSave.add(d);
                });
                batch.clear();
            }



        }

        disorderComorbidWithDisorderRepository.saveAll(toSave);
        log.debug("Updated comorbid_with_disorder table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }

    public List<DisorderComorbidWithDisorder> getEntries(Collection<PairId> toFind) {
        toFind.forEach(p->{
            if(p.getId1()>p.getId1())
                p.flipIds();
        });
        return disorderComorbidWithDisorderRepository.findDisorderComorbidWithDisordersByIdIn(toFind);
    }

    public List<DisorderComorbidWithDisorder> findAll() {
        return disorderComorbidWithDisorderRepository.findAll();
    }

    public List<PairId> getAllIDs() {
        LinkedList<PairId> allIDs = new LinkedList<>();
        edges.values().forEach(v->allIDs.addAll(v.keySet()));
        return allIDs;
    }

    public void importEdges() {
        edges.clear();
        findAll().forEach(edge -> {
            importEdge(edge.getPrimaryIds());
        });
    }

    private void importEdge(PairId edge) {
        if (!edges.containsKey(edge.getId1()))
            edges.put(edge.getId1(), new HashMap<>());
        edges.get(edge.getId1()).put(edge, true);

        if (!edges.containsKey(edge.getId2()))
            edges.put(edge.getId2(), new HashMap<>());
        edges.get(edge.getId2()).put(edge, !directed);
    }

    public boolean isEdge(PairId edge) {
        try {
            return edges.get(edge.getId1()).get(edge);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isEdge(int id1, int id2) {
        return isEdge(new PairId(id1,id2));
    }

    public HashSet<PairId> getEdges(int id) {
        return edges.get(id).entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toCollection(HashSet::new));
    }


    public PairId mapIds(Pair<String, String> ids) {
        return new PairId(disorderService.map(ids.getFirst()), disorderService.map(ids.getSecond()));
    }

    public Optional<DisorderComorbidWithDisorder> find(PairId id) {
        if(id.getId1()>id.getId2())
            id.flipIds();
        return disorderComorbidWithDisorderRepository.findById(id);
    }

    public DisorderComorbidWithDisorder setDomainIds(DisorderComorbidWithDisorder item) {
        item.setMemberOne(disorderService.map(item.getPrimaryIds().getId1()));
        item.setMemberTwo(disorderService.map(item.getPrimaryIds().getId2()));

        item.setNodeNames(disorderService.getName(item.getPrimaryIds().getId1()),disorderService.getName(item.getPrimaryIds().getId2()));

        return item;
    }

    public boolean isDirected() {
        return directed;
    }

    public Long getCount() {
        return disorderComorbidWithDisorderRepository.count();
    }
}
