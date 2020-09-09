package de.exbio.reposcapeweb.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class DBUtils {
    private static final Logger log = LoggerFactory.getLogger(DBUtils.class);

    private static File tempDir = null;

    private static final File queryExecuter = new File("../scripts/executeSQLCommand.sh");
    private static final File tableToFile = new File("../scripts/tableToFile.sh");


    public static File getTempDir() {
        return tempDir;
    }

    public static boolean setTempDir(String tempPath) {
        //TODO check read/write access
        if (tempPath != null)
            tempDir = new File(tempPath);
        if (tempDir == null || !tempDir.exists()) {
            tempDir = new File(DBUtils.checkSetting("secure_file_priv"));
            return tempDir.exists();
        }
        return true;
    }

    public static String checkSetting(String parameter) {
        //TODO make more elegant paths
        ProcessBuilder pb = new ProcessBuilder(queryExecuter.getAbsolutePath(), "SHOW variables;");
        String value = "";
        try {
            for (String line : StringUtils.split(ProcessUtils.executeProcessWait(pb, new StringBuffer()).toString(), '\n')) {
                if (line.startsWith(parameter)) {
                    value = line.split("\t")[1];
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void executeNodeDump(File dest, String table) {
        if (!dest.exists())
            dest.mkdirs();
        log.debug("Executing dump for " + table + " to " + dest.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder(tableToFile.getAbsolutePath(), queryExecuter.getParent(), dest.getAbsolutePath(), table, "SELECT id, primary_domain_id FROM " + table + ";");
        try {
            ProcessUtils.executeProcessWait(pb);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
