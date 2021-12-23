package de.exbio.reposcapeweb.communication.reponses;

import de.exbio.reposcapeweb.communication.jobs.Job;
import de.exbio.reposcapeweb.communication.jobs.JobResult;
import de.exbio.reposcapeweb.tools.ToolService;

import java.util.HashMap;
import java.util.HashSet;

public class JobResponse {

    public String jobId;
    public String parentJid;
    public String type;

    public  String userId;
    public String basisGraph;
    public  String derivedGraph;
    public String parentGraph;
    public  HashMap<String,String> params;

    public HashSet<Integer> seeds;

    public ToolService.Tool method = null;

    public Job.JobState state;


    public  String target;

    public JobResult result;

    public Job.JobGoal goal;

    public HashMap<String, HashMap<Integer, Object>> nodes;

    public JobResponse(Job j){
        this.jobId = j.getJobId();
        this.userId = j.getUserId();
        this.basisGraph = j.getBasisGraph();
        this.derivedGraph = j.getDerivedGraph();
        this.params = j.getParams();
        this.method = j.getMethod();
        this.state = j.getState();
        this.target = j.getTarget();
        this.result = j.getResult();
        this.goal = j.getGoal();
        this.parentJid=j.getParentJid();
    }
}
