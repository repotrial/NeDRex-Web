package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.DrugHasIndication;
import de.exbio.reposcapeweb.db.entities.edges.DrugHasTargetGene;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProteinEncodedByService {

    private final Logger log = LoggerFactory.getLogger(ProteinEncodedByService.class);

    private final ProteinEncodedByRepository proteinEncodedByRepository;

    private final ProteinService proteinService;
    private final GeneService geneService;

    private final boolean directed = true;
    private final HashMap<Integer, HashSet<PairId>> edgesTo = new HashMap<>();
    private final HashMap<Integer, HashSet<PairId>> edgesFrom = new HashMap<>();

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

    public Iterable<ProteinEncodedBy> findAll() {
        return proteinEncodedByRepository.findAll();
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

    public HashSet<PairId> getEdgesTo(Integer id){
        return edgesTo.get(id);
    }

    public HashSet<PairId> getEdgesFrom(Integer id){
        return edgesFrom.get(id);
    }


    public PairId mapIds(Pair<String, String> ids) {
        return new PairId(proteinService.map(ids.getFirst()), geneService.map(ids.getSecond()));
    }

    public List<ProteinEncodedBy> getEntries(Collection<PairId> ids) {
        return proteinEncodedByRepository.findProteinEncodedsByIdIn(ids);
    }

    public Optional<ProteinEncodedBy> find(PairId id) {
        return proteinEncodedByRepository.findById(id);
    }

    public ProteinEncodedBy setDomainIds(ProteinEncodedBy item) {
        item.setSourceDomainId(proteinService.map(item.getPrimaryIds().getId1()));
        item.setTargetDomainId(geneService.map(item.getPrimaryIds().getId2()));
        item.setNodeNames(proteinService.getName(item.getPrimaryIds().getId1()),geneService.getName(item.getPrimaryIds().getId2()));
        return item;
    }


    public boolean isDirected() {
        return directed;
    }

    public Long getCount() {
        return proteinEncodedByRepository.count();
    }
}
