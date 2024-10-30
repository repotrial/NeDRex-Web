package de.exbio.reposcapeweb;

import de.exbio.reposcapeweb.communication.jobs.JobController;
import de.exbio.reposcapeweb.communication.reponses.WebGraphService;
import de.exbio.reposcapeweb.db.DbCommunicationService;
import de.exbio.reposcapeweb.db.entities.nodes.Disorder;
import de.exbio.reposcapeweb.db.io.ImportService;
import de.exbio.reposcapeweb.db.services.controller.EdgeController;
import de.exbio.reposcapeweb.db.services.controller.NodeController;
import de.exbio.reposcapeweb.db.services.edges.DisorderComorbidWithDisorderService;
import de.exbio.reposcapeweb.db.services.edges.ProteinInteractsWithProteinService;
import de.exbio.reposcapeweb.db.services.nodes.DisorderService;
import de.exbio.reposcapeweb.db.updates.UpdateService;
import de.exbio.reposcapeweb.tools.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

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
    private DisorderService disorderService;
    @Autowired
    private EdgeController edgeController;
    @Autowired
    private NodeController nodeController;
    @Autowired
    private DbCommunicationService dbService;
    @Autowired
    ProteinInteractsWithProteinService proteinInteractsWithProteinService;
    @Autowired
    WebGraphService webGraphService;

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

        toolService.validateTools();
        dbService.setImportInProgress(false);
//        System.out.println(disorderComorbidWithDisorderService.getCount());
//        webGraphService.remapHistory(new File(env.getProperty("path.usr.cache")));
        if (Boolean.parseBoolean(env.getProperty("update.onstartup"))) {
            updateService.scheduleDataUpdate();
        } else {
            importService.importEdges(false);
            log.warn("Startup Database update is deactivated! Activate it by setting 'update.onstartup=true' in the application.properties.");
        }

        jobController.importJobsHistory();
        importService.importHistory();

        if (env.getProperty("update.db-dump").equals("true"))
            updateService.renewDBDumps();
        webGraphService.remapHistory(new File(env.getProperty("path.usr.cache")));

        log.info("Startup took " + (int) ((System.currentTimeMillis() - start) / 1000) + "s");
        log.debug("Current RAM usage: " + (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024)
                + "MB");
        log.info("Loaded " + nodeController.getCount() + " nodes and " + edgeController.getSize() + " edges!");
        updateService.getMetadata();
        log.info("Service can be used!");
//        disorderService.readIdDomainMapsFromDb();
//        disorderComorbidWithDisorderService.buildComorbiditome();
//        disorderComorbidWithDisorderService.importComorbiditome(new File("./"));

    }
}
