package de.exbio.reposcapeweb.db.updates;

import de.exbio.reposcapeweb.db.entities.nodes.Drug;
import de.exbio.reposcapeweb.db.services.DrugService;
import de.exbio.reposcapeweb.utils.FileUtils;
import de.exbio.reposcapeweb.utils.ReaderUtils;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class UpdateService {
    private final Logger log = LoggerFactory.getLogger(UpdateService.class);

    private final Environment env;
    private final DrugService drugService;

    @Autowired
    public UpdateService(Environment environment, DrugService drugService) {
        this.env = environment;
        this.drugService = drugService;
    }


    public boolean executeDataUpdate() {
        String url = env.getProperty("url.api.db");
        File cacheDir = new File(env.getProperty("path.db.cache"));
        try {
            log.info("Executing DB update.");
            update(url, cacheDir);
        } catch (Exception e) {
            log.error("Update could not be executed correctly: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Terminated because of update failure caused by" + e.getClass().toString().substring(5));
        }
        return true;
    }

    private void update(String url, File cacheDir) {
        log.debug("Executing Database update from " + url + " to cache-directory " + cacheDir.getAbsolutePath());

        HashMap<String, Collection> collections = new HashMap<>();

        File updateDir = new File(cacheDir, "update_" + System.currentTimeMillis());
        updateDir.deleteOnExit();
        String fileType = env.getProperty("file.collections.filetype");
        fileType = fileType.charAt(0) == '.' ? fileType : '.' + fileType;

        log.info("Downloading database files!");
        downloadUpdates(url, updateDir, fileType, collections);
        updateDB(collections);
        overrideOldData(cacheDir, collections);

        updateDir.delete();
    }

    private void updateDB(HashMap<String, Collection> collections) {
        //TODO implement
        //TODO just for dev
//        reformatCollections(collections);

        identifyUpdates(collections);
    }

    private void identifyUpdates(HashMap<String, Collection> collections) {
        collections.forEach((k, c) -> {
            try {
                String attributeDefinition = ReaderUtils.getUrlContent(new URL(env.getProperty("url.api.db") + k + "/attributes"));
                boolean formatValidated = false;
                switch (k) {
                    case "drug": {
                        //TODO check diff for update -> read inputstream -> update/remove/insert
                        formatValidated = Drug.validateFormat(getAttributeNames(attributeDefinition));
                        drugService.importUpdates(c.getFile());
                    }
                    //TODO add all other types
                    //TODO create statistics for inserts/updates/removals
                    //TODO validate edges and create statistics
                }
                if (!formatValidated)
                    log.warn("Format validation for " + k + ": Error!");
                else
                    log.debug("Format validation for " + k + ": Success!");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

    }

//    private HashMap<String,String> getAttributes(String values){
//        HashMap<String,String> map = new HashMap<>();
//        System.out.println(StringUtils.split(values, ",\""));
//    }

    private HashSet<String> getAttributeNames(String content) {
        HashSet<String> attributes = new HashSet<>();
        StringUtils.split(content.substring(1, content.length() - 2), ",").forEach(a -> attributes.add(a.substring(1, a.length() - 1)));
        return attributes;
    }

    private void reformatCollections(HashMap<String, Collection> collections) {
        collections.values().forEach(col -> {
            FileUtils.formatJson(col.getFile());
            BufferedReader br = ReaderUtils.getBasicReader(col.getFile());
            int count = 0;
            String line = "";
            try {
                while ((line = br.readLine()) != null) {
                    count++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            }
            int officialCount = getCountFromDetails(col.getName());
            log.debug(col.getName() + " contains " + count + " entries!");
            if (count != officialCount) {
                log.error("Entry count for " + col.getName() + " does not match to official number from repotrial (" + count + " vs " + officialCount + ")");
                throw new RuntimeException("Error while validating the entitiy counts. Maybe file format has changed.");
            }
        });
        log.info("Validation of entity count in cached files!");
    }

    private int getCountFromDetails(String name) {
        try {
            BufferedReader br = ReaderUtils.getBufferedReader(new URL(env.getProperty("url.api.db") + name + "/details"));
            String line = "";
            while ((line = br.readLine()) != null) {
                for (String l : StringUtils.split(line, ","))
                    if (l.startsWith("\"count"))
                        return Integer.parseInt(l.substring(8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void overrideOldData(File cacheDir, HashMap<String, Collection> collections) {
        collections.forEach((k, v) -> {
            try {
                log.debug("Moving " + v.getFile().toPath() + " to " + new File(cacheDir, v.getFile().getName()).toPath());
                v.setFile(Files.move(v.getFile().toPath(), new File(cacheDir, v.getFile().getName()).toPath(), REPLACE_EXISTING).toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void downloadUpdates(String api, File destDir, String fileType, HashMap<String, Collection> collections) {
        //TODO let downloads happen in background
        destDir.mkdirs();

        prepareCollections(env.getProperty("file.collections.nodes"), collections, true);
        prepareCollections(env.getProperty("file.collections.edges"), collections, false);

//        collections.forEach((k, v) -> v.setFile(FileUtils.download(createUrl(api, k), createFile(destDir, k, fileType))));

        //TODO just for dev
        collections.forEach((k, v) -> v.setFile(createFile(new File(env.getProperty("path.db.cache")), k, fileType)));

    }

    private String createUrl(String api, String k) {
        return api + k + "/all";
    }

    private File createFile(File destDir, String k, String fileType) {
        return new File(destDir, k + fileType);
    }

    private void prepareCollections(String file, HashMap<String, Collection> collections, boolean typeNode) {
        BufferedReader br = ReaderUtils.getBasicReader(file);
        String line = "";
        try {
            while ((line = br.readLine()) != null) {
                if (line.charAt(0) == '#')
                    continue;
                Collection c = typeNode ? new Node(line) : new Edge(line);
                collections.put(c.getName(), c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
