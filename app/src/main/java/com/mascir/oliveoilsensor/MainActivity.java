package com.mascir.oliveoilsensor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mascir.oliveoilsensor.bluetooth.BluetoothCalibratev2;
import com.mascir.oliveoilsensor.bluetooth.BluetoothConnect;
import com.mascir.oliveoilsensor.bluetooth.BluetoothHelper;
import com.mascir.oliveoilsensor.bluetooth.BluetoothScanv2;
import com.mascir.oliveoilsensor.internet.NetworkChangeReceiver;
import com.mascir.oliveoilsensor.utils.UtilsMethod;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kotlin.Pair;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private BluetoothHelper bluetoothHelper;
    private BluetoothAdapter mBluetoothAdapter;
    private LocationManager manager;
    public static BluetoothDevice mmDevice;
    private CardView scanBTCard, activeBTCard;
    private Button launchCalibBtn, launchScanBtn, launchStopBtn;
    private TextView btTextView;
    private BluetoothStatusReceiver bluetoothStatusReceiver = new BluetoothStatusReceiver();
    private BluetoothConnect bluetoothConnect;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private int selectedItem;
    //public static CardView cardView3 = null;
    //public static TextView textViewRes3 = null;
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    IntentFilter intentFilter;
    //NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    int counter = 5;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothStatusReceiver);
        unregisterReceiver(mPairReceiver);
        unregisterReceiver(deviceReceiver);
        //unregisterReceiver(networkChangeReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activeBTCard = findViewById(R.id.btCard);
        scanBTCard = findViewById(R.id.scanCard);
        launchCalibBtn = findViewById(R.id.calibr_btn);
        launchScanBtn = findViewById(R.id.scan_btn);
        launchStopBtn = findViewById(R.id.stop_btn);
        btTextView = findViewById(R.id.bluetoothText);
        progressBar = findViewById(R.id.progressbar);
        progressDialog = new ProgressDialog(this);
        //cardView3 = findViewById(R.id.resultCard3);
        //textViewRes3 = findViewById(R.id.resultValue3);

        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        bluetoothHelper = new BluetoothHelper(MainActivity.this, mBluetoothAdapter, manager);
        bluetoothConnect = new BluetoothConnect(this, mBluetoothAdapter, progressDialog, bluetoothHelper);


        if (bluetoothHelper.isBluetoothEnabled()) {
            ifBTON();
        } else {
            ifBTOFF();
        }

        activeBTCard.setClickable(true);
        activeBTCard.setOnClickListener(this);
        scanBTCard.setOnClickListener(this);

        registerReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        intentFilter = new IntentFilter(CONNECTIVITY_ACTION);
        //registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btCard) {
            /*UtilsMethod utilsMethod = new UtilsMethod(this);
            String readMessage = "22666//2050.22-100.00//2051.22-100.00//2052.22-100.00//2053.22-100.00//2054.22-100.00//2055.22-100.00//2056.22-100.00//2057.22-100.00//2058.22-100.00//2059.22-100.00//2060.22-100.00//2061.22-100.00//2062.22-100.00//2063.22-100.00//2064.22-100.00//2065.22-100.00//2066.22-100.00//2067.22-100.00//2068.22-100.00//2069.22-100.00//2070.22-100.00//2071.22-100.00";
            Pair<Integer, List<Pair<Float, Float>>> result  = utilsMethod.transformMessage(readMessage);
            String filename = utilsMethod.saveReadingValueXlsx(result);
            ArrayList<String> array = utilsMethod.readXlsxFile(getExternalFilesDir(null)+"/sensorDirectory/" + filename);
            System.out.println(array);*/
            if (!bluetoothHelper.isBTSupported()) {
                bluetoothHelper.showToast("Votre appareil ne supporte pas le Bluetooth");
                finish();
            }

            if (bluetoothHelper.isBluetoothEnabled()) {
                bluetoothHelper.disableBluetooth();
            } else {
                bluetoothHelper.enableBluetooth();
            }

        } else if (v.getId() == R.id.scanCard) {

            if (bluetoothHelper.isBluetoothEnabled() && bluetoothHelper.isLocationGranted() && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //isScanning = true;
                progressBar.setVisibility(View.VISIBLE);
                mBluetoothAdapter.startDiscovery();
                //Find device
                getBoundedDevices();
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(deviceReceiver, intentFilter);

            } else {

                if (!bluetoothHelper.isBluetoothEnabled())
                    bluetoothHelper.enableBluetooth();
                else if (!bluetoothHelper.isLocationGranted())
                    bluetoothHelper.requestPermission();
                else if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    bluetoothHelper.statusCheck();
                else
                    bluetoothHelper.showToast("Veuillez vérifier votre Bluetooth et/ou GPS");
            }
        } else if (v.getId() == R.id.calibr_btn) {

            if(launchCalibBtn.isClickable()) {
                //dialog();
                //launchScanBtn.setVisibility(View.GONE);
                //new BluetoothConnect(MainActivity.this, mBluetoothAdapter, progressDialog, bluetoothHelper).execute(mmDevice);
                //new BluetoothCalibrate(MainActivity.this, mBluetoothAdapter, progressDialog, bluetoothHelper).execute(selectedItem);
                new BluetoothCalibratev2(MainActivity.this, mBluetoothAdapter, progressDialog, bluetoothHelper).execute();
            } else
                bluetoothHelper.showToast("Vous devez arrêter le Scan avant de relancer le calibrage");

        }else if (v.getId() == R.id.scan_btn) {
            if(launchScanBtn.isClickable()) {
                //counter--;
                activeBTCard.setClickable(false);
                new BluetoothScanv2(MainActivity.this, progressDialog).execute();
            }

        }
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
                            mmDevice = device;
                        }
                    }

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    // bluetoothHelper.showToast("Discovery finished");
                    //dismissProgress();

            }
            invalidateOptionsMenu();
        }
    };

    public boolean isValidSensor(String deviceName) {
        return deviceName.matches(bluetoothHelper.getSensor_name());
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
                        ifBTOFF();

                    case BluetoothAdapter.STATE_OFF:
                        refresh();

                        //Log.d(TAG, "BluetoothAdapter.STATE_OFF");
                    case BluetoothAdapter.STATE_TURNING_ON:
                        // Log.d(TAG, "BluetoothAdapter.STATE_TURNING_ON");
                        ifBTON();

                    case BluetoothAdapter.STATE_ON:
                        // Log.d(TAG, "BluetoothAdapter.STATE_ON");
                }
            }
        }
    }

    private void ifBTOFF(){
        btTextView.setText(R.string.active_btn_name);
        btTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bluetooth, 0, R.drawable.ic_state, 0);
    }
    private void ifBTON(){
        btTextView.setText(R.string.deactivate_btn_name);
        btTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bluetooth, 0, R.drawable.ic_state_active, 0);
    }

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    progressBar.setVisibility(View.GONE);
                    bluetoothHelper.showToast("Capteur associé");
                    doIfPair();
                }
            }
        }
    };


    private void getBoundedDevices() {
        Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {

            for (final BluetoothDevice device : pairedDevice) {
                if (isValidSensor(device.getName())) {
                    progressBar.setVisibility(View.GONE);
                    mmDevice = device;
                    doIfPair();
                }
            }
            invalidateOptionsMenu();
        }
    }
    private void registerReceivers() {
        //Event when bluetooth state is changed
        IntentFilter bluetoothStatusReceivedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStatusReceiver, bluetoothStatusReceivedFilter);

        //Event when bond state is changed
        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mPairReceiver, intent);

        registerReceiver(deviceReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    private void doIfPair() {
        scanBTCard.setVisibility(View.GONE);
        launchCalibBtn.setVisibility(View.VISIBLE);
        launchCalibBtn.setOnClickListener(MainActivity.this);
        launchScanBtn.setOnClickListener(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.id_refresh) {
            refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void refresh(){
        Intent i = new Intent(MainActivity.this, MainActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void dialog(){
        AlertDialog.Builder aa = new AlertDialog.Builder(this);
        aa.setTitle("Choose any one extension type");
        aa.setSingleChoiceItems(R.array.traitements, 2, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                selectedItem = which;
            }
        });
        aa.show();
    }


}
