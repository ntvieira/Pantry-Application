package com.example.babin.pantry_app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private MyAdapter mAdapter;
    private DatabaseReference mDatabase;
    private Drawable incrementIcon;
    private Drawable decrementIcon;
    private Drawable deleteIcon;
    private Drawable buyIcon;
    private final ColorDrawable background = new ColorDrawable(Color.RED);;
    private boolean swipeBack = false;
    private ButtonsState buttonShowedState = ButtonsState.GONE;
    private static final float buttonWidth = 300;

    public SwipeToDeleteCallback(MyAdapter adapter, DatabaseReference dbref) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        mAdapter = adapter;
        mDatabase = dbref;
        incrementIcon = ContextCompat.getDrawable(mAdapter.getContext(), R.drawable.icons8_plus_math_24);
        decrementIcon = ContextCompat.getDrawable(mAdapter.getContext(), R.drawable.icons8_minus_24);
        deleteIcon = ContextCompat.getDrawable(mAdapter.getContext(), R.drawable.icons8_waste_24);
        buyIcon = ContextCompat.getDrawable(mAdapter.getContext(), R.drawable.icons8_buy_24);

    }

    enum ButtonsState {
        GONE,
        LEFT_VISIBLE,
        RIGHT_VISIBLE
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        mAdapter.deleteItem(position, mDatabase);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ACTION_STATE_SWIPE) {
            setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void setTouchListener(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, final float dX, float dY, int actionState, boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;

                if(swipeBack) {
                    if (dX > buttonWidth) buttonShowedState  = ButtonsState.LEFT_VISIBLE;
                }

                return false;
            }
        });
    }
    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh1, RecyclerView.ViewHolder vh2) { return true; }
}
