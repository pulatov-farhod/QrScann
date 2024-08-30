package com.qrscan.plugin;

import android.content.Context;
import android.content.Intent;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * This class echoes a string called from JavaScript.
 */
public class QrScann extends CordovaPlugin  {
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    private CallbackContext callbackContext;

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        this.callbackContext = callbackContext;
        Context context = cordova.getActivity().getApplicationContext();
        if(action.equals("qrRun")) {
            String lng = args.getString(0);
            this.openQrActivity2(context,lng);

            PluginResult pluginResult = new  PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true); // Keep callback

            return true;
        }
        return false;
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Intent originalIntent = result.getOriginalIntent();
                    Context context = cordova.getActivity().getApplicationContext();
                    if (originalIntent == null) {
                        Log.d("MainActivity", "Cancelled scan");
                        Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show();
                    } else if(originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                        Log.d("MainActivity", "Cancelled scan due to missing camera permission");
                        Toast.makeText(context, "Cancelled due to missing camera permission", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("MainActivity", "Scanned");
                    Toast.makeText(context, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                    PluginResult resultado = new PluginResult(PluginResult.Status.OK, result.getContents());
                    resultado.setKeepCallback(true);
                    this.callbackContext.sendPluginResult(resultado);

                }
            });

    private void openQrActivity2(Context context,String lng) {



        ScanOptions options = new ScanOptions().setOrientationLocked(false).setCaptureActivity(QrActivity.class);
        barcodeLauncher.launch(options);

//        Intent intent = new Intent(context, QrActivity.class);
//        intent.putExtra("LNG",lng);
//
//        cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
    }

    private void openQrActivity(Context context,String lng) {

        Intent intent = new Intent(context, QrActivity.class);
        intent.putExtra("LNG",lng);
        cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
    }
   
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if(resultCode == cordova.getActivity().RESULT_OK){

            String qrResult = data.getStringExtra("QrResult");

            PluginResult resultado = new PluginResult(PluginResult.Status.OK, qrResult);
            resultado.setKeepCallback(true);
            this.callbackContext.sendPluginResult(resultado);
            return;
        }else if(resultCode == cordova.getActivity().RESULT_CANCELED){
            PluginResult resultado = new PluginResult(PluginResult.Status.ERROR, "Error decode");
            resultado.setKeepCallback(true);
            this.callbackContext.sendPluginResult(resultado);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }
}
