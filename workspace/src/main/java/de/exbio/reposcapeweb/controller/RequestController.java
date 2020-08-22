package de.exbio.reposcapeweb.controller;

import de.exbio.reposcapeweb.db.TestRepository;
import de.exbio.reposcapeweb.db.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RequestController {

    private Logger log = LoggerFactory.getLogger(RequestController.class);

    private final TestService testService;

    public RequestController(@Autowired TestService testService){
        this.testService=testService;
    }

    @RequestMapping(value = "/running", method = RequestMethod.GET)
    @ResponseBody
    public String checkRunning(){
        log.info("Requested Springboot running check!");
        return "Router is Running!";
    }

    @RequestMapping(value = "/dbread", method = RequestMethod.GET)
    @ResponseBody
    public String checkDBRead(){
        log.info("Requested DB reading check!");
        return "Read access from DB:"+testService.testRead();
    }

    @RequestMapping(value = "/dbwrite", method = RequestMethod.GET)
    @ResponseBody
    public String checkDBWrite(){
        log.info("Requested DB writing check!");
        return "Write access to DB:"+testService.testWrite();
    }

    @RequestMapping(value = "/dbdump", method = RequestMethod.GET)
    @ResponseBody
    public String checkDBDump(){
        log.info("Requested DB dumping check!");
        return testService.dump();
    }

}
