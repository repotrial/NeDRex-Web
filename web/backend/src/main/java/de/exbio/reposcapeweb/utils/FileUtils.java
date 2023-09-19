package de.exbio.reposcapeweb.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    private final static Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Function for Downloading a file located at given url to specific path.
     * Download is executed by unix-environment command 'wget'.
     * Redirection and reading the inputstream is necessary for p.waitFor() to terminate eventually.
     *
     * @param url      Location where to find the web resource.
     * @param fileName Destination of the downloaded file.
     * @return Path of the downloaded file.
     */
    public static String download(String url, String fileName) {
        log.debug("Downloading " + url + " to " + fileName);
        ProcessBuilder pb = new ProcessBuilder("wget", "-O", fileName, url, "--no-check-certificate");
        try {
            ProcessUtils.executeProcessWait(pb, false);
        } catch (IOException | InterruptedException e) {
            log.error("Error executing download!");
            e.printStackTrace();
            return null;
        }
        return fileName;
    }

    public static File downloadPaginated(String url, File mergeScript, File file, int total, File jsonFormatter) {
        int batch = 10000;
//        WriterUtils.writeTo(file, "[");
        try {
            for (int i = 0; i < total; i += batch) {
                File part = new File(file.getParentFile(), file.getName() + "_" + (1 + (i / batch)));
                log.debug("Downloading part "+(1+(i / batch))+"/"+(1+(total / batch)));
                download(url + "?offset=" + i + "&limit=" + batch, part.getAbsolutePath()).charAt(0);
                formatJson(jsonFormatter,part);
                ProcessBuilder pb = new ProcessBuilder(mergeScript.getAbsolutePath(), part.getAbsolutePath(), file.getAbsolutePath());
                if (i + batch >= total)
                    pb.command().add("true");
                ProcessUtils.executeProcessWait(pb, true);
                part.delete();
            }
        } catch (NullPointerException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File download(String url, File file) {
        try {
            download(url, file.getAbsolutePath()).charAt(0);
        } catch (NullPointerException e) {
            return null;
        }
        return file;
    }

    public static void formatJson(File jsonReformatter, File file) {
        ProcessBuilder pb = new ProcessBuilder(jsonReformatter.getAbsolutePath(), file.getAbsolutePath());
        try {
            ProcessUtils.executeProcessWait(pb, false);
        } catch (IOException | InterruptedException e) {
            log.error("Error converting json file!");
            e.printStackTrace();
        }
    }


    public static void deleteDirectory(File updateDir) {
        try {
            org.apache.tomcat.util.http.fileupload.FileUtils.deleteDirectory(updateDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
