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

public class QrActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private Class<?> mClss;

    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;
    
    private static List<BarcodeFormat> formats = new ArrayList<>();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        getActionBar().hide();
        //getSupportActionBar().hide();
        Intent intent = getIntent();
        
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
            Log.e("QR_READER", "Error decoding barcode", e);
        }
        return contents;
    }

    public void openInGallery() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    private boolean askPermissionAndBrowseFile()  {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23

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

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if(resCode == Activity.RESULT_OK && data != null){
            String realPath;
            // SDK < API11
            if (Build.VERSION.SDK_INT < 11) {
                realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, data.getData());
            }

            // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19) {
                realPath = RealPathUtil.getRealPathFromURI_API11to18(this, data.getData());
            }

            // SDK > 19 (Android 4.4)
            else {
                realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());
            }

            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(realPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            String decoded=scanQRImage(bitmap);

            if(decoded!=null)
                setResult(Activity.RESULT_OK, new Intent().putExtra("QrResult", decoded));
            else
                setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }


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

