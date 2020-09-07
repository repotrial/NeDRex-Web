package de.exbio.reposcapeweb.db.io;

import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.services.nodes.*;
import de.exbio.reposcapeweb.utils.ReaderUtils;
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

    @Autowired
    public ImportService(Environment env,
                         DbCommunicationService dbCommunication,
                         DrugService drugService,
                         PathwayService pathwayService,
                         DisorderService disorderService,
                         GeneService geneService,
                         ProteinService proteinService) {
        this.env = env;
        this.dbCommunication = dbCommunication;
        this.drugService = drugService;
        this.pathwayService = pathwayService;
        this.disorderService = disorderService;
        this.geneService = geneService;
        this.proteinService = proteinService;
    }

    public void importNodeMaps() {
        log.info("NodeIdMap import: Start!");
        HashMap<String, Collection> collections = new HashMap<>();
        getCollections(collections);

        File cacheDir = new File(env.getProperty("path.db.cache"));
        importIdMaps(collections, cacheDir, false);
        log.info("NodeIdMap import: Done!");
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
                ;
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

}
