package de.exbio.reposcapeweb.db.services.edges;

import de.exbio.reposcapeweb.db.entities.edges.GeneInteractsWithGene;
import de.exbio.reposcapeweb.db.entities.edges.ProteinInteractsWithProtein;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.repositories.edges.GeneInteractsWithGeneRepository;
import de.exbio.reposcapeweb.db.repositories.edges.GeneInteractsWithGenePagingRepository;
import de.exbio.reposcapeweb.db.repositories.edges.ProteinInteractsWithProteinPagingRepository;
import de.exbio.reposcapeweb.db.repositories.edges.ProteinInteractsWithProteinRepository;
import de.exbio.reposcapeweb.db.services.nodes.GeneService;
import de.exbio.reposcapeweb.db.services.nodes.ProteinService;
import de.exbio.reposcapeweb.db.updates.UpdateOperation;
import de.exbio.reposcapeweb.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
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

    private final ProteinInteractsWithProteinPagingRepository proteinInteractsWithProteinPagingRepository;

    private final GeneInteractsWithGenePagingRepository geneInteractsWithGenePagingRepository;
    private final GeneInteractsWithGeneRepository geneInteractsWithGeneRepository;
    private final ProteinEncodedByService proteinEncodedByService;
    private final Environment env;

    private final ProteinService proteinService;
    private final GeneService geneService;

    private final boolean directed = false;
    private final TreeMap<String, Integer> tissueIDMap = new TreeMap<>();
    private final TreeMap<Integer, String> idTissueMap = new TreeMap<>();
    private final HashMap<Integer, HashMap<PairId, Pair<Pair<Boolean, Boolean>, HashSet<Integer>>>> proteins = new HashMap<>();
    private final HashMap<Integer, HashMap<PairId, Pair<Pair<Boolean, Boolean>, HashSet<Integer>>>> genes = new HashMap<>();

    private final DataSource dataSource;
    private final String clearQuery = "DELETE FROM gene_interacts_with_gene";
    private final String generationQuery = "INSERT IGNORE INTO gene_interacts_with_gene (id_1,id_2) SELECT enc1.id_2, enc2.id_2 FROM protein_interacts_with_protein ppi INNER JOIN protein_encoded_by enc1 ON ppi.id_1=enc1.id_1 INNER JOIN protein_encoded_by enc2 on ppi.id_2=enc2.id_1 WHERE enc1.id_2 <enc2.id_2 UNION ALL ( SELECT enc2.id_2, enc1.id_2 FROM protein_interacts_with_protein ppi INNER JOIN protein_encoded_by enc1 ON ppi.id_1=enc1.id_1 INNER JOIN protein_encoded_by enc2 on ppi.id_2=enc2.id_1 WHERE enc1.id_2 >enc2.id_2);";
    private PreparedStatement clearPs = null;
    private PreparedStatement generationPs = null;

    @Autowired
    public ProteinInteractsWithProteinService(Environment env, ProteinEncodedByService proteinEncodedByService, ProteinService proteinService, ProteinInteractsWithProteinRepository proteinInteractsWithProteinRepository, ProteinInteractsWithProteinPagingRepository proteinInteractsWithProteinPagingRepository, GeneInteractsWithGeneRepository geneInteractsWithGeneRepository, GeneInteractsWithGenePagingRepository geneInteractsWithGenePagingRepository, DataSource dataSource, GeneService geneService) {
        this.proteinInteractsWithProteinRepository = proteinInteractsWithProteinRepository;
        this.proteinInteractsWithProteinPagingRepository = proteinInteractsWithProteinPagingRepository;
        this.geneInteractsWithGeneRepository = geneInteractsWithGeneRepository;
        this.geneInteractsWithGenePagingRepository = geneInteractsWithGenePagingRepository;
        this.proteinService = proteinService;
        this.dataSource = dataSource;
        this.geneService = geneService;
        this.proteinEncodedByService = proteinEncodedByService;
        this.env = env;
    }


    public boolean submitUpdates(EnumMap<UpdateOperation, HashMap<PairId, ProteinInteractsWithProtein>> updates) {

        if (updates == null)
            return false;
        if (updates.containsKey(UpdateOperation.Deletion))
            proteinInteractsWithProteinRepository.deleteAll(proteinInteractsWithProteinRepository.findProteinInteractsWithProteinsByIdIn(updates.get(UpdateOperation.Deletion).keySet().stream().map(o -> o.getId1() > o.getId2() ? o.flipIds() : o).collect(Collectors.toSet())));
        int insertCount = 0;
        LinkedList<ProteinInteractsWithProtein> toSave = new LinkedList<>();
        if (updates.containsKey(UpdateOperation.Insertion)) {
            toSave.addAll(updates.get(UpdateOperation.Insertion).values());
            insertCount = toSave.size();
            toSave.forEach(p -> {
                if (p.getPrimaryIds().getId1() > p.getPrimaryIds().getId2())
                    p.flipIds();
            });
        }
        if (updates.containsKey(UpdateOperation.Alteration)) {
            HashMap<PairId, ProteinInteractsWithProtein> toUpdate = updates.get(UpdateOperation.Alteration);

            HashSet<PairId> batch = new HashSet<>();
            toUpdate.keySet().forEach(p -> {
                batch.add(p);
                if (batch.size() > 1_000) {
                    proteinInteractsWithProteinRepository.findProteinInteractsWithProteinsByIdIn(batch).forEach(d -> {
                        d.setValues(toUpdate.get(d.getPrimaryIds()));
                        toSave.add(d);
                    });
                    batch.clear();
                }
            });
            if (!batch.isEmpty()) {
                proteinInteractsWithProteinRepository.findProteinInteractsWithProteinsByIdIn(batch).forEach(d -> {
                    d.setValues(toUpdate.get(d.getPrimaryIds()));
                    toSave.add(d);
                });
                batch.clear();
            }
        }
        proteinInteractsWithProteinRepository.saveAll(toSave);
        int changes = insertCount + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0);
        log.debug("Updated protein_interacts_with_protein table: " + insertCount + " Inserts, " + (updates.containsKey(UpdateOperation.Alteration) ? updates.get(UpdateOperation.Alteration).size() : 0) + " Changes, " + (updates.containsKey(UpdateOperation.Deletion) ? updates.get(UpdateOperation.Deletion).size() : 0) + " Deletions identified!");

        return true;
    }

    public boolean generateGeneEntries() {
        //TODO only update changed entries?
        log.debug("Generating entries for gene_interacts_with_gene from protein_interacts_with_protein.");
        HashMap<Integer, HashSet<Integer>> geneProteinMap = new HashMap<>();
        HashMap<Integer, Integer> proteinGeneMap = new HashMap<>();
        proteinEncodedByService.findAll().forEach(e -> {
            if (!geneProteinMap.containsKey(e.getPrimaryIds().getId1()))
                geneProteinMap.put(e.getPrimaryIds().getId1(), new HashSet<>());
            geneProteinMap.get(e.getPrimaryIds().getId1()).add(e.getPrimaryIds().getId2());
            proteinGeneMap.put(e.getPrimaryIds().getId1(), e.getPrimaryIds().getId2());
        });


        try (Connection con = dataSource.getConnection()) {
            clearPs = con.prepareCall(clearQuery);
            clearPs.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        HashMap<Integer, HashMap<Integer, GeneInteractsWithGene>> ggis = new HashMap<>();
        LinkedList<GeneInteractsWithGene> ggiList = new LinkedList<>();

        int pageSize = 100_000;
        long total = proteinInteractsWithProteinRepository.count();
        for (long entry = 0; entry < total; entry += pageSize)
            proteinInteractsWithProteinPagingRepository.findAll(PageRequest.of((int) (entry / pageSize), pageSize)).forEach(ppi -> {
                try {
                    int gid1 = proteinGeneMap.get(ppi.getPrimaryIds().getId1());
                    int gid2 = proteinGeneMap.get(ppi.getPrimaryIds().getId2());
                    if (gid1 > gid2) {
                        int tmp = gid2;
                        gid2 = gid1;
                        gid1 = tmp;
                    }
                    GeneInteractsWithGene ggi;
                    if (ggis.containsKey(gid1) && ggis.get(gid1).containsKey(gid2)) {
                        ggi = ggis.get(gid1).get(gid2);
                        if (ggi == null)
                            ggi = findGene(new PairId(gid1, gid2)).get();
                    } else {
                        ggi = new GeneInteractsWithGene(gid1, gid2);
                        if (!ggis.containsKey(gid1))
                            ggis.put(gid1, new HashMap<>());
                        ggis.get(gid1).put(gid2, ggi);
                        ggiList.add(ggi);
                    }
                    ggi.addEvidenceTypes(ppi.getEvidenceTypes());
//                ggi.addDatabases(ppi.getDatabases());
                    ggi.addDataSources(ppi.getDataSources());
                    ggi.addMethod(ppi.getMethods());
                    ggi.addJointTissues(ppi.getJointTissues());
                    ggi.addSubcellularLocations(ppi.getSubcellularLocations());
                    ggi.addTissues(ppi.getTissues());
                    ggi.addDevelopmentStages(ppi.getDevelopmentStages());
                    ggi.addBrainTissues(ppi.getBrainTissues());


                    if (ggiList.size() > 100_000) {
                        geneInteractsWithGeneRepository.saveAll(ggiList);
                        ggis.values().forEach(m -> m.keySet().forEach(k -> m.put(k, null)));
                        ggiList.clear();
                    }

                } catch (NullPointerException ignore) {
                }
            });
        geneInteractsWithGeneRepository.saveAll(ggiList);
        return true;
    }

    public Iterable<ProteinInteractsWithProtein> findAllProteins(int page, int count) {
        return proteinInteractsWithProteinPagingRepository.findAll(PageRequest.of(page, count));
    }

    public Iterable<GeneInteractsWithGene> findAllGenes() {
        return geneInteractsWithGeneRepository.findAll();
    }

    public void importEdges() {
        int pageSize = 100_000;
        long total = proteinInteractsWithProteinRepository.count();
        proteins.clear();
        for (long entry = 0; entry < total; entry += pageSize)
            proteinInteractsWithProteinPagingRepository.findAll(PageRequest.of((int) (entry / pageSize), pageSize)).
                    forEach(edge -> importProteinEdge(edge.getPrimaryIds(), edge.getEvidenceTypes().contains("exp"), edge.getTissues()));
        total = geneInteractsWithGeneRepository.count();
        genes.clear();
        for (long entry = 0; entry < total; entry += pageSize)
            geneInteractsWithGenePagingRepository.findAll(PageRequest.of((int) (entry / pageSize), pageSize)).forEach(edge -> importGeneEdge(edge.getPrimaryIds(), edge.getEvidenceTypes().contains("exp"), edge.getTissues()));
    }

    public int addTissue(String tissue) {
        int id = tissueIDMap.size();
        tissueIDMap.put(tissue, id);
        idTissueMap.put(id, tissue);
        return id;
    }

    public HashSet<Integer> tissuesToIds(Collection<String> tissues, boolean add) {
        HashSet<Integer> ids = new HashSet<>();
        tissues.forEach(t -> {
            try {
                ids.add(tissueIDMap.get(t).intValue());
            } catch (Exception e) {
                if (add) {
                    ids.add(addTissue(t));
                }
            }
        });
        return ids;
    }

    private void importProteinEdge(PairId edge, boolean experimental, List<String> tissues) {
        if (!proteins.containsKey(edge.getId1()))
            proteins.put(edge.getId1(), new HashMap<>());
        HashSet<Integer> tissueIds = tissuesToIds(tissues, true);
        proteins.get(edge.getId1()).put(edge, new Pair<>(new Pair<>(true, experimental), tissueIds));
        if (!proteins.containsKey(edge.getId2()))
            proteins.put(edge.getId2(), new HashMap<>());
        proteins.get(edge.getId2()).put(edge, new Pair<>(new Pair<>(true, experimental), tissueIds));
    }


    private void importGeneEdge(PairId edge, boolean experimental, List<String> tissues) {
        if (!genes.containsKey(edge.getId1()))
            genes.put(edge.getId1(), new HashMap<>());
        HashSet<Integer> tissueIds = tissuesToIds(tissues, true);
        genes.get(edge.getId1()).put(edge, new Pair<>(new Pair<>(true, experimental), tissueIds));
        if (!genes.containsKey(edge.getId2()))
            genes.put(edge.getId2(), new HashMap<>());
        genes.get(edge.getId2()).put(edge, new Pair<>(new Pair<>(true, experimental), tissueIds));
    }


    public boolean isProteinEdge(PairId edge) {
        try {
            return proteins.get(edge.getId1()).get(edge).first.first;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isProteinEdge(int id1, int id2) {
        return isProteinEdge(new PairId(id1, id2));
    }

    public boolean isExperimentalProtein(PairId edge) {
        try {
            return proteins.get(edge.getId1()).get(edge).first.second;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isExperimentalProtein(int id1, int id2) {
        return isExperimentalProtein(new PairId(id1, id2));
    }

    public boolean isExperimentalGene(PairId edge) {
        try {
            return genes.get(edge.getId1()).get(edge).first.second;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isExperimentalGene(int id1, int id2) {
        return isExperimentalGene(new PairId(id1, id2));
    }

    public boolean isGeneEdge(PairId edge) {
        try {
            return genes.get(edge.getId1()).get(edge).first.first;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isGeneEdge(int id1, int id2) {
        return isGeneEdge(new PairId(id1, id2));
    }

    public HashSet<PairId> getProteins(int id) {
        return proteins.get(id).entrySet().stream().filter(e -> e.getValue().first.first).map(Map.Entry::getKey).collect(Collectors.toCollection(HashSet::new));
    }

    public HashSet<PairId> getGenes(int id) {
        return genes.get(id).entrySet().stream().filter(e -> e.getValue().first.first).map(Map.Entry::getKey).collect(Collectors.toCollection(HashSet::new));
    }


    public PairId mapProteinIds(Pair<String, String> ids) {
        return new PairId(proteinService.map(ids.getFirst()), proteinService.map(ids.getSecond()));
    }

    public PairId mapGeneIds(Pair<String, String> ids) {
        return new PairId(geneService.map(ids.getFirst()), geneService.map(ids.getSecond()));
    }

    public List<ProteinInteractsWithProtein> getProteins(Collection<PairId> ids) {
        ids.forEach(p -> {
            if (p.getId1() > p.getId2())
                p.flipIds();
        });
        return proteinInteractsWithProteinRepository.findProteinInteractsWithProteinsByIdIn(ids);
    }

    public HashMap<Integer, HashMap<PairId, Pair<Pair<Boolean, Boolean>,HashSet<Integer>>>> getProteins() {
        return proteins;
    }

    public HashMap<Integer, HashMap<PairId, Pair<Pair<Boolean, Boolean>,HashSet<Integer>>>> getGenes() {
        return genes;
    }

    public HashSet<String> getTissueNames(){
        return new HashSet<>(tissueIDMap.keySet());
    }

    public TreeMap<Integer,String> getIdTissueMap(){
        return idTissueMap;
    }

    public TreeMap<String,Integer> getTissueIDMap(){
        return tissueIDMap;
    }

    public List<GeneInteractsWithGene> getGenes(Collection<PairId> ids) {
        ids.forEach(p -> {
            if (p.getId1() > p.getId1())
                p.flipIds();
        });
        return geneInteractsWithGeneRepository.findGeneInteractsWithGeneByIdIn(ids);
    }

    public Optional<GeneInteractsWithGene> findGene(PairId id) {
        if (id.getId1() > id.getId2())
            id.flipIds();
        return geneInteractsWithGeneRepository.findById(id);
    }

    public Optional<ProteinInteractsWithProtein> findProtein(PairId id) {
        if (id.getId1() > id.getId2())
            id.flipIds();
        return proteinInteractsWithProteinRepository.findById(id);
    }

    public void setClearPs(PreparedStatement clearPs) {
        this.clearPs = clearPs;
    }

    public ProteinInteractsWithProtein setDomainIds(ProteinInteractsWithProtein item) {
        item.setMemberOne(proteinService.map(item.getPrimaryIds().getId1()));
        item.setMemberTwo(proteinService.map(item.getPrimaryIds().getId2()));
        item.setNodeNames(proteinService.getName(item.getPrimaryIds().getId1()), proteinService.getName(item.getPrimaryIds().getId2()));
        return item;
    }

    public GeneInteractsWithGene setDomainIds(GeneInteractsWithGene item) {
        item.setMemberOne(geneService.map(item.getPrimaryIds().getId1()));
        item.setMemberTwo(geneService.map(item.getPrimaryIds().getId2()));
        item.setNodeNames(geneService.getName(item.getPrimaryIds().getId1()), geneService.getName(item.getPrimaryIds().getId2()));
        return item;
    }


    public boolean isDirected() {
        return directed;
    }

    public Long getGeneCount() {
        return geneInteractsWithGeneRepository.count();
    }

    public Long getProteinCount() {
        return proteinInteractsWithProteinRepository.count();
    }

    public List<PairId> getAllProteinIDs() {
        LinkedList<PairId> allIDs = new LinkedList<>();
        proteins.values().forEach(v -> allIDs.addAll(v.keySet()));
        return allIDs;
    }

    public List<PairId> getAllGeneIDs() {
        LinkedList<PairId> allIDs = new LinkedList<>();
        genes.values().forEach(v -> allIDs.addAll(v.keySet()));
        return allIDs;
    }

    public boolean isTissueProtein(int id1, int id2, Integer tissueId) {
        try {
            return proteins.get(id1).get(new PairId(id1,id2)).second.contains(tissueId);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isTissueGene(int id1, int id2, Integer tissueId) {
        try {
            return genes.get(id1).get(new PairId(id1,id2)).second.contains(tissueId);
        } catch (NullPointerException e) {
            return false;
        }
    }

}
