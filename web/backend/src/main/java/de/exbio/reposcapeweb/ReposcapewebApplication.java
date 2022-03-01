package de.exbio.reposcapeweb;

import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.jobs.JobController;
import de.exbio.reposcapeweb.communication.reponses.Suggestions;
import de.exbio.reposcapeweb.communication.reponses.WebGraphService;
import de.exbio.reposcapeweb.communication.requests.SuggestionRequest;
import de.exbio.reposcapeweb.configs.DBConfig;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.edges.DrugHasIndication;
import de.exbio.reposcapeweb.db.entities.edges.ProteinAssociatedWithDisorder;
import de.exbio.reposcapeweb.db.entities.edges.ProteinInteractsWithProtein;
import de.exbio.reposcapeweb.db.entities.nodes.Disorder;
import de.exbio.reposcapeweb.db.io.ImportService;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.db.services.edges.ProteinInteractsWithProteinService;
import de.exbio.reposcapeweb.db.updates.UpdateService;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.StringUtils;
import de.exbio.reposcapeweb.utils.WriterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class ReposcapewebApplication extends SpringBootServletInitializer {

    private Logger log = LoggerFactory.getLogger(ReposcapewebApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ReposcapewebApplication.class, args);
    }

    @Autowired
    private UpdateService updateService;
    @Autowired
    private Environment env;
    @Autowired
    private ImportService importService;
    @Autowired
    private ToolService toolService;
    @Autowired
    private JobController jobController;
    @Autowired
    private EdgeController edgeController;
    @Autowired
    private NodeController nodeController;
    @Autowired
    private DbCommunicationService dbService;
    @Autowired
    WebGraphService webGraphService;

//    @Autowired
//    public ReposcapewebApplication(DbCommunicationService dbService, ProteinInteractsWithProteinService proteinInteractsWithProteinService, JobController jobController, NodeController nodeController, ObjectMapper objectMapper, EdgeController edgeController, DisorderService disorderService, UpdateService updateService, Environment environment, ImportService importService, FilterService filterService, ToolService toolService, WebGraphService graphService) {
//        this.updateService = updateService;
//        this.importService = importService;
//        this.env = environment;
//        this.toolService = toolService;
//        this.jobController = jobController;
//        this.edgeController = edgeController;
//        this.nodeController = nodeController;
//        this.proteinInteractsWithProteinService = proteinInteractsWithProteinService;
//        this.dbService = dbService;
//
//    }

    public int hash(String params, TreeSet<Integer> ids, String method) {
        return Objects.hash(params, Arrays.hashCode(ids.toArray()), method);
    }

    @Autowired
    ProteinInteractsWithProteinService ppiService;

    @EventListener(ApplicationReadyEvent.class)
    public void postConstruct() throws IOException {
        long start = System.currentTimeMillis();
        updateService.readMetadata();
        dbService.setImportInProgress(true);
        importService.importNodeData();

        //TODO maybe move importJob and importHistory to after update?
        jobController.importJobsHistory();
        importService.importHistory();


        toolService.validateTools();
        dbService.setImportInProgress(false);
        if (Boolean.parseBoolean(env.getProperty("update.onstartup"))) {
            updateService.scheduleDataUpdate();
        } else {
            importService.importEdges(false);
            log.warn("Startup Database update is deactivated! Activate it by setting 'update.onstartup=true' in the application.properties.");
        }

        if (env.getProperty("update.db-dump").equals("true"))
            updateService.renewDBDumps();
        log.info("Startup took " + (int) ((System.currentTimeMillis() - start) / 1000) + "s");
        log.debug("Current RAM usage: " + (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024)
                + "MB");
        log.info("Loaded " + nodeController.getCount() + " nodes and " + edgeController.getSize() + " edges!");
        log.info("Service can be used!");


    }
}
