package de.exbio.reposcapeweb.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    private Logger log = LoggerFactory.getLogger(TestService.class);
    private final TestRepository testRepository;
    private final ObjectMapper objectMapper;
    @Autowired
    public TestService(TestRepository testRepository, ObjectMapper objectMapper) {
        this.testRepository = testRepository;
        this.objectMapper=objectMapper;
    }

    public TestDoc getJob(String id){
        TestDoc result = testRepository.findById(id).orElse(null);
        try{
            result.getId();
        } catch (NullPointerException e){
            log.warn("Job not finished!");
        }
        return result;
    }


    public boolean testWrite() {
        try {
            testRepository.save(new TestDoc("test_" + System.currentTimeMillis()));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean testRead() {
        try {
            if (testRepository.findAll().iterator().hasNext())
                return true;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String dump() {
        try {
            return objectMapper.writeValueAsString(testRepository.findAll());
        } catch (Exception e) {
            return "";
        }
    }

}
