package de.exbio.reposcapeweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.jobs.JobController;
import de.exbio.reposcapeweb.communication.reponses.WebGraphService;
import de.exbio.reposcapeweb.db.entities.edges.DrugHasTargetProtein;
import de.exbio.reposcapeweb.db.entities.edges.ProteinAssociatedWithDisorder;
import de.exbio.reposcapeweb.db.entities.nodes.*;
import de.exbio.reposcapeweb.db.history.HistoryController;
import de.exbio.reposcapeweb.db.io.ImportService;
import de.exbio.reposcapeweb.db.repositories.edges.DrugHasTargetProteinRepository;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import de.exbio.reposcapeweb.db.updates.UpdateService;
import de.exbio.reposcapeweb.filter.FilterService;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.StringUtils;
import de.exbio.reposcapeweb.utils.WriterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.support.incrementer.HanaSequenceMaxValueIncrementer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

@SpringBootApplication
public class ReposcapewebApplication {

    private Logger log = LoggerFactory.getLogger(ReposcapewebApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ReposcapewebApplication.class, args);
    }

    private final UpdateService updateService;
    private final Environment env;
    private final ImportService importService;
    private final ToolService toolService;
    private final JobController jobController;
    private final EdgeController edgeController;
    private final NodeController nodeController;

    @Autowired
    public ReposcapewebApplication(JobController jobController, NodeController nodeController, ObjectMapper objectMapper, EdgeController edgeController, DisorderService disorderService, UpdateService updateService, Environment environment, ImportService importService, FilterService filterService, ToolService toolService, WebGraphService graphService) {
        this.updateService = updateService;
        this.importService = importService;
        this.env = environment;
        this.toolService = toolService;
        this.jobController = jobController;
        this.edgeController = edgeController;
        this.nodeController = nodeController;

    }


    @Autowired
    DrugHasTargetProteinRepository drugHasTargetProteinRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void postConstruct() {

        Graphs.setUp();

        toolService.validateTools();

        jobController.importJobsHistory();
        importService.importHistory();
        importService.importNodeData();

        if (Boolean.parseBoolean(env.getProperty("update.onstartup"))) {
            updateService.scheduleDataUpdate();
        } else {
            importService.importEdges(false);
            log.warn("Startup Database update is deactivated! Activate it by setting 'update.onstartup=true' in the application.properties.");
        }
        log.debug("Current RAM usage: " + (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024)
                + "MB");
        toolService.createInteractionFiles();


        log.info("Current RAM usage: " + (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024)
                + "MB");


        log.info("Service can be used!");

    }
}
