package de.exbio.reposcapeweb.controller;

import de.exbio.reposcapeweb.db.services.nodes.DrugService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the incoming requests on the RepoScape-WEB application.
 *
 * @author Andreas Maier
 */
@Controller
public class RequestController {

    private final Logger log = LoggerFactory.getLogger(RequestController.class);

    private final DrugService drugService;

    @Autowired
    public RequestController(DrugService drugService) {
        this.drugService = drugService;
    }


//    @RequestMapping(value = "/running", method = RequestMethod.GET)
//    @ResponseBody
//    public String checkRunning(){
//        log.info("Requested Springboot running check!");
//        return "Router is Running!";
//    }

}
