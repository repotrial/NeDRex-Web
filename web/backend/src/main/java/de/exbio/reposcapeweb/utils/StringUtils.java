package de.exbio.reposcapeweb.utils;

import java.util.*;
import java.util.stream.Collectors;

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
        LinkedList<String> v = new LinkedList<>();
        String tab = separator;

        int lastTab = 0;
        while (true) {
            int nextTab = line.indexOf(tab, lastTab);
            if (nextTab == -1) {
                v.add(line.substring(lastTab));
                break;
            }
            v.add(line.substring(lastTab, nextTab));
            lastTab = nextTab + separator.length();
        }
        return v;
    }

    public static LinkedList<String> splitFirst(String line, char sep) {
        LinkedList<String> v = new LinkedList<>();
        int idx = line.indexOf(sep);
        if (idx > -1) {
            v.add(line.substring(0, idx));
            v.add(line.substring(idx));
        } else {
            v.add(line);
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

    public static String normalize(String in) {
        StringBuilder out = new StringBuilder();
        String l = in.toLowerCase();
        for (int i = 0; i < l.length(); ++i) {
            char c = l.charAt(i);
            switch (c) {
                case 'á', 'ä', 'à' -> out.append('a');
                case 'é', 'è', 'ê' -> out.append('e');
                case 'ï', 'í', 'ì' -> out.append('i');
                case 'ñ' -> out.append('n');
                case 'ö', 'ó', 'ò', 'ø' -> out.append('o');
                case 'ü', 'ú', 'ù' -> out.append('u');
                case 'ç' -> out.append('c');
                case 'α' -> out.append("alpha");
                case 'β' -> out.append("beta");
                case 'ω' -> out.append("omega");
                case 'ε' -> out.append("epsilon");
                case 'ψ' -> out.append("phi");
                case 'λ' -> out.append("lambda");
                case 'γ' -> out.append("gamma");
                case 'δ' -> out.append("delta");
                case 'ξ' -> out.append("chi");
                case 'μ' -> out.append("mu");
                case 'τ' -> out.append("tau");
                case 'κ' -> out.append("kappa");
                case 'ß' -> out.append("ss");
                case '–' -> out.append("-");
                case '⁰' -> out.append("0");
                case '¹' -> out.append("1");
                case '²' -> out.append("2");
                case '³', '₃' -> out.append("3");
                case '⁴' -> out.append("4");
                case '⁵' -> out.append("5");
                case '⁶' -> out.append("6");
                case '⁷' -> out.append("7");
                case '⁸' -> out.append("8");
                case '⁹' -> out.append("9");
                case '/', '*', ';', '&', ':', '<', '>', '$', '+', '�', '_', '?', ' ', ',', '@', '~', '^', '|', '#', '=', '→', '±', '%' -> out.append(' ');
                case '\"', '’', '′' -> out.append('\'');
                case '-', '(', ')', '\'', '.', '[', ']', '{', '}' -> out.append(c);
                case '−', '\u00AD', '—' -> out.append("-");
                default -> {
                    if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
                        out.append(c);
                    } else if (c == 8202) {
                        out.append(' ');
                    }
//                    else{
//                        System.err.println("Found illegal char at normalization: "+c+" char code = "+((int)c));
//                    }
                }
            }
        }
        return out.toString();
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
        LinkedList<String> v = new LinkedList<>();

        int lastTab = 0;
        while (true) {
            int nextTab = line.indexOf(separator, lastTab);
            if (nextTab == -1) {
                String sub = line.substring(lastTab);
                if (sub.length() > 0)
                    v.add(sub);
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

    public static String concatAll(Collection<String> words, char separator) {
        Iterator<String> wordIt = words.iterator();
        StringBuilder sb = new StringBuilder();
        while (wordIt.hasNext()) {
            sb.append(wordIt.next());
            if (wordIt.hasNext())
                sb.append(separator);
        }
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
        return replaceAll(list.toString(), ", ", ",");
    }

    public static LinkedList<String> stringToList(String s) {
        return split(s.substring(s.charAt(0) == '[' ? 1 : 0, s.charAt(s.length() - 1) == ']' ? s.length() - 1 : s.length()), ',');
    }

    public static HashMap<String, String> stringToMap(String s) {
        HashMap<String, String> out = new HashMap<>();
        stringToList(s).forEach(i->{
            LinkedList<String> e = splitFirst(i, ':');
            out.put(e.get(0), e.get(1));
        });

        return out;
    }

    public static String mapToString(HashMap<String, String> map) {
        return listToString(map.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList()));
    }

    public static LinkedList<String> convertBase64(String data) {
        if (data.indexOf(',') > -1)
            data = StringUtils.split(data, ',').get(1);
        return StringUtils.split(new String(Base64.getDecoder().decode(data)), '\n');
    }

}
