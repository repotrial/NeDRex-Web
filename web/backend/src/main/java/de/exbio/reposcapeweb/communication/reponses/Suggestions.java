package de.exbio.reposcapeweb.communication.reponses;

import de.exbio.reposcapeweb.filter.FilterEntry;
import de.exbio.reposcapeweb.filter.FilterKey;
import de.exbio.reposcapeweb.filter.FilterType;

import java.util.*;
import java.util.stream.Collectors;

public class Suggestions {
    private String gid;
    private String query;
    private LinkedList<Suggestion> suggestions;


    public Suggestions(String gid, String query) {
        this.gid = gid;
        this.query = query;
        suggestions = new LinkedList<>();
    }


    public void setDistinct(EnumMap<FilterType, HashMap<Integer, List<FilterEntry>>> distinctMap, EnumMap<FilterType, HashMap<Integer, FilterKey>> keyMap, HashSet<Integer> idsList) {
        distinctMap.forEach((type, filters) ->
                filters.forEach((key, entries) ->
                        suggestions.add(new Suggestion(type, true, keyMap.get(type).get(key).getName(), keyMap.get(type).get(key).getKey(), key, entries.size()))
                )
        );
    }

    public void setDistinct(EnumMap<FilterType, HashMap<Integer, List<FilterEntry>>> distinctMap, EnumMap<FilterType, HashMap<Integer, FilterKey>> keyMap) {
        distinctMap.forEach((type, filters) -> {
                    HashMap<Integer, FilterKey> keys = keyMap.get(type);
                    if (keys != null)
                        filters.forEach((key, entries) ->
                                suggestions.add(new Suggestion(type, true, keys.get(key).getName(), keys.get(key).getKey(), key, entries.size()))
                        );
                }
        );
    }

    public void setUnique(EnumMap<FilterType, HashMap<Integer, FilterEntry>> uniqueMap, EnumMap<FilterType, HashMap<Integer, FilterKey>> keyMap, HashSet<Integer> idsList) {
        uniqueMap.forEach((type, filters) ->
                filters.forEach((key, entry) ->
                        suggestions.add(new Suggestion(type, false, entry.getName(), keyMap.get(type).get(key).getKey(), entry.getNodeId(), 1)))
        );
    }

    public void setUnique(EnumMap<FilterType, HashMap<Integer, FilterEntry>> uniqueMap, EnumMap<FilterType, HashMap<Integer, FilterKey>> keyMap) {
        uniqueMap.forEach((type, filters) -> {
            HashMap<Integer, FilterKey> keys = keyMap.get(type);
            filters.forEach((key, entry) -> suggestions.add(new Suggestion(type, false, entry.getName(), keys.get(key).getKey(), entry.getNodeId(), 1)));
        });
    }

    public String getGid() {
        return gid;
    }

    public String getQuery() {
        return query;
    }

    public LinkedList<Suggestion> getSuggestions() {
        return suggestions;
    }


    private class SuggestionEntry {

        private Integer[] ids;

        public SuggestionEntry(Integer id) {
            ids = new Integer[]{id};
        }

        public SuggestionEntry(Collection<Integer> ids) {
            this.ids = ids.toArray(Integer[]::new);
        }

        public Integer[] getIds() {
            return ids;
        }
    }

}
