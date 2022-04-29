package com.example.babin.pantry_app;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class GroceryList extends AppCompatActivity implements View.OnClickListener {

    //variables for elements
    private EditText text_input;
    private EditText quantity_input;
    private Button add_button;
    private SwipeMenuListView grocery_list;
    private Button item_sort_name;
    private Button item_sort_quan;

    //list and list adapter to keep track of entered elements
    private ArrayList<Item> scanned_items;
    //private ArrayList<String> scanned_names;
    private ArrayAdapter<String> adapter;

    //new variables
    //adaparters allow the view of the listView to be updated
    private ArrayAdapter adapter2;
    private HashMap<String, String> scanned_names;
    private ArrayList<Item> scanned_list;
    boolean flag;

    private Button buy_button;



    //list of keys used to delete from Database
    private ArrayList<String> listKeys = new ArrayList<String>();

    //used to read and write from DataBase
    private DatabaseReference mDatabase;

    //used to store user's individual grocery list data
    private String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(this, "Swipe items to modify", Toast.LENGTH_SHORT).show();
        scanned_items = new ArrayList<Item>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Grocery List");
        item_sort_name = findViewById(R.id.sort_by_name);
        item_sort_name.setOnClickListener(this);
        item_sort_quan = findViewById(R.id.sort_by_quantity);
        item_sort_quan.setOnClickListener(this);
        buy_button = findViewById(R.id.buy_all);
        buy_button.setOnClickListener(this);

        //assigns elements to variables by ID
        text_input = findViewById(R.id.text_input);
        quantity_input = findViewById(R.id.quantity_input);
        add_button = findViewById(R.id.add_item);
        grocery_list = findViewById(R.id.grocery_list);

        //next two lines make it so users cant add items if the fields are empty
        //THIS DISABLES AND ENABLES THE BUTTON DEPENDING ON THE TEXT FIELDS BEING NON-EMPTY
        quantity_input.addTextChangedListener(textWatcher);
        text_input.addTextChangedListener(textWatcher);
        add_button.setEnabled(false);

        //updated variable
        scanned_list = new ArrayList<Item>();
        scanned_names = new HashMap<String, String>();

        //initialize adapter
        /*adapter2 = new SimpleAdapter(this, scanned_list,
                R.layout.sub_item2,
                new String[] { "line1"},
                new int[] {R.id.itemView});*/
        adapter2 = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, scanned_list);

        //connect the list and adapter
        grocery_list.setAdapter(adapter2);
        add_button.setBackgroundColor(Color.GRAY);
        add_button.setOnClickListener(this);

