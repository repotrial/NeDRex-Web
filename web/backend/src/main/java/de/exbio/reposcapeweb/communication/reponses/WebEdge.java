package de.exbio.reposcapeweb.communication.reponses;

import de.exbio.reposcapeweb.db.entities.ids.PairId;

import java.util.HashMap;

public class WebEdge {

    String from;
    String to;
    String label = "";
    String title = "";
    boolean dashes = false;
    HashMap<String, Object> arrows;

    public WebEdge(int from, int to) {
        this.from = "" + from;
        this.to = "" + to;
    }

    public WebEdge(int from, int to, String label) {
        this(from, to);
        this.label = label;
    }

    public WebEdge setTitle(String title) {
        this.title = title;
        return this;
    }

    public WebEdge(String prefix1, int from, String prefix2, int to) {
        this.from = prefix1 + from;
        this.to = prefix2 + to;
    }

    public WebEdge(PairId id) {
        this.from = "" + id.getId1();
        this.to = "" + id.getId2();
    }

    public WebEdge(String from, String to) {
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

    public HashMap<String, Object> getArrows() {
        return arrows;
    }

    public void setArrows(HashMap<String, Object> arrows) {
        this.arrows = arrows;
    }

    public WebEdge setDashes(boolean dashed) {
        this.dashes = dashed;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public WebEdge setLabel(String label) {
        this.label = label;
        return this;
    }

    public boolean isDashes() {
        return dashes;
    }

    public WebEdge addPrefixes(String prefix1, String prefix2) {
        this.from = prefix1 + this.from;
        this.to = prefix2 + this.to;
        return this;
    }

    public WebEdge setArrowHead(boolean val) {
        boolean arrowsCreated = arrows != null;
        if (val & !arrowsCreated)
            return this;
        if (!arrowsCreated)
            arrows = new HashMap<>();
        if (!arrows.containsKey("to"))
            arrows.put("to", new HashMap<>());
        HashMap<String, Object> to = (HashMap<String, Object>) arrows.get("to");
        to.put("enabled", val);
        return this;
    }

    public String getTitle() {
        return title;
    }
}
