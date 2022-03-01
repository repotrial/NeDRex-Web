package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.*;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.GeneAssociatedWithDisorderRepository;
import de.exbio.reposcapeweb.db.repositories.edges.ProteinAssociatedWithDisorderRepository;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import de.exbio.reposcapeweb.db.services.nodes.GeneService;
import de.exbio.reposcapeweb.db.services.nodes.ProteinService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssociatedWithDisorderService {

    private final Logger log = LoggerFactory.getLogger(GeneAssociatedWithDisorder.class);

    private final GeneAssociatedWithDisorderRepository geneAssociatedWithDisorderRepository;
    private final ProteinAssociatedWithDisorderRepository proteinAssociatedWithDisorderRepository;

    private final GeneService geneService;
    private final ProteinService proteinService;
    private final DisorderService disorderService;

    private final boolean directed = true;
    private final HashMap<Integer, HashSet<PairId>> proteinEdgesTo = new HashMap<>();
    private final HashMap<Integer, HashSet<PairId>> proteinEdgesFrom = new HashMap<>();
    private final HashMap<Integer, HashSet<PairId>> geneEdgesTo = new HashMap<>();
    private final HashMap<Integer, HashSet<PairId>> geneEdgesFrom = new HashMap<>();

    private final DataSource dataSource;
    private final String clearQuery = "DELETE FROM protein_associated_with_disorder";
    private final String generationQuery = "INSERT IGNORE INTO protein_associated_with_disorder (id_1 , id_2, score,asserted_by) SELECT protein_encoded_by.id_1, gene_associated_with_disorder.id_2,gene_associated_with_disorder.score, gene_associated_with_disorder.asserted_by " +
            "FROM protein_encoded_by INNER JOIN gene_associated_with_disorder " +
            "ON protein_encoded_by.id_2=gene_associated_with_disorder.id_1;";
    private PreparedStatement clearPs = null;
    private PreparedStatement generationPs = null;

    @Autowired
    public AssociatedWithDisorderService(DataSource dataSource, GeneService geneService, DisorderService disorderService, GeneAssociatedWithDisorderRepository geneAssociatedWithDisorderRepository, ProteinAssociatedWithDisorderRepository proteinAssociatedWithDisorderRepository, ProteinService proteinService) {
        this.geneAssociatedWithDisorderRepository = geneAssociatedWithDisorderRepository;
        this.proteinAssociatedWithDisorderRepository = proteinAssociatedWithDisorderRepository;
        this.disorderService = disorderService;
        this.geneService = geneService;
        this.dataSource = dataSource;
        this.proteinService = proteinService;
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

            HashSet<PairId> batch = new HashSet<>();
            toUpdate.keySet().forEach(p -> {
                batch.add(p);
                if (batch.size() > 1_000) {
                    geneAssociatedWithDisorderRepository.findGeneAssociatedWithDisorderByIdIn(batch).forEach(d -> {
                        d.setValues(toUpdate.get(d.getPrimaryIds()));
                        toSave.add(d);
                    });
                    batch.clear();
                }
            });
            if (!batch.isEmpty()) {
                geneAssociatedWithDisorderRepository.findGeneAssociatedWithDisorderByIdIn(batch).forEach(d -> {
                    d.setValues(toUpdate.get(d.getPrimaryIds()));
                    toSave.add(d);
                });
                batch.clear();
            }



        }
        geneAssociatedWithDisorderRepository.saveAll(toSave);
        log.debug("Updated gene_associated_with_disorder table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        log.debug("Deriving entries for protein_associated_with_disorder.");
        if (!generateGeneEntries())
            return false;
        log.debug("Derived " + proteinAssociatedWithDisorderRepository.count() + " protein -> disorder relations.");
        return true;
    }


    public boolean generateGeneEntries() {
        log.debug("Generating entries for protein_associated_with_disorder from gene_associated_with_disorder(_protein).");
        try (Connection con = dataSource.getConnection()) {
            clearPs = con.prepareStatement(clearQuery);
            generationPs = con.prepareStatement(generationQuery);
            clearPs.executeUpdate();
            generationPs.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }

    public Iterable<GeneAssociatedWithDisorder> findAllGenes() {
        return geneAssociatedWithDisorderRepository.findAll();
    }

    public List<PairId> getAllGeneIDs() {
        LinkedList<PairId> allIDs = new LinkedList<>();
        geneEdgesFrom.values().forEach(allIDs::addAll);
        return allIDs;
    }

    public List<PairId> getAllProteinIDs() {
        LinkedList<PairId> allIDs = new LinkedList<>();
        proteinEdgesFrom.values().forEach(allIDs::addAll);
        return allIDs;
    }

    public void importEdges() {
        findAllGenes().forEach(edge -> {
            importGeneEdge(edge.getPrimaryIds());
        });
        findAllGenes().forEach(edge -> {
            importGeneEdge(edge.getPrimaryIds());
        });

        findAllProteins().forEach(edge -> {
            importProteinEdge(edge.getPrimaryIds());
        });
        findAllProteins().forEach(edge -> {
            importProteinEdge(edge.getPrimaryIds());
        });
    }

    private void importGeneEdge(PairId edge) {
        if (!geneEdgesFrom.containsKey(edge.getId1()))
            geneEdgesFrom.put(edge.getId1(), new HashSet<>());
        geneEdgesFrom.get(edge.getId1()).add(edge);

        if (!geneEdgesTo.containsKey(edge.getId2()))
            geneEdgesTo.put(edge.getId2(), new HashSet<>());
        geneEdgesTo.get(edge.getId2()).add(edge);
    }


    public boolean isGeneEdgeTo(PairId edge) {
        try {
            return geneEdgesTo.get(edge.getId1()).contains(edge);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isGeneEdgeTo(int id1, int id2) {
        return isGeneEdgeTo(new PairId(id1,id2));
    }
    public HashSet<PairId> getGeneEdgesFrom(int id) {
        return geneEdgesFrom.get(id);
    }
    public HashSet<PairId> getGeneEdgesTo(int id) {
        return geneEdgesTo.get(id);
    }

    public boolean isGeneEdgeFrom(PairId edge) {
        try {
            return geneEdgesFrom.get(edge.getId1()).contains(edge);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isGeneEdgeFrom(int id1, int id2) {
        return isGeneEdgeFrom(new PairId(id1,id2));
    }


    public Iterable<ProteinAssociatedWithDisorder> findAllProteins() {
        return proteinAssociatedWithDisorderRepository.findAll()
;    }

    private void importProteinEdge(PairId edge) {
        if (!proteinEdgesFrom.containsKey(edge.getId1()))
            proteinEdgesFrom.put(edge.getId1(), new HashSet<>());
        proteinEdgesFrom.get(edge.getId1()).add(edge);

        if (!proteinEdgesTo.containsKey(edge.getId2()))
            proteinEdgesTo.put(edge.getId2(), new HashSet<>());
        proteinEdgesTo.get(edge.getId2()).add(edge);
    }


    public boolean isProteinEdgeFrom(PairId edge) {
        try {
            return proteinEdgesFrom.get(edge.getId1()).contains(edge);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isProteinEdgeFrom(int id1, int id2) {
        return isProteinEdgeFrom(new PairId(id1,id2));
    }

    public boolean isProteinEdgeTo(PairId edge) {
        try {
            return proteinEdgesTo.get(edge.getId1()).contains(edge);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isProteinEdgeTo(int id1, int id2) {
        return isProteinEdgeTo(new PairId(id1,id2));
    }


    public HashSet<PairId> getProteinEdgesFrom(int id) {
        return proteinEdgesFrom.get(id);
    }

    public HashSet<PairId> getProteinEdgesTo(int id) {
        return proteinEdgesTo.get(id);
    }

    public PairId mapIds(Pair<String, String> ids) {
        return new PairId(geneService.map(ids.getFirst()), disorderService.map(ids.getSecond()));
    }


    public List<GeneAssociatedWithDisorder> getGenes(Collection<PairId> ids) {
        return geneAssociatedWithDisorderRepository.findGeneAssociatedWithDisorderByIdIn(ids);
    }

    public List<ProteinAssociatedWithDisorder> getProteins(Collection<PairId> ids) {
        return proteinAssociatedWithDisorderRepository.findProteinAssociatedWithDisorderByIdIn(ids);
    }

    public Optional<GeneAssociatedWithDisorder> findGene(PairId id) {
        return geneAssociatedWithDisorderRepository.findById(id);
    }

    public Optional<ProteinAssociatedWithDisorder> findProtein(PairId id) {
        return proteinAssociatedWithDisorderRepository.findById(id);
    }

    public ProteinAssociatedWithDisorder setDomainIds(ProteinAssociatedWithDisorder item) {
        item.setSourceDomainId(proteinService.map(item.getPrimaryIds().getId1()));
        item.setTargetDomainId(disorderService.map(item.getPrimaryIds().getId2()));
        item.setNodeNames(proteinService.getName(item.getPrimaryIds().getId1()),disorderService.getName(item.getPrimaryIds().getId2()));
        return item;
    }
    public GeneAssociatedWithDisorder setDomainIds(GeneAssociatedWithDisorder item) {
        item.setSourceDomainId(geneService.map(item.getPrimaryIds().getId1()));
        item.setTargetDomainId(disorderService.map(item.getPrimaryIds().getId2()));
        item.setNodeNames(geneService.getName(item.getPrimaryIds().getId1()),disorderService.getName(item.getPrimaryIds().getId2()));
        return item;
    }

    public boolean isDirected() {
        return directed;
    }

    public Long getGeneCount() {
        return geneAssociatedWithDisorderRepository.count();
    }

    public Long getProteinCount() {
        return proteinAssociatedWithDisorderRepository.count();
    }
}