//        grocery_list.setOnItemClickListener(this);
//        grocery_list.setOnItemLongClickListener(this);

        mDatabase.addValueEventListener(valueEventListener);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "increment" item
                SwipeMenuItem incrementItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                incrementItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                incrementItem.setWidth(180);
                // set item title
                incrementItem.setIcon(R.drawable.icons8_plus_math_24);
                // add to menu
                menu.addMenuItem(incrementItem);

                //Create "decrement" item
                SwipeMenuItem decrementItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                decrementItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                decrementItem.setWidth(180);
                // set item title
                decrementItem.setIcon(R.drawable.icons8_minus_24);
                // add to menu
                menu.addMenuItem(decrementItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(180);
                // set a icon
                deleteItem.setIcon(R.drawable.icons8_waste_24);
                // add to menu
                menu.addMenuItem(deleteItem);

                //Create "buy" item
                SwipeMenuItem buyItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                buyItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                buyItem.setWidth(180);
                // set item title
                buyItem.setIcon(R.drawable.icons8_buy_24);
                // add to menu
                menu.addMenuItem(buyItem);
            }
        };
        grocery_list.setMenuCreator(creator);
        grocery_list.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // increment
                        incrementItem(position, menu, index);
                        break;
                    case 1:
                        // decrement
                        decrementItem(position, menu, index);
                        break;
                    case 2:
                        // delete
                        deleteItem(position, menu, index, true);
                        break;
                    case 3:
                        // buy
                        buyItem(position, menu, index);
                        break;
                }
                // false : close the menu; true : not close the menu
                return true;
            }
        });
    }
    //This disables the button to only allow the user to enter data how we handle it.
    //The buttons are disabled if they do something bad or if the fields are empty(dont allow null)
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            add_button.setEnabled(false);
            add_button.setBackgroundColor(Color.GRAY);
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String itemName = text_input.getText().toString().trim();
            String quantity = quantity_input.getText().toString().trim();
            if ((!itemName.isEmpty() && !quantity.isEmpty() && itemName.charAt(0) != '0' && isValidQuan(quantity) &&
                    itemName.charAt(0) != '1' && itemName.charAt(0) != '2' && itemName.charAt(0) != '3' &&
                    itemName.charAt(0) != '4' && itemName.charAt(0) != '5' && itemName.charAt(0) != '6' &&
                    itemName.charAt(0) != '7' && itemName.charAt(0) != '8' && itemName.charAt(0) != '9')){
                add_button.setBackgroundColor(Color.rgb(0,133,119));
                add_button.setEnabled(true);
            }
        }



        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    //Method to check not not allow "." by itself (breaks app)
    public boolean isValidQuan(String quan){
        if(quan.charAt(0) == '.' && quan.length() == 1)
            return false;
        else {
            int a = 0;
            try{
                a = Integer.parseInt(quan);
            } catch (NumberFormatException e){

            }
            return a != 0;
        }
    }

    public ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
            scanned_names.clear();

            scanned_list.clear();
            //iterate over everything in the item database
            //Check if the key is a uPC12 or a name.
            //UPC12 has only digits, Name has 1+ letter
            for (DataSnapshot d: dataSnapshot.getChildren()) {
                Item i = new Item();
                boolean temp = false;
                String key = d.getKey();
                for(int a = 0; a < key.length(); a++){
                    if(alpha.contains(key.charAt(a)+"")) {
                        temp = true;
                        break;
                    }
                }
                if(temp) {
                    //It's not a Upc12, its a manual item
                    i.setName(d.getKey());
                    i.setQuantity(d.child("quantity").getValue(String.class));
                } else { //its a scanned item
                    i.setName(d.child("name").getValue(String.class));
                    i.setBrand(d.child("brand").getValue(String.class));
                    i.setUpc14(d.child("upc14").getValue(String.class));
                    i.setUpc12(d.getKey());
                    i.setQuantity(d.child("quantity").getValue(String.class));
                    i.setExpirationDate(d.child("expirationDate").getValue(String.class));
                    i.setDateAdded(d.child("dateAdded").getValue(String.class));
                }
                scanned_list.add(i);
            }
            adapter2.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };



    public void onClick(View V) {
        //Check if the list is already sorted alphabetically, if so, we reverse sort it
        if(V.getId() == R.id.sort_by_name){
            ArrayList tempList = new ArrayList(scanned_list);
            Collections.sort(tempList, new sortByNameInc());
            if(tempList.equals(scanned_list)) {
                //we sorted temp list so if they're equal it means its in increasing order
                //we then want to reverse scanned_list to do decreasing order
                Collections.reverse(scanned_list);
            } else { //its not sorted, sort it
                Collections.sort(scanned_list, new sortByNameInc());
            }
            adapter2.notifyDataSetChanged();
        }
        if(V.getId() == R.id.sort_by_quantity){
            //check if it is sorted in decending order
            //if it isnt, we sort in decending, otherwise its sorted so reverse it to be increasing
            ArrayList tempList = new ArrayList(scanned_list);
            Collections.sort(tempList, new sortByQuantity());
            if(tempList.equals(scanned_list)) {
                //we sorted temp list so if they're equal it means its in increasing order
                //we then want to reverse scanned_list to do decreasing order
                Collections.reverse(scanned_list);
            } else { //its not sorted, sort it
                Collections.sort(scanned_list, new sortByQuantity());
            }
            adapter2.notifyDataSetChanged();
        }


        if(V.getId() == R.id.add_item)
        {
            //add a custom item to the grocery list
            if(text_input.getText().toString() != "")
            {
                final String item_entered = text_input.getText().toString();
                final String quantity_entered = quantity_input.getText().toString();
                final String realQuantity;
                //if the field is empty, default to 1
                if(quantity_entered.equals("")) {
                    realQuantity = "1";
                }
                else {
                    realQuantity = quantity_entered;
                }
                //Check if what we're trying to add is already in the groc DB before we add it
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Grocery List").child(item_entered);
                dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //key doesn't exist
                        if(dataSnapshot.getChildrenCount() == 0 || dataSnapshot == null) {
                            Item i = new Item();
                            i.setDateAdded();
                            i.setExpirationDateToday();
                            mDatabase.child(item_entered).child("quantity").setValue(realQuantity);
//                            //Adding a dateAdded and expirateion date to custom items in the grocery list page
                            mDatabase.child(item_entered).child("dateAdded").setValue(i.getDateAdded());
                            mDatabase.child(item_entered).child("expirationDate").setValue(i.getExpirationDate());
                        }
                        else {
                            //key exists, we need to update the quantity
                            int quant = Integer.parseInt(realQuantity);
                            int quant2 = Integer.parseInt(dataSnapshot.child("quantity").getValue(String.class));
                            quant += quant2;
                            mDatabase.child(item_entered).child("quantity").setValue(quant + "");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                //clears out text from the input area
                text_input.setText("");
                quantity_input.setText("");

                Toast.makeText(this, "Item successfully added", Toast.LENGTH_SHORT).show();
            }
        }

        if(V.getId() == R.id.buy_all){
            //iterate over all the items in the grocery list
            //check if theyre in the inventory list, if so update quantity
            //if not, just add them
            final DatabaseReference gListref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Grocery List");
            scanned_items.clear();
            populateWithGroceryList(gListref);
            final DatabaseReference invListref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Inventory");
            addShoppingListToInventory(invListref);
            gListref.setValue(null);
        }
    }
    //does the iterating over the grocery/inventory to check if theyre in there or not, as mentioned above
    public void addShoppingListToInventory(final DatabaseReference dbref) {
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Date date = Calendar.getInstance().getTime();//getting date
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");//formating according to my need
                final String strDate = simpleDateFormat.format(date);
                for(Item i:scanned_items) {
                    boolean inventoryContains = false;
                    for(DataSnapshot d: dataSnapshot.getChildren()){
                        if(i.getName().equals(d.getKey().toString())){
                            int a;
                            a = Integer.parseInt(d.child("quantity").getValue(String.class));
                            a += i.getQuantityAsInt();
                            dbref.child(d.getKey()).child("quantity").setValue(a+"");
                            inventoryContains = true;
                            break;
                        } else if(i.getUpc12().equals(d.getKey().toString())){
                            int a;
                            a = Integer.parseInt(d.child("quantity").getValue(String.class));
                            a += i.getQuantityAsInt();
                            dbref.child(d.getKey()).child("quantity").setValue(a+"");
                            inventoryContains = true;
                            break;
                        }
                    }
                    if(!inventoryContains) {
                        if(i.getUpc12()=="") {
                            dbref.child(i.getName()).child("quantity").setValue(i.getQuantity());
                            dbref.child(i.getName()).child("expirationDate").setValue(strDate);
                            dbref.child(i.getName()).child("dateAdded").setValue(strDate);
                        }
                        else {
                            i.addItemToDataBase(dbref);
                            dbref.child(i.getUpc12()).child("expirationDate").setValue(strDate);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //method to check if a name has a letter in it
    public boolean containsLetter(String s) {
        String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        boolean temp = false;
        for(int a = 0; a < s.length(); a++){
            if(alpha.contains(s.charAt(a)+"")) {
                temp = true;
                break;
            }
        }
        return temp;
    }
    //iterate over the inventory list,
    //if item is in there update quantity
    //if item isn't in ther just add it
    public void populateWithGroceryList(DatabaseReference dbref) {
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d: dataSnapshot.getChildren()) {
                    Item item = new Item();
                    if(containsLetter(d.getKey().toString())) {
                        //It's not a Upc12, its a weird identifier
                        item.setName(d.getKey());
                        item.setQuantity(d.child("quantity").getValue(String.class));
                        item.setDateAdded();
                    }
                    else {
                        item.setName(d.child("name").getValue(String.class));
                        item.setBrand(d.child("brand").getValue(String.class));
                        item.setUpc14(d.child("upc14").getValue(String.class));
                        item.setUpc12(d.getKey());
                        item.setQuantity(d.child("quantity").getValue(String.class));
                        item.setExpirationDate(d.child("expirationDate").getValue(String.class));
                        item.setDateAdded(d.child("dateAdded").getValue(String.class));
                    }
                    scanned_items.add(item);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void decrementItem(final int position, SwipeMenu menu, int index) {
        //This method derements the items in the groery list
        //if the item quantity is < 1 after the decrement, it removes it from the DB and listview
        //removes the selected item form the list
        final Item item = scanned_list.get(position);

        final String name = item.getName();
        final String quantity = scanned_list.get(position).getQuantityAsInt()-1 + "";
        final String date = item.getExpirationDate();

        final DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Grocery List");
        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    boolean flag = false;
                    if(d.getKey().equals(name)){ //check if the name is UPC12
                        //its a custom item
                        int newValue = Integer.parseInt(dataSnapshot.child(name).child("quantity").getValue(String.class));
                        if(newValue<=1){
                            scanned_list.remove(position);
                            dbr.child(name).setValue(null);
                            flag = false;
                            break;
                        }else{ //
                            newValue--;
                            //item.setQuantity(newValue+"");
                            dbr.child(name).child("quantity").setValue(newValue + "");
                            flag = true;
                            break;
                        }
                    }
                    else if(d.child("name").getValue() != null && d.child("name").getValue().equals(name)){
                        //it was a scanned item

                        String upc14Name = d.child("upc14").getValue(String.class);
                        String childName = upc14Name.substring(2);

                        int upcValue = Integer.parseInt(dataSnapshot.child(childName).child("quantity").getValue(String.class));
                        //if <= 1 then remove it
                        //else decrement
                        if(upcValue<=1){
                            scanned_list.remove(position);
                            dbr.child(childName).setValue(null);
                            flag = false;
                            break;
                        }else{
                            upcValue--;
                            dbr.child(childName).child("quantity").setValue(scanned_list.get(position).getQuantityAsInt()-1 + "");
                            flag = true;
                            break;
                        }

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        Toast.makeText(this, "1 " + name + " deleted", Toast.LENGTH_SHORT).show();
    }

    public void incrementItem(final int position, SwipeMenu menu, int index) {
        //This method derements the items in the groery list
        //if the item quantity is < 1 after the decrement, it removes it from the DB and listview
        //removes the selected item form the list
        final Item item = scanned_list.get(position);

        final String name = item.getName();
        final String quantity = scanned_list.get(position).getQuantityAsInt()+1 + "";
        final String date = item.getExpirationDate();

        final DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Grocery List");
        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    boolean flag = false;
                    if(d.getKey().equals(name)){ //check if the name is UPC12
                        //its a custom item
                        int newValue = Integer.parseInt(dataSnapshot.child(name).child("quantity").getValue(String.class));
                        newValue++;
                        //item.setQuantity(newValue+"");
                        dbr.child(name).child("quantity").setValue(newValue + "");
                        flag = true;
                        break;
                    }
                    else if(d.child("name").getValue() != null && d.child("name").getValue().equals(name)){
                        //it was a scanned item

                        String upc14Name = d.child("upc14").getValue(String.class);
                        String childName = upc14Name.substring(2);

                        int upcValue = Integer.parseInt(dataSnapshot.child(childName).child("quantity").getValue(String.class));
                        upcValue++;
                        dbr.child(childName).child("quantity").setValue(scanned_list.get(position).getQuantityAsInt()+1 + "");
                        flag = true;
                        break;
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        Toast.makeText(this, "1 " + name + " added", Toast.LENGTH_SHORT).show();
    }

    public void deleteItem(final int position, SwipeMenu menu, int index, boolean showToast) {
        //This method derements the items in the groery list
        //if the item quantity is < 1 after the decrement, it removes it from the DB and listview
        //removes the selected item form the list
        final Item item = scanned_list.get(position);

        final String name = item.getName();
        final String date = item.getExpirationDate();

        final DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Grocery List");
        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    if(d.getKey().equals(name)){ //check if the name is UPC12
                        //its a custom item
                        scanned_list.remove(position);
                        dbr.child(name).setValue(null);
                        break;
                    }
                    else if(d.child("name").getValue() != null && d.child("name").getValue().equals(name)){
                        //it was a scanned item

                        String upc14Name = d.child("upc14").getValue(String.class);
                        String childName = upc14Name.substring(2);
                        scanned_list.remove(position);
                        dbr.child(childName).setValue(null);
                        break;
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        if(showToast)
            Toast.makeText(this,name + " deleted", Toast.LENGTH_SHORT).show();
    }

    public void buyItem (final int position, SwipeMenu menu, int index) {
        final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Inventory");

        deleteItem(position, menu, index, false);

        Date date = Calendar.getInstance().getTime();//getting date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");//formating according to my need
        final String strDate = simpleDateFormat.format(date);

        final Item i = scanned_list.get(position);
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean inventoryContains = false;
                for(DataSnapshot d: dataSnapshot.getChildren()){
                    if(i.getName().equals(d.getKey().toString())){
                        int a;
                        a = Integer.parseInt(d.child("quantity").getValue(String.class));
                        a += i.getQuantityAsInt();
                        dbref.child(d.getKey()).child("quantity").setValue(a+"");
                        inventoryContains = true;
                        break;
                    } else if(i.getUpc12().equals(d.getKey().toString())){
                        int a;
                        a = Integer.parseInt(d.child("quantity").getValue(String.class));
                        a += i.getQuantityAsInt();
                        dbref.child(d.getKey()).child("quantity").setValue(a+"");
                        inventoryContains = true;
                        break;
                    }
                }
                if(!inventoryContains) {
                    if(i.getUpc12()=="") {
                        dbref.child(i.getName()).child("quantity").setValue(i.getQuantity());
                        dbref.child(i.getName()).child("expirationDate").setValue(strDate);
                        dbref.child(i.getName()).child("dateAdded").setValue(strDate);
                    }
                    else {
                        i.addItemToDataBase(dbref);
                        dbref.child(i.getUpc12()).child("expirationDate").setValue(strDate);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        Toast.makeText(this, i.getName() + " sent to Pantry Inventory", Toast.LENGTH_SHORT).show();
    }

}

