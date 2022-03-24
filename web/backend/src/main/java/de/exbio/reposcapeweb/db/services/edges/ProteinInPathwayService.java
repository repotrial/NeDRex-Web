package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.DrugHasIndication;
import de.exbio.reposcapeweb.db.entities.edges.DrugHasTargetGene;
import de.exbio.reposcapeweb.db.entities.edges.ProteinEncodedBy;
import de.exbio.reposcapeweb.db.entities.edges.ProteinInPathway;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.ProteinInPathwayRepository;
import de.exbio.reposcapeweb.db.services.nodes.PathwayService;
import de.exbio.reposcapeweb.db.services.nodes.ProteinService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProteinInPathwayService {

    private final Logger log = LoggerFactory.getLogger(ProteinInPathwayService.class);

    private final ProteinInPathwayRepository proteinInPathwayRepository;

    private final ProteinService proteinService;
    private final PathwayService pathwayService;

    private final boolean directed = true;
    private final HashMap<Integer, HashSet<PairId>> edgesTo = new HashMap<>();
    private final HashMap<Integer, HashSet<PairId>> edgesFrom = new HashMap<>();

    @Autowired
    public ProteinInPathwayService(PathwayService pathwayService, ProteinService proteinService, ProteinInPathwayRepository proteinInPathwayRepository) {
        this.proteinInPathwayRepository = proteinInPathwayRepository;
        this.pathwayService = pathwayService;
        this.proteinService = proteinService;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<PairId, ProteinInPathway>> updates) {

        if (updates == null)
            return false;

        if (updates.containsKey(UpdateOperation.Deletion))
            proteinInPathwayRepository.deleteAll(proteinInPathwayRepository.findProteinInPathwaysByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet())));

        LinkedList<ProteinInPathway> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<PairId, ProteinInPathway> toUpdate = updates.get(UpdateOperation.Alteration);


            HashSet<PairId> batch = new HashSet<>();
            toUpdate.keySet().forEach(p -> {
                batch.add(p);
                if (batch.size() > 1_000) {
                    proteinInPathwayRepository.findProteinInPathwaysByIdIn(batch).forEach(d -> {
                        d.setValues(toUpdate.get(d.getPrimaryIds()));
                        toSave.add(d);
                    });
                    batch.clear();
                }
            });
            if (!batch.isEmpty()) {
                proteinInPathwayRepository.findProteinInPathwaysByIdIn(batch).forEach(d -> {
                    d.setValues(toUpdate.get(d.getPrimaryIds()));
                    toSave.add(d);
                });
                batch.clear();
            }
        }
        proteinInPathwayRepository.saveAll(toSave);
        log.debug("Updated protein_in_pathway table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        return true;
    }

    public Iterable<ProteinInPathway> findAll() {
        return proteinInPathwayRepository.findAll();
    }

    public List<PairId> getAllIDs() {
        LinkedList<PairId> allIDs = new LinkedList<>();
        edgesFrom.values().forEach(allIDs::addAll);
        return allIDs;
    }

    public void importEdges() {
        edgesFrom.clear();
        edgesTo.clear();
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

    public HashSet<PairId> getEdgesTo(Integer id){
        return edgesTo.get(id);
    }

    public HashSet<PairId> getEdgesFrom(Integer id){
        return edgesFrom.get(id);
    }


    public PairId mapIds(Pair<String, String> ids) {
        return new PairId(proteinService.map(ids.getFirst()), pathwayService.map(ids.getSecond()));
    }

    public List<ProteinInPathway> getEntries(Collection<PairId> ids) {
        return proteinInPathwayRepository.findProteinInPathwaysByIdIn(ids);
    }


    public Optional<ProteinInPathway> find(PairId id) {
        return proteinInPathwayRepository.findById(id);
    }

    public ProteinInPathway setDomainIds(ProteinInPathway item) {
        item.setSourceDomainId(proteinService.map(item.getPrimaryIds().getId1()));
        item.setTargetDomainId(pathwayService.map(item.getPrimaryIds().getId2()));
        item.setNodeNames(proteinService.getName(item.getPrimaryIds().getId1()),pathwayService.getName(item.getPrimaryIds().getId2()));
        return item;
    }


    public boolean isDirected() {
        return directed;
    }

    public Long getCount() {
        return proteinInPathwayRepository.count();
    }

}
