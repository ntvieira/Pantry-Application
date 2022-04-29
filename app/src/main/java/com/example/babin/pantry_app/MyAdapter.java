package com.example.babin.pantry_app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    //where do we populate this
    private ArrayList<Item> mDataset;
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.itemView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<Item> myDataset, Context context) {
        mContext = context;
        mDataset = myDataset;
    }

    public Context getContext() {
        return mContext;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sub_item2, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String displayString = mDataset.get(position).getQuantity() + "  " + mDataset.get(position).getName();
        holder.textView.setText(displayString);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void deleteItem(int position, DatabaseReference dbref) {
        //item name and upc are set to the respective attributes of the item
        String itemName = mDataset.get(position).getName();
        String upc = mDataset.get(position).getUpc12();
        //delete the item from the database.  If it doesn't have a UPC, it's custom and the key is the name
        if(mDataset.get(position).getUpc12() == "") {
            dbref.child(itemName).setValue(null);
        }
        //Otherwise it's a scanned item and the key is the UPC
        else if(mDataset.get(position).getUpc12() != ""){
            dbref.child(upc).setValue(null);
        }
        //If it gets here, for one reason or another the value is null
        else {
            //TODO: Display didn't find the item
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return convertView;
    }
}