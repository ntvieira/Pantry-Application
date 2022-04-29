package com.example.babin.pantry_app;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class GroceryListReplacement extends AppCompatActivity implements View.OnClickListener{

    private EditText text_input;
    private EditText quantity_input;
    private Button add_button;
    private RecyclerView grocery_list;
    private Button item_sort_name;
    private Button item_sort_quan;
    private Button buy_button;
    private DatabaseReference mDatabase;
    private String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private MyAdapter mAdapter;
    private ArrayList<String> displayNames;
    ArrayList<Item> itemCollect;
    private RecyclerView.LayoutManager layoutManager;

    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_replacement);
        //Database reference that points to the user's grocery list
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Grocery List");
        //Creating buttons, text fields, etc. and adding on click listeners
        text_input = findViewById(R.id.text_input);
        quantity_input = findViewById(R.id.quantity_input);
        item_sort_name = findViewById(R.id.sort_by_name);
        item_sort_name.setOnClickListener(this);
        item_sort_quan = findViewById(R.id.sort_by_quantity);
        item_sort_quan.setOnClickListener(this);
        buy_button = findViewById(R.id.buy_all);
        buy_button.setOnClickListener(this);
        add_button = findViewById(R.id.add_item);
        add_button.setOnClickListener(this);
        add_button.setEnabled(false);
        quantity_input.addTextChangedListener(textWatcher);
        text_input.addTextChangedListener(textWatcher);
        //Layout manager that attaches to the recycle view (grocery_list)
        layoutManager = new LinearLayoutManager(this);
        //creating the Recycle viewer
        grocery_list = findViewById(R.id.grocery_list);
        //attaching the layout manager to the layout manager
        grocery_list.setLayoutManager(layoutManager);
        //instantiate the array list
        itemCollect = new ArrayList<Item>();
        //Attaches an adapter to the array list, so that what the user sees is updated as the array list is updated
        mAdapter = new MyAdapter(itemCollect, this);
        grocery_list.setAdapter(mAdapter);
        //Creating the item touch helper to add the ability to swipe on items
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(mAdapter, mDatabase));
        itemTouchHelper.attachToRecyclerView(grocery_list);
        //populate the array list with the items in the database
        mDatabase.addValueEventListener(valueEventListener);
    }

    public ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String digit = "0123456789";
            String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
            itemCollect.clear();
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
                //This means it's a scanned item
                itemCollect.add(i);
            }
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public void onClick(View V) {
        if(V.getId() == R.id.add_item)
        {
        //add a custom item to the grocery list
            final String item_entered = text_input.getText().toString();
            final String quantity_entered = quantity_input.getText().toString();
            //Check if what we're trying to add is already in the grocery DB before we add it
            DatabaseReference dbr = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Grocery List").child(item_entered);
            dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //key doesn't exist
                    if(dataSnapshot.getChildrenCount() == 0 || dataSnapshot == null) {
                        Item i = new Item();
                        i.setDateAdded();
                        i.setExpirationDateToday();
                        mDatabase.child(item_entered).child("quantity").setValue(quantity_entered);
//                            //Adding a dateAdded and expirateion date to custom items in the grocery list page
                        mDatabase.child(item_entered).child("dateAdded").setValue(i.getDateAdded());
                        mDatabase.child(item_entered).child("expirationDate").setValue(i.getExpirationDate());
                    }
                    else {
                        //key exists, we need to update the quantity
                        int quant = Integer.parseInt(quantity_entered);
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
        if(Integer.parseInt(quan) > 0)
            return true;
        return false;
    }

}
