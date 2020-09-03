package de.exbio.reposcapeweb.filter;

import java.util.Objects;

public class FilterKey implements Comparable<FilterKey> {
    private String key;

    public FilterKey(String key) {
        this.key = key;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterKey filterKey = (FilterKey) o;
        return Objects.equals(key, filterKey.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public int compareTo(FilterKey other) {
        if (this.equals(other))
            return 1;
        return this.key.compareTo(other.key);
    }

    @Override
    public String toString() {
        return key;
    }

    public boolean contains(String c) {
        return key.contains(c);
    }

    public boolean startsWith(String s) {
        return key.startsWith(s);
    }

    public boolean matches(String m) {
        return key.matches(m);
    }
}
