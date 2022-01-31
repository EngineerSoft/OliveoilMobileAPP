package com.mascir.oliveoilsensor.internet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.mascir.oliveoilsensor.ConfigActivity;
import com.mascir.oliveoilsensor.utils.SharedPrefManager;

import com.mascir.oliveoilsensor.utils.AsyncCallerResult;
import com.mascir.oliveoilsensor.utils.AsyncCallerSensor;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Thread.sleep;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String actionOfIntent = intent.getAction();
        File sensorDir = new File(context.getExternalFilesDir(null).toString() + "/sensorDirectory/");
        File resultDir = new File(context.getExternalFilesDir(null).toString() + "/resultDirectory/");

        if (actionOfIntent.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            if (isNetworkAvailable(context)) {
                try {
                    sendSensorFiles(sensorDir, context);
                    sendResultFiles(resultDir, context);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error while sending files", Toast.LENGTH_LONG).show();
                }

            }
        }
    }


    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private static boolean isDirEmpty(final Path directory) throws IOException {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    private void sendSensorFiles(File sensorDir, Context context) throws InterruptedException {
        if (sensorDir.exists()) {
            File[] files = sensorDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // do something here with the file
                    if(context instanceof ConfigActivity) {
                        String spectroId = SharedPrefManager.getInstance(context).getSpectreId();
                        ConfigActivity activity = (ConfigActivity) context;
                        new AsyncCallerSensor(spectroId, file, activity).execute();
                    }
                    sleep(500);
                }
            }else{
                Toast.makeText(context, "Directory is empty", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendResultFiles(File resultDir, Context context) throws InterruptedException {
        if (resultDir.exists()) {
            File[] files = resultDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // do something here with the file
                    if(context instanceof ConfigActivity) {
                        String spectroId = SharedPrefManager.getInstance(context).getSpectreId();
                        ConfigActivity activity = (ConfigActivity) context;
                        new AsyncCallerResult(spectroId, file, activity).execute();
                    }
                    sleep(500);
                }
            }else{
                Toast.makeText(context, "Directory is empty", Toast.LENGTH_LONG).show();
            }
        }
    }

}
