package com.qrscan.plugin;
import static android.Manifest.permission.READ_MEDIA_IMAGES;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import android.Manifest;
import android.widget.Toast;

//import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrActivity extends AppCompatActivity {
//    private ZXingScannerView mScannerView;

    private static final int ZXING_CAMERA_PERMISSION = 1;
    private Class<?> mClss;

    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;
    int SELECT_PICTURE = 200;
    Context context;
    private static List<BarcodeFormat> formats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getActionBar().hide();
        //getSupportActionBar().hide();
        Intent intent = getIntent();
        context = getApplicationContext();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("QR_READER", "TRY PERMISSION CAMERA");
            mClss =  QrActivity.class;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        }
        else {
            Log.d("QR_READER", "ERROR PERMISSION CAMERA");
        }
        formats.add(BarcodeFormat.QR_CODE);
        formats.add(BarcodeFormat.CODE_128);
        String package_name = getApplication().getPackageName();
        Button btn = findViewById(getApplication().getResources().getIdentifier("button", "id", package_name));
        Button btn2 = findViewById(getApplication().getResources().getIdentifier("button2", "id", package_name));
        Button btn3 = findViewById(getApplication().getResources().getIdentifier("button3", "id", package_name));
        String dt = intent.getStringExtra("LNG");
        if(dt.equals("tj")||dt.equals("TJ")){
            btn.setText("Боргирии QR аз галерея");
            btn3.setText("Чароғак");
        }else if(dt.equals("ru") || dt.equals("RU")){
            btn.setText("QR загрузить с галереи");
            btn3.setText("Фонарик");
        }
//        mScannerView = new ZXingScannerView(this);
        btn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                checkAndRequestPermissions();
//                if(askPermissionAndBrowseFile()) {
//                    Log.d("QR_11","hiiiiiii");
//                    openInGallery();
//                } else {
//                    Log.d("QR_READER", "ERROR PERMISSION");
//                }
            }
        });

//        btn2.setOnClickListener( new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                finish();
//            }
//        });
//
//        btn3.setOnClickListener( new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                finish();
//
//                // TODO Auto-generated method stub
////                if(!mScannerView.getFlash())
////                    mScannerView.setFlash(true);
////                else
////                    mScannerView.setFlash(false);
//            }
//        });
    }
    public void openInGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
//        galleryLauncher.launch(intent);
//        someActivityResultLauncher.launch(intent);
        startActivityForResult(intent, 0);
    }
    private void checkAndRequestPermissions() {
        // Permissions to request
        String[] permissions = {
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
        };

        // Check if permissions are not granted
        boolean shouldRequestPermissions = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                shouldRequestPermissions = true;
                break;
            }
        }

        // Request permissions
        if (shouldRequestPermissions) {
            ActivityCompat.requestPermissions(this, permissions, MY_REQUEST_CODE_PERMISSION);
        }
    }
    private boolean askPermissionAndBrowseFile()  {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                (
                        ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) == PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO) == PERMISSION_GRANTED
                )) { // Level 23
            Log.d("First if","true");

            // Check if we have Call permission
            int permisson = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permisson != PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                Log.d("Second if","true");
                this.requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_REQUEST_CODE_PERMISSION
                );
                return false;
            }
        }
        return true;
    }


//    @Override
//    public void onPointerCaptureChanged(boolean hasCapture) {
//        super.onPointerCaptureChanged(hasCapture);
//    }
//
//    @Override
//    public void handleResult(Result rawResult) {
//        setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", rawResult.getText()));
//        finish();
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        switch (requestCode) {
            case MY_REQUEST_CODE_PERMISSION: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    openInGallery();
                }

                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }


