package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.DrugHasIndication;
import de.exbio.reposcapeweb.db.entities.edges.GeneInteractsWithGene;
import de.exbio.reposcapeweb.db.entities.edges.ProteinInteractsWithProtein;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.GeneInteractsWithGeneRepository;
import de.exbio.reposcapeweb.db.repositories.edges.ProteinInteractsWithProteinRepository;
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
public class ProteinInteractsWithProteinService {

    private final Logger log = LoggerFactory.getLogger(ProteinInteractsWithProteinService.class);

    private final ProteinInteractsWithProteinRepository proteinInteractsWithProteinRepository;
    private final GeneInteractsWithGeneRepository geneInteractsWithGeneRepository;

    private final ProteinService proteinService;
    private final GeneService geneService;

    private final boolean directed = false;
    private final HashMap<Integer, HashMap<Integer, Boolean>> proteins = new HashMap<>();
    private final HashMap<Integer, HashMap<Integer, Boolean>> genes = new HashMap<>();

    private final DataSource dataSource;
    private final String clearQuery = "DELETE FROM gene_interacts_with_gene";
    private final String generationQuery = "INSERT IGNORE INTO gene_interacts_with_gene SELECT enc1.id_2, enc2.id_2 FROM protein_interacts_with_protein ppi INNER JOIN protein_encoded_by enc1 ON ppi.id_1=enc1.id_1 INNER JOIN protein_encoded_by enc2 on ppi.id_2=enc2.id_1 WHERE enc1.id_2 <enc2.id_2 UNION ALL ( SELECT enc2.id_2, enc1.id_2 FROM protein_interacts_with_protein ppi INNER JOIN protein_encoded_by enc1 ON ppi.id_1=enc1.id_1 INNER JOIN protein_encoded_by enc2 on ppi.id_2=enc2.id_1 WHERE enc1.id_2 >enc2.id_2);";
    private PreparedStatement clearPs = null;
    private PreparedStatement generationPs = null;

