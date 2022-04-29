package com.example.babin.pantry_app;



import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CheckGroceryListAdded {
    @Test
    public void checkTWoLists() {
        ArrayList<Item> list1 = new ArrayList<>();
        ArrayList<Item> list2 = new ArrayList<>();
        Item it = new Item();
        it.setExpirationDateToday();
        String date = it.getExpirationDate();
        for(int i = 0; i < 10; i++){
            Item item = new Item("", i+"", "", "", "", "", date);
            list1.add(item);
            item .setExpirationDate("");
            item.setExpirationDateToday();
            list2.add(item);
        }
        assertEquals(list1,list2);
    }
}