package de.exbio.reposcapeweb.controller;

import de.exbio.reposcapeweb.db.JobDoc;
import de.exbio.reposcapeweb.db.JobService;
import de.exbio.reposcapeweb.db.TestRepository;
import de.exbio.reposcapeweb.db.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RequestController {

    private Logger log = LoggerFactory.getLogger(RequestController.class);

    private final TestService testService;
    private final JobService jobService;

    public RequestController(@Autowired TestService testService, @Autowired JobService jobService){
        this.testService=testService;
        this.jobService = jobService;
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

    @RequestMapping(value = "/submitJob", method = RequestMethod.GET)
    @ResponseBody
    public String submitJob(@RequestParam("id") String uid){
        String jobId = jobService.submitJob(testService.getJob(uid));
        log.info("Submitted "+uid+" as job "+jobId+"!");
        return "Register for jobUpdate: "+jobId;
    }

    @RequestMapping(value = "/finishedJob", method = RequestMethod.GET)
    @ResponseBody
    public void registerFinishedJob(@RequestParam("j") String uid){
        log.debug("j="+uid);
        JobDoc doc= jobService.getJob(uid);
        log.info("Got job "+uid+" with Document: "+doc.getDoc()+"!");
    }

}
