package de.exbio.reposcapeweb.tools;

import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.cache.Graphs;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.jobs.JobRequest;
import de.exbio.reposcapeweb.db.entities.edges.ProteinInteractsWithProtein;
import de.exbio.reposcapeweb.db.services.edges.ProteinInteractsWithProteinService;
import de.exbio.reposcapeweb.utils.ProcessUtils;
import de.exbio.reposcapeweb.utils.ReaderUtils;
import de.exbio.reposcapeweb.utils.StringUtils;
import de.exbio.reposcapeweb.utils.WriterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

@Service
public class ToolService {

    private final Environment env;
    private final ProteinInteractsWithProteinService interactionService;

    private File scriptDir = new File("/home/andimajore/projects/RepoScapeWeb/web/resources/scripts");
    private File dataDir;
    private File executor;
    private File diamond;

    private HashMap<String, File> tempDirs = new HashMap<>();

    private Logger log = LoggerFactory.getLogger(ToolService.class);

    @Autowired
    public ToolService(Environment env, ProteinInteractsWithProteinService interactionService) {
        this.env = env;
        this.interactionService = interactionService;
        dataDir = new File(env.getProperty("path.external.cache"));
        validateEnvironment();
    }

    private void validateEnvironment() {
        envValidate("python3", "--version");
//        envValidate("pip3", "--version");
//        pythonValidate("pipreqs", true);

    }

