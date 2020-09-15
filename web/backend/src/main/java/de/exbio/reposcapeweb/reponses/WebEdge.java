package de.exbio.reposcapeweb.reponses;

import de.exbio.reposcapeweb.db.entities.ids.PairId;

public class WebEdge {
    String from;
    String to;

    public WebEdge(int from, int to) {
        this.from = ""+from;
        this.to = ""+to;
    }

    public WebEdge(PairId id) {
        this.from = ""+id.getId1();
        this.to = ""+id.getId2();
    }

    public WebEdge(String from, String to){
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
