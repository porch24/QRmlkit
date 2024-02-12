package com.example.qrmlkit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.keyence.autoid.sdk.scan.DecodeResult;
import com.keyence.autoid.sdk.scan.ScanManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements ScanManager.DataListener {

    private static final int GALLERY_PERMISSION_REQUEST = 1002;

    private ScanManager mScanManager;
    private ImageView imageIv;
    private TextView resultIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScanManager = ScanManager.createScanManager(this);
        mScanManager.addDataListener(this);

        imageIv = findViewById(R.id.imageIv);
        resultIv = findViewById(R.id.resultIv);

        MaterialButton galleryBtn = findViewById(R.id.galleryBtn);

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkGalleryPermission();
            }
        });
    }

    private void checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    GALLERY_PERMISSION_REQUEST);
        } else {
            // Permission already granted, open gallery
            openGallery();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_PERMISSION_REQUEST);
    }



    @Override
    public void onDataReceived(DecodeResult decodeResult) {
        // Process the scan result
        String response = decodeResult.getData();

        Log.d("ScanResult", "Scanned Result: " + response);

        // Assuming the prefix to remove is "VL"
        String prefixToRemove = "VL";

        // Check if the result starts with the specified prefix
        if (response.startsWith(prefixToRemove) && response.length() > prefixToRemove.length()) {
            // Remove the prefix
            String puid = response.substring(prefixToRemove.length());

            // Use the puid string directly for comparison or send it to the web service
            sendDataToWebService(puid);
        } else {
            // Handle the case when the result does not start with the expected prefix
            // You may want to show an error message or take appropriate action
            resultIv.setText("Invalid result format");
        }
    }

    // Inside MainActivity, define the method to send data to the web service
    private void sendDataToWebService(String puid) {
        // Pass the TextView to the SendDataToWebServiceTask constructor
        SendDataToWebServiceTask task = new SendDataToWebServiceTask(resultIv);
        // Execute the task with the PUID to send
        task.execute(puid);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScanManager.removeDataListener(this);
        mScanManager.releaseScanManager();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Gallery permission granted, open gallery
                openGallery();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PERMISSION_REQUEST && resultCode == RESULT_OK && data != null) {
            // Handle selected image from gallery
            Uri selectedImage = data.getData();
            imageIv.setImageURI(selectedImage);
        }
    }
}
