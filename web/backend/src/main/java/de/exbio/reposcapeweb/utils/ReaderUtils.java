package de.exbio.reposcapeweb.utils;

import java.io.*;
import java.net.URL;

public class ReaderUtils {

    public static BufferedReader getBasicReader(File file) throws FileNotFoundException {
        return new BufferedReader(new FileReader(file));
    }

    public static BufferedReader getBasicReader(String url) throws FileNotFoundException {
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
            throw new RuntimeException("Unable to read attributes from "+url+ "! Either the network is down or names have changed.");
        }
    }

    public static String getUrlContent(URL url) {
        StringBuilder content = new StringBuilder();
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


    public static String getFileContent(File licenceFile) {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader br = getBasicReader(licenceFile);
            String line = "";
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
