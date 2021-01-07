package de.exbio.reposcapeweb.communication.reponses;

import java.awt.geom.Point2D;
import java.util.Objects;

public class WebNode {
    String id;
    String label;
    String title;
    String group;
    double x;
    double y;
    boolean hasEdge = false;

    public WebNode(int id, String label, String group) {
        this.id = id + "";
        this.label = label;
        this.group = group;
    }

    public WebNode(int id, String label, boolean hasEdge) {
        this.id = id + "";
        this.label = label;
        this.hasEdge = hasEdge;
    }

    public WebNode(int id, String label, String group, String title) {
        this(id, label, group);
        this.title = title;
    }


    public WebNode(String prefix, int id, String label, String group) {
        this.id = prefix + id;
        this.label = label;
        this.group = group;
    }

    public WebNode(String prefix, int id, String label, String title, String group) {
        this.title = title;
        this.id = prefix + id;
        this.label = label;
        this.group = group;
    }

    public String getId() {
        return id + "";
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

    public boolean hasEdge() {
        return hasEdge;
    }

    public WebNode setPrefix(String prefix) {
        this.id = prefix + this.id;
        return this;
    }

    public WebNode setGroup(String group) {
        this.group = group;
        return this;
    }

    public WebNode setPosition(Point2D point) {
        if (point != null) {
            setX(point.getX());
            setY(point.getY());
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebNode webNode = (WebNode) o;
        return id.equals(webNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
