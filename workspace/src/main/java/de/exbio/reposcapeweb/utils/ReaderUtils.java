package de.exbio.reposcapeweb.utils;

import java.io.*;
import java.net.URL;

public class ReaderUtils {

    public static BufferedReader getBasicReader(File file) {
        try {
            return new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedReader getBasicReader(String url) {
        return getBasicReader(new File(url));
    }

    public static InputStream getInputStream(String url) {
        try {
            return new FileInputStream(url);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream getInputStream(File file) {
        return getInputStream(file.getAbsolutePath());
    }

    public static BufferedReader getBufferedReader(URL url) {
        try {
            return new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUrlContent(URL url) {
        StringBuffer content = new StringBuffer();
        BufferedReader br = getBufferedReader(url);
        String line = "";
        try {
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
