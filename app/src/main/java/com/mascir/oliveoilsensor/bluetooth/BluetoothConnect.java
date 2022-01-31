package com.mascir.oliveoilsensor.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mascir.oliveoilsensor.R;
import com.mascir.oliveoilsensor.bluetooth.BluetoothHelper;
import com.mascir.oliveoilsensor.utils.AsyncCallerSensor;
import com.mascir.oliveoilsensor.utils.UtilsMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import kotlin.Pair;

//Not used
public class BluetoothConnect extends AsyncTask<BluetoothDevice, Void, Void> {

    private Activity activity;
    private BluetoothAdapter mBluetoothAdapter;
    public  BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private ProgressDialog progressDialog;
    private BluetoothHelper bluetoothHelper;
    private static final String TAG = "TAG:BluetoothConnect";
    private Button calibrBtn, scanBtn, stopBtn;
    private TextView data;
    private UtilsMethod utilsMethod;

    public BluetoothConnect(Activity activity, BluetoothAdapter mBluetoothAdapter, ProgressDialog progressDialog, BluetoothHelper bluetoothHelper) {
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.progressDialog = progressDialog;
        this.bluetoothHelper = bluetoothHelper;
        this.scanBtn = activity.findViewById(R.id.scan_btn);
        this.calibrBtn = activity.findViewById(R.id.calibr_btn);
        this.stopBtn = activity.findViewById(R.id.stop_btn);
        //this.data = activity.findViewById(R.id.valueText);
        this.activity = activity;
        this.utilsMethod = new UtilsMethod(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressDialog.setMessage("Veuillez patienter...");
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();
    }

    @Override
    protected Void doInBackground(BluetoothDevice... devices) {

        BluetoothSocket tmp = null;
        mmDevice = devices[0];
        try {

            tmp = mmDevice.createRfcommSocketToServiceRecord(bluetoothHelper.getUuid());

        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed: " + e);
        }
        mmSocket = tmp;
        mBluetoothAdapter.cancelDiscovery();
        try {
            mmSocket.connect();
            // bluetoothHelper.showToast("Connected successfully to : " + device.getName() + ", " + device.getAddress());
        } catch (IOException connectException) {
            //Log.v(TAG, "Connection exception!");
            Log.e(TAG, "Connection exception!");
            try {
               mmSocket.close();

            } catch (IOException closeException) {
                Log.e(TAG, "Connection exception!");
            }
        }

        try {
            sendBG();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void sendBG() throws IOException {

        String msg = "bg\r\n";
        OutputStream mmOutputStream = mmSocket.getOutputStream();
        mmOutputStream.write(msg.getBytes(StandardCharsets.UTF_8));
        receiveBG();
    }

    public void receiveBG() throws IOException {
        InputStream mmInputStream = mmSocket.getInputStream();
        byte[] buffer = new byte[256];
        int bytes;

        try {
            bytes = mmInputStream.read(buffer);
            final String readMessage = new String(buffer, 0, bytes);
            //Log.d(TAG, "Received: " + readMessage);

            if (readMessage.contains("BCK_Done")) {
                progressDialog.dismiss();
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Stuff that updates the UI
                        calibrBtn.setVisibility(View.VISIBLE);
                        calibrBtn.setClickable(false);
                        scanBtn.setVisibility(View.VISIBLE);
                    }
                });

                scanBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            sendSC();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } else {
                Log.e(TAG, "Echec du lancement du Background");
            }

            //mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Problems occurred!");
            return;
        }
    }

    public void sendSC() throws IOException {
        String msg = "sc\r\n";
        OutputStream mmOutputStream = mmSocket.getOutputStream();
        mmOutputStream.write(msg.getBytes(StandardCharsets.UTF_8));
        receiveSC();
    }

    public void receiveSC() throws IOException {
        InputStream mmInputStream = mmSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(mmInputStream));
        //byte[] buffer = new byte[256];/*[256]*/
        //int bytes;

        try {
            //bytes = mmInputStream.read(buffer);
            //String readMessage = new String(buffer, 0, bytes);
            String readMessage = reader.readLine();
            Log.d(TAG, "Received: " + readMessage);
            //showToast("Data received : "+readMessage);
            Pair<Integer, List<Pair<Float, Float>>> result = utilsMethod.transformMessage(readMessage);
            //String filename = utilsMethod.saveReadingValue(result);
            //File fileToSend = new File(activity.getExternalFilesDir(null)+"/sensorDirectory/" + filename);
            //new AsyncCallerSensor(fileToSend, activity).execute();

            //data.setVisibility(View.VISIBLE);
            //data.append("Valeur reçue : " + readMessage);
            scanBtn.setText("Relancer le scan");
            stopBtn.setVisibility(View.VISIBLE);
            stopBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    closeSocket();
                    scanBtn.setVisibility(View.GONE);
                    stopBtn.setVisibility(View.GONE);
                    calibrBtn.setClickable(true);
                }
            });
            //mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Problems occurred!");
            return;
        }
    }



    private void closeSocket(){
        if(mmSocket!=null) {
            try {
                mmSocket.getInputStream().close();
                mmSocket.getInputStream().close();
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


