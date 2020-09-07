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


    public void removeByNodeIds(Collection<Integer> ids) {
        removeByNodeIdsDistinct(ids);
        removeByNodeIdsUnique(ids);
    }

    private void removeByNodeIdsUnique(Collection<Integer> ids) {
        EnumMap<FilterType, Set<FilterKey>> del = new EnumMap<>(FilterType.class);
        uniqueMap.forEach((type, keys) -> {
            Set<FilterKey> left = keys.entrySet().stream().filter(e -> ids.contains(e.getValue().getNodeId())).map(Map.Entry::getKey).collect(Collectors.toSet());
            del.put(type, left);
        });

        del.forEach((type, keys) -> keys.forEach(uniqueMap.get(type)::remove));
    }

    private void removeByNodeIdsDistinct(Collection<Integer> ids) {
        EnumMap<FilterType, TreeMap<FilterKey, List<FilterEntry>>> del = new EnumMap<>(FilterType.class);
        distinctMap.forEach((type, keys) -> keys.forEach((k, v) -> {
            List<FilterEntry> left = v.stream().filter(e -> ids.contains(e.getNodeId())).collect(Collectors.toList());
            if (left.size() > 0)
                try {
                    del.get(type).put(k, left);
                } catch (NullPointerException e) {
                    del.put(type, new TreeMap<>());
                    del.get(type).put(k, left);
                }
        }));
        del.forEach((type, keys) -> keys.forEach((k, v) -> {
            distinctMap.get(type).get(k).removeAll(v);
            if (distinctMap.get(type).get(k).size() == 0)
                distinctMap.get(type).remove(k);
        }));
    }

    public void addUnique(FilterType type, Map<FilterKey, FilterEntry> entries) {
        if (!uniqueMap.containsKey(type))
            uniqueMap.put(type, new TreeMap<>());
        uniqueMap.get(type).putAll(entries);
    }

    public void addDistinct(FilterType type, Map<FilterKey, List<FilterEntry>> entries) {
        if (!distinctMap.containsKey(type))
            distinctMap.put(type, new TreeMap<>());
        entries.forEach((key, values) -> addDistinct(type, key, values));
    }

    public void addDistinct(FilterType type, FilterKey key, List<FilterEntry> entries) {
        if (!distinctMap.containsKey(type))
            distinctMap.put(type, new TreeMap<>());
        if (!distinctMap.get(type).containsKey(key))
            distinctMap.get(type).put(key, new LinkedList<>());
        distinctMap.get(type).get(key).addAll(entries);
    }
}
