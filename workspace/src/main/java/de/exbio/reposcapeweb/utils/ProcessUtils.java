package de.exbio.reposcapeweb.utils;

import java.io.*;

public class ProcessUtils {

    public static void executeProcessWait(ProcessBuilder pb) throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader bw = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while(bw.readLine()!=null){}
        p.waitFor();
    }

    public static StringBuffer executeProcessWait(ProcessBuilder pb, StringBuffer sb) throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        while((line =br.readLine())!=null)
            sb.append(line).append('\n');
        p.waitFor();
        return sb;
    }



}
