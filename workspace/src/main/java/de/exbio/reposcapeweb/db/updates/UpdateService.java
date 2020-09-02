package de.exbio.reposcapeweb.db.updates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.edges.*;
import de.exbio.reposcapeweb.db.entities.ids.PairId;
import de.exbio.reposcapeweb.db.entities.nodes.*;
import de.exbio.reposcapeweb.db.io.ImportService;
import de.exbio.reposcapeweb.db.io.Node;
import de.exbio.reposcapeweb.db.services.edges.*;
import de.exbio.reposcapeweb.db.services.nodes.*;
import de.exbio.reposcapeweb.utils.FileUtils;
import de.exbio.reposcapeweb.utils.ReaderUtils;
import de.exbio.reposcapeweb.utils.RepoTrialUtils;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private final DisorderService disorderService;
    private final DrugService drugService;
    private final GeneService geneService;
    private final PathwayService pathwayService;
    private final ProteinService proteinService;

    private final DisorderComorbidWithDisorderService disorderComorbidWithDisorderService;
    private final DisorderIsADisorderService disorderIsADisorderService;
    private final DrugHasIndicationService drugHasIndicationService;
    private final DrugHasTargetService drugHasTargetService;
    private final GeneAssociatedWithDisorderService geneAssociatedWithDisorderService;
    private final ProteinEncodedByService proteinEncodedByService;
    private final ProteinInPathwayService proteinInPathwayService;
    private final ProteinInteractsWithProteinService proteinInteractsWithProteinServic;


    @Autowired
    public UpdateService(Environment environment,
                         ObjectMapper objectMapper,
                         ImportService importService,
                         DbCommunicationService dbCommunicationService,
                         DrugService drugService,
                         PathwayService pathwayService,
                         DisorderService disorderService,
                         GeneService geneService,
                         ProteinService proteinService,
                         DisorderComorbidWithDisorderService disorderComorbidWithDisorderService,
                         DisorderIsADisorderService disorderIsADisorderService,
                         DrugHasIndicationService drugHasIndicationService,
                         DrugHasTargetService drugHasTargetService,
                         GeneAssociatedWithDisorderService geneAssociatedWithDisorderService,
                         ProteinEncodedByService proteinEncodedByService,
                         ProteinInPathwayService proteinInPathwayService,
                         ProteinInteractsWithProteinService proteinInteractsWithProteinService
    ) {
        this.env = environment;
        this.objectMapper = objectMapper;
        this.importService = importService;
        this.dbCommunication = dbCommunicationService;
        this.drugService = drugService;
        this.pathwayService = pathwayService;
        this.disorderService = disorderService;
        this.geneService = geneService;
        this.proteinService = proteinService;
        this.disorderComorbidWithDisorderService = disorderComorbidWithDisorderService;
        this.disorderIsADisorderService = disorderIsADisorderService;
        this.drugHasIndicationService = drugHasIndicationService;
        this.drugHasTargetService = drugHasTargetService;
        this.geneAssociatedWithDisorderService = geneAssociatedWithDisorderService;
        this.proteinEncodedByService = proteinEncodedByService;
        this.proteinInPathwayService = proteinInPathwayService;
        this.proteinInteractsWithProteinServic = proteinInteractsWithProteinService;

    }

    @Async
    @Scheduled(cron = "${update.interval}", zone = "${update.interval.zone}")
    public void executeDataUpdate() {
        if (dbCommunication.isUpdateInProgress()) {
            log.warn("Update already in progress!");
            return;
        }
        dbCommunication.scheduleUpdate();
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
        log.debug("Current RAM usage: " + (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024)
                + "MB");
        Runtime.getRuntime().gc();
        log.debug("Current RAM usage: " + (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024)
                + "MB");
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

        importService.importIdMaps(collections, cacheDir, true);

        importRepoTrialEdges(collections);

        dbCommunication.setDbLocked(false);

    }

    private void importRepoTrialEdges(HashMap<String, de.exbio.reposcapeweb.db.io.Collection> collections) {
        collections.forEach((k, c) -> {
            if (c instanceof Node)
                return;
            try {
                HashSet<String> attributeDefinition = getAttributeNames(ReaderUtils.getUrlContent(new URL(env.getProperty("url.api.db") + k + "/attributes")));
                boolean updateSuccessful = true;

                switch (k) {
                    case "disorder_comorbid_with_disorder":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, DisorderComorbidWithDisorder.attributes))
                            updateSuccessful = disorderComorbidWithDisorderService.submitUpdates(runEdgeUpdates(DisorderComorbidWithDisorder.class, c, disorderComorbidWithDisorderService::mapIds));
                        break;
                    case "disorder_is_a_disorder":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, DisorderIsADisorder.attributes))
                            updateSuccessful = disorderIsADisorderService.submitUpdates(runEdgeUpdates(DisorderIsADisorder.class, c, disorderIsADisorderService::mapIds));
                        break;
                    case "drug_has_indication":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, DrugHasIndication.attributes))
                            updateSuccessful = drugHasIndicationService.submitUpdates(runEdgeUpdates(DrugHasIndication.class, c, drugHasIndicationService::mapIds));
                        break;
                    case "drug_has_target":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, DrugHasTarget.attributes))
                            updateSuccessful = drugHasTargetService.submitUpdates(runEdgeUpdates(DrugHasTarget.class, c, drugHasTargetService::mapIds));
                        break;
                    case "gene_associated_with_disorder":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, GeneAssociatedWithDisorder.attributes))
                            updateSuccessful = geneAssociatedWithDisorderService.submitUpdates(runEdgeUpdates(GeneAssociatedWithDisorder.class, c, geneAssociatedWithDisorderService::mapIds));
                        break;
                    case "protein_encoded_by":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, ProteinEncodedBy.attributes))
                            updateSuccessful = proteinEncodedByService.submitUpdates(runEdgeUpdates(ProteinEncodedBy.class, c, proteinEncodedByService::mapIds));
                        break;
                    case "protein_in_pathway":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, ProteinInPathway.attributes))
                            updateSuccessful = proteinInPathwayService.submitUpdates(runEdgeUpdates(ProteinInPathway.class, c, proteinInPathwayService::mapIds));
                        break;
                    case "protein_interacts_with_protein":
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, ProteinInteractsWithProtein.attributes))
                            updateSuccessful = proteinInteractsWithProteinServic.submitUpdates(runEdgeUpdates(ProteinInteractsWithProtein.class, c, proteinInteractsWithProteinServic::mapIds));
                        break;

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


    private void importRepoTrialNodes(HashMap<String, de.exbio.reposcapeweb.db.io.Collection> collections) {
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
                        break;
                    }
                    case "pathway": {
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, Pathway.attributes))
                            updateSuccessful = pathwayService.submitUpdates(runNodeUpdates(Pathway.class, c));
                        break;
                    }
                    case "disorder": {
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, Disorder.attributes))
                            updateSuccessful = disorderService.submitUpdates(runNodeUpdates(Disorder.class, c));
                        break;
                    }
                    case "gene": {
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, Gene.attributes))
                            updateSuccessful = geneService.submitUpdates(runNodeUpdates(Gene.class, c));
                        break;
                    }
                    case "protein": {
                        if (updateSuccessful = RepoTrialUtils.validateFormat(attributeDefinition, Protein.attributes))
                            updateSuccessful = proteinService.submitUpdates(runNodeUpdates(Protein.class, c));
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
        BufferedReader br = ReaderUtils.getBasicReader(updateFile);
        String line = "";
        boolean first = true;
        try {
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
        BufferedReader br = ReaderUtils.getBasicReader(updateFile);
        String line = "";
        boolean first = true;
        try {
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
            BufferedReader br = ReaderUtils.getBasicReader(col.getFile());
            int count = 0;
            String line = "";
            try {
                while ((line = br.readLine()) != null) {
                    count++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            }
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

        collections.forEach((k, v) -> v.setFile(FileUtils.download(createUrl(api, k), createFile(destDir, k, fileType))));

    }


    private String createUrl(String api, String k) {
        return api + k + "/all";
    }

    private File createFile(File destDir, String k, String fileType) {
        return new File(destDir, k + fileType);
    }

}
