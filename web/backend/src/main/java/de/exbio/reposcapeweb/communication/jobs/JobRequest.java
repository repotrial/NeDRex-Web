package de.exbio.reposcapeweb.communication.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class JobRequest {

    public String userId;
    public String graphId;
    public String jobId;
    public String dbVersion;
    public String algorithm;
    public HashMap<String, String> params;
    public boolean selection;
    public List<Integer> nodes;
    public String exprData;
    public boolean experimentalOnly;
    public String goal;
    @JsonIgnore
    public HashSet<Integer> ids;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGraphId() {
        return graphId;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

}
