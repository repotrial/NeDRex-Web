package de.exbio.reposcapeweb.utils;

import java.util.*;

public class StringUtils {

    public static int[] getPositions(String line, String sep, int count) {
        int[] positions = new int[count];
        int a = 0;
        positions[0] = line.indexOf(sep);
        a++;
        count--;
        while (count-- != 0) {
            positions[a] = line.indexOf(sep, positions[a - 1] + 1);
            a++;
        }
        return positions;
    }

    public static int getIndex(String line, int counter, String sep) {
        if (counter == 0) {
            return -sep.length();
        }
        int pos = line.indexOf(sep);
        if (pos == -1) {
            return 0;
        }
        return pos + sep.length() + getIndex(line.substring(pos + sep.length()), counter - 1, sep);
    }

    public static LinkedList<String> split(String line, String separator) {
        LinkedList<String> v = new LinkedList<String>();
        String tab = separator;

        int lastTab = 0;
        while (true) {
            int nextTab = line.indexOf(tab, lastTab);
            if (nextTab == -1) {
                v.add(line.substring(lastTab));
                break;
            }
            v.add(line.substring(lastTab, nextTab));
            lastTab = nextTab + 1;
        }
        return v;
    }

    public static ArrayList<String> split(String line, String separator, int lineSize) {
        ArrayList<String> v = new ArrayList<String>(lineSize);
        String tab = separator;

        int lastTab = 0;
        while (true) {
            int nextTab = line.indexOf(tab, lastTab);
            if (nextTab == -1) {
                v.add(line.substring(lastTab));
                break;
            }
            v.add(line.substring(lastTab, nextTab));
            lastTab = nextTab + 1;
        }
        return v;
    }

    public static ArrayList<String> split(String line, char separator, int lineSize) {
        ArrayList<String> v = new ArrayList<String>(lineSize);
        char tab = separator;

        int lastTab = 0;
        while (true) {
            int nextTab = line.indexOf(tab, lastTab);
            if (nextTab == -1) {
                v.add(line.substring(lastTab));
                break;
            }
            v.add(line.substring(lastTab, nextTab));
            lastTab = nextTab + 1;
        }
        return v;
    }

    public static LinkedList<String> split(String line, char separator) {
        LinkedList<String> v = new LinkedList<String>();
        char tab = separator;

        int lastTab = 0;
        while (true) {
            int nextTab = line.indexOf(tab, lastTab);
            if (nextTab == -1) {
                v.add(line.substring(lastTab));
                break;
            }
            v.add(line.substring(lastTab, nextTab));
            lastTab = nextTab + 1;
        }
        return v;
    }


    public static LinkedList<String> split(Collection<String> lines, String sep) {
        LinkedList<String> v = new LinkedList<>();
        Iterator<String> itLines = lines.iterator();
        while (itLines.hasNext())
            v.addAll(split(itLines.next(), sep));
        return v;
    }


    public static LinkedList<String> split(String line, Collection<String> seperator) {
        LinkedList<String> v = new LinkedList<>(Collections.singleton(line));
        Iterator<String> seps = seperator.iterator();
        while (seps.hasNext())
            v = split(v, seps.next());
        return v;
    }

    public static String concatAll(Collection<String> words, String separator) {
        Iterator<String> wordIt = words.iterator();
        StringBuilder sb = new StringBuilder();
        if (0 < separator.length())
            while (wordIt.hasNext()) {
                sb.append(wordIt.next());
                if (wordIt.hasNext())
                    sb.append(separator);
            }
        else
            while (wordIt.hasNext())
                sb.append(wordIt.next());
        return sb.toString();
    }

    public static String replaceAll(String line, String replace, String replaceWith) {
        return concatAll(split(line, replace), replaceWith);
    }

    public static String replaceAll(String line, char replace, String replaceWith) {
        return concatAll(split(line, replace), replaceWith);
    }

    public static String replaceAll(String line, Collection<String> replace, String replaceWith) {
        return concatAll(split(line, replace), replaceWith);
    }

    public static String replaceAll(String line, Collection<Pair<String[], String>> replacements) {
        Iterator<Pair<String[], String>> itRep = replacements.iterator();
        String result = line;
        while (itRep.hasNext()) {
            Pair<String[], String> p = itRep.next();
            replaceAll(result, Arrays.asList(p.first), p.second);
        }
        return result;
    }

    public static String listToString(List<String> list) {
        return replaceAll(list.toString(), ' ', "");
    }

    public static LinkedList<String> stringToList(String s) {
        return split(s.substring(s.charAt(0) == '[' ? 1 : 0, s.charAt(s.length() - 1) == ']' ? s.length() - 1 : s.length()), ',');
    }

}