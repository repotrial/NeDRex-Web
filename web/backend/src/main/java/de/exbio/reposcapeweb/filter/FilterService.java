package de.exbio.reposcapeweb.filter;

import de.exbio.reposcapeweb.utils.ReaderUtils;
import de.exbio.reposcapeweb.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.LinkedList;

@Service
public class FilterService {


    private final Logger log = LoggerFactory.getLogger(FilterService.class);

    public void writeToFile(NodeFilter nf, File f) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f.getAbsoluteFile() + ".distinct"));

            nf.getDistinctMap().forEach((type, keys) -> keys.forEach((key, entries) -> entries.forEach(e -> {
                try {
                    bw.write(type.ordinal() + "\t" + nf.getDistinctID2Keys().get(type).get(key).getKey() + "\t" + nf.getDistinctID2Keys().get(type).get(key).getName().trim()+ "\t"+ e.getNodeId() + "\t" + e.getName() + "\n");
                } catch (IOException |NullPointerException exception) {
                    exception.printStackTrace();
                }
            })));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f.getAbsoluteFile() + ".unique"));

            nf.getUniqueMap().forEach((type, keys) -> keys.forEach((key, e) -> {
                try {
                    bw.write(type.ordinal() + "\t" + nf.getUniqueID2Keys().get(type).get(key).getKey() +"\t"+ nf.getUniqueID2Keys().get(type).get(key).getName().trim()+ "\t" + e.getNodeId() + "\t" + e.getName() + "\n");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NodeFilter readFromFiles(File f) {
        NodeFilter nf = new NodeFilter();
        String line = "";

        try {
            BufferedReader br = ReaderUtils.getBasicReader(new File(f.getAbsoluteFile() + ".unique"));
            while ((line = br.readLine()) != null) {
                LinkedList<String> values = StringUtils.split(line, '\t');
                nf.addUnique(FilterType.values()[Integer.parseInt(values.get(0))], values.get(1), values.get(2),values.get(4), Integer.parseInt(values.get(3)));
            }
            br.close();
        } catch (IOException e) {
            log.debug("Read error on filter import for " + f.getName());
            return null;
        }

        try {
            BufferedReader br = ReaderUtils.getBasicReader(new File(f.getAbsoluteFile() + ".distinct"));
            while ((line = br.readLine()) != null) {
                LinkedList<String> values = StringUtils.split(line, '\t');
                nf.addDistinct(FilterType.values()[Integer.parseInt(values.get(0))], values.get(1), values.get(2),values.get(4), Integer.parseInt(values.get(3)));
            }
            br.close();
        } catch (IOException e) {
            log.debug("Read error on filter import for " + f.getName());
            return null;
        }

        return nf;
    }


}
