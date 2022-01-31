package com.mascir.oliveoilsensor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mascir.oliveoilsensor.bluetooth.BluetoothHelper1;
import com.mascir.oliveoilsensor.internet.NetworkChangeReceiver;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private Button proceedButton;
    private TextView activeBLE, activeLocal;
    private TextView permissionValid;
    private BluetoothAdapter mBluetoothAdapter = null;
    private static final int  REQUEST_ENABLE_BT = 123;
    private static final int PERMISSION_REQUEST_ACCESS_LOCATION = 456;
    private static final int  REQUEST_INT_GPS = 124;
    private int apiVersion = android.os.Build.VERSION.SDK_INT;
    private LocationManager manager;
    private BluetoothHelper1 bluetoothHelper;
    private RadioGroup radioGroup;
    private RadioButton acidRadioBtn, mixRadioBtn, bothRadioBtn;
    public static int selectedRadioBtn = 0;
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    IntentFilter intentNetworkFilter;
    NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        activeBLE = findViewById(R.id.bleText);
        activeLocal = findViewById(R.id.localText);
        permissionValid = findViewById(R.id.textConfirm);
        proceedButton = findViewById(R.id.proceedBtn);
        radioGroup = findViewById(R.id.radio_group);
        acidRadioBtn = findViewById(R.id.acidityRadioBtn);
        mixRadioBtn = findViewById(R.id.mixingRadioBtn);
        bothRadioBtn = findViewById(R.id.bothRadioBtn);

        bluetoothHelper = new BluetoothHelper1(this, mBluetoothAdapter, manager);

        setElements();

        activeBLE.setOnClickListener(this);
        activeLocal.setOnClickListener(this);
        proceedButton.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioId = group.getCheckedRadioButtonId();

                if(checkedRadioId== R.id.acidityRadioBtn) {
                    selectedRadioBtn = 1;

                } else if(checkedRadioId== R.id.mixingRadioBtn ) {
                    selectedRadioBtn = 2;
                } else if(checkedRadioId== R.id.bothRadioBtn){
                    selectedRadioBtn = 3;
                }
                showSetup();
            }
        });
    }

    private void setElements() {

        if(bluetoothHelper.isBluetoothEnabled()){
            activeBLE.setBackgroundColor(getResources().getColor(R.color.green));
        }else {
            activeBLE.setBackgroundColor(getResources().getColor(R.color.darkGray));
        }

        if (bluetoothHelper.isLocationGranted() && bluetoothHelper.isGPSEnabled()){
            activeLocal.setBackgroundColor(getResources().getColor(R.color.green));
            if(bluetoothHelper.isBluetoothEnabled()){
                acidRadioBtn.setEnabled(true);
                mixRadioBtn.setEnabled(true);
            }
        }else{
            if(bluetoothHelper.isBluetoothEnabled()) {
                activeLocal.setBackgroundColor(getResources().getColor(R.color.darkGray));
                activeLocal.setEnabled(true);
                activeLocal.setClickable(true);
            }else
                activeLocal.setBackgroundColor(getResources().getColor(R.color.secondary_text));

        }
    }

    public void showSetup() {
            permissionValid.setText(R.string.config_compl);
            permissionValid.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
            proceedButton.setEnabled(true);
            proceedButton.setClickable(true);
            proceedButton.setBackgroundResource(R.drawable.btn_shape_round);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.bleText) {
            if (!bluetoothHelper.isBTSupported()) {
                bluetoothHelper.showToast("Votre appareil ne supporte pas le Bluetooth");
                finish();
            }
            if (!bluetoothHelper.isBluetoothEnabled()) {
                bluetoothHelper.enableBluetooth();
            }
        }else if (v.getId() == R.id.localText){
            if(activeLocal.isEnabled() && activeLocal.isClickable()) {
                if (!bluetoothHelper.isLocationGranted())
                    bluetoothHelper.requestPermission();
                else if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    bluetoothHelper.statusCheck();
            }
        }else if(v.getId() == R.id.proceedBtn){
            if(selectedRadioBtn == 1 || selectedRadioBtn == 2) {
                Intent nextIntent = new Intent(ConfigActivity.this, SensorActivity.class);
                nextIntent.putExtra("selectedOption", selectedRadioBtn);
                startActivity(nextIntent);
            }else if(selectedRadioBtn == 3)
                bluetoothHelper.showToast("Cette option n'est pas encore fonctionnelle");
            }else{
                bluetoothHelper.showToast("Veuillez configurer d'abord votre mode d'execution");
                refresh();
            }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(bluetoothHelper.isBluetoothEnabled() && bluetoothHelper.isLocationGranted() && bluetoothHelper.isGPSEnabled()){
            activeLocal.setBackgroundColor(getResources().getColor(R.color.green));
            acidRadioBtn.setEnabled(true);
            mixRadioBtn.setEnabled(true);
            //bothRadioBtn.setEnabled(true);
        }
        intentNetworkFilter = new IntentFilter(CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentNetworkFilter);
    }

    //OnActivityResult for Bluetooth request
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                bluetoothHelper.statusCheck();
            } else {
                // permission denied
                activeLocal.setBackgroundColor(getResources().getColor(R.color.darkGray));
            }
        }
    }

    //OnActivityResult for Bluetooth request
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // permission was granted
                activeBLE.setBackgroundColor(getResources().getColor(R.color.green));
                activeLocal.setBackgroundColor(getResources().getColor(R.color.darkGray));
                activeLocal.setEnabled(true);
                activeLocal.setClickable(true);
                //showSetup();
            }else {
                // permission denied
                activeBLE.setBackgroundColor(getResources().getColor(R.color.darkGray));
            }
        } else if(requestCode == REQUEST_INT_GPS){
            if(resultCode == RESULT_OK){
                    activeLocal.setBackgroundColor(getResources().getColor(R.color.green));
                    acidRadioBtn.setEnabled(true);
                    mixRadioBtn.setEnabled(true);
                    //bothRadioBtn.setEnabled(true);
            }else {
                // permission denied
                activeLocal.setBackgroundColor(getResources().getColor(R.color.darkGray));
                acidRadioBtn.setEnabled(false);
                mixRadioBtn.setEnabled(false);
                //bothRadioBtn.setEnabled(false);
            }
        }
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
        Intent i = new Intent(ConfigActivity.this, ConfigActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

}