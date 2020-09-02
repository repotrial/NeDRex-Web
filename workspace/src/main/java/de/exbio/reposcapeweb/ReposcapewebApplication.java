package de.exbio.reposcapeweb;

import de.exbio.reposcapeweb.db.io.ImportService;
import de.exbio.reposcapeweb.db.statistics.StatisticsRepository;
import de.exbio.reposcapeweb.db.statistics.StatisticsService;
import de.exbio.reposcapeweb.db.updates.UpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class ReposcapewebApplication {

    private Logger log = LoggerFactory.getLogger(ReposcapewebApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ReposcapewebApplication.class, args);

    }

    private final UpdateService updateService;
    private final Environment env;
    private final ImportService importService;
    private final StatisticsRepository statisticsRepository;

    @Autowired
    public ReposcapewebApplication(UpdateService updateService, Environment environment, ImportService importService, StatisticsRepository statisticsRepository) {
        this.updateService = updateService;
        this.importService = importService;
        this.env = environment;
        this.statisticsRepository = statisticsRepository;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void postConstruct() {

        importService.importNodeMaps();
        log.debug("Current RAM usage: " + (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024)
                + "MB");

        if (Boolean.parseBoolean(env.getProperty("update.onstartup")))
            updateService.executeDataUpdate();
        else
            log.warn("Startup Database update is deactivated! Activate it by setting 'update.onstartup=true' in the application.properties.");


        log.info("Service can be used!");
    }
}
