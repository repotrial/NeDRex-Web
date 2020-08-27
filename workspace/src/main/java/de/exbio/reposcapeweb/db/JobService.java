package de.exbio.reposcapeweb.db;

import de.exbio.reposcapeweb.controller.RequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

//@Service
public class JobService {

    private Logger log = LoggerFactory.getLogger(JobService.class);
//    private final JobRepository jobRepository;

//    @Autowired
//    public JobService(JobRepository jobRepository){
//        this.jobRepository = jobRepository;
//    }


//    public JobDoc getJob(String id){
//        JobDoc result = jobRepository.findById(id).orElse(null);
//        try{
//            result.getId();
//        } catch (NullPointerException e){
//            log.warn("Job not finished!");
//        }
//        return result;
//    }

    public String submitJob(TestDoc input){
        String jobId = UUID.randomUUID().toString();
        ProcessBuilder pb = new ProcessBuilder("/home/andimajore/projects/RepoScapeWeb/workspace/scripts/testJob.sh",input.getId(),jobId);
        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jobId;
    }


}
