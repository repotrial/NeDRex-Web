package de.exbio.reposcapeweb.db.updates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.RepoTrialEdge;
import de.exbio.reposcapeweb.db.entities.RepoTrialNode;
import de.exbio.reposcapeweb.db.entities.edges.*;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.entities.nodes.*;
import de.exbio.reposcapeweb.db.io.Collection;
import de.exbio.reposcapeweb.db.io.ImportService;
import de.exbio.reposcapeweb.db.io.Node;
import de.exbio.reposcapeweb.db.services.edges.*;
import de.exbio.reposcapeweb.db.services.nodes.*;
import de.exbio.reposcapeweb.filter.FilterService;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class UpdateService {
    private final Logger log = LoggerFactory.getLogger(UpdateService.class);

    private final Environment env;
    private final ObjectMapper objectMapper;
    private final ImportService importService;
    private final DbCommunicationService dbCommunication;
    private final DataSource dataSource;
    private final FilterService filterService;

    private final DisorderService disorderService;
    private final DrugService drugService;
    private final GeneService geneService;
    private final PathwayService pathwayService;
    private final ProteinService proteinService;

    private final DisorderComorbidWithDisorderService disorderComorbidWithDisorderService;
    private final DisorderIsADisorderService disorderIsADisorderService;
    private final DrugHasIndicationService drugHasIndicationService;
    private final DrugHasTargetService drugHasTargetService;
    private final AssociatedWithDisorderService associatedWithDisorderService;
    private final ProteinEncodedByService proteinEncodedByService;
    private final ProteinInPathwayService proteinInPathwayService;
    private final ProteinInteractsWithProteinService proteinInteractsWithProteinService;
    private final DrugHasContraindicationService drugHasContraindicationService;
    private final ToolService toolService;

    @Autowired
    public UpdateService(Environment environment,
                         ObjectMapper objectMapper,
                         ImportService importService,
                         DbCommunicationService dbCommunicationService,
                         DataSource dataSource,
                         FilterService filterServic,
                         DrugService drugService,
                         PathwayService pathwayService,
                         DisorderService disorderService,
                         GeneService geneService,
                         ProteinService proteinService,
                         DisorderComorbidWithDisorderService disorderComorbidWithDisorderService,
                         DisorderIsADisorderService disorderIsADisorderService,
                         DrugHasIndicationService drugHasIndicationService,
                         DrugHasTargetService drugHasTargetService,
                         AssociatedWithDisorderService associatedWithDisorderService,
                         ProteinEncodedByService proteinEncodedByService,
                         ProteinInPathwayService proteinInPathwayService,
                         ProteinInteractsWithProteinService proteinInteractsWithProteinService,
                         DrugHasContraindicationService drugHasContraindicationService,
                         ToolService toolService
    ) {
        this.env = environment;
        this.objectMapper = objectMapper;
        this.importService = importService;
        this.dbCommunication = dbCommunicationService;
        this.dataSource = dataSource;
        this.filterService = filterServic;
        this.drugService = drugService;
        this.pathwayService = pathwayService;
        this.disorderService = disorderService;
        this.geneService = geneService;
        this.proteinService = proteinService;
        this.disorderComorbidWithDisorderService = disorderComorbidWithDisorderService;
        this.disorderIsADisorderService = disorderIsADisorderService;
        this.drugHasIndicationService = drugHasIndicationService;
        this.drugHasTargetService = drugHasTargetService;
        this.associatedWithDisorderService = associatedWithDisorderService;
        this.proteinEncodedByService = proteinEncodedByService;
        this.proteinInPathwayService = proteinInPathwayService;
        this.proteinInteractsWithProteinService = proteinInteractsWithProteinService;
        this.drugHasContraindicationService = drugHasContraindicationService;
        this.toolService = toolService;

    }


    @Scheduled(cron = "${update.interval}", zone = "${update.interval.zone}")
    public void scheduleDataUpdate() {
        if (dbCommunication.isUpdateInProgress()) {
            log.warn("Update already in progress!");
            return;
        }
        dbCommunication.scheduleUpdate();
        executeDataUpdate();
        renewDBDumps();
    }

    public void renewDBDumps() {
        File dir = new File(env.getProperty("path.external.cache"));
        File ppiFile = new File(dir, "proteinInteractsWithProtein.tsv");
        try (BufferedWriter bw = WriterUtils.getBasicWriter(ppiFile)) {
            proteinInteractsWithProteinService.getProteins().forEach((id1, map) -> map.forEach((id2, vals) -> {
                try {
                    bw.write(proteinService.map(id1) + "\t" + proteinService.map(id2) + "\t" + vals.second + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

//            proteinInteractsWithProteinService.findAllProteins().forEach(ppi -> {
//                try {
//                    bw.write(proteinService.map(ppi.getPrimaryIds().getId1()) + "\t" + proteinService.map(ppi.getPrimaryIds().getId2()) + "\t" + ppi.getEvidenceTypes().contains("exp") + "\n");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
        } catch (IOException e) {
        }

        File ggiFile = new File(dir, "geneInteractsWithGene.tsv");
        try (BufferedWriter bw = WriterUtils.getBasicWriter(ggiFile)) {
            proteinInteractsWithProteinService.getGenes().forEach((id1, map) -> map.forEach((id2, vals) -> {
                try {
                    bw.write(geneService.map(id1) + "\t" + geneService.map(id2) + "\t" + vals.second + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
//            proteinInteractsWithProteinService.findAllGenes().forEach(ggi -> {
//                try {
//                    bw.write(geneService.map(ggi.getPrimaryIds().getId1()) + "\t" + geneService.map(ggi.getPrimaryIds().getId2()) + "\t" + ggi.getEvidenceTypes().contains("exp") + "\n");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
        } catch (IOException e) {
        }
        File gdFile = new File(dir, "drugHasTargetGene.tsv");
        try (BufferedWriter bw = WriterUtils.getBasicWriter(gdFile)) {
            drugHasTargetService.getGeneEdgesFrom().forEach((id1, set) -> set.forEach(id2 -> {
                try {
                    bw.write(drugService.map(id1) + "\t" + geneService.map(id2) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
//            drugHasTargetService.findAllGenes().forEach(gd -> {
//                try {
//                    bw.write(drugService.map(gd.getPrimaryIds().getId1()) + "\t" + geneService.map(gd.getPrimaryIds().getId2()) + "\n");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
        } catch (IOException e) {
        }

        File pdFile = new File(dir, "drugHasTargetProtein.tsv");
        try (BufferedWriter bw = WriterUtils.getBasicWriter(pdFile)) {
            drugHasTargetService.getProteinEdgesFrom().forEach((id1, set) -> set.forEach(id2 -> {
                try {
                    bw.write(drugService.map(id1) + "\t" + proteinService.map(id2) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
//            drugHasTargetService.findAllProteins().forEach(pd -> {
//                try {
//                    bw.write(drugService.map(pd.getPrimaryIds().getId1()) + "\t" + proteinService.map(pd.getPrimaryIds().getId2()) + "\n");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }));
        } catch (IOException e) {
        }

        File dFile = new File(dir, "drugs.tsv");
        try (BufferedWriter bw = WriterUtils.getBasicWriter(dFile)) {
            drugService.findAll().forEach(d -> {
                try {
                    bw.write(d.getPrimaryDomainId() + "\t");
                    Iterator<String> groups = d.getDrugGroups().iterator();
                    while (groups.hasNext())
                        bw.write(groups.next() + (groups.hasNext() ? ", " : ""));
                    bw.write("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
        }

        toolService.createInteractionFiles();
    }

    @Async
    public void executeDataUpdate() {
        String url = env.getProperty("url.api.db");
        File cacheDir = new File(env.getProperty("path.db.cache"));
        cleanUpdateDirectories(cacheDir);
        try {
            log.info("DB update: Started!");
            update(url, cacheDir);
            log.info("DB udpate: Success!");
        } catch (Exception e) {
            dbCommunication.setUpdateInProgress(false);
            e.printStackTrace();
            log.error("Update could not be executed correctly: " + e.getMessage());
        }
        dbCommunication.setUpdateInProgress(false);
        Runtime.getRuntime().gc();
    }

    private void cleanUpdateDirectories(File cacheDir) {
        if (Boolean.parseBoolean(env.getProperty("update.dir.remove"))) {
            String prefix = env.getProperty("update.dir.prefix");
            LinkedList<File> delete = new LinkedList<>();
            Arrays.stream(cacheDir.listFiles()).forEach(file -> {
                if (file.isDirectory() & file.getName().startsWith(prefix)) {
                    delete.add(file);
                }
            });
            delete.forEach(file -> {
                try {
                    org.apache.commons.io.FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    private void update(String url, File cacheDir) {
        log.debug("Loading Database update from " + url + " to cache-directory " + cacheDir.getAbsolutePath());

        HashMap<String, de.exbio.reposcapeweb.db.io.Collection> collections = new HashMap<>();

        File updateDir = new File(cacheDir, env.getProperty("update.dir.prefix") + System.currentTimeMillis());
        updateDir.deleteOnExit();
        String fileType = env.getProperty("file.collections.filetype");
        fileType = fileType.charAt(0) == '.' ? fileType : '.' + fileType;

        log.info("Downloading database files!");
        downloadUpdates(url, updateDir, fileType, collections);

        if (!Boolean.parseBoolean(env.getProperty("dev.skip.dl")))
            restructureUpdates(collections);


        executingUpdates(collections, cacheDir);


        overrideOldData(cacheDir, collections);


        updateDir.delete();
    }

    private <T extends RepoTrialNode> EnumMap<UpdateOperation, HashMap<String, T>> runNodeUpdates(Class<T> valueType, de.exbio.reposcapeweb.db.io.Collection c) {
        EnumMap<UpdateOperation, HashMap<String, T>> updates = new EnumMap<>(UpdateOperation.class);
        if (!readNodeUpdates(c, updates, valueType))
            importNodeInsertions(c.getFile(), updates, valueType);
        return updates;
    }

    private <T extends RepoTrialEdge> EnumMap<UpdateOperation, HashMap<PairId, T>> runEdgeUpdates(Class<T> valueType, de.exbio.reposcapeweb.db.io.Collection c, IdMapper mapper) {
        EnumMap<UpdateOperation, HashMap<PairId, T>> updates = new EnumMap<>(UpdateOperation.class);
        if (!readEdgeUpdates(c, updates, valueType, mapper))
            importEdgeInsertions(c.getFile(), updates, valueType, mapper);
        return updates;
    }

    private void executingUpdates(HashMap<String, de.exbio.reposcapeweb.db.io.Collection> collections, File cacheDir) {
        dbCommunication.scheduleLock();

        importRepoTrialNodes(collections);

//        updateIdMaps(collections, cacheDir, true);

        importRepoTrialEdges(collections);

        regenerateTransitiveEdges(collections);

        dbCommunication.setDbLocked(false);

    }

    private void regenerateTransitiveEdges(HashMap<String, Collection> collections) {


    }

    private void importRepoTrialEdges(HashMap<String, de.exbio.reposcapeweb.db.io.Collection> collections) {

        boolean updateSuccessful = true;
        File filterCacheDir = new File(env.getProperty("path.db.cache") + "filters");
        String first = "protein_encoded_by";
        HashSet<String> attributeDefinition = null;
        try {
            attributeDefinition = getAttributeNames(ReaderUtils.getUrlContent(new URL(env.getProperty("url.api.db") + first + "/attributes")));
            if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, ProteinEncodedBy.attributes))
                updateSuccessful = proteinEncodedByService.submitUpdates(runEdgeUpdates(ProteinEncodedBy.class, collections.get(first), proteinEncodedByService::mapIds));
            proteinEncodedByService.importEdges();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (!updateSuccessful)
//            log.debug("Update execution for " + first + ": Success!");
//        else
            log.warn("Update execution for " + first + ": Error!");

        for (String k : collections.keySet()) {
            Collection c = collections.get(k);
            if (c instanceof Node)
                continue;
            try {
                attributeDefinition = getAttributeNames(ReaderUtils.getUrlContent(new URL(env.getProperty("url.api.db") + k + "/attributes")));
                updateSuccessful = true;

                switch (k) {
                    case "disorder_comorbid_with_disorder":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, DisorderComorbidWithDisorder.attributes))
                            updateSuccessful = disorderComorbidWithDisorderService.submitUpdates(runEdgeUpdates(DisorderComorbidWithDisorder.class, c, disorderComorbidWithDisorderService::mapIds));
                        disorderComorbidWithDisorderService.importEdges();
                        break;
                    case "disorder_is_subtype_of_disorder":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, DisorderIsADisorder.attributes))
                            updateSuccessful = disorderIsADisorderService.submitUpdates(runEdgeUpdates(DisorderIsADisorder.class, c, disorderIsADisorderService::mapIds));
                        filterService.writeToFile(disorderService.getFilter(), new File(filterCacheDir, k));
                        disorderIsADisorderService.importEdges();
                        break;
                    case "drug_has_indication":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, DrugHasIndication.attributes))
                            updateSuccessful = drugHasIndicationService.submitUpdates(runEdgeUpdates(DrugHasIndication.class, c, drugHasIndicationService::mapIds));
                        drugHasIndicationService.importEdges();
                        break;
                    case "drug_has_contraindication":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, DrugHasContraindication.attributes))
                            updateSuccessful = drugHasContraindicationService.submitUpdates(runEdgeUpdates(DrugHasContraindication.class, c, drugHasIndicationService::mapIds));
                        drugHasIndicationService.importEdges();
                        break;
                    case "drug_has_target":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, DrugHasTargetProtein.attributes))
                            updateSuccessful = drugHasTargetService.submitUpdates(runEdgeUpdates(DrugHasTargetProtein.class, c, drugHasTargetService::mapIds));
                        drugHasTargetService.importEdges();
                        break;
                    case "gene_associated_with_disorder":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, GeneAssociatedWithDisorder.attributes))
                            updateSuccessful = associatedWithDisorderService.submitUpdates(runEdgeUpdates(GeneAssociatedWithDisorder.class, c, associatedWithDisorderService::mapIds));
                        associatedWithDisorderService.importEdges();
                        break;
                    case "protein_in_pathway":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, ProteinInPathway.attributes))
                            updateSuccessful = proteinInPathwayService.submitUpdates(runEdgeUpdates(ProteinInPathway.class, c, proteinInPathwayService::mapIds));
                        proteinInPathwayService.importEdges();
                        break;
                    case "protein_interacts_with_protein":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, ProteinInteractsWithProtein.attributes))
                            updateSuccessful = proteinInteractsWithProteinService.submitUpdates(runEdgeUpdates(ProteinInteractsWithProtein.class, c, proteinInteractsWithProteinService::mapProteinIds));
                        proteinInteractsWithProteinService.importEdges();
                        break;
//                    case "protein_encoded_by":
                    //TODO should work
//                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, ProteinEncodedBy.attributes))
//                            updateSuccessful = proteinEncodedByService.submitUpdates(runEdgeUpdates(ProteinEncodedBy.class, c, proteinEncodedByService::mapIds));
//                        proteinEncodedByService.importEdges();
//                        break;
                }
                if (updateSuccessful)
                    log.debug("Update execution for " + k + ": Success!");
                else
                    log.warn("Update execution for " + k + ": Error!");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }


    private void importRepoTrialNodes(HashMap<String, de.exbio.reposcapeweb.db.io.Collection> collections) {
        File nodeCacheDir = new File(env.getProperty("path.db.cache") + "nodes");
        nodeCacheDir.mkdirs();
        File filterCacheDir = new File(env.getProperty("path.db.cache") + "filters");
        filterCacheDir.mkdirs();
        collections.forEach((k, c) -> {
            if (!(c instanceof Node))
                return;
            try {
                HashSet<String> attributeDefinition = getAttributeNames(ReaderUtils.getUrlContent(new URL(env.getProperty("url.api.db") + k + "/attributes")));
                boolean updateSuccessful = true;

                switch (k) {
                    case "drug": {
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, Drug.attributes))
                            updateSuccessful = drugService.submitUpdates(runNodeUpdates(Drug.class, c));
                        RepoTrialUtils.writeNodeMap(new File(nodeCacheDir, k + ".map"), drugService.getIdToDomainMap());
                        filterService.writeToFile(drugService.getFilter(), new File(filterCacheDir, k));
                        break;
                    }
                    case "pathway": {
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, Pathway.attributes))
                            updateSuccessful = pathwayService.submitUpdates(runNodeUpdates(Pathway.class, c));
                        RepoTrialUtils.writeNodeMap(new File(nodeCacheDir, k + ".map"), pathwayService.getIdToDomainMap());
                        filterService.writeToFile(pathwayService.getFilter(), new File(filterCacheDir, k));
                        break;
                    }
                    case "disorder": {
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, Disorder.attributes))
                            updateSuccessful = disorderService.submitUpdates(runNodeUpdates(Disorder.class, c));
                        RepoTrialUtils.writeNodeMap(new File(nodeCacheDir, k + ".map"), disorderService.getIdToDomainMap());
                        filterService.writeToFile(disorderService.getFilter(), new File(filterCacheDir, k));
                        break;
                    }
                    case "gene": {
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, Gene.attributes))
                            updateSuccessful = geneService.submitUpdates(runNodeUpdates(Gene.class, c));
                        RepoTrialUtils.writeNodeMap(new File(nodeCacheDir, k + ".map"), geneService.getIdToDomainMap());
                        filterService.writeToFile(geneService.getFilter(), new File(filterCacheDir, k));
                        break;
                    }
                    case "protein": {
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, Protein.attributes))
                            updateSuccessful = proteinService.submitUpdates(runNodeUpdates(Protein.class, c));
                        RepoTrialUtils.writeNodeMap(new File(nodeCacheDir, k + ".map"), proteinService.getIdToDomainMap());
                        filterService.writeToFile(proteinService.getFilter(), new File(filterCacheDir, k));
                        break;
                    }
                }
                if (updateSuccessful)
                    log.debug("Update execution for " + k + ": Success!");
                else
                    log.warn("Update execution for " + k + ": Error!");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        });
    }


    private <T extends RepoTrialNode> boolean readNodeUpdates(de.exbio.reposcapeweb.db.io.Collection c, EnumMap<UpdateOperation, HashMap<String, T>> updates, Class<T> valueType) {
        File cached = RepoTrialUtils.getCachedFile(c, env.getProperty("path.db.cache"));
        if (!cached.exists())
            return false;
        ProcessBuilder pb = new ProcessBuilder("diff", cached.getAbsolutePath(), c.getFile().getAbsolutePath());

        pb.redirectErrorStream(true);
        Process p = null;
        HashMap<String, T> ins = new HashMap<>();
        HashMap<String, T> upd = new HashMap<>();
        HashMap<String, T> dels = new HashMap<>();
        updates.put(UpdateOperation.Alteration, upd);
        updates.put(UpdateOperation.Deletion, dels);
        updates.put(UpdateOperation.Insertion, ins);

        try {
            p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                char startC = line.charAt(0);
                if (startC == '>' | startC == '<') {
                    int start = line.charAt(2) == '[' ? 3 : 2;
                    T d = objectMapper.readValue(line.substring(start), valueType);
                    if (startC == '<') {
                        dels.put(d.getUniqueId(), d);
                    } else {
                        ins.put(d.getUniqueId(), d);
                    }
                }
            }

            HashSet<String> overlap = new HashSet<>();
            dels.keySet().forEach(id -> {
                if (ins.containsKey(id))
                    overlap.add(id);
            });

            overlap.forEach(id -> {
                upd.put(id, ins.get(id));
                dels.remove(id);
                ins.remove(id);
            });

            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    private <T extends RepoTrialEdge> boolean readEdgeUpdates(de.exbio.reposcapeweb.db.io.Collection c, EnumMap<UpdateOperation, HashMap<PairId, T>> updates, Class<T> valueType, IdMapper mapper) {
        File cached = RepoTrialUtils.getCachedFile(c, env.getProperty("path.db.cache"));
        if (!cached.exists())
            return false;
        ProcessBuilder pb = new ProcessBuilder("diff", cached.getAbsolutePath(), c.getFile().getAbsolutePath());

        pb.redirectErrorStream(true);
        Process p = null;
        HashMap<PairId, T> ins = new HashMap<>();
        HashMap<PairId, T> upd = new HashMap<>();
        HashMap<PairId, T> dels = new HashMap<>();
        updates.put(UpdateOperation.Alteration, upd);
        updates.put(UpdateOperation.Deletion, dels);
        updates.put(UpdateOperation.Insertion, ins);

        try {
            p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                char startC = line.charAt(0);
                if (startC == '>' | startC == '<') {
                    int start = line.charAt(2) == '[' ? 3 : 2;
                    T d = objectMapper.readValue(line.substring(start), valueType);
                    d.setId(mapper.mapIds(d.getIdsToMap()));
                    if (startC == '<') {
                        dels.put(d.getPrimaryIds(), d);
                    } else {
                        ins.put(d.getPrimaryIds(), d);
                    }
                }
            }

            HashSet<PairId> overlap = new HashSet<>();
            dels.keySet().forEach(id -> {
                if (ins.containsKey(id))
                    overlap.add(id);
            });

            overlap.forEach(id -> {
                upd.put(id, ins.get(id));
                dels.remove(id);
                ins.remove(id);
            });

            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public <T extends RepoTrialNode> void importNodeInsertions(File updateFile, EnumMap<UpdateOperation, HashMap<String, T>> updates, Class<T> valueType) {
        log.debug("Importing insertions only from " + updateFile);
        HashMap<String, T> inserts = new HashMap<>();
        updates.put(UpdateOperation.Insertion, inserts);
        try {
            BufferedReader br = ReaderUtils.getBasicReader(updateFile);
            String line = "";
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    if (line.charAt(0) == '[')
                        line = line.substring(1);
                }
                try {
                    T d = objectMapper.readValue(line, valueType);
                    inserts.put(d.getUniqueId(), d);
                } catch (MismatchedInputException e) {
                    e.printStackTrace();
                    log.error("Malformed input line in " + updateFile.getName() + ": " + line);
                }
                if (line.charAt(line.length() - 1) == ']')
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T extends RepoTrialEdge> void importEdgeInsertions(File updateFile, EnumMap<UpdateOperation, HashMap<PairId, T>> updates, Class<T> valueType, IdMapper mapper) {
        log.debug("Importing insertions only from " + updateFile);
        HashMap<PairId, T> inserts = new HashMap<>();
        updates.put(UpdateOperation.Insertion, inserts);
        try {
            BufferedReader br = ReaderUtils.getBasicReader(updateFile);
            String line = "";
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    if (line.charAt(0) == '[')
                        line = line.substring(1);
                }
                try {
                    T d = objectMapper.readValue(line, valueType);
                    d.setId(mapper.mapIds(d.getIdsToMap()));
                    inserts.put(d.getPrimaryIds(), d);
                } catch (MismatchedInputException e) {
                    e.printStackTrace();
                    log.error("Malformed input line in " + updateFile.getName() + ": " + line);
                } catch (NullPointerException e) {
                    continue;
                }
                if (line.charAt(line.length() - 1) == ']')
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashSet<String> getAttributeNames(String content) {
        HashSet<String> attributes = new HashSet<>();
        StringUtils.split(content.substring(1, content.length() - 2), ",").forEach(a -> attributes.add(a.substring(1, a.length() - 1)));
        return attributes;
    }

    private void restructureUpdates(HashMap<String, de.exbio.reposcapeweb.db.io.Collection> collections) {
        collections.values().forEach(col -> {
            FileUtils.formatJson(col.getFile());
            int count = 0;
            try {
                BufferedReader br = ReaderUtils.getBasicReader(col.getFile());

                while (br.readLine() != null) {
                    count++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            int officialCount = getCountFromDetails(col.getName());
            log.debug(col.getName() + " contains " + count + " entries!");
            if (count != officialCount) {
                log.error("Entry count for " + col.getName() + " does not match to official number from repotrial (" + count + " vs " + officialCount + ")");
                throw new RuntimeException("Error while validating the entitiy counts. Maybe file format has changed.");
            }
        });
        log.info("Validation of entity count in cached files!");
    }

    private int getCountFromDetails(String name) {
        try {
            BufferedReader br = ReaderUtils.getBufferedReader(new URL(env.getProperty("url.api.db") + name + "/details"));
            String line = "";
            while ((line = br.readLine()) != null) {
                for (String l : StringUtils.split(line, ","))
                    if (l.startsWith("\"count"))
                        return Integer.parseInt(l.substring(8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void overrideOldData(File cacheDir, HashMap<String, de.exbio.reposcapeweb.db.io.Collection> collections) {
        collections.forEach((k, v) -> {
            try {
                log.debug("Moving " + v.getFile().toPath() + " to " + new File(cacheDir, v.getFile().getName()).toPath());
                v.setFile(Files.move(v.getFile().toPath(), new File(cacheDir, v.getFile().getName()).toPath(), REPLACE_EXISTING).toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void downloadUpdates(String api, File destDir, String fileType, HashMap<String, de.exbio.reposcapeweb.db.io.Collection> collections) {
        destDir.mkdirs();

        importService.getCollections(collections);

        if (Boolean.parseBoolean(env.getProperty("dev.skip.dl")))
            collections.forEach((k, v) -> v.setFile(createFile(destDir.getParentFile(), k, fileType)));
        else
            collections.forEach((k, v) -> v.setFile(FileUtils.download(createUrl(api, k), createFile(destDir, k, fileType))));
    }


    private String createUrl(String api, String k) {
        return api + k + "/all";
    }

    private File createFile(File destDir, String k, String fileType) {
        return new File(destDir, k + fileType);
    }

}
