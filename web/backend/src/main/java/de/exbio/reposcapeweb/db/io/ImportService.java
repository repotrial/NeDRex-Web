package de.exbio.reposcapeweb.db.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.configs.DBConfig;
import de.exbio.reposcapeweb.configs.schema.Config;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.edges.*;
import de.exbio.reposcapeweb.db.history.HistoryController;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.db.services.edges.*;
import de.exbio.reposcapeweb.db.services.nodes.*;
import de.exbio.reposcapeweb.filter.FilterService;
import de.exbio.reposcapeweb.tools.ToolService;
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
import java.io.FileNotFoundException;
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
        this.drugHasContraindicationService=drugHasContraindicationService;
        this.historyController = historyController;
        this.objectMapper=objectMapper;
        this.edgeController = edgeController;
        this.nodeController=nodeController;
    }

    public void importNodeData() {
        log.info("NodeDataMap import: Start!");
        File conf = new File(env.getProperty("file.db.config"));
        try {
            DBConfig.importConfig(conf,objectMapper.readValue(conf, Config.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        nodeController.setUp();
        edgeController.setUp();
//        HashMap<String, Collection> collections = new HashMap<>();
//        getCollections(collections);
        log.info("Importing nodeIDs");
        File cacheDir = new File(env.getProperty("path.db.cache"));
        importIdMaps(cacheDir, false);
        log.info("NodeIdMap import: Done!");
        importNodeFilters(new File(cacheDir, "filters"));
    }


    public void importHistory(){
        historyController.importHistory();
    }


    private void importNodeIdMaps(File cacheDir, String table, HashMap<Integer, Pair<String,String>> idToDomain, HashMap<String, Integer> domainToString) {
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
                idToDomain.put(id, new Pair<>(entry.get(1),entry.get(2)));
                domainToString.put(entry.get(1), id);
            }
            log.debug("NodeIdMap import: Updated " + table);
        } catch (IOException | NullPointerException e) {
            log.warn("Error on importing nodeidmaps for " + table);
        }
    }


    public void importEdges(boolean allowOnUpdate) {
        log.info("Edge-Data import: Start!");
        log.info("Importing edgeIds");
        File cacheDir = new File(env.getProperty("path.db.cache"));
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
        DBConfig.getConfig().nodes.forEach(node -> {
            switch (node.name) {
                case "drug": {
                    importNodeIdMaps(cacheDir, node.name, drugService.getIdToDomainMap(), drugService.getDomainToIdMap());
                    break;
                }
                case "pathway": {
                    importNodeIdMaps(cacheDir, node.name, pathwayService.getIdToDomainMap(), pathwayService.getDomainToIdMap());
                    break;
                }
                case "disorder": {
                    importNodeIdMaps(cacheDir, node.name, disorderService.getIdToDomainMap(), disorderService.getDomainToIdMap());
                    break;
                }
                case "gene": {
                    importNodeIdMaps(cacheDir, node.name, geneService.getIdToDomainMap(), geneService.getDomainToIdMap());
                    break;
                }
                case "protein": {
                    importNodeIdMaps(cacheDir, node.name, proteinService.getIdToDomainMap(), proteinService.getDomainToIdMap());
                    break;
                }
            }
        });
        dbCommunication.setImportInProgress(false);

    }

    public void importNodeFilters( File cacheDir) {
        DBConfig.getConfig().nodes.forEach(node -> {

            switch (node.name) {
                case "drug": {
                    drugService.setFilter(filterService.readFromFiles(new File(cacheDir, node.name)));
                    break;
                }
                case "pathway": {
                    pathwayService.setFilter(filterService.readFromFiles(new File(cacheDir, node.name)));
                    break;
                }
                case "disorder": {
                    disorderService.setFilter(filterService.readFromFiles(new File(cacheDir, node.name)));
                    break;
                }
                case "gene": {
                    geneService.setFilter(filterService.readFromFiles(new File(cacheDir, node.name)));
                    break;
                }
                case "protein": {
                    proteinService.setFilter(filterService.readFromFiles(new File(cacheDir, node.name)));
                    break;
                }
            }
        });

    }
}
