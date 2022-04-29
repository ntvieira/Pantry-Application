package com.example.babin.pantry_app;
import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.*;
public class CheckSortByName {
    @Test
    public void checkIfSorts(){
        ArrayList<Item> toBeSorted = new ArrayList<>();
        Random rand = new Random();
        //populate arraylist with these wonderful hashmap<string, string>
        for(int i = 0; i < 10; i ++){
            Item item = new Item();
            item.setName("name" + rand.nextInt(10));
            toBeSorted.add(item);
        }
        //sort it

        Collections.sort(toBeSorted, new sortByNameInc());
        //check if its sorted
        boolean isInOrder = true;
        for(int i = 0; i < toBeSorted.size() - 1; i++){
            String str = toBeSorted.get(i).getName();
            String str2 = toBeSorted.get(i+1).getName();
            if(str.compareTo(str2) > 1) {
                isInOrder = false;
                assert isInOrder;
                break;
            }
        }
    }

}
