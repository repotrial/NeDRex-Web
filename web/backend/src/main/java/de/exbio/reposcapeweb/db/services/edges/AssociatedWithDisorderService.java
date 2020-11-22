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
    private final HashMap<Integer, HashSet<Integer>> proteinEdgesTo = new HashMap<>();
    private final HashMap<Integer, HashSet<Integer>> proteinEdgesFrom = new HashMap<>();
    private final HashMap<Integer, HashSet<Integer>> geneEdgesTo = new HashMap<>();
    private final HashMap<Integer, HashSet<Integer>> geneEdgesFrom = new HashMap<>();

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

            geneAssociatedWithDisorderRepository.findGeneAssociatedWithDisorderByIdIn(new HashSet<>(toUpdate.keySet().stream().map(o -> (PairId) o).collect(Collectors.toSet()))).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryIds()));
                toSave.add(d);
            });
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
        geneEdgesFrom.get(edge.getId1()).add(edge.getId2());

        if (!geneEdgesTo.containsKey(edge.getId2()))
            geneEdgesTo.put(edge.getId2(), new HashSet<>());
        geneEdgesTo.get(edge.getId2()).add(edge.getId1());
    }

    public boolean isGeneEdgeFrom(PairId edge) {
        return isGeneEdgeFrom(edge.getId1(), edge.getId2());
    }

    public boolean isGeneEdgeFrom(int id1, int id2) {
        try {
            return geneEdgesFrom.get(id1).contains(id2);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public HashSet<Integer> getGeneEdgesTo(int id) {
        return geneEdgesTo.get(id);
    }

    public boolean isGeneEdgeTo(int id1, int id2){
        return geneEdgesTo.get(id1).contains(id2);
    }

    public HashSet<Integer> getGeneEdgesFrom(int id) {
        return geneEdgesFrom.get(id);
    }


    public Iterable<ProteinAssociatedWithDisorder> findAllProteins() {
        return proteinAssociatedWithDisorderRepository.findAll()
;    }

    private void importProteinEdge(PairId edge) {
        if (!proteinEdgesFrom.containsKey(edge.getId1()))
            proteinEdgesFrom.put(edge.getId1(), new HashSet<>());
        proteinEdgesFrom.get(edge.getId1()).add(edge.getId2());

        if (!proteinEdgesTo.containsKey(edge.getId2()))
            proteinEdgesTo.put(edge.getId2(), new HashSet<>());
        proteinEdgesTo.get(edge.getId2()).add(edge.getId1());
    }


    public boolean isProteinEdgeFrom(PairId edge) {
        return isProteinEdgeFrom(edge.getId1(), edge.getId2());
    }

    public boolean isProteinEdgeFrom(int id1, int id2) {
        try {
            return proteinEdgesFrom.get(id1).contains(id2);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public HashSet<Integer> getProteinEdgesTo(int id) {
        return proteinEdgesTo.get(id);
    }

    public boolean isProteinEdgeTo(int id1, int id2) {
        try {
            return proteinEdgesTo.get(id1).contains(id2);
        }catch (NullPointerException e){
            return false;
        }
    }


    public HashSet<Integer> getProteinEdgesFrom(int id) {
        return proteinEdgesFrom.get(id);
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
}
