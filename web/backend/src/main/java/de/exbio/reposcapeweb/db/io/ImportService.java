package de.exbio.reposcapeweb.db.io;

import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.edges.*;
import de.exbio.reposcapeweb.db.services.edges.*;
import de.exbio.reposcapeweb.db.services.nodes.*;
import de.exbio.reposcapeweb.filter.FilterService;
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
                         ProteinInteractsWithProteinService proteinInteractsWithProteinService) {
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
    }

    public void importNodeData() {
        log.info("NodeDataMap import: Start!");
        HashMap<String, Collection> collections = new HashMap<>();
        getCollections(collections);
        log.info("Importing nodeIDs");
        File cacheDir = new File(env.getProperty("path.db.cache"));
        importIdMaps(collections, cacheDir, false);
        log.info("NodeIdMap import: Done!");
        importNodeFilters(collections, new File(cacheDir, "filters"));
    }

    private void prepareCollections(String file, HashMap<String, de.exbio.reposcapeweb.db.io.Collection> collections, boolean typeNode) {
        try {
            BufferedReader br = ReaderUtils.getBasicReader(file);
            String line = "";
            try {
                while ((line = br.readLine()) != null) {
                    if (line.charAt(0) == '#')
                        continue;
                    Collection c = typeNode ? new Node(line) : new Edge(line);
                    collections.put(c.getName(), c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void getCollections(HashMap<String, Collection> collections) {
        prepareCollections(env.getProperty("file.collections.nodes"), collections, true);
        prepareCollections(env.getProperty("file.collections.edges"), collections, false);
    }


    private void importNodeIdMaps(File cacheDir, String table, HashMap<Integer, String> idToDomain, HashMap<String, Integer> domainToString) {
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
                idToDomain.put(id, entry.get(1));
                domainToString.put(entry.get(1), id);
            }
            log.debug("NodeIdMap import: Updated " + table);
        } catch (IOException | NullPointerException e) {
            log.warn("Error on importing nodeidmaps for " + table);
        }
    }


    public void importEdges(boolean allowOnUpdate) {
        log.info("Edge-Data import: Start!");
        HashMap<String, Collection> collections = new HashMap<>();
        getCollections(collections);
        log.info("Importing edgeIds");
        File cacheDir = new File(env.getProperty("path.db.cache"));
        dbCommunication.scheduleImport(allowOnUpdate);
        collections.forEach((k, c) -> {
            if (!(c instanceof Edge))
                return;
            switch (k) {
                case "disorder_comorbid_with_disorder":
                    disorderComorbidWithDisorderService.importEdges();
                    break;
                case "disorder_is_a_disorder":
                    disorderIsADisorderService.importEdges();
                    break;
                case "drug_has_indication":
                    drugHasIndicationService.importEdges();
                    break;
                case "drug_has_target":
                    drugHasTargetService.importEdges();
                    break;
                case "gene_associated_with_disorder":
                    associatedWithDisorderService.importEdges();
                    break;
                case "protein_in_pathway":
                    proteinInPathwayService.importEdges();
                    break;
                case "protein_interacts_with_protein":
                    proteinInteractsWithProteinService.importEdges();
                    break;
                case "protein_encoded_by":
                    proteinEncodedByService.importEdges();
                    break;
            }
        });
        dbCommunication.setImportInProgress(false);
        log.info("Edge-Data import: Done!");
    }


    public void importIdMaps(HashMap<String, de.exbio.reposcapeweb.db.io.Collection> collections, File cacheDir, boolean allowOnUpdate) {
        dbCommunication.scheduleImport(allowOnUpdate);
        collections.forEach((k, c) -> {
            if (!(c instanceof Node))
                return;

            switch (k) {
                case "drug": {
                    importNodeIdMaps(cacheDir, k, drugService.getIdToDomainMap(), drugService.getDomainToIdMap());
                    break;
                }
                case "pathway": {
                    importNodeIdMaps(cacheDir, k, pathwayService.getIdToDomainMap(), pathwayService.getDomainToIdMap());
                    break;
                }
                case "disorder": {
                    importNodeIdMaps(cacheDir, k, disorderService.getIdToDomainMap(), disorderService.getDomainToIdMap());
                    break;
                }
                case "gene": {
                    importNodeIdMaps(cacheDir, k, geneService.getIdToDomainMap(), geneService.getDomainToIdMap());
                    break;
                }
                case "protein": {
                    importNodeIdMaps(cacheDir, k, proteinService.getIdToDomainMap(), proteinService.getDomainToIdMap());
                    break;
                }
            }
        });
        dbCommunication.setImportInProgress(false);

    }

    public void importNodeFilters(HashMap<String, Collection> collections, File cacheDir) {
        collections.forEach((k, c) -> {
            if (!(c instanceof Node))
                return;

            switch (k) {
                case "drug": {
                    drugService.setFilter(filterService.readFromFiles(new File(cacheDir, k)));
                    break;
                }
                case "pathway": {
                    pathwayService.setFilter(filterService.readFromFiles(new File(cacheDir, k)));
                    break;
                }
                case "disorder": {
                    disorderService.setFilter(filterService.readFromFiles(new File(cacheDir, k)));
                    break;
                }
                case "gene": {
                    geneService.setFilter(filterService.readFromFiles(new File(cacheDir, k)));
                    break;
                }
                case "protein": {
                    proteinService.setFilter(filterService.readFromFiles(new File(cacheDir, k)));
                    break;
                }
            }
        });

    }
}
