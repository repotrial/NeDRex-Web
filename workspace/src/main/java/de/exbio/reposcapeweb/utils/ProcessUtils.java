package de.exbio.reposcapeweb.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtils {

    public static void executeProcess(ProcessBuilder pb) throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader bw = new BufferedReader(new InputStreamReader(p.getInputStream()));
//        String line = "";
//        while ((line = bw.readLine()) != null) {
//            System.out.println(line);
//        }
        while(bw.readLine()!=null){}
        p.waitFor();
    }
}
