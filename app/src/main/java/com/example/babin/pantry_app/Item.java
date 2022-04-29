package com.example.babin.pantry_app;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Item {
    //Item class to represent
    private String brand;
    private String name;
    private String upc12;
    private String upc14;
    private String dateAdded;
    private String quantity;
    private String expirationDate;
    //Takes a database reference and adds the scanned instance of
    // the item to the grocery/inventory depending on the database ref
    public void addItemToDataBase(DatabaseReference ref) {
        DatabaseReference dbref = ref.child(this.upc12);
        dbref.child("brand").setValue(this.getBrand());
        dbref.child("name").setValue(this.getName());
        dbref.child("upc14").setValue(this.getUpc14());
        dbref.child("dateAdded").setValue(this.getDateAdded());
        dbref.child("quantity").setValue(this.getQuantity());
        dbref.child("expirationDate").setValue(this.getExpirationDate());
    }
    //add a mantually entered item to the grocery or inventory
    //reference as passed in
    public void addItemToItemDB(DatabaseReference ref) {
        ref.child(this.upc12).child("brand").setValue(this.getBrand());
        ref.child(this.upc12).child("name").setValue(this.getName());
        ref.child(this.upc12).child("upc14").setValue(this.getUpc14());
    }
    
    public static HashMap<String, String> getHashMapFromItems(ArrayList<Item> items){
        //used to populate the listview when the activity is first enterd
        //key is the name, experation date is the value
        HashMap<String, String> retHashMap = new HashMap<String, String>();
        for(int i = 0; i < items.size(); i++){
            retHashMap.put(items.get(i).getName(), items.get((i)).getExpirationDate());
        }
        return retHashMap;
    }

    public boolean equalNameAndExpDate(Item item){
        //check if two items have the same name and expiration date
        //used to update the items on collision in grocery/inventory
        if(this.getName().equals(item.getName()) && this.getExpirationDate().equals(item.getExpirationDate()))
            return true;
        return false;
    }
    public String toString(){
        if(getExpirationDate() == null || getExpirationDate().equals("")) {
            if(getBrand().equals(""))
                return getQuantity() + "  " + getName();
            return getQuantity() + "  "  + getName() + "  " + getBrand();
        }
        else {
            if(getBrand().equals(""))
                return getQuantity() + "  " + getName() + "\n" + getExpirationDate();
            return getQuantity() + "  " + getName() + "  " + getBrand() + "\n" + getExpirationDate();
        }
    }


    //method to get an arraylist of strings(names) from a collection of items
    public static ArrayList<String> getStringsItems(ArrayList<Item> items){
        ArrayList<String> retItems = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++){
            retItems.add(items.get(i).getName());
        }
        return retItems;
    }
    //default constructor
    public Item (){
        brand = "";
        name = "";
        upc12 = "";
        upc14 = "";
        dateAdded = "";
        quantity = "";
        expirationDate = "";
    }
    //get the quantity of an item as an int
    public int getQuantityAsInt(){
        return Integer.parseInt(this.getQuantity());
    }
    //get the quantity of an item as a double
    public double getQuantityAsDouble(){return Double.parseDouble(this.getQuantity());}
    //parameterized constructor
    public Item(String brand, String name, String upc12, String upc14, String dateAdded, String quantity, String expirationDate){
        this.brand = brand;
        this.name = name;
        this.upc12 = upc12;
        this.upc14 = upc14;
        this.dateAdded = dateAdded;
        this.quantity = quantity;
    }
    //getters and setters
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpc12() {
        return upc12;
    }

    public void setUpc12(String upc12) {
        this.upc12 = upc12;
    }

    public String getUpc14() {
        return upc14;
    }

    public void setUpc14(String upc14) {
        this.upc14 = upc14;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded() {
        Date date = Calendar.getInstance().getTime();//getting date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");//formating according to my need
        String strDate = simpleDateFormat.format(date);
        this.dateAdded = strDate;
    }

    public void setExpirationDateToday() {
        Date date = Calendar.getInstance().getTime();//getting date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");//formating according to my need
        String strDate = simpleDateFormat.format(date);
        this.expirationDate = strDate;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    //check if two items are equal
    public boolean isEqual(Item item){
        if (this.getName().equals(item.getName()) &&
        this.getUpc12().equals(item.getUpc12()) &&
        this.getUpc14().equals(item.getUpc14()) &&
        this.getBrand().equals(item.getBrand())) {
            return true;
        } else {
            return false;
        }
    }
}
