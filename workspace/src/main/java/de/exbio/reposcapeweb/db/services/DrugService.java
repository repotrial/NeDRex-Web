package de.exbio.reposcapeweb.db.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.controller.RequestController;
import de.exbio.reposcapeweb.db.entities.nodes.Drug;
import de.exbio.reposcapeweb.db.repositories.DrugRepository;
import de.exbio.reposcapeweb.utils.ReaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

@Service
public class DrugService {

    private Logger log = LoggerFactory.getLogger(DrugService.class);
    private final DrugRepository drugRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DrugService(DrugRepository drugRepository, ObjectMapper objectMapper) {
        this.drugRepository = drugRepository;
        this.objectMapper = objectMapper;
    }

    public void importUpdates(File updates) {
        BufferedReader br = ReaderUtils.getBasicReader(updates);
        String line = "";
        boolean first = true;
        try {
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    if (line.charAt(0) == '[')
                        line = line.substring(1);
                }
                Drug d = objectMapper.readValue(line, Drug.class);
                Drug.entries.put(d.getPrimaryDomainId(), d);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        drugRepository.saveAll(Drug.entries.values());
    }
}
