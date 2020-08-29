package de.exbio.reposcapeweb.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

public class ProcessUtils {

    public static void executeProcessWait(ProcessBuilder pb) throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader bw = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while(bw.readLine()!=null){}
        p.waitFor();
    }



}
