package com.mascir.oliveoilsensor;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mascir.oliveoilsensor.bluetooth.BluetoothCalibrate;
import com.mascir.oliveoilsensor.bluetooth.BluetoothHelper1;
import com.mascir.oliveoilsensor.bluetooth.BluetoothScanAcidity;
import com.mascir.oliveoilsensor.bluetooth.BluetoothScanMixing;

import java.io.UnsupportedEncodingException;
import java.util.Set;

public class SensorActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressBar progressBar;
    private TextView textView;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothHelper1 bluetoothHelper;
    private LocationManager manager;
    public static BluetoothDevice mmDevice;
    public static int selectedOption;
    private BluetoothStatusReceiver bluetoothStatusReceiver = new BluetoothStatusReceiver();
    private Button calibrateBtn, scanBtn, printBtn;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothStatusReceiver);
        unregisterReceiver(mPairReceiver);
        unregisterReceiver(deviceReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        selectedOption = getIntent().getIntExtra("selectedOption", 0);

        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        bluetoothHelper = new BluetoothHelper1(this, mBluetoothAdapter, manager);

        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.sensorName);
        calibrateBtn = findViewById(R.id.calibr_btn);
        scanBtn = findViewById(R.id.scan_btn);
        printBtn = findViewById(R.id.print_btn);

        //Event when bluetooth state is changed
        IntentFilter bluetoothStatusReceivedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStatusReceiver, bluetoothStatusReceivedFilter);
        //Event when bond state is changed
        registerReceiver(deviceReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mPairReceiver, intent);


        if(bluetoothHelper.isBluetoothEnabled() && bluetoothHelper.isLocationGranted() && bluetoothHelper.isGPSEnabled()) {
            progressBar.setVisibility(View.VISIBLE);
            mBluetoothAdapter.startDiscovery();
            //Find device
            getBoundedDevices();
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(deviceReceiver, intentFilter);

            /*Thread timer = new Thread() {
                public void run() {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        mBluetoothAdapter.startDiscovery();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Find device
                                getBoundedDevices();

                            }
                        });
                        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(deviceReceiver, intentFilter);
                        //finish();

                    }
                }

            };
            timer.start();*/

        }else{
            goBack();
        }

        //calibrateBtn.setOnClickListener(this);
        //scanBtn.setOnClickListener(this);
        //printBtn.setOnClickListener(this);
    }

    private void getBoundedDevices() {
        Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {

            for (final BluetoothDevice device : pairedDevice) {
                if (isValidSensor(device.getName())) {
                    mmDevice = device;
                    doIfPair();
                }
            }
            invalidateOptionsMenu();
        }
    }

   public boolean isValidSensor(String deviceName) {
        return deviceName.matches(bluetoothHelper.getSensor_name());
   }

    private void doIfPair() {
        progressBar.setVisibility(View.GONE);
        textView.setText(bluetoothHelper.getSensor_name());
        calibrateBtn.setVisibility(View.VISIBLE);
        calibrateBtn.setEnabled(true);
        scanBtn.setVisibility(View.VISIBLE);
        scanBtn.setEnabled(true);
        calibrateBtn.setOnClickListener(this);
        scanBtn.setOnClickListener(this);
    }

    private boolean isDevicePaired(BluetoothDevice device){
        Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {
            for (final BluetoothDevice paireddevice : pairedDevice) {
                return paireddevice.getName().equals(device.getName());
            }
            invalidateOptionsMenu();
        }
        return false;
    }

    BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case BluetoothDevice.ACTION_FOUND:

                    final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if(device!=null && device.getBondState() == BluetoothDevice.BOND_NONE) {
                        if (device.getName() != null && isValidSensor(device.getName())) {
                            bluetoothHelper.pairDevice(device);
                            //mmDevice = device;
                            if(isDevicePaired(device)){
                                mmDevice = device;
                                doIfPair();
                            }
                        }
                    }

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    //bluetoothHelper.showToast("Discovery finished");
                    //dismissProgress();
                    //progressBar.setVisibility(View.GONE);

            }
            invalidateOptionsMenu();
        }
    };

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    progressBar.setVisibility(View.GONE);
                    bluetoothHelper.showToast("Analyseur associé");
                    doIfPair();
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.calibr_btn){
            new BluetoothCalibrate(this, mBluetoothAdapter, progressBar, textView, bluetoothHelper).execute();
        }else if(v.getId() == R.id.scan_btn){
            if(selectedOption == 1)
                new BluetoothScanAcidity(this, progressBar, textView).execute();
            else if(selectedOption == 2)
                new BluetoothScanMixing(this, progressBar, textView).execute();
            else {
                bluetoothHelper.showToast("Veuillez configurer d'abord votre mode d'execution");
                goBack();
            }
        }

    }

    private class BluetoothStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        // Log.d(TAG, "BluetoothAdapter.STATE_TURNING_OFF");
                        goBack();

                    case BluetoothAdapter.STATE_OFF:
                        goBack();

                        //Log.d(TAG, "BluetoothAdapter.STATE_OFF");
                    case BluetoothAdapter.STATE_TURNING_ON:
                        // Log.d(TAG, "BluetoothAdapter.STATE_TURNING_ON");
                        refresh();

                    case BluetoothAdapter.STATE_ON:
                        // Log.d(TAG, "BluetoothAdapter.STATE_ON");
                }
            }
        }
    }

    private void goBack(){
        Intent i = new Intent(SensorActivity.this, ConfigActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
    private void refresh(){
        Intent i = new Intent(SensorActivity.this, SensorActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("selectedOption",selectedOption);
        startActivity(i);
    }
}

