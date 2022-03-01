package de.exbio.reposcapeweb.db.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.configs.DBConfig;
import de.exbio.reposcapeweb.configs.VisConfig;
import de.exbio.reposcapeweb.configs.schema.Config;
import de.exbio.reposcapeweb.configs.schema.NodeConfig;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.history.HistoryController;
import de.exbio.reposcapeweb.db.services.NodeService;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.db.services.edges.*;
import de.exbio.reposcapeweb.db.services.nodes.*;
import de.exbio.reposcapeweb.filter.FilterService;
import de.exbio.reposcapeweb.filter.NodeFilter;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.ReaderUtils;
import de.exbio.reposcapeweb.utils.RepoTrialUtils;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class ImportService {

    private final Logger log = LoggerFactory.getLogger(ImportService.class);

    private Environment env;
    private DbCommunicationService dbCommunication;

    private final DisorderService disorderService;
    private final DrugService drugService;
    private final GeneService geneService;
    private final PathwayService pathwayService;
    private final ProteinService proteinService;

    private final FilterService filterService;

    private final DisorderComorbidWithDisorderService disorderComorbidWithDisorderService;
    private final DisorderIsADisorderService disorderIsADisorderService;
    private final DrugHasIndicationService drugHasIndicationService;
    private final DrugHasTargetService drugHasTargetService;
    private final AssociatedWithDisorderService associatedWithDisorderService;
    private final ProteinEncodedByService proteinEncodedByService;
    private final ProteinInPathwayService proteinInPathwayService;
    private final ProteinInteractsWithProteinService proteinInteractsWithProteinService;
    private final DrugHasContraindicationService drugHasContraindicationService;
    private final EdgeController edgeController;
    private final NodeController nodeController;

    private final File dbCacheDir;

    private final ObjectMapper objectMapper;


    private final HistoryController historyController;

    @Autowired
    public ImportService(Environment env,
                         DbCommunicationService dbCommunication,
                         DrugService drugService,
                         PathwayService pathwayService,
                         DisorderService disorderService,
                         GeneService geneService,
                         FilterService filterService,
                         ProteinService proteinService,
                         DisorderComorbidWithDisorderService disorderComorbidWithDisorderService,
                         DisorderIsADisorderService disorderIsADisorderService,
                         DrugHasIndicationService drugHasIndicationService,
                         DrugHasTargetService drugHasTargetService,
                         AssociatedWithDisorderService associatedWithDisorderService,
                         ProteinEncodedByService proteinEncodedByService,
                         ProteinInPathwayService proteinInPathwayService,
                         ProteinInteractsWithProteinService proteinInteractsWithProteinService,
                         HistoryController historyController,
                         ObjectMapper objectMapper,
                         DrugHasContraindicationService drugHasContraindicationService,
                         EdgeController edgeController,
                         NodeController nodeController
    ) {
        this.env = env;
        this.dbCommunication = dbCommunication;
        this.drugService = drugService;
        this.filterService = filterService;
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
        this.historyController = historyController;
        this.objectMapper = objectMapper;
        this.edgeController = edgeController;
        this.nodeController = nodeController;

        this.dbCacheDir = new File(env.getProperty("path.db.cache"));
    }

    public void importNodeData() {
        log.info("NodeDataMap import: Start!");
        File conf = new File(env.getProperty("file.db.config"));
        try {
            DBConfig.importConfig(conf, objectMapper.readValue(conf, Config.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        File visconf = new File(env.getProperty("file.vis.config"));
        try {
            VisConfig.importConfig(visconf, objectMapper.readValue(visconf, Object.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        nodeController.setUp();
        edgeController.setUp();
        log.info("Importing nodeIDs");
        importIdMaps(dbCacheDir, false);
        log.info("NodeIdMap import: Done!");
        importNodeFilters(new File(dbCacheDir, "filters"));
    }


    public void importHistory() {
        historyController.importHistory();
    }


    private boolean importNodeIdMaps(File cacheDir, String table, HashMap<Integer, Pair<String, String>> idToDomain, HashMap<String, Integer> domainToString) {
        File nodeDir = new File(cacheDir, "nodes");

        File f = new File(nodeDir, table + ".map");
        String line = "";

        idToDomain.clear();
        domainToString.clear();

        try {
            BufferedReader br = ReaderUtils.getBasicReader(f);
            while ((line = br.readLine()) != null) {
                if (line.charAt(0) == '#')
                    continue;
                ArrayList<String> entry = StringUtils.split(line, '\t', 2);
                int id = Integer.parseInt(entry.get(0));
                idToDomain.put(id, new Pair<>(entry.get(1), entry.get(2)));
                domainToString.put(entry.get(1), id);
            }
            log.debug("NodeIdMap import: Updated " + table);
            return true;
        } catch (IOException | NullPointerException e) {
            log.warn("Error on importing nodeidmaps for " + table);
            return false;
        }
    }


    public void importEdges(boolean allowOnUpdate) {
        log.info("Edge-Data import: Start!");
        log.info("Importing edgeIds");
//        File cacheDir = new File(env.getProperty("path.db.cache"));
        dbCommunication.scheduleImport(allowOnUpdate);
        DBConfig.getConfig().edges.forEach(edge -> {
            switch (edge.mapsTo) {
                case "DisorderComorbidity" -> disorderComorbidWithDisorderService.importEdges();
                case "DisorderHierarchy" -> disorderIsADisorderService.importEdges();
                case "DrugIndication" -> drugHasIndicationService.importEdges();
                case "DrugTargetProtein" -> drugHasTargetService.importEdges();
                case "GeneAssociatedWithDisorder" -> associatedWithDisorderService.importEdges();
                case "ProteinPathway" -> proteinInPathwayService.importEdges();
                case "ProteinProteinInteraction" -> proteinInteractsWithProteinService.importEdges();
                case "ProteinEncodedBy" -> proteinEncodedByService.importEdges();
                case "DrugContraindication" -> drugHasContraindicationService.importEdges();
            }
        });
        dbCommunication.setImportInProgress(false);
        log.info("Edge-Data import: Done!");
    }


    public void importIdMaps(File cacheDir, boolean allowOnUpdate) {
        dbCommunication.scheduleImport(allowOnUpdate);
        File nodeCacheDir = new File(dbCacheDir, "nodes");
        DBConfig.getConfig().nodes.forEach(node -> {
            NodeService s = null;
            switch (node.name) {
                case "drug" -> {
                    s = drugService;
                    break;
                }
                case "pathway" -> {
                    s = pathwayService;
                    break;
                }
                case "disorder" -> {
                    s = disorderService;
                    break;
                }
                case "gene" -> {
                    s = geneService;
                    break;
                }
                case "protein" -> {
                    s = proteinService;
                    break;
                }
            }
            if (s != null) {
                if (!importNodeIdMaps(cacheDir, node.label, s.getIdToDomainMap(), s.getDomainToIdMap())) {
                    log.info("Fixing nodeidmaps for " + node.label);
                    s.readIdDomainMapsFromDb();
                    RepoTrialUtils.writeNodeMap(new File(nodeCacheDir, node.label + ".map"), s.getIdToDomainMap());
                    log.info("Done fixing nodeidmaps for " + node.label);
                }
            }
        });
        dbCommunication.setImportInProgress(false);
    }

    public void importNodeFilters(File cacheDir) {
        cacheDir.mkdirs();
        DBConfig.getConfig().nodes.forEach(node -> {
            NodeService s = null;
            switch (node.name) {
                case "drug" -> s = drugService;
                case "pathway" -> s = pathwayService;
                case "disorder" -> s = disorderService;
                case "gene" -> s = geneService;
                case "protein" -> s = proteinService;
            }
            File cached = new File(cacheDir, node.label);
            NodeFilter nf = filterService.readFromFiles(cached);
            if (nf == null || nf.size() == 0) {
                log.info("Fixing filter cache for " + node.label);
                s.readFilterFromDB();
                if (node.name.equals("disorder"))
                    disorderIsADisorderService.createDistinctFilters();
                filterService.writeToFile(s.getFilter(), cached);
                log.info("Done fixing.");
            } else
                s.setFilter(nf);
        });

    }

    public void updateNodeData(boolean allowOnUpdate, boolean edgesReady) {
        log.info("NodeDataMap update: Start!");
        if (DBConfig.getConfig() == null) {
            File conf = new File(env.getProperty("file.db.config"));
            try {
                DBConfig.importConfig(conf, objectMapper.readValue(conf, Config.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
            File visconf = new File(env.getProperty("file.vis.config"));
            try {
                VisConfig.importConfig(visconf, objectMapper.readValue(visconf, Object.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        nodeController.setUp();
//        edgeController.setUp();
        log.info("Updating nodeIDs");
        updateIdMaps(allowOnUpdate);
        log.info("NodeIdMap update: Done!");
        updateNodeFilters(new File(dbCacheDir, "filters"), edgesReady);

    }

    private void updateNodeFilters(File cacheDir, boolean edgesReady) {
        cacheDir.mkdirs();
        DBConfig.getConfig().nodes.forEach(node -> {
            NodeService s = null;
            switch (node.name) {
                case "drug" -> s = drugService;
                case "pathway" -> s = pathwayService;
                case "disorder" -> s = disorderService;
                case "gene" -> s = geneService;
                case "protein" -> s = proteinService;
            }
            File cached = new File(cacheDir, node.label);
            log.info("Updating filter cache for " + node.label);
            s.readFilterFromDB();
            if (node.name.equals("disorder") && edgesReady)
                disorderIsADisorderService.createDistinctFilters();
            filterService.writeToFile(s.getFilter(), cached);
            log.info("Done updating.");
        });
    }

    public void updateDisorderFilters() {
        File cacheDir = new File(dbCacheDir, "filters");
        cacheDir.mkdirs();
        NodeConfig node = DBConfig.getConfig().nodes.get(Graphs.getNode("disorder"));
        NodeService s = disorderService;
        File cached = new File(cacheDir, node.label);
        log.info("Updating filter cache for " + node.label);
        s.readFilterFromDB();
        if (node.name.equals("disorder"))
            disorderIsADisorderService.createDistinctFilters();
        filterService.writeToFile(s.getFilter(), cached);
        log.info("Done updating.");
    }

    private void updateIdMaps(boolean allowOnUpdate) {
        if (dbCommunication.isUpdateInProgress() && !allowOnUpdate) {
            log.error("DB update in progress and IDMap updated was prohibited!");
            return;
        }
        File nodeCacheDir = new File(dbCacheDir, "nodes");
        DBConfig.getConfig().nodes.forEach(node -> {
            NodeService s = null;
            switch (node.name) {
                case "drug" -> {
                    s = drugService;
                    break;
                }
                case "pathway" -> {
                    s = pathwayService;
                    break;
                }
                case "disorder" -> {
                    s = disorderService;
                    break;
                }
                case "gene" -> {
                    s = geneService;
                    break;
                }
                case "protein" -> {
                    s = proteinService;
                    break;
                }
            }
            if (s != null) {
                log.info("Updating nodeIDMaps for " + node.label);
                s.readIdDomainMapsFromDb();
                RepoTrialUtils.writeNodeMap(new File(nodeCacheDir, node.label + ".map"), s.getIdToDomainMap());
                log.info("Done updating nodeIDMaps for " + node.label + ": "+s.getDomainToIdMap().size()+" entries!");
            }
        });
    }
}
