package de.exbio.reposcapeweb.communication.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.exbio.reposcapeweb.utils.ProcessUtils;
import de.exbio.reposcapeweb.utils.WriterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class NedrexService {

    Environment env;
    ObjectMapper objectMapper;

    private String APIKey;

    @Autowired
    public NedrexService(Environment env, ObjectMapper objectMapper) {
        this.env = env;
        this.objectMapper = objectMapper;
        this.initAPIKey();
    }

    public String getAPI(){
        return env.getProperty("url.api.validation")!=null ? env.getProperty("url.api.validation"): env.getProperty("url.api.db");
    }

    public void initAPIKey(){
//        TODO figure out when to reload the API key
        URL url = null;
        try {
            url = new URL(getAPI()+"/admin/api_key/generate");
            BufferedReader in = getBufferedReader(url);
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            this.APIKey = content.toString().replace('"', ' ').trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static BufferedReader getBufferedReader(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setDoOutput(true);
        String payload = "{\"accept_eula\": true}";
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        return in;
    }

    public String getAPIKey(){
        return this.APIKey;
    }

    public String get(String path) {
        StringBuffer sb = new StringBuffer();
        try {
            ProcessUtils.executeProcessWait(new ProcessBuilder("curl", "-s", "-k","-H", "@{'apikey' = '"+this.getAPIKey()+"'}", getAPI() +(path.charAt(0)=='/' ? path.substring(1):path)), sb);
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
            ProcessUtils.executeProcessWait(new ProcessBuilder("curl", "-s", "-X", "POST","-k", "-H", "Content-Type: application/json", "-H", "@{'apikey' = '"+this.getAPIKey()+"'}","-d", "@" + input.getAbsolutePath() ,getAPI()+(path.charAt(0)=='/' ? path.substring(1):path)), sb);
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
