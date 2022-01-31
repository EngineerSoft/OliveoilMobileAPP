package com.mascir.oliveoilsensor.utils;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Calendar;

public class SharedPrefManager {

    private SharedPreferences sharedPreferences;
    private static SharedPrefManager mInstance;
    private static Context mCtx;
    private SharedPreferences.Editor editor;
    private int PRIVATE_MODE = 0;


    private static final String SHARED_PREF_NAME = "oliveoilsharedpref";
    private static final String TAG_DEVICE = "tagdevice";
    private static final String ID_SPECTRE = "idspectre";

    private static final String TAG_FILE = "tagfile";

    private SharedPrefManager(Context context) {
        mCtx = context;
        sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    public void saveSpectre(String spectrId){
        editor.putString(ID_SPECTRE, spectrId);
        editor.commit();
        editor.apply();
    }

    public String getSpectreId(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ID_SPECTRE, "");
    }

    public void saveDevice(BluetoothDevice device){
        Gson gson = new Gson();
        String json = gson.toJson(device);
        editor.putString(TAG_DEVICE, json);
        editor.commit();

        editor.apply();
    }

    public BluetoothDevice getScannedDevice(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(TAG_DEVICE, "");
        BluetoothDevice device = gson.fromJson(json, BluetoothDevice.class);
        return device;
    }


    public void saveFile(String filename){
        Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int seconds = c.get(Calendar.SECOND);
        String dateForSto = day+"/"+month+"/"+year+" "+hour+":"+minute+":"+seconds;
        editor.putString(filename, dateForSto);
        editor.apply();
        editor.commit();
    }

}
