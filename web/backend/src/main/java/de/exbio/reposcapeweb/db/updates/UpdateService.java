package de.exbio.reposcapeweb.db.updates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import de.exbio.reposcapeweb.communication.reponses.WebGraphService;
import de.exbio.reposcapeweb.configs.DBConfig;
import de.exbio.reposcapeweb.configs.schema.EdgeConfig;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.Metadata;
import de.exbio.reposcapeweb.db.entities.RepoTrialEdge;
import de.exbio.reposcapeweb.db.entities.RepoTrialNode;
import de.exbio.reposcapeweb.db.entities.edges.*;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.entities.nodes.*;
import de.exbio.reposcapeweb.db.io.ImportService;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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

    private final NodeController nodeController;
    private final EdgeController edgeController;

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

    private final WebGraphService webGraphService;

    private Metadata metadata;
    private long lastUpdate;
    private long lastCheck;

    private final File jsonReformatter;

    @Autowired
    public UpdateService(Environment environment,
                         WebGraphService webGraphService,
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
                         ToolService toolService,
                         NodeController nodeController,
                         EdgeController edgeController
    ) {
        this.env = environment;
        this.webGraphService=webGraphService;
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
        this.nodeController = nodeController;
        this.edgeController = edgeController;

        jsonReformatter = new File(new File(env.getProperty("path.scripts.dir")), "reformatJson.sh");

    }

    @Scheduled(cron = "${update.interval}", zone = "${update.interval.zone}")
    public void scheduleDataUpdate() {
        if (dbCommunication.isUpdateInProgress()) {
            log.warn("Update already in progress!");
            return;
        }
        dbCommunication.scheduleUpdate();
        executeDataUpdate();
    }

    public void renewDBDumps() {
        log.info("Creating database dump files for external Tool");
        File dir = new File(env.getProperty("path.external.cache"));
        if (dir.exists())
            dir.delete();
        dir.mkdirs();
        String sifHead = "#node1\tedgetype\tnode2\n";
        HashMap<Integer, BufferedWriter> tsv_writers = new HashMap<>();
        HashMap<Integer, BufferedWriter> sif_all_writers = new HashMap<>();
        HashMap<Integer, BufferedWriter> sif_exp_writers = new HashMap<>();
        TreeMap<Integer, String> tissueMap = proteinInteractsWithProteinService.getIdTissueMap();
        tissueMap.forEach((k, v) -> {
            String tissueInFile = v.replaceAll(" ","");
            tsv_writers.put(k, WriterUtils.getBasicWriter(new File(dir,"proteinInteractsWithProtein-"+tissueInFile+".tsv")));
            sif_all_writers.put(k, WriterUtils.getBasicWriter(new File(dir, "proteinInteractsWithProtein-"+tissueInFile+"_all.sif")));
            sif_exp_writers.put(k, WriterUtils.getBasicWriter(new File(dir, "proteinInteractsWithProtein-"+tissueInFile+"_exp.sif")));
        });

        File ppiFile = new File(dir, "proteinInteractsWithProtein.tsv");
        File ppiSif_all = new File(dir, "proteinInteractsWithProtein_all.sif");
        File ppiSif_exp = new File(dir, "proteinInteractsWithProtein_exp.sif");
        try (BufferedWriter bw = WriterUtils.getBasicWriter(ppiFile); BufferedWriter bwSifa = WriterUtils.getBasicWriter(ppiSif_all); BufferedWriter bwSife = WriterUtils.getBasicWriter(ppiSif_exp)) {
            bwSifa.write(sifHead);
            bwSife.write(sifHead);
            sif_all_writers.values().forEach(w-> {
                try {
                    w.write(sifHead);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            sif_exp_writers.values().forEach(w-> {
                try {
                    w.write(sifHead);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            proteinInteractsWithProteinService.getProteins().forEach((id1, map) -> map.forEach((id, vals) -> {
                try {
                    String tsvLine = proteinService.map(id1) + "\t" + proteinService.map(id.getId2()) + "\t" + vals.second + "\n";
                    bw.write(tsvLine);
                    String sifLine = "_" + id1 + "\tpp\t_" + id.getId2() + "\n";
                    bwSifa.write(sifLine);
                    if (vals.first.second)
                        bwSife.write(sifLine);
                    for (int tissueId : vals.second) {
                        tsv_writers.get(tissueId).write(tsvLine);
                        sif_all_writers.get(tissueId).write(sifLine);
                        if(vals.first.second)
                            sif_exp_writers.get(tissueId).write(sifLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                    log.warn("Could not map either " + id1 + " or " + id.getId2() + " to internal protein entities.");
                    return;
                }
            }));
        } catch (IOException e) {
            log.error("Error on writing some data-file: " + ppiFile.getAbsolutePath());
            e.printStackTrace();
        }
        tsv_writers.values().forEach(b -> {
            try {
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sif_all_writers.values().forEach(b -> {
            try {
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sif_exp_writers.values().forEach(b -> {
            try {
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        File ggiFile = new File(dir, "geneInteractsWithGene.tsv");
        File ggiSif_all = new File(dir, "geneInteractsWithGene_all.sif");
        File ggiSif_exp = new File(dir, "geneInteractsWithGene_exp.sif");
        tsv_writers.clear();
        sif_all_writers.clear();
        sif_exp_writers.clear();
        tissueMap.forEach((k, v) -> {
            String tissueInFile = v.replaceAll(" ","");
            tsv_writers.put(k, WriterUtils.getBasicWriter(new File(dir, "geneInteractsWithGene-"+tissueInFile+".tsv")));
            sif_all_writers.put(k, WriterUtils.getBasicWriter(new File(dir, "geneInteractsWithGene-"+tissueInFile+"_all.sif")));
            sif_exp_writers.put(k, WriterUtils.getBasicWriter(new File(dir, "geneInteractsWithGene-"+tissueInFile+"_exp.sif")));
        });
        try (BufferedWriter bw = WriterUtils.getBasicWriter(ggiFile); BufferedWriter bwSifa = WriterUtils.getBasicWriter(ggiSif_all); BufferedWriter bwSife = WriterUtils.getBasicWriter(ggiSif_exp)) {
            bwSifa.write(sifHead);
            bwSife.write(sifHead);
            proteinInteractsWithProteinService.getGenes().forEach((id1, map) -> map.forEach((id, vals) -> {
                try {
                    String tsvLine = geneService.map(id1) + "\t" + geneService.map(id.getId2()) + "\t" + vals.second + "\n";
                    bw.write(tsvLine);
                    String sifLine = "_" + id1 + "\tgg\t_" + id.getId2() + "\n";
                    bwSifa.write(sifLine);
                    if (vals.first.second)
                        bwSife.write(sifLine);
                    for (int tissueId : vals.second) {
                        tsv_writers.get(tissueId).write(tsvLine);
                        sif_all_writers.get(tissueId).write(sifLine);
                        if(vals.first.second)
                            sif_exp_writers.get(tissueId).write(sifLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } catch (IOException e) {
            log.error("Error on writing some data-file: " + ggiFile.getAbsolutePath());
            e.printStackTrace();
        }
        tsv_writers.values().forEach(b -> {
            try {
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sif_all_writers.values().forEach(b -> {
            try {
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sif_exp_writers.values().forEach(b -> {
            try {
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        File gdFile = new File(dir, "drugHasTargetGene.tsv");
        try (BufferedWriter bw = WriterUtils.getBasicWriter(gdFile)) {
            drugHasTargetService.getGeneEdgesFrom().forEach((id1, set) -> set.forEach(id -> {
                try {
                    bw.write(drugService.map(id1) + "\t" + geneService.map(id.getId2()) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } catch (IOException e) {
            log.error("Error on writing some data-file: " + gdFile.getAbsolutePath());
        }

        File pdFile = new File(dir, "drugHasTargetProtein.tsv");
        try (BufferedWriter bw = WriterUtils.getBasicWriter(pdFile)) {
            drugHasTargetService.getProteinEdgesFrom().forEach((id1, set) -> set.forEach(id -> {
                try {
                    bw.write(drugService.map(id1) + "\t" + proteinService.map(id.getId2()) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } catch (IOException e) {
            log.error("Error on writing some data-file: " + pdFile.getAbsolutePath());
            e.printStackTrace();
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
            log.error("Error on writing some data-file: " + dFile.getAbsolutePath());
            e.printStackTrace();
        }

        toolService.createIndexFiles();
        toolService.createInteractionFiles(tissueMap);
        log.info("Done");
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
        Runtime.getRuntime().gc();
//        if (!skipUpdateList().isEmpty()) {
//            DBConfig.getConfig().edges.forEach(edge -> {
//                if (!skipUpdateList().contains(edge.name))
//                    return;
//                switch (edge.mapsTo) {
//                    case "DisorderComorbidity" -> disorderComorbidWithDisorderService.importEdges();
//                    case "DisorderHierarchy" -> disorderIsADisorderService.importEdges();
//                    case "DrugIndication" -> drugHasIndicationService.importEdges();
//                    case "DrugTargetProtein" -> drugHasTargetService.importEdges();
//                    case "GeneAssociatedWithDisorder" -> associatedWithDisorderService.importEdges();
//                    case "ProteinPathway" -> proteinInPathwayService.importEdges();
//                    case "ProteinProteinInteraction" -> proteinInteractsWithProteinService.importEdges();
//                    case "ProteinEncodedBy" -> proteinEncodedByService.importEdges();
//                    case "DrugContraindication" -> drugHasContraindicationService.importEdges();
//                }
//            });
//        }
        renewDBDumps();
        webGraphService.remapHistory(new File(env.getProperty("path.usr.cache")));
        dbCommunication.setUpdateInProgress(false);
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

        File updateDir = new File(cacheDir, env.getProperty("update.dir.prefix") + System.currentTimeMillis());
        updateDir.deleteOnExit();
        String fileType = env.getProperty("file.collections.filetype");
        fileType = fileType.charAt(0) == '.' ? fileType : '.' + fileType;

        String partialUpdate = env.getProperty("update.partial");

        if (validateSchema() || (partialUpdate != null && partialUpdate.equals("true"))) {


            log.info("Downloading database files!");
            downloadUpdates(url, updateDir, fileType);
            log.info("Validation of entity count in cached files!");

            DBConfig.getConfig().nodes.stream().filter(n -> !skipUpdateList().contains(n.name)).forEach(node -> restructureUpdates(node.file, node.name));
            DBConfig.getConfig().edges.stream().filter(e -> e.original).filter(e -> !skipUpdateList().contains(e.name)).forEach(edge -> restructureUpdates(edge.file, edge.name));

            executingUpdates();
            log.info("Update Metadata");
            buildMetadata();
            if (this.lastUpdate != this.metadata.getLastUpdate()) {
                this.lastCheck = this.lastUpdate;
                updateMetadata(this.lastCheck, this.lastUpdate);
            } else {
                this.lastCheck = LocalDateTime.now().toEpochSecond(ZoneOffset.ofTotalSeconds(0));
                updateMetadata(this.lastCheck);
            }
            log.info("Update Licence text");
            updateLicenceText();
            overrideOldData(cacheDir);
        } else {
            log.warn("Database update is skipped due to errors!");
        }

        FileUtils.deleteDirectory(updateDir);
    }

    private boolean validateSchema() {
        AtomicBoolean valid = new AtomicBoolean(true);
        DBConfig.getConfig().nodes.forEach(n -> {
            if (skipUpdateList().contains(n.name))
                return;
            HashSet<String> attributes = null;
            try {
                attributes = getAttributeNames(ReaderUtils.getUrlContent(new URL(env.getProperty("url.api.db") + n.name + "/attributes")));
                if (!RepoTrialUtils.validateFormat((HashSet<String>) attributes.clone(), nodeController.getSourceAttributes(n.name))) {
                    valid.set(false);
                    log.error("Node " + n.name + " changed schema in NeDRexDB! please update the database-config file and internal structure.");
                    log.error("DB-Schema: " + attributes);
                    log.error("Internal Schema: " + nodeController.getSourceAttributes(n.name));
                    log.error("=> Missing: " + attributes.stream().filter(e -> !nodeController.getSourceAttributes(n.name).contains(e)).collect(Collectors.toList()));
                    HashSet<String> finalAttributes = attributes;
                    log.error("=> Obsolete: " + nodeController.getSourceAttributes(n.name).stream().filter(e -> !finalAttributes.contains(e)).collect(Collectors.toList()));
                } else {
                    log.debug("Schema of " + n.name + " from RepoTrialDB is valid!");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

        DBConfig.getConfig().edges.forEach(e -> {
            if (skipUpdateList().contains(e.name))
                return;
            HashSet<String> attributes = null;
            if (e.original)
                try {
                    attributes = getAttributeNames(ReaderUtils.getUrlContent(new URL(env.getProperty("url.api.db") + e.name + "/attributes")));
                    if (!RepoTrialUtils.validateFormat((HashSet<String>) attributes.clone(), edgeController.getSourceAttributes(e.mapsTo))) {
                        valid.set(false);
                        log.error("Edge " + e.name + " changed schema in RepoTrialDB! please update the database-config file and internal structure.");
                        log.error("DB-Schema: " + attributes.toString());
                        log.error("Internal Schema: " + edgeController.getSourceAttributes(e.mapsTo));
                        log.error("=> Missing: " + attributes.stream().filter(edge -> !edgeController.getSourceAttributes(e.mapsTo).contains(edge)).collect(Collectors.toList()));
                        HashSet<String> finalAttributes = attributes;
                        log.error("=> Obsolete: " + edgeController.getSourceAttributes(e.mapsTo).stream().filter(edge -> !finalAttributes.contains(edge)).collect(Collectors.toList()));
                    } else {
                        log.debug("Schema of " + e.name + " from RepoTrialDB is valid!");
                    }
                } catch (MalformedURLException e2) {
                    e2.printStackTrace();
                }
        });

        return valid.get();
    }

    private <T extends RepoTrialNode> EnumMap<UpdateOperation, HashMap<String, T>> startNodeUpdate(Class<T> valueType, File c) {
        EnumMap<UpdateOperation, HashMap<String, T>> updates = new EnumMap<>(UpdateOperation.class);
        if (!readNodeUpdates(c, updates, valueType))
            importNodeInsertions(c, updates, valueType);
        if (updates.values().stream().map(HashMap::size).reduce((a, b) -> a + b).orElse(0) > 0)
            this.lastUpdate = LocalDateTime.now().toEpochSecond(ZoneOffset.ofTotalSeconds(0));
        else
            this.lastCheck = LocalDateTime.now().toEpochSecond(ZoneOffset.ofTotalSeconds(0));
        return updates;
    }

    private <T extends RepoTrialEdge> EnumMap<UpdateOperation, HashMap<PairId, T>> runEdgeUpdates(Class<T> valueType, File c, IdMapper mapper) {
        EnumMap<UpdateOperation, HashMap<PairId, T>> updates = new EnumMap<>(UpdateOperation.class);
        if (!readEdgeUpdates(c, updates, valueType, mapper))
            importEdgeInsertions(c, updates, valueType, mapper);
        return updates;
    }

    private <T extends RepoTrialEdge> EnumMap<UpdateOperation, HashMap<PairId, T>> runEdgeUpdatesPartitioned(Class<T> valueType, File c, IdMapper mapper, int skip, int count) {
        EnumMap<UpdateOperation, HashMap<PairId, T>> updates = new EnumMap<>(UpdateOperation.class);
        if (RepoTrialUtils.getCachedFile(c, env.getProperty("path.db.cache")).exists()) {
            readEdgeUpdatesPartitioned(c, updates, valueType, mapper, skip, count);
            if (!readEdgeUpdatesPartitioned(c, updates, valueType, mapper, skip, count)) {
                updates.put(UpdateOperation.Alteration, null);
            }
        } else {
            importEdgeInsertionsPartitioned(c, updates, valueType, mapper, skip, count);
        }
        return updates;
    }

    private void executingUpdates() {
        dbCommunication.scheduleLock();

        importRepoTrialNodes();

        importService.updateIdMaps(true);
        importRepoTrialEdges();

        importService.updateNodeData(true, false);
        dbCommunication.setDbLocked(false);

    }


    private void importRepoTrialEdges() {
        log.info("Starting edge update import!");
        boolean updateSuccessful = true;
//        File filterCacheDir = new File(env.getProperty("path.db.cache") + "filters");
        String first = "protein_encoded_by_gene";
//        HashSet<String> attributeDefinition = null;
        if (!skipUpdateList().contains(first)) {
            try {
                getAttributeNames(ReaderUtils.getUrlContent(new URL(env.getProperty("url.api.db") + first + "/attributes")));
                updateSuccessful = proteinEncodedByService.submitUpdates(runEdgeUpdates(ProteinEncodedBy.class, DBConfig.getConfig().edges.stream().filter(edge -> edge.mapsTo.equals("ProteinEncodedBy")).collect(Collectors.toList()).get(0).file, proteinEncodedByService::mapIds));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if (!updateSuccessful)
                log.error("Update execution for " + first + ": Error!");
        }
        for (EdgeConfig edge : DBConfig.getConfig().edges) {
            if (!edge.original)
                continue;
            if (skipUpdateList().contains(edge.name))
                continue;
            try {
                getAttributeNames(ReaderUtils.getUrlContent(new URL(env.getProperty("url.api.db") + edge.name + "/attributes")));
                updateSuccessful = true;

                switch (edge.mapsTo) {
                    case "DisorderComorbidity":
                        updateSuccessful = disorderComorbidWithDisorderService.submitUpdates(runEdgeUpdates(DisorderComorbidWithDisorder.class, edge.file, disorderComorbidWithDisorderService::mapIds));
                        disorderComorbidWithDisorderService.importEdges();
                        break;
                    case "DisorderHierarchy":
                        updateSuccessful = disorderIsADisorderService.submitUpdates(runEdgeUpdates(DisorderIsADisorder.class, edge.file, disorderIsADisorderService::mapIds));
                        importService.updateDisorderFilters();
                        disorderIsADisorderService.importEdges();
                        break;
                    case "DrugIndication":
                        updateSuccessful = drugHasIndicationService.submitUpdates(runEdgeUpdates(DrugHasIndication.class, edge.file, drugHasIndicationService::mapIds));
                        drugHasIndicationService.importEdges();
                        break;
                    case "DrugContraindication":
                        updateSuccessful = drugHasContraindicationService.submitUpdates(runEdgeUpdates(DrugHasContraindication.class, edge.file, drugHasContraindicationService::mapIds));
                        drugHasContraindicationService.importEdges();
                        break;
                    case "DrugTargetProtein":
                        updateSuccessful = drugHasTargetService.submitUpdates(runEdgeUpdates(DrugHasTargetProtein.class, edge.file, drugHasTargetService::mapIds));
                        drugHasTargetService.importEdges();
                        break;
                    case "GeneAssociatedWithDisorder":
                        updateSuccessful = associatedWithDisorderService.submitUpdates(runEdgeUpdates(GeneAssociatedWithDisorder.class, edge.file, associatedWithDisorderService::mapIds));
                        associatedWithDisorderService.importEdges();
                        break;
                    case "ProteinPathway":
                        updateSuccessful = proteinInPathwayService.submitUpdates(runEdgeUpdates(ProteinInPathway.class, edge.file, proteinInPathwayService::mapIds));
                        proteinInPathwayService.importEdges();
                        break;
                    case "ProteinProteinInteraction":
                        int skip = 0;
                        int batch = 100_000;
                        boolean full = !RepoTrialUtils.getCachedFile(edge.file, env.getProperty("path.db.cache")).exists();
                        EnumMap<UpdateOperation, HashMap<PairId, ProteinInteractsWithProtein>> rest = new EnumMap<>(UpdateOperation.class);
                        rest.put(UpdateOperation.Deletion, new HashMap<>());
                        rest.put(UpdateOperation.Insertion, new HashMap<>());
                        while (true) {
                            EnumMap<UpdateOperation, HashMap<PairId, ProteinInteractsWithProtein>> updates = runEdgeUpdatesPartitioned(ProteinInteractsWithProtein.class, edge.file, proteinInteractsWithProteinService::mapProteinIds, skip, batch);
                            if (full) {
                                if (updates.get(UpdateOperation.Insertion).size() == 0)
                                    break;
                                if (!proteinInteractsWithProteinService.submitUpdates(updates))
                                    updateSuccessful = false;
                            } else {
                                rest.get(UpdateOperation.Insertion).putAll(updates.remove(UpdateOperation.Insertion));
                                rest.get(UpdateOperation.Deletion).putAll(updates.remove(UpdateOperation.Deletion));
                                boolean done = updates.get(UpdateOperation.Alteration) == null;
                                if (done)
                                    updates.put(UpdateOperation.Alteration, new HashMap<>());
                                if (!proteinInteractsWithProteinService.submitUpdates(updates))
                                    updateSuccessful = false;
                                if (done) {
                                    HashSet<PairId> overlap = new HashSet<>();
                                    rest.get(UpdateOperation.Deletion).keySet().forEach(id -> {
                                        if (rest.get(UpdateOperation.Insertion).containsKey(id))
                                            overlap.add(id);
                                    });

                                    overlap.forEach(id -> {
                                        rest.get(UpdateOperation.Alteration).put(id, rest.get(UpdateOperation.Insertion).get(id));
                                        rest.get(UpdateOperation.Deletion).remove(id);
                                        rest.get(UpdateOperation.Insertion).remove(id);
                                    });
                                    if (!proteinInteractsWithProteinService.submitUpdates(rest))
                                        updateSuccessful = false;
                                    break;
                                }
                            }
                            skip += batch;
                        }
                        if (updateSuccessful) {
                            updateSuccessful = proteinInteractsWithProteinService.generateGeneEntries();
                        }
                        proteinInteractsWithProteinService.importEdges();
                        break;
                }
                if (updateSuccessful)
                    log.debug("Update execution for " + edge.label + ": Success!");
                else
                    log.error("Update execution for " + edge.label + ": Error!");
//              Update protein-gene mapping
                proteinEncodedByService.importEdges();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

    }

    private HashSet<String> skipUpdateList() {
        HashSet<String> set = new HashSet<>();
        String skip = env.getProperty("update.skip");
        if (skip == null)
            return set;
        set.addAll(StringUtils.split(skip, ","));
        return set;
    }


    private void importRepoTrialNodes() {
        log.info("Starting node update import!");
        File nodeCacheDir = new File(env.getProperty("path.db.cache") + "nodes");
        nodeCacheDir.mkdirs();
        File filterCacheDir = new File(env.getProperty("path.db.cache") + "filters");
        filterCacheDir.mkdirs();
        DBConfig.getConfig().nodes.forEach(node -> {
            if (skipUpdateList().contains(node.name))
                return;
            try {
                getAttributeNames(ReaderUtils.getUrlContent(new URL(env.getProperty("url.api.db") + node.name + "/attributes")));
                boolean updateSuccessful = true;

                switch (node.name) {
                    case "drug": {
                        updateSuccessful = drugService.submitUpdates(startNodeUpdate(Drug.class, node.file));
                        break;
                    }
                    case "pathway": {
                        updateSuccessful = pathwayService.submitUpdates(startNodeUpdate(Pathway.class, node.file));
                        break;
                    }
                    case "disorder": {
                        updateSuccessful = disorderService.submitUpdates(startNodeUpdate(Disorder.class, node.file));
                        break;
                    }
                    case "gene": {
                        updateSuccessful = geneService.submitUpdates(startNodeUpdate(Gene.class, node.file));
                        break;
                    }
                    case "protein": {
                        updateSuccessful = proteinService.submitUpdates(startNodeUpdate(Protein.class, node.file));
                        break;
                    }
                }
                if (updateSuccessful)
                    log.debug("Update execution for " + node.label + ": Success!");
                else
                    log.error("Update execution for " + node.label + ": Error!");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });
    }


    private <T extends RepoTrialNode> boolean readNodeUpdates(File c, EnumMap<UpdateOperation, HashMap<String, T>> updates, Class<T> valueType) {
        File cached = RepoTrialUtils.getCachedFile(c, env.getProperty("path.db.cache"));
        if (!cached.exists())
            return false;
        ProcessBuilder pb = new ProcessBuilder("diff", cached.getAbsolutePath(), c.getAbsolutePath());

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

    private <T extends RepoTrialEdge> boolean readEdgeUpdates(File c, EnumMap<UpdateOperation, HashMap<PairId, T>> updates, Class<T> valueType, IdMapper mapper) {
        File cached = RepoTrialUtils.getCachedFile(c, env.getProperty("path.db.cache"));
        if (!cached.exists())
            return false;
        ProcessBuilder pb = new ProcessBuilder("diff", cached.getAbsolutePath(), c.getAbsolutePath());

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
                    try {
                        d.setId(mapper.mapIds(d.getIdsToMap()));
                    } catch (NullPointerException e) {
                        log.warn("Entry could not be mapped to current database state! Update will be skipped for :'" + line.substring(start) + "'");
                        continue;
                    }
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

    private <T extends RepoTrialEdge> boolean readEdgeUpdatesPartitioned(File c, EnumMap<UpdateOperation, HashMap<PairId, T>> updates, Class<T> valueType, IdMapper mapper, int skip, int count) {
        File diff = new File(c.getParent(), c.getName() + "_diff");
        File cached = RepoTrialUtils.getCachedFile(c, env.getProperty("path.db.cache"));
        if (!diff.exists()) {
            try {
                diff.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProcessBuilder pb = new ProcessBuilder(new File(new File(env.getProperty("path.scripts.dir")), "createBatchDiff.sh").getAbsolutePath(), cached.getAbsolutePath(), c.getAbsolutePath(), diff.getAbsolutePath());
            try {
                ProcessUtils.executeProcessWait(pb, false);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
//        pb.redirectErrorStream(true);
//        Process p = null;
        HashMap<PairId, T> ins = new HashMap<>();
        HashMap<PairId, T> upd = new HashMap<>();
        HashMap<PairId, T> dels = new HashMap<>();
        updates.put(UpdateOperation.Alteration, upd);
        updates.put(UpdateOperation.Deletion, dels);
        updates.put(UpdateOperation.Insertion, ins);

        try {
//            p = pb.start();
            BufferedReader br = ReaderUtils.getBasicReader(diff);
            String line = "";
            int l = 0;
            while ((line = br.readLine()) != null) {
                char startC = line.charAt(0);
                if (startC == '>' | startC == '<') {
                    l++;
                    if (l < skip)
                        continue;
                    int start = line.charAt(2) == '[' ? 3 : 2;
                    line = line.substring(start);
                    line = line.charAt(0) == '{' ? line : ('{' + line);
                    line = line.charAt(line.length() - 1) == ']' | line.charAt(line.length() - 1) == ',' ? line.substring(0, line.length() - 1) : line;
                    line = line.charAt(line.length() - 1) == '}' ? line : (line + '}');
                    T d = objectMapper.readValue(line, valueType);
                    try {
                        d.setId(mapper.mapIds(d.getIdsToMap()));
                    } catch (NullPointerException e) {
                        log.warn("Entry could not be mapped to current database state! Update will be skipped for :'" + line.substring(start) + "'");
                        continue;
                    }
                    if (startC == '<') {
                        dels.put(d.getPrimaryIds(), d);
                    } else {
                        ins.put(d.getPrimaryIds(), d);
                    }
                    if (l - skip >= count)
                        break;
                }
            }
            if (skip >= l)
                return false;

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

//            p.waitFor();
        } catch (IOException e) {
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
            while ((line = br.readLine()) != null) {
                if (line.charAt(0) == '[')
                    line = line.substring(1);
                try {
                    T d = objectMapper.readValue(line, valueType);
                    inserts.put(d.getUniqueId(), d);
                } catch (MismatchedInputException e) {
                    e.printStackTrace();
                    log.error("Malformed input line in " + updateFile.getName() + ": " + line);
                }
//                if (line.charAt(line.length() - 1) == ']')
//                    break;
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

            while ((line = br.readLine()) != null) {
                if (line.charAt(0) == '[')
                    line = line.substring(1);
                if (line.charAt(0) != '{')
                    line = '{' + line;
                try {
                    T d = objectMapper.readValue(line, valueType);
                    d.setId(mapper.mapIds(d.getIdsToMap()));
                    inserts.put(d.getPrimaryIds(), d);
                } catch (MismatchedInputException e) {
                    e.printStackTrace();
                    log.error("Malformed input line in " + updateFile.getName() + ": " + line);
                } catch (NullPointerException e) {
                    log.debug("Edge could not be mapped in "+ updateFile.getName() + ": " + line);
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T extends RepoTrialEdge> void importEdgeInsertionsPartitioned(File updateFile, EnumMap<UpdateOperation, HashMap<PairId, T>> updates, Class<T> valueType, IdMapper mapper, int skip, int count) {
        log.debug("Importing insertions only from " + updateFile);
        HashMap<PairId, T> inserts = new HashMap<>();
        updates.put(UpdateOperation.Insertion, inserts);
        try {
            BufferedReader br = ReaderUtils.getBasicReader(updateFile);
            String line = "";
            int l = 0;
            while ((line = br.readLine()) != null) {
                l++;
                if (l < skip)
                    continue;
                if (line.charAt(0) == '[')
                    line = line.substring(1);
                if (line.charAt(0) != '{')
                    line = '{' + line;
                try {
                    T d = objectMapper.readValue(line, valueType);
                    d.setId(mapper.mapIds(d.getIdsToMap()));
                    inserts.put(d.getPrimaryIds(), d);
                    if (inserts.size() >= count)
                        break;
                } catch (MismatchedInputException e) {
                    e.printStackTrace();
                    log.error("Malformed input line in " + updateFile.getName() + ": " + line);
                } catch (NullPointerException e) {
                    continue;
                }
//                if (line.charAt(line.length() - 1) == ']')
//                    break;

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

    private void restructureUpdates(File f, String name) {
        if (!name.equals("protein_interacts_with_protein"))
            FileUtils.formatJson(jsonReformatter, f);
        int count = 0;
        try {
            BufferedReader br = ReaderUtils.getBasicReader(f);

            while (br.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int officialCount = getCountFromDetails(name);
        log.debug(f.getName() + " contains " + count + " entries!");
        if (count != officialCount) {
            log.error("Entry count for " + name + " does not match to official number from repotrial (" + count + " vs " + officialCount + ")");
            throw new RuntimeException("Error while validating the entity counts. Maybe file format has changed.");
        }

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

    private void overrideOldData(File cacheDir) {
        DBConfig.getConfig().nodes.stream().filter(n -> !skipUpdateList().contains(n.name)).forEach(node -> {
            try {
                log.debug("Moving " + node.file.toPath() + " to " + new File(cacheDir, node.file.getName()).toPath());
                node.file = Files.move(node.file.toPath(), new File(cacheDir, node.file.getName()).toPath(), REPLACE_EXISTING).toFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        DBConfig.getConfig().edges.stream().filter(e -> e.original).filter(e -> !skipUpdateList().contains(e.name)).forEach(edge -> {
            try {
                log.debug("Moving " + edge.file.toPath() + " to " + new File(cacheDir, edge.file.getName()).toPath());
                edge.file = Files.move(edge.file.toPath(), new File(cacheDir, edge.file.getName()).toPath(), REPLACE_EXISTING).toFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    private void downloadUpdates(String api, File destDir, String fileType) {
        destDir.mkdirs();

        DBConfig.getConfig().nodes.stream().filter(n -> !skipUpdateList().contains(n.name)).forEach(node -> node.file = FileUtils.download(createUrl(api, node.name), createFile(destDir, node.name, fileType)));
        DBConfig.getConfig().edges.stream().filter(e -> e.original).filter(e -> !skipUpdateList().contains(e.name)).forEach(edge -> {
            if (edge.name.equals("protein_interacts_with_protein")) {
                edge.file = FileUtils.downloadPaginated(createPaginatedUrl(api, edge.paginatedName), new File(env.getProperty("path.scripts.dir"), "mergeParts.sh"), createFile(destDir, edge.mapsTo, fileType), getEntryCount(api, edge.name), jsonReformatter);
            } else
                edge.file = FileUtils.download(createUrl(api, edge.name), createFile(destDir, edge.mapsTo, fileType));

        });
    }

    private int getEntryCount(String api, String name) {
        try {
            String details = ReaderUtils.getUrlContent(new URL(api + name + "/details"));
            HashMap<String, Object> map = objectMapper.readValue(details, HashMap.class);
            return (Integer) map.get("count");
        } catch (MalformedURLException | JsonProcessingException e) {
            e.printStackTrace();
        }
        return 0;
    }


    private String createUrl(String api, String k) {
        return api + k + "/all";
    }

    private String createPaginatedUrl(String api, String k) {
        return api + k;
    }

    private File createFile(File destDir, String k, String fileType) {
        return new File(destDir, k + fileType);
    }

    public void readMetadata() {
        try {
            metadata = objectMapper.readValue(new File(env.getProperty("path.db.cache"), "metadata.json"), Metadata.class);
            this.lastUpdate = metadata.getLastUpdate();
            this.lastCheck = metadata.getLastCheck();
        } catch (IOException e) {
            log.warn("No metadata-file found.");
            buildMetadata();
            saveMetadata();
        }
    }

    public void buildMetadata() {
        try {
            String meta = ReaderUtils.getUrlContent(new URL(env.getProperty("url.api.db") + "static/metadata"));
            this.metadata = new Metadata(objectMapper.readValue(meta, HashMap.class));
            metadata.setLastCheck(this.lastCheck);
            metadata.setLastUpdate(this.lastUpdate);
        } catch (MalformedURLException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void updateMetadata(long checked) {
        this.metadata.setLastCheck(checked);
        saveMetadata();
    }

    public void updateMetadata(long checked, long updated) {
        this.metadata.setLastCheck(checked);
        this.metadata.setLastUpdate(updated);
        saveMetadata();
    }

    public File getLicenceFile() {
        return new File(env.getProperty("path.db.cache"), "NeDRex_licence.txt");
    }

    public String getLicenceText() {
        File licenceFile = getLicenceFile();
        if (!licenceFile.exists())
            updateLicenceText();
        String licenceText = ReaderUtils.getFileContent(getLicenceFile());
        if (licenceText.length() == 0) {
            this.updateLicenceText();
            return getLicenceText();
        }
        return licenceText;
    }

    public String queryLicenseText() {
        try {
            return ReaderUtils.getUrlContent(new URL(env.getProperty("url.api.db") + "static/licence"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateLicenceText() {
        try {
            WriterUtils.writeTo(getLicenceFile(), queryLicenseText());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void saveMetadata() {
        try (BufferedWriter bw = WriterUtils.getBasicWriter(new File(env.getProperty("path.db.cache"), "metadata.json"))) {
            bw.write(objectMapper.writeValueAsString(metadata));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Metadata getMetadata() {
        return metadata;
    }
}
