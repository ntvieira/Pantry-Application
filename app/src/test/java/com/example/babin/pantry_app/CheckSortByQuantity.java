package com.example.babin.pantry_app;
import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.*;
public class CheckSortByQuantity {
    @Test
    public void checkIfSorts(){
        ArrayList<Item> toBeSorted = new ArrayList<>();
        Random rand = new Random();
        //populate arraylist with these wonderful hashmap<string, string>
        for(int i = 0; i < 10; i ++){
            Item item = new Item();
            item.setQuantity(rand.nextInt(10)+"");
            toBeSorted.add(item);
        }
        //sort it
        Collections.sort(toBeSorted, new sortByQuantity());
        //check if its sorted
        boolean isInOrder = true;
        for(int i = 0; i < toBeSorted.size() - 1; i++){
            int int1 = toBeSorted.get(i).getQuantityAsInt();
            int int2 = toBeSorted.get(i+1).getQuantityAsInt();
            if(int1 > int2) { // we found something out of order
                isInOrder = false;
                break;
            }
        }
        assert isInOrder;
    }

}
