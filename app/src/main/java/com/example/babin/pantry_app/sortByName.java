package com.example.babin.pantry_app;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class sortByName {
    public static void sortByNameInc(ArrayList<HashMap<String, String>> hs){
        //Parse out the information from the Hashmap<String, String>
        //This method is worse than the sortByNameInc
        //But it works, the Collections.sort is just nlogn and this is n2
        for(int i = 0; i < hs.size()-1; i++){
            for(int j = i+1; j <hs.size(); j++) {
                String aName = hs.get(i).toString();
                String bName = hs.get(j).toString();

                if (aName.contains("  ") && bName.contains("  ")) {
                    aName = aName.substring(aName.indexOf("  ") + 2).toUpperCase();
                    bName = bName.substring(bName.indexOf("  ") + 2).toUpperCase();
                }
                Log.d("aName", aName);
                Log.d("bName", bName);
                if (aName.equals(bName)) {
                    continue;
                }
                if(aName.length() == bName.length()){
                    for(int p = 0; p < aName.length(); p++) {
                        if (aName.charAt(i) < bName.charAt(i)) {
                            //do nothing
                        } else if (aName.charAt(i) > bName.charAt(i)) {
                            HashMap<String, String> one, two;
                            one = hs.get(i);
                            two = hs.get(j);
                            hs.set(i, two);
                            hs.set(j, one);
                        }
                    }
                } else if(aName.length() < bName.length()){
                    for(int p = 0; p < aName.length(); p++) {
                        if (aName.charAt(i) < bName.charAt(i)) {
                            //do nothing
                        } else if (aName.charAt(i) > bName.charAt(i)) {
                            HashMap<String, String> one, two;
                            one = hs.get(i);
                            two = hs.get(j);
                            hs.set(i, two);
                            hs.set(j, one);

                        }
                    }
                } else if(aName.length() > bName.length()){
                    for(int p = 0; p <bName.length(); p++) {
                        if (aName.charAt(i) < bName.charAt(i)) {
                            //do nothing
                        } else if (aName.charAt(i) > bName.charAt(i)) {
                            HashMap<String, String> one, two;
                            one = hs.get(i);
                            two = hs.get(j);
                            hs.set(i, two);
                            hs.set(j, one);

                        }
                    }
                    HashMap<String, String> one, two;
                    one = hs.get(i);
                    two = hs.get(j);
                    hs.set(i, two);
                    hs.set(j, one);
                }
            }
        }
    }
}
