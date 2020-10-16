package de.exbio.reposcapeweb.communication.requests;

import java.util.HashMap;
import java.util.LinkedList;

public class UpdateRequest {
   public String id;
   public HashMap<String, Integer[]> nodes;
   public HashMap<String,String[]> edges;
}
