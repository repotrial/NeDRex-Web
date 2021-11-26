package de.exbio.reposcapeweb.utils;

import de.exbio.reposcapeweb.tools.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ProcessUtils {

    private static Logger log = LoggerFactory.getLogger(ToolService.class);

    public static void executeProcessWait(ProcessBuilder pb, boolean print) throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        log.debug("Waiting for process: " + pb.command());
        Process p = pb.start();
        BufferedReader bw = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        while ((line = bw.readLine()) != null) {
            if (print)
                System.out.println(line);
        }
        p.waitFor();
    }

    public static Process executeProcess(ProcessBuilder pb) throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        pb.redirectOutput(new File("/dev/null"));
        return pb.start();
    }

    public static void executeProcessWait(String[] command, boolean print) throws IOException, InterruptedException {
        executeProcessWait(new ProcessBuilder(command), print);
    }

    public static void executeProcessWait(String command, boolean print) throws IOException, InterruptedException {
        log.debug("Waiting for process: " + command);
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader bw = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        while ((line = bw.readLine()) != null) {
            if (print)
                System.out.println(line);
        }
        p.waitFor();
    }

    public static StringBuffer executeProcessWait(ProcessBuilder pb, StringBuffer sb) throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        log.debug("Listening to process: " + pb.command());
        Process p = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        while ((line = br.readLine()) != null)
            sb.append(line).append('\n');
        p.waitFor();
        return sb;
    }


}
