package de.exbio.reposcapeweb.utils;

public class Pair<A, B> {
    public A first;
    public B second;


    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public Pair() {

    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public void setSecond(B second) {
        this.second = second;
    }

}