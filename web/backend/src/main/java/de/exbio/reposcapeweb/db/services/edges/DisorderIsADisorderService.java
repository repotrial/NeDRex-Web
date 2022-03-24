package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.DisorderIsADisorder;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.DisorderIsADisorderRepository;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.filter.FilterEntry;
import de.exbio.reposcapeweb.filter.FilterType;
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
    private final boolean directed = true;
    private final HashMap<Integer, HashSet<PairId>> parentEdges = new HashMap<>();
    private final HashMap<Integer, HashSet<PairId>> childEdges = new HashMap<>();

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

            HashSet<PairId> batch = new HashSet<>();
            toUpdate.keySet().forEach(p -> {
                batch.add(p);
                if (batch.size() > 1_000) {
                    disorderIsADisorderRepository.findDisorderIsADisordersByIdIn(batch).forEach(d -> {
                        d.setValues(toUpdate.get(d.getPrimaryIds()));
                        toSave.add(d);
                    });
                    batch.clear();
                }
            });
            if (!batch.isEmpty()) {
                disorderIsADisorderRepository.findDisorderIsADisordersByIdIn(batch).forEach(d -> {
                    d.setValues(toUpdate.get(d.getPrimaryIds()));
                    toSave.add(d);
                });
                batch.clear();
            }
        }
        disorderIsADisorderRepository.saveAll(toSave);

        log.debug("Updated disorder_is_subtype_of_disorder table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        if (toSave.size() > 0) {
            log.debug("Rebuilding disorder-hierarchy for filters");
            createDistinctFilters();
        }

        return true;
    }

    public Iterable<DisorderIsADisorder> findAll() {
        return disorderIsADisorderRepository.findAll();
    }

    public List<PairId> getAllIDs() {
        HashSet<PairId> allIDs = new HashSet<>();
        parentEdges.values().forEach(allIDs::addAll);
        childEdges.values().forEach(allIDs::addAll);
        return new LinkedList<>(allIDs);
    }

    public void importEdges() {
        parentEdges.clear();
        childEdges.clear();
        findAll().forEach(edge -> {
            importEdge(edge.getPrimaryIds());
        });
    }

    private void importEdge(PairId edge) {
        if (!childEdges.containsKey(edge.getId1()))
            childEdges.put(edge.getId1(), new HashSet<>());
        childEdges.get(edge.getId1()).add(edge);

        if (!parentEdges.containsKey(edge.getId2()))
            parentEdges.put(edge.getId2(), new HashSet<>());
        parentEdges.get(edge.getId2()).add(edge);
    }

    public boolean isParentEdge(PairId edge) {
        try {
            return parentEdges.get(edge.getId1()).contains(edge);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isParentEdge(int id1, int id2) {
        return isParentEdge(new PairId(id1, id2));
    }

    public boolean isChildEdge(PairId edge) {
        try {
            return childEdges.get(edge.getId1()).contains(edge);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isChildEdge(int id1, int id2) {
        return isChildEdge(new PairId(id1, id2));
    }

    public HashSet<PairId> getParentEdges(int id) {
        return parentEdges.get(id);
    }

    public HashSet<PairId> getChildEdges(int id) {
        return childEdges.get(id);
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

    public Long getCount() {
        return disorderIsADisorderRepository.count();
    }

    public void createDistinctFilters() {
        HashMap<Integer, FilterEntry> entryMap = new HashMap<>();

        importEdges();
        disorderService.findAll().forEach(d -> {
            entryMap.put(d.getId(), new FilterEntry(d.getDisplayName(), FilterType.UMBRELLA_DISORDER, d.getId()));
        });

        entryMap.keySet().forEach(d -> {
            String key = entryMap.get(d).getName();
            HashSet<Integer> children = getChildren(d);
            if (!children.isEmpty())
                disorderService.getFilter().addDistinct(FilterType.UMBRELLA_DISORDER, key, entryMap.get(d));
            children.forEach(c -> {
                disorderService.getFilter().addDistinct(FilterType.UMBRELLA_DISORDER, key, entryMap.get(c));
            });
        });
    }

    public HashSet<Integer> getChildren(int parent) {
        HashSet<Integer> out = new HashSet<>();
        if (parentEdges.containsKey(parent))
            parentEdges.get(parent).forEach(c -> {
                out.add(c.getId1());
                out.addAll(getChildren(c.getId1()));
            });
        return out;
    }

}
