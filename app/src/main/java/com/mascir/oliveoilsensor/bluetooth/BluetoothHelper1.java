package com.mascir.oliveoilsensor.bluetooth;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mascir.oliveoilsensor.R;

import java.lang.reflect.Method;
import java.util.UUID;


public class BluetoothHelper1 {
    private Activity activity;
    private UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    private String sensor_name = "AgriSensor";
    BluetoothAdapter mBluetoothAdapter;
    private LocationManager manager;
    private static final int  REQUEST_ENABLE_BT = 123;
    private static final int  REQUEST_INT_GPS = 124;
    private static final int PERMISSION_REQUEST_ACCESS_LOCATION = 456;


    public BluetoothHelper1(Activity activity, BluetoothAdapter bluetoothAdapter, LocationManager manager) {
        this.activity = activity;
        this.mBluetoothAdapter = bluetoothAdapter;
        this.manager = manager;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getSensor_name() {
        return sensor_name;
    }

    public void enableBluetooth() {

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    public void disableBluetooth() {
        mBluetoothAdapter.disable();
    }

    public void requestPermission() {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_ACCESS_LOCATION);

        } else {
            // Permission has already been granted, check if its enabled
            statusCheck();
        }
    }

    public void statusCheck() {

        if (!isGPSEnabled()) {
            buildAlertMessageNoGps();
        }
    }

    public void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Permettre à l'application d'accéder à votre position GPS ?")
                .setCancelable(false)
                .setPositiveButton("Autoriser", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        Intent enableGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivityForResult(enableGPS, REQUEST_INT_GPS);
                    }
                })
                .setNegativeButton("Refuser", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isBTSupported() {
        return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) && mBluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public boolean isLocationGranted() {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isGPSEnabled() {
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void showToast(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }

    public void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
