package de.exbio.reposcapeweb.communication.jobs;


import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    private String jobId;

    private String userId;
    private String basisGraph;
    private String derivedGraph;

    @Enumerated(EnumType.ORDINAL)
    private JobState state = JobState.INITIALIZED;

    @Transient
    private JobRequest request;


    private LocalDateTime created;

    private LocalDateTime finished;

    @Column(columnDefinition = "text")
    private String command;


    public Job() {

    }

    public Job(String id,JobRequest job){
        this(id,job.userId,job.graphId);
        this.request=job;
    }

    public Job(String jobId, String userId, String graphId) {
        this.userId = userId;
        this.basisGraph = graphId;
        this.jobId = jobId;
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

    public JobRequest getRequest() {
        return request;
    }

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
        this.derivedGraph=id;
    }

    public String getDerivedGraph() {
        return derivedGraph;
    }

    enum JobState {
        INITIALIZED, QUEUED, EXECUTING, DONE
    }

}
