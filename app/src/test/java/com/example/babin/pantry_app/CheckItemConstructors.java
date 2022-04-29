package com.example.babin.pantry_app;
import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.*;
public class CheckItemConstructors {
    @Test
    public void testItemClass(){
        Item item1 = new Item();
        Item item2 = new Item();
        assert (item1.isEqual(item2));
        Item item3 = new Item("Peter Pan","Creamy Peanut Butter","000111222333","00000111222333","","","");
        assert (!item1.isEqual(item3)); //These items arent the same, negate it so the test passes.  If the test fails it means it thought they were the same
        Item item4 = item1;
        item4.setName("Creamy Peanut Butter");
        item4.setBrand("Peter Pan");
        item4.setUpc12("000111222333");
        item4.setUpc14("00000111222333");
        assert (item3.isEqual(item4));//should be true
    }

}
 