package de.exbio.reposcapeweb.communication.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.utils.ProcessUtils;
import de.exbio.reposcapeweb.utils.WriterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Service
public class NedrexService {

    Environment env;
    ObjectMapper objectMapper;

    @Autowired
    public NedrexService(Environment env, ObjectMapper objectMapper) {
        this.env = env;
        this.objectMapper = objectMapper;
    }

    public String getAPI(){
        return env.getProperty("url.api.validation")!=null ? env.getProperty("url.api.validation"): env.getProperty("url.api.db");
    }

    public String get(String path) {
        StringBuffer sb = new StringBuffer();
        try {
            ProcessUtils.executeProcessWait(new ProcessBuilder("curl", "-s", getAPI() +(path.charAt(0)=='/' ? path.substring(1):path)), sb);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    public String post(String path, HashMap<String, Object> data) {
        StringBuffer sb = new StringBuffer();
        try {
            File input = File.createTempFile("nedrex_post",".txt");
            BufferedWriter bw = WriterUtils.getBasicWriter(input);
            bw.write(toJson(data));
            bw.close();
            ProcessUtils.executeProcessWait(new ProcessBuilder("curl", "-s", "-X", "POST", "-H", "Content-Type: application/json", "-d", "@" + input.getAbsolutePath() ,getAPI()+(path.charAt(0)=='/' ? path.substring(1):path)), sb);
            input.delete();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


}
