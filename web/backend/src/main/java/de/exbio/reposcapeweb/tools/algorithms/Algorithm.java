package de.exbio.reposcapeweb.tools.algorithms;

import de.exbio.reposcapeweb.communication.cache.Graph;
import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.jobs.JobRequest;
import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.Pair;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public interface Algorithm {

    ToolService.Tool getEnum();

    void prepare();

    File[] interactionFiles(JobRequest request);

    String getResultSuffix();

    String createCommand(File[] interactions, JobRequest request);

    void prepareJobFiles(File tempDir, JobRequest req, Graph g, HashMap<Integer, Pair<String, String>> domainMap);

    void getResults(HashMap<Integer, HashMap<String, Object>> nodes, HashMap<Integer, HashMap<Integer, HashMap<String, Object>>> edges, File tempDir, Job j, HashMap<Integer, HashMap<String, Integer>> domainMaps) throws IOException;

    File getResultFile(File tempDir);

    int getNodeType(Job j);

    boolean integrateOriginalGraph(Job j);

    void createGraph(Graph derived, Job j, int nodeTypeId, Graph g);

    boolean usesExpressionInput();

    boolean usesSeedInput();

    void setTreads(Job j, int max);

    boolean hasMultipleResultFiles();

    void createIndex();

    boolean hasCustomEdges();

    ProcessBuilder getExecutionEnvironment(String[] command);

}
