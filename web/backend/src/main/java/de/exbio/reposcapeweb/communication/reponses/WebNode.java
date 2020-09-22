package de.exbio.reposcapeweb.communication.reponses;

public class WebNode {
    String id;
    String label;
    String title;
    double x;
    double y;
    String group;
    boolean hasEdge = false;

    public WebNode(int id, String label,String group){
        this.id = id+"";
        this.label = label;
        this.group=group;
    }

    public WebNode(String prefix,int id, String label,String group){
        this.id = prefix+id;
        this.label = label;
        this.group=group;
    }

    public WebNode(String prefix,int id, String label,String title, String group){
        this.title=title;
        this.id = prefix+id;
        this.label = label;
        this.group=group;
    }

    public String getId() {
        return id+"";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean hasEdge() {
        return hasEdge;
    }
}
