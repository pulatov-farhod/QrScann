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
            this.openQrActivity(context);

            PluginResult pluginResult = new  PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true); // Keep callback

            return true;
        }
        return false;
    }

    private void openQrActivity(Context context) {

        Intent intent = new Intent(context, QrActivity.class);

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