    private void envValidate(String component, String versionCommand) {
        StringBuffer sb = new StringBuffer();
        try {
            ProcessUtils.executeProcessWait(new ProcessBuilder("which", component), sb);
        } catch (IOException | InterruptedException e) {
            log.error(component + " not found in environment. Make sure it is installed properly!");
            throw new RuntimeException("Missing dependency: " + component);
        }
        StringBuffer version = new StringBuffer();
        try {
            ProcessUtils.executeProcessWait(new ProcessBuilder(component, versionCommand), version);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Using " + component + ": " + sb.toString().strip() + " [version: " + version.toString().strip() + "]");
    }

//    private void pythonValidate(String lib, boolean install) {
//        StringBuffer sb = new StringBuffer();
//
//        try {
//            ProcessUtils.executeProcessWait(new ProcessBuilder("pip3", "freeze"), sb);
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        List<String> libs = StringUtils.split(sb.toString(), '\n').stream().filter(s -> s.startsWith(lib + "==")).collect(Collectors.toList());
//        if (libs.size() == 0) {
//            log.error("Missing python library '" + lib + "'");
//            if (install)
//                pythonInstall(lib);
//        } else {
//            log.info("Python library installed: " + libs.get(0));
//        }
//    }

//    private void installRequirements(File directory) {
//        try {
//            ProcessUtils.executeProcessWait(new ProcessBuilder("pipreqs", directory.getAbsolutePath()));
//            ProcessUtils.executeProcessWait(new ProcessBuilder("pip3", "install", "-r", new File(directory, "requirements.txt").getAbsolutePath()));
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void pythonInstall(String lib) {
//        log.info("Installing python lib '" + lib + "'");
//        try {
//            ProcessUtils.executeProcessWait(new ProcessBuilder("pip3", "install", "pipreqs"));
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        pythonValidate(lib, false);
//    }


    public void validateTools() {
        validateJobExecutor();

        prepareDiamond();
        validateDiamond();

    }

    private void validateJobExecutor() {
        executor = new File(env.getProperty("jobs.scripts.executor"));
        if (!executor.exists()) {
            log.error("JobExecutor executable was not found in config.");
            new RuntimeException("Missing reference.");
        }
    }

    private void prepareDiamond() {
        log.info("Setting up DIAMOnD");
        File diamondSetUpScript = new File(scriptDir, "diamond_setup.sh");
        diamond = new File(env.getProperty("path.tool.diamond"));
        log.info("Diamond path=" + diamond.getAbsolutePath());
        if (!diamond.exists()) {
            log.error("DIAMOnD executable was not found in config. Please make sure it is referenced correctly! For installation you can use " + diamondSetUpScript.getAbsolutePath());
            new RuntimeException("Missing reference.");
        }
    }

//    private void prepareFiles(String jobId, String algorithm, HashMap<String, String> params, Graph g) {
//        if (!dataDir.exists()) {
//            dataDir.mkdirs();
//            createInteractionFiles();
//        }
//    }

    public void createInteractionFiles() {
        File ggi = new File(dataDir, "gene_gene_interaction.pairs");
        BufferedWriter bw = WriterUtils.getBasicWriter(ggi);
        interactionService.getGenes().forEach((id1, list) -> list.forEach((id2, bool) -> {
            if (id1 < id2) {
                try {
                    bw.write(id1 + "," + id2 + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File ppi = new File(dataDir, "protein_protein_interaction.pairs");
        BufferedWriter bw2 = WriterUtils.getBasicWriter(ppi);
        interactionService.getProteins().forEach((id1, list) -> list.forEach((id2, bool) -> {
            if (id1 < id2) {
                try {
                    bw2.write(id1 + "," + id2 + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        try {
            bw2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

//    public void writeGraphToGraphML(Graph g, File outfile, boolean idsOnly) {
//        BufferedWriter bw = WriterUtils.getBasicWriter(outfile);
//        char nl = '\n';
//        HashMap<String, String> keymap = new HashMap();
//        keymap.put("id", "id");
//        keymap.put("idOne", "source");
//        keymap.put("sourceId", "source");
//        keymap.put("idTwo", "target");
//        keymap.put("targetId", "target");
//        if (!idsOnly) {
//
//        }
//        g.getNodes().
//
//                keySet()
//
//        try {
//            bw.write("<?xml version='1.0' encoding='utf-8'?>" + nl + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">" + nl);
//
//
//        } catch (
//                IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public void executeDiamond(boolean genes, HashSet<Integer> seeds) {
//        File seed = null;
//        try {
//            seed = Files.createTempFile("/tmp", "diamond_seeds").toFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        BufferedWriter bw = WriterUtils.getBasicWriter(seed);
//        seeds.forEach(s -> {
//            try {
//                bw.write(s + "\n");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        try {
//            bw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        StringBuffer sb = new StringBuffer();
//        try {
//            ProcessUtils.executeProcessWait(new ProcessBuilder("python3", diamond.getAbsolutePath(), (genes ? new File(dataDir, "gene_gene_interaction.pairs") : new File(dataDir, "protein_protein_interaction.pairs")).getAbsolutePath(), seed.getAbsolutePath(), "100"), sb);
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println(sb.toString().strip());
//
//    }

    private void validateDiamond() {
        //TODO get version info of diamond
    }

    public File getTempDir(String jobId) {
        if (!tempDirs.containsKey(jobId))
            try {
                File f = Files.createTempDirectory(new File("/tmp").toPath(), "reposcape_web_job_" + jobId).toFile();
                //TODO ignored for dev
//                f.deleteOnExit();
                tempDirs.put(jobId, f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        return tempDirs.get(jobId);
    }


    public String createCommand(String jobId, String algorithm, HashMap<String, String> params) {
        String command = "localhost:8090/backend/api/finishedJob?id=" + jobId + " " + getTempDir(jobId).getAbsolutePath() + " ";
        switch (algorithm) {
            case "diamond": {
                command += "diamond " +
                        diamond.getAbsolutePath() + " " +
                        (params.get("type").equals("genes") ? new File(dataDir, "gene_gene_interaction.pairs") : new File(dataDir, "protein_protein_interaction.pairs")).getAbsolutePath() + " " +
                        "seeds.list " +
                        100;
                break;
            }
        }
        return command;
    }

    public void prepareJobFiles(String jobId, JobRequest req, Graph g) {
        switch (req.algorithm) {
            case "diamond": {
                File seed = new File(getTempDir(jobId), "seeds.list");
                if (req.selection)
                    writeSeedFile(req.params.get("type"), seed, req.nodes);
                else
                    writeSeedFile(req.params.get("type"), seed, g);
                break;
            }
        }

    }

    private void writeSeedFile(String type, File file, Graph g) {
        writeSeedFile(type, file, g.getNodes().get(Graphs.getNode(type)).keySet());
    }

    private void writeSeedFile(String type, File file, Collection<Integer> nodeIds) {
        BufferedWriter bw = WriterUtils.getBasicWriter(file);
        nodeIds.forEach(node -> {
            try {
                bw.write(node + "\n");
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

    public void executeJob(String command) {
//        ProcessBuilder pb = new ProcessBuilder(executor.getAbsolutePath(), command);
        try {
            Runtime.getRuntime().exec(executor.getAbsolutePath() + " " + command);
//            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, HashMap<String, Object>> getJobResults(Job j) {
        HashMap<Integer, HashMap<String, Object>> results;
        for (File f : getTempDir(j.getJobId()).listFiles()) {
            if (j.getRequest().algorithm.equals("diamond") && f.getName().endsWith(".txt")) {
                return readDiamondResults(f);
            }
        }
        return null;
    }

    public void clearDirectories(Job j) {
        getTempDir(j.getJobId()).delete();
    }

    private HashMap<Integer, HashMap<String, Object>> readDiamondResults(File f) {
        HashMap<Integer, HashMap<String, Object>> results = new HashMap<>();
        try {
            BufferedReader br = ReaderUtils.getBasicReader(f);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.charAt(0) == '#')
                    continue;
                LinkedList<String> attrs = StringUtils.split(line, "\t");
                int id = Integer.parseInt(attrs.get(1));
                results.put(id, new HashMap<>());
                results.get(id).put("rank", Integer.parseInt(attrs.get(0)));
                results.get(id).put("p_hyper", attrs.get(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }
}
