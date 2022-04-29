package com.example.babin.pantry_app;

import android.util.Log;

import java.util.Comparator;
import java.util.HashMap;

public class sortByQuantity implements Comparator<Item>
{
    //This method implements the compare to be used in the Collections.sort method.
    public int compare(Item one, Item two) {
        //Parse out the information we need from the Hashmap<String, String>
        int quanOne = one.getQuantityAsInt();
        int quanTwo = two.getQuantityAsInt();
        //We have parsed the string, we not need to parse the value as a number
        //standard compareTo stuff
        if(quanOne < quanTwo)
            return -1;
        else if(quanOne < quanTwo)
            return 1;
        return 0;
    }
}