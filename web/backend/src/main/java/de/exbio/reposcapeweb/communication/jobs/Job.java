package de.exbio.reposcapeweb.communication.jobs;


import de.exbio.reposcapeweb.tools.ToolService;
import de.exbio.reposcapeweb.utils.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Entity
@Table(name = "jobs")
public class Job {


    public enum JobState {
        INITIALIZED, QUEUED, EXECUTING, DONE, NOCHANGE, ERROR, TIMEOUT,LIMITED, WAITING
    }

    @Id
    private String jobId;

    private String userId;
    private String basisGraph;
    private String dbVersion;
    private String derivedGraph;
    private String params;
    @Transient
    private TreeSet<Integer> seeds;

    @Enumerated(EnumType.ORDINAL)
    private ToolService.Tool method = null;

    @Enumerated(EnumType.ORDINAL)
    private JobState state = JobState.INITIALIZED;

//    @Transient
//    private JobRequest request;

    public void setSeeds(Collection<Integer> list) {
        this.seeds= new TreeSet<>(list);
    }

    private LocalDateTime created;

    @Transient
    private LocalDateTime started;

    private LocalDateTime finished;

    private boolean resultFile;

    private String message;

    private String target;

    @Transient
    private Process process;

    @Column(columnDefinition = "text")
    private String command;

    @Transient
    private JobResult result;


    public Job() {

    }

    public Job(String id, JobRequest job) {
        this(id, job.userId, job.graphId, job.algorithm, job.params.get("type"), job.dbVersion);
//        this.request=job;
    }

    public Job(String jobId, String userId, String graphId, String algorithm, String target, String dbVersion) {
        this.userId = userId;
        this.basisGraph = graphId;
        this.jobId = jobId;
        this.method = ToolService.Tool.valueOf(algorithm.toUpperCase());
        this.target = target;
        this.dbVersion = dbVersion;
        created = LocalDateTime.now();
    }

    public void setStatus(JobState state) {
        this.state = state;
        switch (state) {
            case DONE:
                finished = LocalDateTime.now();
                break;
        }
    }

    public String getJobId() {
        return jobId;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getUserId() {
        return userId;
    }

    public String getBasisGraph() {
        return basisGraph;
    }

    public void setBasisGraph(String basisGraph) {
        this.basisGraph = basisGraph;
    }

    public JobState getState() {
        return state;
    }

//    public JobRequest getRequest() {
//        return request;
//    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getFinished() {
        return finished;
    }

    public String getCommand() {
        return command;
    }

    public void setDerivedGraph(String id) {
        this.derivedGraph = id;
    }

    public String getDerivedGraph() {
        return derivedGraph;
    }

    public void setProcess(Process executeJob) {
        this.process = executeJob;
    }

    public void setStarted() {
        this.started = LocalDateTime.now();
    }

    public LocalDateTime getStarted() {
        return this.started;
    }

    public Process getProcess() {
        return process;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getUpdate() {
        return message;
    }

    public void setUpdate(String update) {
        this.message = update;
    }

    public String getTarget() {
        return target;
    }

    public ToolService.Tool getMethod() {
        return method;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> out = new HashMap<>();
        out.put("state", getState().name());
        out.put("gid", getDerivedGraph());
        out.put("basis", getBasisGraph());
        out.put("jid", getJobId());
        out.put("update", message);
        out.put("algorithm", getMethod().name());
        out.put("target", getTarget());
        out.put("created", getCreated().toEpochSecond(ZoneOffset.ofTotalSeconds(0)));
        out.put("download", resultFile);
        return out;
    }

    public HashMap<String, String> getParams() {
        return params != null ? StringUtils.stringToMap(params) : new HashMap<>();
    }

    public void addParam(String key, Object value) {
        HashMap<String, String> map = getParams();
        map.put(key, value.toString());
        setParams(StringUtils.mapToString(map));
    }

    public void setParams(String params) {
        this.params = params;
    }

    public boolean isResultFile() {
        return resultFile;
    }

    public void setResultFile(boolean resultFile) {
        this.resultFile = resultFile;
    }

    public void setResult(JobResult result) {
        this.result = result;
    }

    public JobResult getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return params.equals(job.params) && seeds.equals(job.seeds) && method == job.method && dbVersion.equals(job.dbVersion);
    }

    //FIXME can overflow but unlikely to cause collisions
    @Override
    public int hashCode() {
        return Objects.hash(params, Arrays.hashCode(seeds.toArray()),dbVersion);
    }
}
