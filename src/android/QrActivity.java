package com.qrscan.plugin;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.content.Context;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
public class QrActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private Class<?> mClss;

    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;
    int SELECT_PICTURE = 200;
    private static List<BarcodeFormat> formats = new ArrayList<>();
    Context context;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        getActionBar().hide();
        //getSupportActionBar().hide();
        Intent intent = getIntent();
        context = getApplicationContext();
        String package_name = getApplication().getPackageName();
        setContentView(getApplication().getResources().getIdentifier("activity_qr", "layout", package_name));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("QR_READER", "TRY PERMISSION CAMERA");
            mClss = QrActivity.class;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        }
        else {
            Log.d("QR_READER", "ERROR PERMISSION CAMERA");
        }

        //R.id.content_frame
        ViewGroup contentFrame = (ViewGroup) findViewById(getApplication().getResources().getIdentifier("content_frame", "id", package_name));
        mScannerView = new ZXingScannerView(this);
        
        formats.add(BarcodeFormat.QR_CODE);
        formats.add(BarcodeFormat.CODE_128);
        mScannerView.setFormats(formats);
        
        contentFrame.addView(mScannerView);

        Button btn = findViewById(getApplication().getResources().getIdentifier("button", "id", package_name));
        Button btn2 = findViewById(getApplication().getResources().getIdentifier("button2", "id", package_name));
        Button btn3 = findViewById(getApplication().getResources().getIdentifier("button3", "id", package_name));
        String dt =intent.getStringExtra("LNG");
        if(dt.equals("tj")||dt.equals("TJ")){
            btn.setText("Боргирии QR аз галерея");
            btn3.setText("Чароғак");
        }else if(dt.equals("ru") || dt.equals("RU")){
            btn.setText("QR загрузить с галереи");
            btn3.setText("Фонарик");
        }

        btn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(askPermissionAndBrowseFile()) {
                    openInGallery();
                } else {
                    Log.d("QR_READER", "ERROR PERMISSION");
                }
            }
        });

        btn2.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });

        btn3.setOnClickListener( new View.OnClickListener() {
       
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(!mScannerView.getFlash())
                    mScannerView.setFlash(true);
                else
                    mScannerView.setFlash(false);
            }
        });
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Handle the Intent
                        Intent data = result.getData();
                        // Do something with the result

//                        if(resCode == Activity.RESULT_OK && reqCode == MY_RESULT_CODE_FILECHOOSER){

                            try {
                                Bitmap originalImage  = null;
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

                                background = Bitmap.createBitmap(1500,1500, Bitmap.Config.ARGB_8888);

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
                                content = scanQRImage(background);
                                if(content == null){
                                    background = Bitmap.createBitmap(900,900, Bitmap.Config.ARGB_8888);

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
                                    content = scanQRImage(background);
                                    if(content != null){
                                        setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", content));
                                    }else{
                                        setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", content));
                                        //setResult(Activity.RESULT_CANCELED);
                                    }
                                }else{
                                    if(content != null){
                                        setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", content));
                                    }else{
                                        setResult(Activity.RESULT_CANCELED);
                                    }
                                }



                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }



                            finish();
                    }else{
                        Toast.makeText(this, String.valueOf(result.getResultCode()), Toast.LENGTH_SHORT).show();
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

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        someActivityResultLauncher.launch(intent);
//        startActivityForResult(intent, 0);
    }

    private boolean askPermissionAndBrowseFile()  {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
        (
                ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) == PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO) == PERMISSION_GRANTED
        )) { // Level 23

            // Check if we have Call permission
            int permisson = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permisson != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_REQUEST_CODE_PERMISSION
                );
                return false;
            }
        }
        return true;
    }

    @Override
    public void handleResult(Result rawResult) {
        setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", rawResult.getText()));
        finish();
    }

//    @Override
//    protected void onActivityResult(int reqCode, int resCode, Intent data) {
//
//        if(resCode == Activity.RESULT_OK && reqCode == MY_RESULT_CODE_FILECHOOSER){
//
//            try {
//                Bitmap originalImage  = null;
//                Bitmap background = null;
//                String content;
//                float originalWidth;
//                float originalHeight;
//                Canvas canvas;
//                float scale;
//                float xTranslation;
//                float yTranslation;
//                Matrix transformation;
//                Paint paint;
//                final Uri imageUri = data.getData();
//
//                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//
//                originalImage = selectedImage;
//
//                background = Bitmap.createBitmap(1500,1500, Bitmap.Config.ARGB_8888);
//
//                originalWidth = originalImage.getWidth();
//                originalHeight = originalImage.getHeight();
//
//                canvas = new Canvas(background);
//
//                scale = 1500 / originalWidth;
//
//                xTranslation = 0.0f;
//                yTranslation = (1500 - originalHeight * scale) / 2.0f;
//
//                transformation = new Matrix();
//                transformation.postTranslate(xTranslation, yTranslation);
//                transformation.preScale(scale, scale);
//
//                paint = new Paint();
//                paint.setFilterBitmap(true);
//
//                canvas.drawBitmap(originalImage, transformation, paint);
//                content = scanQRImage(background);
//                if(content == null){
//                    background = Bitmap.createBitmap(900,900, Bitmap.Config.ARGB_8888);
//
//                    originalWidth = originalImage.getWidth();
//                    originalHeight = originalImage.getHeight();
//
//                    canvas = new Canvas(background);
//
//                    scale = 900 / originalWidth;
//
//                    xTranslation = 0.0f;
//                    yTranslation = (900 - originalHeight * scale) / 2.0f;
//
//                    transformation = new Matrix();
//                    transformation.postTranslate(xTranslation, yTranslation);
//                    transformation.preScale(scale, scale);
//
//                    paint = new Paint();
//                    paint.setFilterBitmap(true);
//
//                    canvas.drawBitmap(originalImage, transformation, paint);
//                    content = scanQRImage(background);
//                    if(content != null){
//                        setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", content));
//                    }else{
//                        setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", content));
//                        //setResult(Activity.RESULT_CANCELED);
//                    }
//                }else{
//                    if(content != null){
//                        setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", content));
//                    }else{
//                        setResult(Activity.RESULT_CANCELED);
//                    }
//                }
//
//
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//
//
//            finish();
//        }else{
//            Toast.makeText(this, String.valueOf(resCode), Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, String.valueOf(reqCode), Toast.LENGTH_SHORT).show();
//        }
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

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {

        if(mScannerView!=null)
            mScannerView.stopCamera();
        super.onPause();
    }


    public static class RealPathUtil {

        @SuppressLint("NewApi")
        public static String getRealPathFromURI_API19(Context context, Uri uri){
            String filePath = "";
            String wholeID = DocumentsContract.getDocumentId(uri);

            String id = wholeID.split(":")[1];

            String[] column = { MediaStore.Images.Media.DATA };

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{ id }, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
            return filePath;
        }


        @SuppressLint("NewApi")
        public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
            String[] proj = { MediaStore.Images.Media.DATA };
            String result = null;

            CursorLoader cursorLoader = new CursorLoader(
                    context,
                    contentUri, proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();

            if(cursor != null){
                int column_index =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                result = cursor.getString(column_index);
            }
            return result;
        }

        public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index
                    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
    }
}

