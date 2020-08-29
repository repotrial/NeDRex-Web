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
        ProcessBuilder pb = new ProcessBuilder("wget", "-O", fileName, url);
        try {
            ProcessUtils.executeProcessWait(pb);
        } catch (IOException | InterruptedException e) {
            log.error("Error executing download!");
            e.printStackTrace();
            return null;
        }
        return fileName;
    }

    public static File download(String url, File file) {
        try {
            download(url, file.getAbsolutePath()).charAt(0);
        } catch (NullPointerException e) {
            return null;
        }
        return file;
    }

    public static void formatJson(File file) {
        ProcessBuilder pb = new ProcessBuilder("sed","-i","s/},{\"/},\\n{\"/g", file.getAbsolutePath());
        try {
            ProcessUtils.executeProcessWait(pb);
        } catch (IOException | InterruptedException e) {
            log.error("Error converting json file!");
            e.printStackTrace();
        }
//        pb = new ProcessBuilder("sed","-i","s/},{\"sourceDomainId/},\\n{\"sourceDomainId/g", file.getAbsolutePath());
//        try {
//            ProcessUtils.executeProcess(pb);
//        } catch (IOException | InterruptedException e) {
//            log.error("Error converting json file!");
//            e.printStackTrace();
//        }
//
//        pb = new ProcessBuilder("sed","-i","s/},{\"memberOne/},\\n{\"memberOne/g", file.getAbsolutePath());
//        try {
//            ProcessUtils.executeProcess(pb);
//        } catch (IOException | InterruptedException e) {
//            log.error("Error converting json file!");
//            e.printStackTrace();
//        }
    }


}
