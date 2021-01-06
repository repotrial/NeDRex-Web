package de.exbio.reposcapeweb.db;

import java.util.HashMap;

public class Metadata {
    HashMap<String, Object> repotrial;
    long lastUpdate;
    long lastCheck;

    public Metadata() {
    }

    public Metadata(HashMap data) {
        repotrial=(HashMap<String, Object>) data;
    }


    public HashMap<String, Object> getRepotrial() {
        return repotrial;
    }

    public void setRepotrial(HashMap<String, Object> repotrial) {
        this.repotrial = repotrial;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(long lastCheck) {
        this.lastCheck = lastCheck;
    }
}
