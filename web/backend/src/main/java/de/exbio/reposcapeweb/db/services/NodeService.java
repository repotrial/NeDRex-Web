package de.exbio.reposcapeweb.db.services;

import java.util.HashMap;

public abstract class NodeService {

    public abstract HashMap<Integer,String> getIdToDomainMap();
    public abstract String[] getListAttributes();

}
