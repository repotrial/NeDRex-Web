package de.exbio.reposcapeweb;

import de.exbio.reposcapeweb.db.updates.UpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@SpringBootApplication
public class ReposcapewebApplication {

    private Logger log = LoggerFactory.getLogger(ReposcapewebApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ReposcapewebApplication.class, args);

    }

    private final UpdateService updateService;
    private final Environment env;

    @Autowired
    public ReposcapewebApplication(UpdateService updateService, Environment environment) {
        this.updateService = updateService;
        this.env = environment;
    }




    @EventListener(ApplicationReadyEvent.class)
    public void postConstruct() {
        if (Boolean.parseBoolean(env.getProperty("update.onstartup")))
            if(updateService.executeDataUpdate())
                log.info("Database successfully updated!");
            else
                log.info("Database could not be updated!");

        else
            log.warn("Startup Database update is deactivated! Activate it by setting 'update.onstartup=true' in the application.properties.");
    }
}
