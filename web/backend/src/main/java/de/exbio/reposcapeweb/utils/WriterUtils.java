package de.exbio.reposcapeweb.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriterUtils {

    public static void writeTo(File outFile, String message){
        outFile.getParentFile().mkdirs();
        try {
            BufferedWriter bw = getBasicWriter(outFile);
            bw.write(message);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static BufferedWriter getBasicWriter(File outFile){
        if(!outFile.exists()) {
            try {
                outFile.getParentFile().mkdirs();
                outFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            return new BufferedWriter(new FileWriter(outFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
