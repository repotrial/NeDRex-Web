package de.exbio.reposcapeweb.tools;

import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.jobs.JobRequest;
import de.exbio.reposcapeweb.communication.jobs.JobResult;
import de.exbio.reposcapeweb.configs.DBConfig;
import de.exbio.reposcapeweb.db.services.edges.ProteinInteractsWithProteinService;
import de.exbio.reposcapeweb.db.services.nodes.GeneService;
import de.exbio.reposcapeweb.db.services.nodes.ProteinService;
import de.exbio.reposcapeweb.tools.algorithms.*;
import de.exbio.reposcapeweb.utils.Pair;
import de.exbio.reposcapeweb.utils.ProcessUtils;
import de.exbio.reposcapeweb.utils.WriterUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class ToolService {

    private final Environment env;
    private final ProteinInteractsWithProteinService interactionService;
    private final ProteinService proteinService;
    private final GeneService geneService;

    private File scriptDir;
    private File dataDir;
    private File layoutDir;
    private File executor;
    private EnumMap<Tool, Algorithm> algorithms = new EnumMap<>(Tool.class);

    private HashMap<String, File> tempDirs = new HashMap<>();

    private Logger log = LoggerFactory.getLogger(ToolService.class);

    @Autowired
    public ToolService(KPM kpm, Robust robust, Domino domino, TrustRank trustrank, ClosenessCentrality cc, Bicon bicon, Must must, Diamond diamond, ProteinService proteinService, GeneService geneService, Environment env, ProteinInteractsWithProteinService interactionService) {
        this.env = env;
        this.interactionService = interactionService;
        this.proteinService = proteinService;
        this.geneService = geneService;

        Algorithm[] algorithms = new Algorithm[]
                {diamond, must, bicon, cc, trustrank, domino, robust, kpm};
        for (Algorithm algorithm : algorithms) {
            this.algorithms.put(algorithm.getEnum(), algorithm);
        }

        dataDir = new File(env.getProperty("path.external.cache"));
        scriptDir = new File(env.getProperty("path.scripts.dir"));
        layoutDir = new File(scriptDir, "layouts");
        validateEnvironment();
    }

    private String getPythonPath(){
        return env.getProperty("path.tool.python", "python3");
    }

    private String getLayoutingPythonPath(){
        return env.getProperty("path.tool.layout.python", "python3");
    }

    private void validateEnvironment() {
        envValidate(getPythonPath(), "--version");
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


    public void validateTools() {
        validateJobExecutor();
        algorithms.values().forEach(Algorithm::prepare);
    }

    private void validateJobExecutor() {
        executor = new File(env.getProperty("jobs.scripts.executor"));
        if (!executor.exists()) {
            log.error("JobExecutor executable was not found in config.");
            new RuntimeException("Missing reference.");
        }
    }

    public void createInteractionFiles(TreeMap<Integer, String> tissueMap) {
        File ggi_all = new File(dataDir, "gene_gene_interaction_all.pairs");
        File ggi_exp = new File(dataDir, "gene_gene_interaction_exp.pairs");
        HashMap<Integer, BufferedWriter> all_writers = new HashMap<>();
        HashMap<Integer, BufferedWriter> exp_writers = new HashMap<>();

        tissueMap.forEach((k, v) -> {
            String tissueInFiles = v.replaceAll(" ", "");
            all_writers.put(k, WriterUtils.getBasicWriter(new File(dataDir, "gene_gene_interaction-" + tissueInFiles + "_all.pairs")));
            exp_writers.put(k, WriterUtils.getBasicWriter(new File(dataDir, "gene_gene_interaction-" + tissueInFiles + "_exp.pairs")));
        });

        try (BufferedWriter bw_all = WriterUtils.getBasicWriter(ggi_all); BufferedWriter bw_exp = WriterUtils.getBasicWriter(ggi_exp)) {
            interactionService.getGenes().forEach((id1, map) -> map.forEach((id, vals) -> {
                        if (id.getId1() != id1)
                            return;
                        try {
                            String line = geneService.map(id.getId1()) + "\t" + geneService.map(id.getId2()) + "\n";
                            bw_all.write(line);
                            if (vals.first.second) {
                                bw_exp.write(line);
                            }
                            for (int tissueId : vals.second) {
                                all_writers.get(tissueId).write(line);
                                if (vals.first.second)
                                    exp_writers.get(tissueId).write(line);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        all_writers.values().forEach(bw -> {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        exp_writers.values().forEach(bw -> {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        all_writers.clear();
        exp_writers.clear();

        File ppi_all = new File(dataDir, "protein_protein_interaction_all.pairs");
        File ppi_exp = new File(dataDir, "protein_protein_interaction_exp.pairs");
        tissueMap.forEach((k, v) -> {
            String tissueInFiles = v.replaceAll(" ", "");
            all_writers.put(k, WriterUtils.getBasicWriter(new File(dataDir, "protein_protein_interaction-" + tissueInFiles + "_all.pairs")));
            exp_writers.put(k, WriterUtils.getBasicWriter(new File(dataDir, "protein_protein_interaction-" + tissueInFiles + "_exp.pairs")));
        });
        try (BufferedWriter bw_all = WriterUtils.getBasicWriter(ppi_all); BufferedWriter bw_exp = WriterUtils.getBasicWriter(ppi_exp)) {
            interactionService.getProteins().forEach((id1,map) -> map.forEach((id, vals) -> {
                if(id.getId1()!=id1)
                    return;
                try {
                    String line = proteinService.map(id.getId1()) + "\t" + proteinService.map(id.getId2()) + "\n";
                    bw_all.write(line);
                    if (vals.first.second) {
                        bw_exp.write(line);
                    }
                    for (int tissueId : vals.second) {
                        all_writers.get(tissueId).write(line);
                        if (vals.first.second)
                            exp_writers.get(tissueId).write(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

        } catch (IOException e) {
            e.printStackTrace();
        }

        all_writers.values().forEach(bw -> {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        exp_writers.values().forEach(bw -> {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        File rankCache = new File(dataDir, "ranking_files");
        if (rankCache.exists())
            rankCache.delete();
        rankCache.mkdir();
        try {
            ProcessUtils.executeProcessWait(new ProcessBuilder(getPythonPath(), env.getProperty("jobs.scripts.ranking_preparation"), dataDir.getAbsolutePath(), rankCache.getAbsolutePath()), false);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public File getTempDir(String jobId) {
        if (!tempDirs.containsKey(jobId))
            try {
                File f = Files.createTempDirectory(new File("/tmp").toPath(), "nedrex_job_" + jobId).toFile();
                tempDirs.put(jobId, f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        return tempDirs.get(jobId);
    }

    @Async
    public void removeTempDir(String jobId) {
        File wd = getTempDir(jobId);
        if (wd.exists()) {
            try {
                FileUtils.deleteDirectory(wd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public String createCommand(Job job, JobRequest request) {
        String command = "localhost:" + env.getProperty("server.port") + env.getProperty("server.servlet.context-path") + "api/finishedJob?id=" + job.getJobId() + " " + getTempDir(job.getJobId()).getAbsolutePath() + " ";
        LinkedList<File> neededFiles = new LinkedList<>();

        Algorithm algorithm = algorithms.get(job.getMethod());
        File[] interactions = algorithm.interactionFiles(request);
        neededFiles.addAll(Arrays.asList(interactions));
        command += algorithm.createCommand(interactions, request);

        prepareTempDir(job, neededFiles);
        return command;
    }

    private void prepareTempDir(Job job, LinkedList<File> neededFiles) {
        File dir = getTempDir(job.getJobId());
        neededFiles.forEach(f -> {
            try {
                Files.copy(f.toPath(), new File(dir, f.getName()).toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public void prepareJobFiles(Job job, JobRequest req, Graph g, HashMap<Integer, Pair<String, String>> domainMap) {
        algorithms.get(job.getMethod()).prepareJobFiles(getTempDir(job.getJobId()), req, g, domainMap);
    }

    public Process executeJob(String command, Tool algo) {
        try {
            ProcessBuilder pb = algorithms.get(algo).getExecutionEnvironment((executor.getAbsolutePath() + " " + command).split(" "));
//            ProcessUtils.executeProcessWait(pb,true);
            return ProcessUtils.executeProcess(pb);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getJobResults(Job j, HashMap<Integer, HashMap<String, Integer>> domainMaps) throws IOException {
        JobResult result = new JobResult();
        j.setResult(result);
        HashMap<Integer, HashMap<String, Object>> nodes = new HashMap<>();
        HashMap<Integer, HashMap<Integer, HashMap<String, Object>>> edges = new HashMap<>();

        algorithms.get(j.getMethod()).getResults(nodes, edges, getTempDir(j.getJobId()), j, domainMaps);

        result.setNodes(nodes);
        result.setEdges(edges);
    }


    public void clearDirectories(Job j, File dest) throws Exception {
        File result = algorithms.get(j.getMethod()).getResultFile(getTempDir(j.getJobId()));

        try {
            if (result.exists())
                j.setResultFile(true);
            dest.getParentFile().mkdirs();
            Files.move(result.toPath(), dest.toPath());
        } catch (IOException | NullPointerException e) {
            getTempDir(j.getJobId()).delete();
            e.printStackTrace();
            throw new Exception("Missing results exception");
        }
        removeTempDir(j.getJobId());
    }

    public void createGraphmlFromFS(File wd, File graphml) {
        try {
            ProcessUtils.executeProcessWait(new ProcessBuilder(getPythonPath(), new File(scriptDir, "tablesToGraphml.py").getAbsolutePath(), wd.getAbsolutePath(), graphml.getAbsolutePath()), false);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (!graphml.exists()) {
            log.error(graphml.getAbsolutePath() + " should hav been created but wasn't!");
        }
    }

    public void createThumbnail(File graphml, File thumb) {
        thumb.getParentFile().mkdirs();
        try {
            ProcessUtils.executeProcessWait(new ProcessBuilder(getPythonPath(), new File(layoutDir, "computeLayouting.py").getAbsolutePath(), graphml.getAbsolutePath(), "1", thumb.getAbsolutePath(), DBConfig.getConfFile().getAbsolutePath()), false);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void createLayoutAndThumbnail(File graphml, File layout, File thumb) {
        layout.getParentFile().mkdirs();
        try {
            ProcessUtils.executeProcessWait(new ProcessBuilder(getPythonPath(), new File(layoutDir, "computeLayouting.py").getAbsolutePath(), graphml.getAbsolutePath(), "2", layout.getAbsolutePath(), thumb.getAbsolutePath(), DBConfig.getConfFile().getAbsolutePath()), false);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void createLayout(File graphml, File layout) {
        layout.getParentFile().mkdirs();
        try {
            ProcessUtils.executeProcessWait(new ProcessBuilder(getPythonPath(), new File(layoutDir, "computeLayouting.py").getAbsolutePath(), graphml.getAbsolutePath(), "0", layout.getAbsolutePath(), DBConfig.getConfFile().getAbsolutePath()), false);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void createLayout(File layout, File nodes, File edges, String layout_type) {
        layout.getParentFile().mkdirs();
        try {
            ProcessUtils.executeProcessWait(new ProcessBuilder(getLayoutingPythonPath(), new File(layoutDir, "cartographs.py").getAbsolutePath(), nodes.getAbsolutePath(), edges.getAbsolutePath(), layout.getAbsolutePath(), layout_type), false);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void createTripartiteLayout(File graphml, File layout, File sources, File targets) {
        layout.getParentFile().mkdirs();
        try {
            ProcessUtils.executeProcessWait(new ProcessBuilder(getPythonPath(), new File(layoutDir, "tripartiteLayout.py").getAbsolutePath(), graphml.getAbsolutePath(), layout.getAbsolutePath(), sources.getAbsolutePath(), targets.getAbsolutePath(), DBConfig.getConfFile().getAbsolutePath()), false);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public EnumMap<Tool, Algorithm> getAlgorithms() {
        return algorithms;
    }


    public void createIndexFiles() {
        algorithms.values().forEach(algorithm -> {
            algorithm.createIndex();
        });
    }


    public enum Tool {
        DIAMOND, BICON, TRUSTRANK, CENTRALITY, MUST, DOMINO, ROBUST, KPM;
    }
}
