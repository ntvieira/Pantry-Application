package com.example.babin.pantry_app;

import java.util.Comparator;
import java.util.HashMap;

public class sortByNameDec implements Comparator<HashMap <String, String>>
{
    public int compare(HashMap<String, String> a, HashMap<String, String> b) {
        String aName = a.toString();
        String bName = b.toString();
        if(aName.contains("  ") && bName.contains("  ")) {
            aName = aName.substring(0, aName.indexOf("  "));
            bName = bName.substring(0, bName.indexOf("  "));
        }
        int sComp = bName.compareTo(aName);
        return sComp;
    }

}