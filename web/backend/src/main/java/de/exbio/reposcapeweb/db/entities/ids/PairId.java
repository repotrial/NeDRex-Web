package de.exbio.reposcapeweb.db.entities.ids;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PairId implements Serializable {

    @Column(name = "id_1")
    private int id1 = -1;
    @Column(name = "id_2")
    private int id2 = -1;


    public PairId() {

    }

    public PairId(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
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
        PairId pairId = (PairId) o;
        return Objects.equals(id1, pairId.id1) &&
                Objects.equals(id2, pairId.id2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }

    @Override
    public String toString() {
        return "("+id1+","+id2+")";
    }
}