//    private ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == Activity.RESULT_OK) {
//                    Intent data = result.getData();
//                    if (data != null && data.getData() != null) {
//                        try {
//                            Bitmap originalImage  = null;
//                            Bitmap background = null;
//                            String content;
//                            float originalWidth;
//                            float originalHeight;
//                            Canvas canvas;
//                            float scale;
//                            float xTranslation;
//                            float yTranslation;
//                            Matrix transformation;
//                            Paint paint;
//                            final Uri imageUri = data.getData();
//
//                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//
//                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//
//                            originalImage = selectedImage;
//
//                            background = Bitmap.createBitmap(1500,1500, Bitmap.Config.ARGB_8888);
//
//                            originalWidth = originalImage.getWidth();
//                            originalHeight = originalImage.getHeight();
//
//                            canvas = new Canvas(background);
//
//                            scale = 1500 / originalWidth;
//
//                            xTranslation = 0.0f;
//                            yTranslation = (1500 - originalHeight * scale) / 2.0f;
//
//                            transformation = new Matrix();
//                            transformation.postTranslate(xTranslation, yTranslation);
//                            transformation.preScale(scale, scale);
//
//                            paint = new Paint();
//                            paint.setFilterBitmap(true);
//
//                            canvas.drawBitmap(originalImage, transformation, paint);
//                            content = readQRCodeFromImage(context,imageUri);
////                            content = scanQRImage(background);
//                            Log.d("GAT_DATA_1",content);
//                            if(content == null){
//
//                                background = Bitmap.createBitmap(900,900, Bitmap.Config.ARGB_8888);
//
//                                originalWidth = originalImage.getWidth();
//                                originalHeight = originalImage.getHeight();
//
//                                canvas = new Canvas(background);
//
//                                scale = 900 / originalWidth;
//
//                                xTranslation = 0.0f;
//                                yTranslation = (900 - originalHeight * scale) / 2.0f;
//
//                                transformation = new Matrix();
//                                transformation.postTranslate(xTranslation, yTranslation);
//                                transformation.preScale(scale, scale);
//
//                                paint = new Paint();
//                                paint.setFilterBitmap(true);
//
//                                canvas.drawBitmap(originalImage, transformation, paint);
////                                content = scanQRImage(background);
//                                content = readQRCodeFromImage(context,imageUri);
//
//                                if(content != null){
//                                    Log.d("GAT_DATA_2",content);
//                                    setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", content));
//                                }else{
//                                    setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", content));
//                                    //setResult(Activity.RESULT_CANCELED);
//                                }
//                            }else{
////                                if(content != null){
////                                    Log.d("GAT_DATA_2",content);
////                                    setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", content));
////                                }else{
////                                    setResult(Activity.RESULT_CANCELED);
////                                }
//                            }
//
//
//
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
//                        finish();
//                    }
//                }
//            }
//    );

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {

        super.onActivityResult(reqCode, resCode, data);
        if (resCode == Activity.RESULT_OK) {

            try {
                Bitmap originalImage = null;
                Bitmap background = null;
                String content;
                float originalWidth;
                float originalHeight;
                Canvas canvas;
                float scale;
                float xTranslation;
                float yTranslation;
                Matrix transformation;
                Paint paint;
                final Uri imageUri = data.getData();

                final InputStream imageStream = getContentResolver().openInputStream(imageUri);

                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                originalImage = selectedImage;

                background = Bitmap.createBitmap(1500, 1500, Bitmap.Config.ARGB_8888);

                originalWidth = originalImage.getWidth();
                originalHeight = originalImage.getHeight();

                canvas = new Canvas(background);

                scale = 1500 / originalWidth;

                xTranslation = 0.0f;
                yTranslation = (1500 - originalHeight * scale) / 2.0f;

                transformation = new Matrix();
                transformation.postTranslate(xTranslation, yTranslation);
                transformation.preScale(scale, scale);

                paint = new Paint();
                paint.setFilterBitmap(true);

                canvas.drawBitmap(originalImage, transformation, paint);
//                content = readQRCodeFromImage(context,imageUri);
//                content  = readQRCode(background);
                content = scanQRImage(background);
                Log.d("GAT_DATA_1",content);
                if (content == null) {
                    background = Bitmap.createBitmap(900, 900, Bitmap.Config.ARGB_8888);

                    originalWidth = originalImage.getWidth();
                    originalHeight = originalImage.getHeight();

                    canvas = new Canvas(background);

                    scale = 900 / originalWidth;

                    xTranslation = 0.0f;
                    yTranslation = (900 - originalHeight * scale) / 2.0f;

                    transformation = new Matrix();
                    transformation.postTranslate(xTranslation, yTranslation);
                    transformation.preScale(scale, scale);

                    paint = new Paint();
                    paint.setFilterBitmap(true);

                    canvas.drawBitmap(originalImage, transformation, paint);
//                    content = readQRCodeFromImage(context,imageUri);
//                    content  = readQRCode(background);
                    content = scanQRImage(background);
                    if (content != null) {
                        setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", content));
                    } else {
                        setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", content));
                        //setResult(Activity.RESULT_CANCELED);
                    }
                } else {
                    Log.d("GAT_DATA_2",content);

                    if (content != null) {
                        setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", content));
                    } else {
                        setResult(Activity.RESULT_CANCELED);
                    }
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            finish();
        } else {
            Toast.makeText(this, String.valueOf(resCode), Toast.LENGTH_SHORT).show();
            Toast.makeText(this, String.valueOf(reqCode), Toast.LENGTH_SHORT).show();
        }
    }

    private static String readQRCodeFromImage(Context context,Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            String qrContent = readQRCode(bitmap);

            if (qrContent != null) {
                // QR code successfully read
                Log.d("QRCodeReader", "QR Code content: " + qrContent);
                // Do something with the QR code content
            } else {
                // Failed to read QR code
                Log.d("QRCodeReader", "No QR Code found in the image");
            }
            return qrContent;
        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }
    }
    private static String readQRCode(Bitmap bitmap) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            Reader reader = new MultiFormatReader();
            Result result = reader.decode(binaryBitmap);

            return result.getText();
        } catch (Resources.NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
            return null;
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
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

}

