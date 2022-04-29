package com.example.babin.pantry_app;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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

import static com.example.babin.pantry_app.Item.getHashMapFromItems;

public class Inventory extends AppCompatActivity implements View.OnClickListener {


    //variables for elements
    private EditText text_input;
    private EditText text_input2;
    private EditText text_inputq;
    private Button add_button;
    private SwipeMenuListView inventory_list;
    private Button item_sort_name;
    private Button item_sort_quan;

    private boolean flag;

    //list and list adapter to keep track of entered elements
    private ArrayList<String> inventory_items;

    private ArrayAdapter<String> adapter;
    private HashMap<String, String> scanned_name;
    private ArrayList<Item> scanned_items;
    private ArrayList<Item> scanned_list;

    //test adapter
    private ArrayAdapter adapter2;

    //used to read and write from DataBase
    private DatabaseReference mDatabase;

    //used to store user's individual grocery list data
    private String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

    //used to open date picker
    DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(this, "Swipe items to modify", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Inventory");

        //assigns elements to variables by ID
        text_input = findViewById(R.id.editText);
        text_input2 = findViewById(R.id.editText2);
        text_inputq = findViewById(R.id.editTextQ);
        add_button = findViewById(R.id.manuallyEnterItem);
        inventory_list = findViewById(R.id.invList);
        item_sort_name = findViewById(R.id.sort_by_name);
        item_sort_name.setOnClickListener(this);
        item_sort_quan = findViewById(R.id.sort_by_quantity);
        item_sort_quan.setOnClickListener(this);





        //list of items displayed
        scanned_list = new ArrayList<Item>();
        scanned_items = new ArrayList<Item>();

        //initialize list
        inventory_items = new ArrayList<String>();
        //item = new HashMap<String, String>();
        scanned_name = new HashMap<String, String>();

        //initialize adapter
        adapter2 = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, scanned_list);

        //inventory_list.setAdapter(sa);
        inventory_list.setAdapter(adapter2);
        //next two lines make it so users cant add items if the fields are empty
        //THIS DISABLES AND ENABLES THE BUTTON DEPENDING ON THE TEXT FIELDS BEING NON-EMPTY
        text_inputq.addTextChangedListener(textWatcher);
        text_input.addTextChangedListener(textWatcher);
        text_input2.addTextChangedListener(textWatcher);
        add_button.setBackgroundColor(Color.GRAY);
        add_button.setEnabled(false);
        add_button.setOnClickListener(this);

        mDatabase.addValueEventListener(valueEventListener);

