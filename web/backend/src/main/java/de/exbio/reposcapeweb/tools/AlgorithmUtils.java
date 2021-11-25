package de.exbio.reposcapeweb.tools;

import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.WriterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

@Service
public class AlgorithmUtils {

    public File dataDir;
    public File scriptDir;
    private Environment env;
    @Autowired
    public AlgorithmUtils(Environment env){
        this.env = env;
        dataDir = new File(env.getProperty("path.external.cache"));
        scriptDir = new File(env.getProperty("path.scripts.dir"));
    }


    public void writeSeedFile(String type, File file, Graph g, HashMap<Integer, Pair<String, String>> domainMap, String prefix, boolean domainName) {
        writeSeedFile(file, g.getNodes().get(Graphs.getNode(type)).keySet(), domainMap, prefix, domainName);
    }

    public void writeSeedFile(String type, File file, Graph g, HashMap<Integer, Pair<String, String>> domainMap, String prefix,String suffix, boolean domainName) {
        writeSeedFile(file, g.getNodes().get(Graphs.getNode(type)).keySet(), domainMap, prefix, suffix, domainName);
    }

    public File getIndexDirectory(){
        File indexDir = new File(dataDir,"index");
        indexDir.mkdirs();
        return indexDir;
    }

    public void writeSeedFile(File file, Collection<Integer> nodeIds, HashMap<Integer, Pair<String, String>> domainMap, String prefix, String suffix, boolean domainName) {
        BufferedWriter bw = WriterUtils.getBasicWriter(file);
        nodeIds.forEach(node -> {
            try {
                bw.write(prefix+ (domainName ?domainMap.get(node).getFirst() : node) +suffix+ "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeSeedFile(File file, Collection<Integer> nodeIds, HashMap<Integer, Pair<String, String>> domainMap, String prefix, boolean domainName) {
       writeSeedFile(file, nodeIds, domainMap, prefix, "", domainName);
    }
}
