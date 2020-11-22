package de.exbio.reposcapeweb.db.services;

import de.exbio.reposcapeweb.utils.Pair;

import java.util.HashMap;

public abstract class NodeService {

    public abstract HashMap<Integer, Pair<String,String>> getIdToDomainMap();
    public abstract String[] getListAttributes();

    public abstract String getName(int id);
}