        //Opens date picker on user click
        text_input2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar myCalendar = Calendar.getInstance();
                final int year = myCalendar.get(Calendar.YEAR);
                final int month = myCalendar.get(Calendar.MONTH);
                final int day = myCalendar.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(Inventory.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        if((month < 9) && (day < 10)){
                            text_input2.setText("0"+(month+1)+"/"+"0"+(day)+"/"+year);
                        }else if((month < 9) && (day >= 10)){
                            text_input2.setText("0"+(month+1)+"/"+(day)+"/"+year);
                        }else if((month >= 10) && (day<10)){
                            text_input2.setText((month+1)+"/"+"0"+(day)+"/"+year);
                        }else{
                            text_input2.setText((month+1)+"/"+(day)+"/"+year);
                        }
                    }
                },year, month, day);
                datePickerDialog.show();
            }
        });

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
                buyItem.setIcon(R.drawable.icons8_shopping_24);
                // add to menu
                menu.addMenuItem(buyItem);
            }
        };
        inventory_list.setMenuCreator(creator);
        inventory_list.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // increment
                        return incrementItem(position, menu, index);
                    case 1:
                        // decrement
                        return decrementItem(position, menu, index);
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

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            add_button.setEnabled(false);
            add_button.setBackgroundColor(Color.GRAY);
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String itemName = text_input.getText().toString().trim();
            String quantity = text_inputq.getText().toString().trim();
            String date = text_input2.getText().toString().trim();
            if ((!itemName.isEmpty() && !quantity.isEmpty() && itemName.charAt(0) != '0' && !date.isEmpty() && isValidQuan(quantity) &&
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
            scanned_list.clear();
            for (DataSnapshot d: dataSnapshot.getChildren()) {
                Item i = new Item();
                String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                boolean temp = false;
                String key = d.getKey();
                for(int a = 0; a < key.length(); a++){
                    if(alpha.contains(key.charAt(a)+"")) {
                        temp = true;
                        break;
                    }
                }
                if(temp) {
                    //It's not a Upc12, its a weird identifier
                    i.setName(d.getKey());
                    i.setQuantity(d.child("quantity").getValue(String.class));
                    i.setExpirationDate(d.child("expirationDate").getValue(String.class));
                    i.setDateAdded(d.child("dateAdded").getValue(String.class));
                } else {
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
        //next two lines make it so users cant add items if the fields are empty
        //THIS DISABLES AND ENABLES THE BUTTON DEPENDING ON THE TEXT FIELDS BEING NON-EMPTY
        text_input.addTextChangedListener(textWatcher);
        text_input2.addTextChangedListener(textWatcher);
        text_inputq.addTextChangedListener(textWatcher);

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
        if(V.getId() == R.id.manuallyEnterItem)
        {

            if(text_input.getText().toString() != "")
            {
                //getting text from editboxes
                String quantity_entered = "";
                final String item_entered = text_input.getText().toString();
                final String item_entered2 = text_input2.getText().toString();
                quantity_entered = text_inputq.getText().toString();
                final String realQuantity;
                realQuantity = quantity_entered;

                Item manualItem = new Item();
                manualItem.setName(item_entered);
                manualItem.setDateAdded(item_entered2);
                manualItem.setExpirationDate(item_entered2);
                scanned_items.add(manualItem);

                Date date = Calendar.getInstance().getTime();//getting date
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");//formating according to my need
                String strDate = simpleDateFormat.format(date);
                String dateString = "";
                if(scanned_items.get(scanned_items.size()-1).getExpirationDate().equals("")){
                    dateString = strDate;
                }
                else {
                    dateString = scanned_items.get(scanned_items.size() - 1).getExpirationDate();
                }
                final String finalDateString = dateString;


                scanned_list.add(manualItem);

                //initialize adapter

                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Inventory").child(item_entered);
                dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount() == 0 || dataSnapshot == null) {
                            mDatabase.child(item_entered).child("quantity").setValue(realQuantity);
                            mDatabase.child(item_entered).child("expirationDate").setValue(finalDateString);
                            mDatabase.child(item_entered).child("dateAdded").setValue(finalDateString);
                        }
                        else {
                            //key exists
                            int quant = Integer.parseInt(realQuantity);
                            int quant2 = Integer.parseInt(dataSnapshot.child("quantity").getValue(String.class));
                            quant += quant2;
                            mDatabase.child(item_entered).child("quantity").setValue(quant + "");
                            mDatabase.child(item_entered).child("expirationDate").setValue(finalDateString);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                //clears out text from the input area
                text_input.setText("");
                text_input2.setText("");
                text_inputq.setText("");

                Toast.makeText(this, "Item successfully added", Toast.LENGTH_SHORT).show();

            }
        }
    }

    public boolean decrementItem(final int position, SwipeMenu menu, int index) {
        //This method decrements the items in the groery list
        //if the item quantity is < 1 after the decrement, it removes it from the DB and listview
        //removes the selected item form the list
        final Item item = scanned_list.get(position);

        final String name = item.getName();
        final String quantity = scanned_list.get(position).getQuantityAsInt()-1 + "";
        final String date = item.getExpirationDate();

        final DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Inventory");
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
        if(scanned_list.get(position).getQuantityAsInt() < 1)
            return false;
        return true;
    }

    public boolean incrementItem(final int position, SwipeMenu menu, int index) {
        //This method derements the items in the groery list
        //if the item quantity is < 1 after the decrement, it removes it from the DB and listview
        //removes the selected item form the list
        final Item item = scanned_list.get(position);

        final String name = item.getName();
        final String quantity = scanned_list.get(position).getQuantityAsInt()+1 + "";
        final String date = item.getExpirationDate();

        final DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Inventory");
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
        return true;
    }

    public void deleteItem(final int position, SwipeMenu menu, int index, boolean showToast) {
        //This method derements the items in the groery list
        //if the item quantity is < 1 after the decrement, it removes it from the DB and listview
        //removes the selected item form the list
        final Item item = scanned_list.get(position);

        final String name = item.getName();
        final String date = item.getExpirationDate();

        final DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Inventory");
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
        final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Grocery List");

        deleteItem(position, menu, index, false);

        Date date = Calendar.getInstance().getTime();//getting date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");//formating according to my need
        final String strDate = simpleDateFormat.format(date);

        final Item i = scanned_list.get(position);
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean groceryContains = false;
                for(DataSnapshot d: dataSnapshot.getChildren()){
                    if(i.getName().equals(d.getKey().toString())){
                        int a;
                        a = Integer.parseInt(d.child("quantity").getValue(String.class));
                        a += i.getQuantityAsInt();
                        dbref.child(d.getKey()).child("quantity").setValue(a+"");
                        groceryContains = true;
                        break;
                    } else if(i.getUpc12().equals(d.getKey().toString())){
                        int a;
                        a = Integer.parseInt(d.child("quantity").getValue(String.class));
                        a += i.getQuantityAsInt();
                        dbref.child(d.getKey()).child("quantity").setValue(a+"");
                        groceryContains = true;
                        break;
                    }
                }
                if(!groceryContains) {
                    if(i.getUpc12()=="") {
                        dbref.child(i.getName()).child("quantity").setValue(i.getQuantity());
                        dbref.child(i.getName()).child("expirationDate").setValue("");
                        dbref.child(i.getName()).child("dateAdded").setValue(strDate);
                    }
                    else {
                        i.addItemToDataBase(dbref);
                        dbref.child(i.getUpc12()).child("expirationDate").setValue("");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        Toast.makeText(this, i.getName() + " sent to Grocery List", Toast.LENGTH_SHORT).show();
    }
}

