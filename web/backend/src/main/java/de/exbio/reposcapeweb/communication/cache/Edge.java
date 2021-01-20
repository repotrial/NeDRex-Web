package de.exbio.reposcapeweb.communication.cache;

import de.exbio.reposcapeweb.communication.reponses.WebEdge;
import de.exbio.reposcapeweb.db.entities.ids.PairId;

import java.util.Objects;

public class Edge {
    private int id1;
    private int id2;

    public Edge(){

    }

    public Edge (PairId id){
        this.id1 = id.getId1();
        this.id2 = id.getId2();
    }

    public Edge(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public WebEdge toWebEdge() {
        return new WebEdge(id1, id2);
    }

    public int getId1() {
        return id1;
    }

    public void setId1(int id1) {
        this.id1 = id1;
    }

    public int getId2() {
        return id2;
    }

    public void setId2(int id2) {
        this.id2 = id2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return id1 == edge.id1 &&
                id2 == edge.id2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }


}
