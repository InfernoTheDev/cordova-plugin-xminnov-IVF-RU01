package com.illnino.Xminnovrfidreaderaudiojack;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.xminnov.ivrjack.ru01.IvrJackAdapter;
import com.xminnov.ivrjack.ru01.IvrJackService;
import com.xminnov.ivrjack.ru01.IvrJackStatus;

import org.apache.cordova.BuildConfig;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class Xminnovrfidreaderaudiojack extends CordovaPlugin implements IvrJackAdapter{

    private static final String TAG = "ru01";

    public static CordovaWebView gWebView;
    public static IvrJackService service;
    private MainHandler handler;
    public static String lastTag = null;
    public static final String javascriptCallBackFunctionTagReceive = "Xminnovrfidreaderaudiojack.tagReceive";
    public static final String javascriptCallBackFunctionDeviceStatus = "Xminnovrfidreaderaudiojack.updateDeviceStatusChange";
    private BroadcastReceiver volumeBroadcast;

    public Xminnovrfidreaderaudiojack(){}

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        gWebView = webView;
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("ready")) {
            callbackContext.success("Xminnovrfidreaderaudiojack: ready");
        }else if (action.equals("registerService")){
            checkGrantedPermission();
            init();
            callbackContext.success("Xminnovrfidreaderaudiojack: registerService");
        }else if (action.equals("unRegisterService")){
            unRegisterService();
            callbackContext.success("Xminnovrfidreaderaudiojack: unregisterService");

        }else if(action.equals("start")){
            //Start UHF/RFID reader service
            startScan();
            callbackContext.success("Xminnovrfidreaderaudiojack: Start scanner.");
        }else if(action.equals("stop")){
            //Stop reader service
            stopScan();
            callbackContext.success("Xminnovrfidreaderaudiojack: Stop scanner.");
        }else if(action.equals("onScannerReceive")){
            callbackContext.success();
        }else if(action.equals("onDeviceStatusChange")){
            callbackContext.success();
        }else {
            return false;
        }

        return true;


    }

    private void checkGrantedPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cordova.getActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                cordova.getActivity().shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);
            }
        }
    }

    @Override
    public void onConnect(String deviceSn) {
        Log.i(TAG, "on connect");
    }

    @Override
    public void onDisconnect() {
        Log.i(TAG, "on disconnect");
    }

    @Override
    public void onStatusChange(IvrJackStatus status) {
        Log.i(TAG, "on status change: " + status);
        String msg = "";
        switch (status) {
            case ijsDetecting:
                msg = "Device detecting...";
                Log.i(TAG, msg);
                sentDeviceStatus(msg);
                break;
            case ijsRecognized:
                msg = "Device connected.";
                Log.i(TAG, msg);
                sentDeviceStatus(msg);
                break;
            case ijsUnRecognized:
                msg = "Device unrecognized.";
                Log.i(TAG, msg);
                sentDeviceStatus(msg);
                break;
            case ijsPlugout:
                msg = "Device disconnected.";
                Log.i(TAG, msg);
                sentDeviceStatus(msg);
                break;
        }
    }

    @Override
    public void onInventory(byte[] epc) {
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<epc.length; i++) {
            builder.append(String.format("%02X", epc[i]));
            if ((i+1)%4==0) builder.append(" ");
        }
        Log.i(TAG, builder.toString());
        lastTag = builder.toString();
        this.sendPayload(lastTag);
        lastTag = null;
    }

    private void sentDeviceStatus(String status){
        final String callBack = "javascript:" + javascriptCallBackFunctionDeviceStatus + "('" + status + "');";
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gWebView.loadUrl(callBack);
            }
        });
    }

    private void sendPayload(final String tag){
        final String callBack = "javascript:" + javascriptCallBackFunctionTagReceive + "('" + tag + "');";
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gWebView.loadUrl(callBack);
            }
        });
    }

    private void init(){

        AudioManager audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeBroadcast = new VolumnBroadcast();
        cordova.getActivity().registerReceiver(volumeBroadcast, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));

        handler = new MainHandler();
        this.registerService();
    }

    private void registerService(){
        service = new IvrJackService(cordova.getActivity(), this);
        service.open();
    }

    private void unRegisterService(){
        service.close();
        service = null;
    }

    private void startScan(){
        new Thread(new StartTask()).start();
    }

    private void stopScan(){
        new Thread(new StopTask()).start();
    }
    
    class StartTask implements Runnable {

        @Override
        public void run() {
            int ret = service.setReadEpcStatus((byte) 1);
            handler.obtainMessage(0, ret).sendToTarget();
        }
    }

    class StopTask implements Runnable {

        @Override
        public void run() {
            int ret = service.setReadEpcStatus((byte) 0);
            handler.obtainMessage(1, ret).sendToTarget();
        }
    }

    class MainHandler extends Handler {

        public MainHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    int ret = (Integer) msg.obj;
                    if (ret == 0) {
                        Log.i(TAG, "Device start read epc");
                    } else {
                        Log.i(TAG, "Device start read epc failed: " + ret);
                    }
                    break;
                }
                case 1: {
                    Log.i(TAG, "Device stop read epc");
                    break;
                }
            }
        }
    }

    private class VolumnBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /*AudioManager audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);*/
        }

    }
}