    @Autowired
    public ProteinInteractsWithProteinService(ProteinService proteinService, ProteinInteractsWithProteinRepository proteinInteractsWithProteinRepository, GeneInteractsWithGeneRepository geneInteractsWithGeneRepository, DataSource dataSource, GeneService geneService) {
        this.proteinInteractsWithProteinRepository = proteinInteractsWithProteinRepository;
        this.geneInteractsWithGeneRepository = geneInteractsWithGeneRepository;
        this.proteinService = proteinService;
        this.dataSource = dataSource;
        this.geneService = geneService;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<PairId, ProteinInteractsWithProtein>> updates) {

        if (updates == null)
            return false;

        if (updates.containsKey(UpdateOperation.Deletion))
            proteinInteractsWithProteinRepository.deleteAll(proteinInteractsWithProteinRepository.findProteinInteractsWithProteinsByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o -> o.getId1() > o.getId2() ? o.flipIds() : o).collect(Collectors.toSet())));

        LinkedList<ProteinInteractsWithProtein> toSave = new LinkedList(updates.get(UpdateOperation.Insertion).values());
        int insertCount = toSave.size();
        toSave.forEach(p -> {
            if (p.getPrimaryIds().getId1() > p.getPrimaryIds().getId2())
                p.flipIds();
        });
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<PairId, ProteinInteractsWithProtein> toUpdate = updates.get(UpdateOperation.Alteration);

            proteinInteractsWithProteinRepository.findProteinInteractsWithProteinsByIdIn(new HashSet<>(toUpdate.keySet().stream().map(o -> o.getId1() <= o.getId2() ? o : o.flipIds()).collect(Collectors.toSet()))).forEach(d -> {
                d.setValues(toUpdate.get(d.getPrimaryIds()));
                toSave.add(d);
            });
        }
        proteinInteractsWithProteinRepository.saveAll(toSave);
        log.debug("Updated protein_interacts_with_protein table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");
        log.debug("Deriving entries for gene_interacts_with_gene.");
        if (!generateGeneEntries())
            return false;
        log.debug("Derived " + geneInteractsWithGeneRepository.count() + " gene -> gene relations.");

        return true;
    }

    public boolean generateGeneEntries() {
        log.debug("Generating entries for gene_interacts_with_gene from drug_has_target(_protein).");
        try (Connection con = dataSource.getConnection()) {
            clearPs = con.prepareCall(clearQuery);
            generationPs = con.prepareStatement(generationQuery);
            clearPs.executeUpdate();
            generationPs.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }

    public Iterable<ProteinInteractsWithProtein> findAllProteins() {
        return proteinInteractsWithProteinRepository.findAll();
    }

    public Iterable<GeneInteractsWithGene> findAllGenes() {
        return geneInteractsWithGeneRepository.findAll();
    }

    public void importEdges() {
        findAllProteins().forEach(edge -> importProteinEdge(edge.getPrimaryIds()));

        findAllGenes().forEach(edge -> importGeneEdge(edge.getPrimaryIds()));
    }

    private void importProteinEdge(PairId edge) {
        if (!proteins.containsKey(edge.getId1()))
            proteins.put(edge.getId1(), new HashMap<>());
        proteins.get(edge.getId1()).put(edge.getId2(), true);

        if (!proteins.containsKey(edge.getId2()))
            proteins.put(edge.getId2(), new HashMap<>());
        proteins.get(edge.getId2()).put(edge.getId1(), !directed);
    }


    private void importGeneEdge(PairId edge) {
        if (!genes.containsKey(edge.getId1()))
            genes.put(edge.getId1(), new HashMap<>());
        genes.get(edge.getId1()).put(edge.getId2(), true);

        if (!genes.containsKey(edge.getId2()))
            genes.put(edge.getId2(), new HashMap<>());
        genes.get(edge.getId2()).put(edge.getId1(), !directed);
    }

    public boolean isProteinEdge(PairId edge) {
        return isProteinEdge(edge.getId1(), edge.getId2());
    }

    public boolean isProteinEdge(int id1, int id2) {
        try {
            return proteins.get(id1).get(id2);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isGeneEdge(PairId edge) {
        return isGeneEdge(edge.getId1(), edge.getId2());
    }

    public boolean isGeneEdge(int id1, int id2) {
        try {
            return genes.get(id1).get(id2);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public HashSet<Integer> getProteins(int id) {
        return proteins.get(id).entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toCollection(HashSet::new));
    }

    public HashSet<Integer> getGenes(int id) {
        return genes.get(id).entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toCollection(HashSet::new));
    }


    public PairId mapProteinIds(Pair<String, String> ids) {
        return new PairId(proteinService.map(ids.getFirst()), proteinService.map(ids.getSecond()));
    }

    public PairId mapGeneIds(Pair<String, String> ids) {
        return new PairId(geneService.map(ids.getFirst()), geneService.map(ids.getSecond()));
    }

    public List<ProteinInteractsWithProtein> getProteins(Collection<PairId> ids) {
        ids.forEach(p->{
            if(p.getId1()>p.getId2())
                p.flipIds();
        });
        return proteinInteractsWithProteinRepository.findProteinInteractsWithProteinsByIdIn(ids);
    }

    public HashMap<Integer, HashMap<Integer, Boolean>> getProteins() {
        return proteins;
    }

    public HashMap<Integer, HashMap<Integer, Boolean>> getGenes() {
        return genes;
    }

    public List<GeneInteractsWithGene> getGenes(Collection<PairId> ids) {
        ids.forEach(p->{
            if(p.getId1()>p.getId1())
                p.flipIds();
        });
        return geneInteractsWithGeneRepository.findGeneInteractsWithGeneByIdIn(ids);
    }

    public Optional<GeneInteractsWithGene> findGene(PairId id) {
        if(id.getId1()>id.getId2())
            id.flipIds();
        return geneInteractsWithGeneRepository.findById(id);
    }

    public Optional<ProteinInteractsWithProtein> findProtein(PairId id) {
        if(id.getId1()>id.getId2())
            id.flipIds();
        return proteinInteractsWithProteinRepository.findById(id);
    }

    public void setClearPs(PreparedStatement clearPs) {
        this.clearPs = clearPs;
    }

    public ProteinInteractsWithProtein setDomainIds(ProteinInteractsWithProtein item) {
        item.setMemberOne(proteinService.map(item.getPrimaryIds().getId1()));
        item.setMemberTwo(proteinService.map(item.getPrimaryIds().getId2()));
        item.setNodeNames(proteinService.getName(item.getPrimaryIds().getId1()),proteinService.getName(item.getPrimaryIds().getId2()));
        return item;
    }

    public GeneInteractsWithGene setDomainIds(GeneInteractsWithGene item) {
        item.setMemberOne(geneService.map(item.getPrimaryIds().getId1()));
        item.setMemberTwo(geneService.map(item.getPrimaryIds().getId2()));
        item.setNodeNames(geneService.getName(item.getPrimaryIds().getId1()),geneService.getName(item.getPrimaryIds().getId2()));
        return item;
    }


    public boolean isDirected() {
        return directed;
    }

    public Long getGeneCount() {
        return proteinInteractsWithProteinRepository.count();
    }

    public Long getProteinCount(){
        return geneInteractsWithGeneRepository.count();
    }
}
