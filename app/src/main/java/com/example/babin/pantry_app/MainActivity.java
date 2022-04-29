package com.example.babin.pantry_app;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private String itemCode;
    private GoogleApiClient googleClient;
    static final int CAMERA_REQUEST_CODE = 1;
    private DatabaseReference mDatabaseRef;
    private String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private Button go_to_grocery;
    private Button go_to_pantry;
    private Button go_to_scanner;
    private TextView scannedItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        go_to_pantry = findViewById(R.id.go_to_pantry);
        go_to_pantry.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Inventory.class);
                startActivity(intent); // startActivity allow you to move

            }
        });
        TextView dateView = (TextView)findViewById(R.id.textView2);
        setDate(dateView);



        go_to_scanner = findViewById(R.id.go_to_scanner);
        go_to_scanner.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //When the go to scanner button is pressed, ensure that the user has given permission to use the camera
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                //If they have given permission...
                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    //Create a new intent for the scanner activity
                    Intent intent = new Intent(MainActivity.this, Scanner.class);
                    //Start the scanner activity, but listen for a result
                    startActivityForResult(intent, 0);
                }
            }
        });


        go_to_grocery = findViewById(R.id.go_to_grocery);
        go_to_grocery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GroceryList.class);
                //Intent intent = new Intent(MainActivity.this, GroceryListReplacement.class);
                startActivity(intent); // startActivity allow you to move
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleClient.connect();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.help:
                Intent intent6 = new Intent(this, Help.class);
                this.startActivity(intent6);
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                Auth.GoogleSignInApi.signOut(googleClient);
                Intent intent5 = new Intent(this, LoginPage.class);
                startActivity(intent5);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }



    //This method is called when the result is received from the scanner activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            //If the result code was a success...
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    //Get the barcode data from the intent data
                    itemCode = data.getStringExtra("barcode");
                    //Create a database reference to check the items database for the scanned item
                    mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Items").child(itemCode);
                    //This is the method that actually checks the database
                    mDatabaseRef.addListenerForSingleValueEvent(valueEventListener);
                }
            }
        }
    }

    public ValueEventListener valueEventListener = new ValueEventListener() {
        //onDataChange is called the first time the listener is attached
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            //The database reference refers to a child of the items database with the key being equal to the barcode value
            //So if the value at that key is not null, the item already exists within the item database and should be
            //added to the grocery list/inventory
            if (dataSnapshot.getValue() != null) {
                //Since the scanned code is in the database, grab the information associated with that item
                //and create a new Item from that information
                Item i = new Item();
                i.setBrand(dataSnapshot.child("brand").getValue(String.class));
                i.setName(dataSnapshot.child("name").getValue(String.class));
                i.setUpc12(dataSnapshot.getKey());
                i.setUpc14(dataSnapshot.child("upc14").getValue(String.class));
                i.setDateAdded();
                //After the Item is created, verify that it's the correct item with the user
                verifyItem(i);
            }
            else {
                //Otherwise, if the item is not in the items database, prompt the user for the item
                //information so that it can be added to the database.
                Item i = new Item();
                addItemToItemsDatabase(i);
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    //This method is called when the user needs to add the scanned item to the items database
    public void addItemToItemsDatabase(final Item item) {
        //Create a small dialog that has the necessary fields for the user to input information
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.add_to_items_database);
        //"name" field
        final EditText name = (EditText) dialog.findViewById(R.id.inputName);
        //"brand" field
        final EditText brand = (EditText) dialog.findViewById(R.id.inputBrand);
        //button to confirm
        Button add = (Button) dialog.findViewById(R.id.toDB);
        //button to cancel
        Button cancel = (Button) dialog.findViewById(R.id.Cancel);
        //This is called when the "add" button is pressed
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set the name and brand of the item to the values in the respective text fields
                //A database reference that points to the items database root
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Items");
                //check that the item actually has a name and brand before adding it to the db
                if(name.getText().toString().length() != 0 && brand.getText().toString().length()!= 0) {
                    //add it to the db
                    item.setName(name.getText().toString());
                    item.setBrand(brand.getText().toString());
                    //The upc 14 is the same as the upc12, but with two zeroes padded
                    item.setUpc14("00"+itemCode);
                    //The scanned in information is the upc12
                    item.setUpc12(itemCode);
                    item.addItemToItemDB(ref);
                }
                //get rid of the dialog
                dialog.dismiss();
                //item.setDateAdded();
                //prompt the user to add it to their grocery list or inventory
                if(name.getText().toString().length() != 0 && brand.getText().toString().length() != 0){
                    verifyItem(item);
                }
            }
        });
        //If the cancel button is pressed, dismiss the dialog
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        //Everything's set up, launch the dialog
        dialog.show();
    }
    public boolean isValidQuan(String quan){
        if(quan.charAt(0) == '.' && quan.length() == 1)
            return false;
        if(Integer.parseInt(quan) > 0)
            return true;
        return false;
    }
    //This method is called when the user scans an item that is already in the items database
    //or after they enter an item into the database.  It's used to add the item to either
    //their grocery list or their pantry inventory
    public void verifyItem(final Item item) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.item_verify);
        dialog.setTitle("Verify the Item");
        //Show the user the item name
        TextView textView = (TextView) dialog.findViewById(R.id.itemName);
        textView.setText(item.getName());
        //Allow a text field to change the quantity
        final EditText quantity = (EditText) dialog.findViewById(R.id.quantity);
        //Button to add to their shopping list
        Button addToShopping = (Button) dialog.findViewById(R.id.toSL);
        //Button to add to their inventory
        Button addToInventory = (Button) dialog.findViewById(R.id.toIV);
        //Button to cancel
        Button nope = (Button) dialog.findViewById(R.id.Cancel);
        //Called when the shopping list button is pressed
        addToShopping.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Create a database reference that points to the user's current grocery list
                final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Grocery List");
                dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //if the item already exists within the grocery list...
                        if (dataSnapshot.hasChild(item.getUpc12())) {
                            //get the amount that is already in there as integer 'a'
                            int a = Integer.parseInt(dataSnapshot.child(item.getUpc12()).child("quantity").getValue(String.class));
                            int b;
                            //Integer 'b' is set to the quantity that is put in the text field above
                            if(quantity.getText().toString().equals("")) {
                                //If that text field is blank, set the quantity to 1
                                b = 1;
                            }
                            else {
                                b = 0;
                                try {
                                    b = Integer.parseInt(quantity.getText().toString());
                                } catch (NumberFormatException e){

                                }
                            }
                            a += b;
                            //Set the quantity of the item to the sum of the old and the new quantities
                            item.setQuantity(a+"");
                        }
                        //If the item is not already in the grocery list....
                        else {
                            //Simply grab the quantity that the user inputs
                            if(quantity.getText().toString().equals("")) {
                                item.setQuantity("1");
                            }
                            else {
                                int b = 0;
                                try{
                                    b = Integer.parseInt(quantity.getText().toString());
                                } catch (NumberFormatException e){

                                }
                                item.setQuantity(b+"");
                            }
                        }
                        //item.setExpirationDateToday();
                        //then add that item to the database
                        item.addItemToDataBase(dbref);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                dialog.dismiss();
            }
        });

        //This method does the exact same thing as the add to grocery list, except the database reference
        //points to the inventory
        addToInventory.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuser).child("Inventory");
                dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(item.getUpc12())) {
                            int a = Integer.parseInt(dataSnapshot.child(item.getUpc12()).child("quantity").getValue(String.class));
                            int b;
                            if(quantity.getText().toString().equals("")) {
                                b = 1;
                            }
                            else {
                                b = Integer.parseInt(quantity.getText().toString());
                            }
                            a += b;
                            item.setQuantity(a+"");
                        }
                        else {
                            if(quantity.getText().toString().equals("")) {
                                item.setQuantity("1");
                            }
                            else {
                                item.setQuantity(quantity.getText().toString());
                            }
                        }
                        item.setDateAdded();
                        item.setExpirationDateToday();
                        item.addItemToDataBase(dbref);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                dialog.dismiss();
            }
        });
        //If the cancel button is pressed, dismiss the dialog
        nope.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //Everything is set up, so launch the dialog
        dialog.show();
    }

    public void setDate (TextView view){
        Date date = Calendar.getInstance().getTime();//getting date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");//formating according to my need
        String str = simpleDateFormat.format(date);
        view.setText(str);
    }
}
