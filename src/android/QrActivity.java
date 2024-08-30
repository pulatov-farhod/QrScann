package com.qrscan.plugin;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.AppCompatImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Context;

import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ViewfinderView;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import java.util.Random;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.zxing.client.android.Intents;
import com.google.zxing.common.HybridBinarizer;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import android.content.res.Resources;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;

/**
 * Custom Scannner Activity extending from Activity to display a custom layout form scanner view.
 */
public class QrActivity extends AppCompatActivity implements DecoratedBarcodeView.TorchListener {
    Button get_img_btn;
    AppCompatImageView close_btn;
    AppCompatImageView flash_btn;
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private Button switchFlashlightButton;
    private ViewfinderView viewfinderView;
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 100;
    private static final int REQUEST_GALLERY = 200;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;
    private ActivityResultLauncher<ScanOptions> barcodeLauncher;

    int cnt = 0;
    private boolean isFlashOn = false;
    String package_name;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        package_name = getApplication().getPackageName();
        Intent intent = getIntent();
        context = getApplicationContext();

        setContentView(getApplication().getResources().getIdentifier("activity_qr", "layout", package_name));
        barcodeScannerView = findViewById(getApplication().getResources().getIdentifier("zxing_barcode_scanner", "id", package_name));
        viewfinderView = findViewById(getApplication().getResources().getIdentifier("zxing_viewfinder_view", "id", package_name));
        barcodeScannerView.setTorchListener(this);
        close_btn = findViewById(getApplication().getResources().getIdentifier("close_btn", "id", package_name));
        flash_btn = findViewById(getApplication().getResources().getIdentifier("flash_btn", "id", package_name));
        get_img_btn = findViewById(getApplication().getResources().getIdentifier("get_img_btn", "id", package_name));

//        String dt = intent.getStringExtra("LNG");
//        Log.d("LNG",dt);
//        if(dt.equals("tj")||dt.equals("TJ")){
//            get_img_btn.setText("Боргирии QR аз галерея");
//            flash_btn.setText("Чароғак");
//        }else if(dt.equals("ru") || dt.equals("RU")){
//            get_img_btn.setText("QR загрузить с галереи");
//            flash_btn.setText("Фонарик");
//        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("QR_READER", "TRY PERMISSION CAMERA");
//            mClss = QrActivity.class;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        }
        else {
            Log.d("QR_READER", "ERROR PERMISSION CAMERA");
        }
        // Initialize the ActivityResultLaunchers
        initializeActivityResultLaunchers();

        close_btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });

        get_img_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                requestPermissions();
            }
        });



        // if the device does not have flashlight in its camera,
        // then remove the switch flashlight button...

//        capture = new CaptureManager(this, barcodeScannerView);
//        capture.initializeFromIntent(getIntent(), savedInstanceState);
//        capture.setShowMissingCameraPermissionDialog(false);
//        capture.decode();
        //barcodeLauncher.launch(capture);

        ScanOptions options = new ScanOptions();
        options.setCaptureActivity(this.class);
        options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES);
        options.setPrompt("Scan something");
        options.setOrientationLocked(false);
        options.setBeepEnabled(false);
        barcodeLauncher.launch(options);

        changeMaskColor(null);
        changeLaserVisibility(true);

    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            requestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
            });
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            requestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            // Handle permissions for Android 11 and below
            requestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
        cnt = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    /**
     * Check if the device's camera has a Flashlight.
     * @return true if there is Flashlight, otherwise false.
     */
    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void switchFlashlight(View view) {
        if (!isFlashOn) {
            barcodeScannerView.setTorchOn();
            flash_btn.setImageResource(getApplication().getResources().getIdentifier("qrflash_on", "drawable", package_name));
            isFlashOn = true;
        } else {
            barcodeScannerView.setTorchOff();
            flash_btn.setImageResource(getApplication().getResources().getIdentifier("qrflash_off", "drawable", package_name));
            isFlashOn = false;
        }
    }

    public void changeMaskColor(View view) {
        Random rnd = new Random();
        int color = Color.argb(100, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        viewfinderView.setMaskColor(color);
    }

    public void changeLaserVisibility(boolean visible) {
        viewfinderView.setLaserVisibility(visible);
    }

    @Override
    public void onTorchOn() {

    }

    @Override
    public void onTorchOff() {

    }
    private void initializeActivityResultLaunchers() {
        // Register the permission request launcher
        requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    for (String permission : result.keySet()) {
                        Boolean isGranted = result.get(permission);
                        if (isGranted != null && isGranted) {
                            if(cnt == 0){
                                openInGallery();
                            }
                        } else {
                            Toast.makeText(this, permission + " denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        // Register the gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),

                result -> {
                    Log.d("RESULT++",String.valueOf(cnt));
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImageUri =  data.getData();
                            try {
//                                    Uri selectedImageUri = result.getData().getData();
                                final InputStream imageStream = context.getContentResolver().openInputStream(selectedImageUri);

                                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                                String qrStr = scanQRImage(selectedImage);
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("QrResult", qrStr);
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                                // Handle the selected image URI
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                // Handle the error, maybe notify the user that the file wasn't found
                            }
                        }

                    }
                }
        );

        barcodeLauncher = registerForActivityResult(
                new ScanContract(),
                result -> {
                    Intent resultIntent = new Intent();
                    if (result.getData() == null) {
                        Intent originalIntent = result.getData();
                        if (originalIntent == null) {
                            Toast.makeText(QrActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                        } //else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                          //  Toast.makeText(CustomScannerActivity.this, "Cancelled due to missing camera permission", Toast.LENGTH_LONG).show();
                        //}
                    } else {
                        //Log.d("MainActivity", "Scanned");
                        Toast.makeText(QrActivity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                        resultIntent.putExtra("QrResult", result.getContents());
                        setResult(Activity.RESULT_OK, resultIntent);
//                        finish();
                    }
                });


    }




    public static String scanQRImage(Bitmap bMap) {
        String contents = null;

        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
        }
        catch (Exception e) {
            //contents = "";
            Log.e("QR_READER", "error img", e);
        }
        return contents;
    }
    public void openInGallery() {
        cnt++;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        Log.d("RESULT++",String.valueOf(cnt));
        galleryLauncher.launch(intent);
    }

}
