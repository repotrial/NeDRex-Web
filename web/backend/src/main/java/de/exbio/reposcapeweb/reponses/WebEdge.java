package de.exbio.reposcapeweb.reponses;

import de.exbio.reposcapeweb.db.entities.ids.PairId;

public class WebEdge {
    int from;
    int to;

    public WebEdge(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public WebEdge(PairId id) {
        this.from = id.getId1();
        this.to = id.getId2();
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }
}
