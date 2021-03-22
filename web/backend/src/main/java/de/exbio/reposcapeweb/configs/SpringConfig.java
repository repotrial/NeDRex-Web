package de.exbio.reposcapeweb.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * A configuration class for the backend server.
 * Asynchronous and scheduled execution is activated by @EnableAsync and @EnableScheduling
 *
 * @author Andreas Maier
 */
@Configuration
@EnableAsync
@EnableScheduling
public class SpringConfig {

}
