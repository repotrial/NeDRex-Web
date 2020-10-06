package de.exbio.reposcapeweb.communication.reponses;

import de.exbio.reposcapeweb.db.entities.ids.PairId;

public class WebEdge {

    String from;
    String to;
    String label ="";
    boolean dashes=false;

    public WebEdge(int from, int to) {
        this.from = ""+from;
        this.to = ""+to;
    }

    public WebEdge(int from, int to, String label){
        this(from,to);
        this.label=label;
    }

    public WebEdge(String prefix1, int from, String prefix2, int to){
        this.from = prefix1+from;
        this.to = prefix2+to;
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

    public WebEdge setDashes(boolean dashed){
        this.dashes=dashed;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isDashes() {
        return dashes;
    }
}
