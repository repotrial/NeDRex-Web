package de.exbio.reposcapeweb.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    private final TestRepository testRepository;
    private final ObjectMapper objectMapper;
    @Autowired
    public TestService(TestRepository testRepository, ObjectMapper objectMapper) {
        this.testRepository = testRepository;
        this.objectMapper=objectMapper;
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
            if (testRepository.findAll().size() > 0)
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
