package cordova-plugin-xminnov-rfid-reader-audio-jack;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class xminnov-UHF-RFID-reader-audio-jack extends CordovaPlugin implements IvrJackAdapter{

    private static final String TAG = "ru01";

    public static IvrJackService service;

    private MainHandler handler;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        return false;
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
        switch (status) {
            case ijsDetecting:
                Log.i(TAG, "Device detecting...");
                break;
            case ijsRecognized:
                Log.i(TAG, "Device connected.");
                break;
            case ijsUnRecognized:
                Log.i(TAG, "Device unrecognized.");
                break;
            case ijsPlugout:
                Log.i(TAG, "Device disconnected.");
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
    }

    private void init(){
        handler = new MainHandler();
        service = new IvrJackService(this, this);
    }

    private void startService(){
        service.open();
    }

    private void stopService(){
        service.close();
        service = null;
    }

    private startScan(){
        new Thread(new StartTask()).start();
    }

    private stopScan(){
        new Thread(new StopTask()).start();
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
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
}
