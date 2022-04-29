package com.example.babin.pantry_app;

import android.Manifest;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Scanner extends AppCompatActivity {
    SurfaceView cameraView; //surface where the camera appears
    static final int CAMERA_REQUEST_CODE = 1; //code for granted permission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        cameraView = (SurfaceView) findViewById(R.id.cameraPreview);
        createCameraSource();
    }

    private void createCameraSource() {
        //creates the barcode detector using Google Mobile Vision API
        BarcodeDetector detector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.UPC_A | Barcode.UPC_E)
                .build();

        //creates the camera source, with auto focus enabled and using the back camera
        final CameraSource cameraSource = new CameraSource.Builder(this, detector)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .build();
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //ensure that the user has given us permission to use the camera
                if (ActivityCompat.checkSelfPermission(Scanner.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Scanner.this, new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                    return;
                    }
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
        //Initialize the actual detector of barcodes
        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }
            @Override
            //This method is called when a barcode is detected
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                //An array of barcodes is created from the detected items
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                //Ensure that the detected items exist
                if (barcodes.size() > 0) {
                    //Check that the detected item is not null
                    if (barcodes.valueAt(0) != null) {
                        //Create a new intent, which will pass the detected information back to the main activity
                        final Intent found = new Intent();
                        //Create a string that is the barcode value
                        String retItem = barcodes.valueAt(0).displayValue;
                        //Put the barcode value into the intent to be passed
                        found.putExtra("barcode", retItem);
                        //Mark this activity as a success
                        setResult(CommonStatusCodes.SUCCESS, found);
                    }
                    //Close the activity and pass the intent to the main activity
                    finish();
                }
            }
        });

    }

}
