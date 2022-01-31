package com.mascir.oliveoilsensor.utils;

import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import com.mascir.oliveoilsensor.utils.Models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalculResult {

    public ArrayList<String> readFileData(String filePath) throws FileNotFoundException
    {

        String[] data;
        ArrayList<String> Tarray = new ArrayList<>();
        File file = new File(filePath);
        int iteration = 0;
        if (file.exists())
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            try
            {
                String csvLine;
                while ((csvLine = br.readLine()) != null)
                {
                    data=csvLine.split(",");
                    if(iteration < 1){
                        iteration++;
                        continue;
                    }
                    try
                    {
                        //Toast.makeText(getApplicationContext(),data[0]+" "+data[1],Toast.LENGTH_SHORT).show();
                        Tarray.add(data[1]);
                    }
                    catch (Exception e)
                    {
                        Log.e("Problem",e.toString());
                    }
                }
            }
            catch (IOException ex)
            {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
        }
        else
        {
          // Toast.makeText(getApplicationContext(),"file not exists",Toast.LENGTH_SHORT).show();
            return null;
        }

        return Tarray;
    }


    /*
    Xarray[], Xcoef[]
   for(int i=0; i<Xarray.length; i++){
       if(Xarray[i]!=Xcoef[i]) continue;
       else{
               sum+= coef[i] * Tarray[i];
           }
       }
       res = sum + intercept;
   }
   */
    public Double calculRes2(double[] Tarray, double[] coef1, double[] coef2){
        double res = 0.0;
        double val1 = 0.0;
        double val2 = 0.0;
        for(int i=0; i<Tarray.length; i++){
            val1 += coef1[i] * Tarray[i];
            val2 += coef2[i] * Tarray[i];
        }

        if(val1 > val2) res = 0; else res = 1;

        return res;
    }

    public Double calculRes(double[] Tarray, double[] coef, double intercept){
        double res = 0.0;
        double sum = 0.0;
        //System.out.println("Tarray.length= "+Tarray.length);
        //System.out.println("Coef.length= "+coef.length);
        for(int i=0; i<Tarray.length; i++){
            sum+= coef[i] * Tarray[i];
        }
        //System.out.println("somme mult= "+sum);
        res = intercept + sum;
        //System.out.println("res= "+res);
        return Math.abs(res);
    }

    public String createCSV(String path, Double resm1, Double resm2, Double resm3, String spectroId) {
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            //   Toast.makeText(MainActivity.this, "Storage not available or read only", Toast.LENGTH_LONG).show();
            return null;
        }

        File folder = new File(path);
        boolean var = false;
        if (!folder.exists())
            var = folder.mkdir();

        SimpleDateFormat databaseDateTimeFormate = new SimpleDateFormat("ddMMyyyy-HHmmss");
        String dateTime = databaseDateTimeFormate.format(new Date());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String date_time = simpleDateFormat.format(new Date());

        String filename = dateTime+".csv";
        String file = folder.toString()+"/"+filename;
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(file));

            String[] header = { "Spectre", "Capteur", "ResultatHOM5", "ResultatHOM6", "ResultatHOM7", "Date de creation"};
            writer.writeNext(header); // Write column header
            writer.writeNext((filename + "/" + spectroId + "/" + decimalFormat(resm1)+ "/"+ decimalFormat(resm2)  + "/"+ decimalFormat(resm3) + "/" + date_time).split("/"));
            writer.close();
            return filename;
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return "";
    }

    private String decimalFormat(double res){
        return new DecimalFormat("##.##").format(res);
    }

    private boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

}
