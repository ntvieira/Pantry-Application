package com.example.babin.pantry_app;

import android.util.Log;

import java.util.Comparator;
import java.util.HashMap;

public class sortByNameInc implements Comparator<Item>
{
    //This method implements compare to use with Collections.sort
    public int compare(Item one, Item two) {
        String nameOne = one.getName().toLowerCase();
        String nameTwo = two.getName().toLowerCase();
        //if the names are equal return 0, standard compare stuff
        if (nameOne.equals(nameTwo)) {
            return 0;
        }
        if(nameOne.length() == nameTwo.length()){
            return nameOne.compareTo(nameTwo);
        } else if (nameOne.length() < nameTwo.length()) { //the compareTo function did weird things when comparing
            //String of equal length it wanted to put aaa before a, so we handle that different
            String bTemp = nameTwo.substring(0, nameOne.length());
            if(nameOne.equals(bTemp)){
                return -1;
            } else {
                return nameOne.compareTo(bTemp);
            }
            //same as above, it was handling aaa and a not how we wanted, this fixes that
        } else if (nameOne.length() > nameTwo.length()) {
            String aTemp = nameOne.substring(0, nameTwo.length());
            if(aTemp.equals(nameTwo)){
                return aTemp.compareTo(nameTwo);
            } else {
                return nameOne.compareTo(nameTwo);
            }
            //return aName.substring(0, bName.length()-1).compareTo(bName);
        }
        return 999999999;
    }
}
