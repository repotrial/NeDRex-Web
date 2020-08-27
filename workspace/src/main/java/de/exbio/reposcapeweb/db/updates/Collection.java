package de.exbio.reposcapeweb.db.updates;

import java.io.File;

public interface Collection {

    public String getName();

    public void setName(String name);

    public File getFile();

    public void setFile(File file);

}
