package de.exbio.reposcapeweb.filter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class NodeFilter {

    private EnumMap<FilterType, TreeMap<FilterKey, List<FilterEntry>>> distinctMap;

    private EnumMap<FilterType, TreeMap<FilterKey, FilterEntry>> uniqueMap;

    //TODO text filter (elastic search?)

    public NodeFilter() {
        distinctMap = new EnumMap<>(FilterType.class);
        uniqueMap = new EnumMap<>(FilterType.class);
    }


    public NodeFilter(NodeFilter all, Collection<Integer> ids) {
        set(all.filteredByIds(ids));
    }

    private void set(NodeFilter filterByIds) {
        setUniqueMap(filterByIds.getUniqueMap());
        setDistinctMap(filterByIds.getDistinctMap());
    }

    private NodeFilter filteredByIds(Collection<Integer> ids) {
        NodeFilter filtered = new NodeFilter();

        distinctMap.forEach((type, keys) -> {
            keys.forEach((key, entries) -> {
                List<FilterEntry> es = entries.stream().filter(e -> ids.contains(e.getNodeId())).collect(Collectors.toList());
                if (es.size() > 0)
                    filtered.addDistinctMap(type, key, es);
            });
        });
        uniqueMap.forEach((type, keys) -> {
            keys.entrySet().stream().filter(e -> ids.contains(e.getValue().getNodeId())).forEach(e -> filtered.addUniqueMap(type, e.getKey(), e.getValue()));
        });
        return filtered;
    }


    private void addDistinctMap(FilterType type, FilterKey key, List<FilterEntry> entries) {
        try {
            distinctMap.get(type).put(key, entries);
        } catch (NullPointerException e) {
            distinctMap.put(type, new TreeMap<>());
            distinctMap.get(type).put(key, entries);
        }
    }

    private void addUniqueMap(FilterType type, FilterKey key, FilterEntry entry) {
        try {
            uniqueMap.get(type).put(key, entry);
        } catch (NullPointerException e) {
            uniqueMap.put(type, new TreeMap<>());
            uniqueMap.get(type).put(key, entry);
        }
    }


    public LinkedList<FilterEntry> filterContains(String key, int size) {
        LinkedList<FilterEntry> filtered = new LinkedList<>();


        TreeSet<FilterEntry> entries = new TreeSet<>();

        uniqueMap.keySet().forEach(type -> entries.addAll(filterUniqueContains(type, key)));
        distinctMap.keySet().forEach(type -> entries.addAll(filterDistinctContains(type, key)));

        AtomicBoolean done = new AtomicBoolean(false);
        entries.forEach(e -> {
                    if (done.get())
                        return;
                    filtered.add(e);
                    if (filtered.size() == size)
                        done.set(true);
                }
        );
        return filtered;
    }

    public LinkedList<FilterEntry> filterStartsWith(String key, int size) {
        LinkedList<FilterEntry> filtered = new LinkedList<>();

        TreeSet<FilterEntry> entries = new TreeSet<>();

        uniqueMap.keySet().forEach(type -> entries.addAll(filterUniqueStartsWith(type, key)));
        distinctMap.keySet().forEach(type -> entries.addAll(filterDistinctStartsWith(type, key)));

        AtomicBoolean done = new AtomicBoolean(false);
        entries.forEach(e -> {
                    if (done.get())
                        return;
                    filtered.add(e);
                    if (filtered.size() == size)
                        done.set(true);
                }
        );
        return filtered;
    }

    public LinkedList<FilterEntry> filterMatches(String key, int size) {
        LinkedList<FilterEntry> filtered = new LinkedList<>();


        TreeSet<FilterEntry> entries = new TreeSet<>();

        uniqueMap.keySet().forEach(type -> entries.addAll(filterUniqueMatches(type, key)));
        distinctMap.keySet().forEach(type -> entries.addAll(filterDistinctMatches(type, key)));

        AtomicBoolean done = new AtomicBoolean(false);
        entries.forEach(e -> {
                    if (done.get())
                        return;
                    filtered.add(e);
                    if (filtered.size() == size)
                        done.set(true);
                }
        );
        return filtered;
    }

    private HashSet<FilterEntry> filterUniqueContains(FilterType type, String key) {
        try {
            return uniqueMap.get(type).entrySet().stream().filter(k -> k.getKey().contains(key)).map(Map.Entry::getValue).collect(Collectors.toCollection(HashSet::new));
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }

    private HashSet<FilterEntry> filterUniqueMatches(FilterType type, String key) {
        try {
            return uniqueMap.get(type).entrySet().stream().filter(k -> k.getKey().matches(key)).map(Map.Entry::getValue).collect(Collectors.toCollection(HashSet::new));
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }

    private HashSet<FilterEntry> filterUniqueStartsWith(FilterType type, String key) {
        try {
            return uniqueMap.get(type).entrySet().stream().filter(k -> k.getKey().startsWith(key)).map(Map.Entry::getValue).collect(Collectors.toCollection(HashSet::new));
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }

    private HashSet<FilterEntry> filterDistinctContains(FilterType type, String key) {
        try {
            return distinctMap.get(type).entrySet().stream().filter(k -> k.getKey().contains(key)).map(Map.Entry::getValue).flatMap(Collection::stream).collect(Collectors.toCollection(HashSet::new));
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }

    private HashSet<FilterEntry> filterDistinctMatches(FilterType type, String key) {
        try {
            return distinctMap.get(type).entrySet().stream().filter(k -> k.getKey().matches(key)).map(Map.Entry::getValue).flatMap(Collection::stream).collect(Collectors.toCollection(HashSet::new));
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }

    private HashSet<FilterEntry> filterDistinctStartsWith(FilterType type, String key) {
        try {
            return distinctMap.get(type).entrySet().stream().filter(k -> k.getKey().startsWith(key)).map(Map.Entry::getValue).flatMap(Collection::stream).collect(Collectors.toCollection(HashSet::new));
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }


    public EnumMap<FilterType, TreeMap<FilterKey, List<FilterEntry>>> getDistinctMap() {
        return distinctMap;
    }

    public void setDistinctMap(EnumMap<FilterType, TreeMap<FilterKey, List<FilterEntry>>> distinctMap) {
        //TODO clone entries
        this.distinctMap = distinctMap;
    }

    public EnumMap<FilterType, TreeMap<FilterKey, FilterEntry>> getUniqueMap() {
        return uniqueMap;
    }

    public void setUniqueMap(EnumMap<FilterType, TreeMap<FilterKey, FilterEntry>> uniqueMap) {
        //TODO clone entries
        this.uniqueMap = uniqueMap;
    }
}
