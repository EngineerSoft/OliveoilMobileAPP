package com.mascir.oliveoilsensor.bluetooth

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.mascir.oliveoilsensor.MainActivity.mmDevice
//import com.mascir.oliveoilsensor.SensorActivity.mmDevice
import com.mascir.oliveoilsensor.R
import com.mascir.oliveoilsensor.bluetooth.BluetoothCalibratev2.Companion.calibrBtn
import com.mascir.oliveoilsensor.bluetooth.BluetoothCalibratev2.Companion.mmSocket
import com.mascir.oliveoilsensor.bluetooth.BluetoothCalibratev2.Companion.scanBtn
//import com.mascir.oliveoilsensor.bluetooth.BluetoothCalibrate.Companion.calibrBtn
//import com.mascir.oliveoilsensor.bluetooth.BluetoothCalibrate.Companion.mmSocket
//import com.mascir.oliveoilsensor.bluetooth.BluetoothCalibrate.Companion.scanBtn
import com.mascir.oliveoilsensor.utils.*
import java.io.*
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.jvm.Throws


class BluetoothCalibratev2(
        act: Activity,
        mBluetoothAdapter: BluetoothAdapter,
        progressDialog: ProgressDialog,
        bluetoothHelper: BluetoothHelper,
) : AsyncTask<BluetoothDevice, Void, Void>() {

    companion object{
        var response = ""
        var message = ""
        var mmSocket: BluetoothSocket? = null
        var calibrBtn: Button? = null
        var scanBtn: Button? = null
    }

    private val activity: Activity = act
    private val progressDialog: ProgressDialog = progressDialog
    private val bluetoothHelper: BluetoothHelper = bluetoothHelper
    private val mBluetoothAdapter: BluetoothAdapter = mBluetoothAdapter


    override fun onPreExecute() {
        super.onPreExecute()
        calibrBtn = activity.findViewById(R.id.calibr_btn)
        scanBtn = activity.findViewById(R.id.scan_btn)
        progressDialog.setMessage("Veuillez patienter...")
        progressDialog.show()
    }

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        if(response == "DONE") {
            progressDialog.dismiss()
            calibrBtn!!.setVisibility(View.VISIBLE)
            calibrBtn!!.setClickable(false)
            calibrBtn!!.setBackgroundColor(Color.LTGRAY)
            scanBtn!!.setVisibility(View.VISIBLE)
            scanBtn!!.isClickable = true
            scanBtn!!.setBackgroundResource(R.drawable.btn_custom)
        }
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    override fun doInBackground(vararg p0: BluetoothDevice?): Void? {
        var tmp: BluetoothSocket? = null
        try {
            if (mmSocket != null) {
                mmSocket!!.close()
            }
            tmp = mmDevice!!.createRfcommSocketToServiceRecord(bluetoothHelper.getUuid())
        } catch (e: IOException) {
            print("Socket's create() method failed: $e")
            progressDialog.dismiss()
            if(mmSocket != null)
                mmSocket!!.close()
        }
        mmSocket = tmp
        mBluetoothAdapter.cancelDiscovery()
        try {
            mmSocket!!.connect()
            sendBG()
            message = "Connecté avec succès au capteur "+ mmDevice.name
        } catch (connectException: IOException) {
            print("Connection exception!")
            try {
                progressDialog.dismiss()
                mmSocket!!.close()
                message = "Connexion a échouée !! Vérifiez si le capteur est correctement alimenté"
            } catch (closeException: IOException) {
                print("Connection exception!")
            }
        }

        return null
    }

    @Throws(IOException::class)
    fun sendBG() {
        val msg = "bg\r\n"
        val mmOutputStream = mmSocket!!.outputStream
        mmOutputStream.write(msg.toByteArray(StandardCharsets.UTF_8))
        receiveBG()
    }

    @Throws(IOException::class)
    fun receiveBG() {
        val mmInputStream = mmSocket!!.inputStream
        val buffer = ByteArray(256)
        val bytes: Int

        try {
            bytes = mmInputStream.read(buffer)
            val readMessage = String(buffer, 0, bytes)
            //Log.d(TAG, "Received: " + readMessage);
            if (readMessage.contains("BCK_Done")) {
                progressDialog.dismiss()
                response ="DONE"

            } else {
                print("Echec du lancement du Background")
                if(mmSocket != null)
                    mmSocket!!.close()
            }

            //mmSocket.close();
        } catch (e: IOException) {
            print("Problems occurred!")
            return
        }
    }
}

/*class BluetoothScanv2(
        act: Activity,
        progressDialog: ProgressDialog,
        //counter: Int,
) : AsyncTask<BluetoothDevice, Void, Void>() {

    private val act: Activity = act
    private val progressDialog: ProgressDialog = progressDialog
    private val utilsMethod: UtilsMethod = UtilsMethod(act)
    //private var counter: Int = counter

    companion object{
        var message = ""
        var stopBtn: Button? = null
        var view: View? = null
        var cardView: CardView? = null
        var cardView2: CardView? = null
        var cardView3: CardView? = null
        var textViewRes1: TextView? = null
        var textViewRes2: TextView? = null
        var textViewRes3: TextView? = null
        var pathname:String? = null
        val calculResult = CalculResult()
        var resultMM1:Double?=null
        var resultMM2:Double?=null
        var resultMMCM:Double?=null
        var resultMMC40:Double?=null
        var resultMMC2:Double?=null
        var resultMMC3:Double?=null
        var resultMMC4:Double?=null
        var resultMMC5:Double?=null

        //var resultMMC2:Double?=null
        //var finalResult:Double?=null
        var isOnLine : Boolean = false
        /*val xList = mutableListOf<Float>()
        val yList_0 = mutableListOf<Float>()
        val yList_1 = mutableListOf<Float>()
        val yList_2 = mutableListOf<Float>()
        val yList_3 = mutableListOf<Float>()
        val yList_4 = mutableListOf<Float>()

        val list: MutableList<MutableList<Float>> = ArrayList<MutableList<Float>>()
        //val list = ArrayList<Pair<Int, MutableList<Float>>>()
        val sumList = mutableListOf<Float>()
        val moyList = mutableListOf<Double>()*/
    }

    override fun onPreExecute() {
        super.onPreExecute()
        pathname =act.getExternalFilesDir(null).toString()
        stopBtn = act.findViewById(R.id.stop_btn)
        view = act.findViewById(R.id.view)
        textViewRes1 = act.findViewById(R.id.resultValue)
        textViewRes2 = act.findViewById(R.id.resultValue2)
        textViewRes3 = act.findViewById(R.id.resultValue3)
        cardView = act.findViewById(R.id.resultCard)
        cardView2 = act.findViewById(R.id.resultCard2)
        cardView3 = act.findViewById(R.id.resultCard3)
        progressDialog.setMessage("Veuillez patienter...")
        progressDialog.show()
    }

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        progressDialog.dismiss()
        //yList.clear()
        scanBtn!!.setText("Lancer le scan")
        view!!.setVisibility(View.VISIBLE)
        scanBtn!!.setBackgroundResource(R.drawable.btn_custom)
        scanBtn!!.isClickable = true
        /*if(counter!=0) {
            stopBtn!!.setVisibility(View.VISIBLE)
            stopBtn!!.setClickable(false)
            stopBtn!!.setBackgroundColor(Color.LTGRAY)
            cardView!!.setVisibility(View.GONE)
            cardView2!!.setVisibility(View.GONE)
            cardView3!!.setVisibility(View.GONE)
            Toast.makeText(act, "Il vous reste " + counter + " scan", Toast.LENGTH_LONG).show()
        }else {
            scanBtn!!.isClickable = false
            scanBtn!!.setBackgroundColor(Color.LTGRAY)
            /*scanBtn!!.setText("Relancer le scan")
            scanBtn!!.setOnClickListener(View.OnClickListener {
                counter--
                BluetoothScan(act, progressDialog, counter).execute()
            })*/
            stopBtn!!.isClickable = true
            stopBtn!!.setBackgroundResource(R.drawable.et_custom)
            cardView!!.setVisibility(View.VISIBLE)
            cardView2!!.setVisibility(View.VISIBLE)
            cardView3!!.setVisibility(View.VISIBLE)
            textViewRes1!!.setText("RésultatMMC-2: "+ decimalFormat(resultMMC2))
            textViewRes2!!.setText("RésultatMMC-4: "+ decimalFormat(resultMMC4))
            //textViewRes3!!.setText("RésultatMMC-5: "+ decimalFormat(resultMMC5))
            Toast.makeText(act, message, Toast.LENGTH_LONG).show()
            //clearLists()
        }
        //textViewRes1!!.setText("RésultatMM4-12.1: " +decimalFormat(resultMM1) +"\nRésultatMM4-14.4: "+ decimalFormat(resultMM2))
        //textViewRes2!!.setText("RésultatMMC: "+ decimalFormat(resultMMCM) +"\nRésultatMMC-40: "+ decimalFormat(resultMMC40))

        //textViewRes1!!.setText("RésultatMM4-12.1: " +decimalFormat(resultMM1) +"\nRésultatMM4-14.4: "+ decimalFormat(resultMM2))


        //if(!isOnLine)
        //   Toast.makeText(act, "vérifier votre connexion pour l'envoi de fichiers au Serveur ", Toast.LENGTH_LONG).show()
    }

    override fun doInBackground(vararg p0: BluetoothDevice?): Void? {
        try {
            sendSC()
            message="Scan terminé avec succès"
        } catch (e: IOException) {
            //e.printStackTrace()
            print("Connection Scan exception!")
            progressDialog.dismiss()
            closeSocket()
            message="Une erreur s'est produite lors du Scan !! Vérifiez si le capteur est correctement alimenté"
        }

        return null
    }

    @Throws(IOException::class)
    fun sendSC() {
        val msg = "sc\r\n"
        if(mmSocket != null) {
            val mmOutputStream: OutputStream = mmSocket!!.getOutputStream()
            mmOutputStream.write(msg.toByteArray(StandardCharsets.UTF_8))
            receiveSC()
        }else{
            progressDialog.dismiss()
            //closeSocket()
            message="Une erreur s'est produite lors du Scan !! Vérifiez si le capteur est correctement alimenté"
        }
    }

    @Throws(IOException::class)
    fun receiveSC() {
        val mmInputStream: InputStream = mmSocket!!.getInputStream()
        val reader = BufferedReader(InputStreamReader(mmInputStream))

        try {
            val readMessage = reader.readLine()
            println("Received: $readMessage")
            val result: Pair<Int, List<Pair<Float, Float>>> = utilsMethod.transformMessage(readMessage)
            //println("Result: $result")
            result.second.forEach{
                xList.add(it.first)
                when (counter) {
                    4 -> yList_4.add(it.second)
                    3 -> yList_3.add(it.second)
                    2 -> yList_2.add(it.second)
                    1 -> yList_1.add(it.second)
                    0 -> yList_0.add(it.second)
                }
            }



            //list.add(yList)

            // val pair = Pair(counter+1, yList)
            //list.add(4-counter, yList)

            //println("YLIST = $yList")

            //list.add(xList)
            //list.add(yList)

            if(counter == 0) {
                println("spectre_0 = $yList_0")
                println("spectre_1 = $yList_1")
                println("spectre_2 = $yList_2")
                println("spectre_3 = $yList_3")
                println("spectre_4 = $yList_4")

                //var somme = 0.0
                for (j in 0 until 1024) {
                    // for (i in 0 until 5) {
                    var somme = yList_0[j] + yList_1[j] + yList_2[j] + yList_3[j] + yList_4[j]
                    //println("Test: ${list[j]}")
                    //   }
                    sumList.add(j, somme.toFloat())
                    // println("somme = $somme")
                }
                println("Somme = $sumList")

                for(j in 0 until 1024) {
                    var moyenne = (sumList[j] / 5).toDouble()
                    moyList.add(j, moyenne)
                    //println("Moyenne = $moyenne")
                }

                println("Moyenne  = $moyList")

                //resultMMC2 = calculResult.calculC2(moyList, Models.MMC2)
                //resultMMC4 = calculResult.calculC2(moyList, Models.MMC4)
                //resultMMC5 = calculResult.calculC5(moyList, Models.MMC5)
                println("Res2=$resultMMC2")
                println("Res4=$resultMMC4")
                //println("Res5=$resultMMC5")
            }
            //println("Test: $xList")
            //println("Test: $yList")


            //for(element in xList){
            //      System.out.print("Result: $element")
            //}
            // result.second.forEach{
            //    xList.toMutableList().add(it.first)
            //    yList.toMutableList().add(it.second)
            //}
            //listSpec = arrayOf(result).toList()
            //for(value in listSpec){
            //    print("helloooooooooo: $value")
            //   println()
            //}

            /*val filename: String = utilsMethod.saveReadingValue(result)
            val fileToSend = File(pathname + "/sensorDirectory/" + filename)
            if(isNetworkAvailable()) {

                AsyncCallerSensor(fileToSend,act).execute()
                isOnLine=true
            }else
                isOnLine=false

            try {
                val data: ArrayList<String> = calculResult.readFileData(pathname + "/sensorDirectory/" + filename)
                val Tarray = DoubleArray(data.size)
                for (i in 0 until data.size) {
                    Tarray[i] = data[i].toDouble()
                }
                resultMM1 = calculResult.calculRes(Tarray, Models.MM4_12, Models.intercept1)
                resultMM2 = calculResult.calculRes(Tarray, Models.MM4_14, Models.intercept2)
                resultMMCM = calculResult.calculRes(Tarray, Models.MCMN, Models.intercept)
                resultMMC40 = calculResult.calcul(Tarray, Models.MMC40)
                resultMMC2 = calculResult.calculC2(Tarray, Models.MMC2)
                resultMMC3 = calculResult.calculC2(Tarray, Models.MMC3)
                //finalResult = calculResult.calculRes_condition(Tarray)

                val filenameToSend: String = calculResult.createCSV(pathname + "/resultDirectory", resultMM1, resultMM2, resultMMCM, resultMMC40, resultMMC2, resultMMC3, result.first.toString())
                //val dateTime: String = calculResult.createCSV(pathname + "/resultDirectory", resultMM1, resultMM2, resultMMC1, resultMMC2, finalResult, result.first.toString())[1]
                if (filenameToSend != "") {
                    val resultfile = File(pathname + "/resultDirectory/" + filenameToSend)
                    //don't execute and send this specific file until there is conx
                    if(isNetworkAvailable()) {
                        AsyncCallerResult(resultfile, act).execute()
                        isOnLine=true
                    }else
                        isOnLine=false
                }
            }catch (e:FileNotFoundException){
                e.printStackTrace()
            }*/
            stopBtn!!.setOnClickListener(View.OnClickListener {
                closeSocket()
                act.runOnUiThread(Runnable {
                    scanBtn!!.setVisibility(View.GONE)
                    stopBtn!!.setVisibility(View.GONE)
                    view!!.setVisibility(View.GONE)
                    cardView!!.setVisibility(View.GONE)
                    cardView2!!.setVisibility(View.GONE)
                    cardView3!!.setVisibility(View.GONE)
                    calibrBtn!!.setClickable(true)
                    calibrBtn!!.setBackgroundResource(R.drawable.btn_custom)
                })
            })
            //mmSocket.close();
        } catch (e: IOException) {
            print("Problems occurred!")
            return
        }
    }

    fun clearLists(){
        xList.clear()
        yList_0.clear()
        yList_1.clear()
        yList_2.clear()
        yList_3.clear()
        yList_4.clear()
        counter=5;
    }
    fun closeSocket() {
        if (mmSocket != null) {
            try {
                mmSocket!!.inputStream.close()
                mmSocket!!.inputStream.close()
                mmSocket!!.close()
                clearLists()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = act.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun decimalFormat(res: Double?): String? {
        return DecimalFormat("##.##").format(res)
    }
}*/

 */

class BluetoothScanv2(
        act: Activity,
        progressDialog: ProgressDialog,
) : AsyncTask<BluetoothDevice, Void, Void>() {

        private val act: Activity = act
        private val progressDialog: ProgressDialog = progressDialog
        private val utilsMethod: UtilsMethod = UtilsMethod(act)

    companion object{
        var message = ""
        var stopBtn: Button? = null
        var view: View? = null
        var cardView: CardView? = null
        var cardView2: CardView? = null
        var cardView3: CardView? = null
        var textViewRes1: TextView? = null
        var textViewRes2: TextView? = null
        var textViewRes3: TextView? = null
        var pathname:String? = null
        val calculResult = CalculResult()
        var resultHO_5:Double?=null
        var resultHO_6:Double?=null
        var resultHO_7:Double?=null
        var isOnLine : Boolean = false

    }

    override fun onPreExecute() {
        super.onPreExecute()
        pathname =act.getExternalFilesDir(null).toString()
        stopBtn = act.findViewById(R.id.stop_btn)
        view = act.findViewById(R.id.view)
        textViewRes1 = act.findViewById(R.id.resultValue)
        textViewRes2 = act.findViewById(R.id.resultValue2)
        textViewRes3 = act.findViewById(R.id.resultValue3)
        cardView = act.findViewById(R.id.resultCard)
        cardView2 = act.findViewById(R.id.resultCard2)
        cardView3 = act.findViewById(R.id.resultCard3)
        progressDialog.setMessage("Veuillez patienter...")
        progressDialog.show()
    }

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        progressDialog.dismiss()
        scanBtn!!.setVisibility(View.VISIBLE)
        scanBtn!!.setText("Lancer le scan")
        view!!.setVisibility(View.VISIBLE)
        stopBtn!!.setVisibility(View.VISIBLE)
        cardView!!.setVisibility(View.VISIBLE)
        cardView2!!.setVisibility(View.VISIBLE)
        cardView3!!.setVisibility(View.VISIBLE)

        textViewRes1!!.setText("M_HO_5:  ${decimalFormat(resultHO_5!!)}")
        textViewRes2!!.setText("M_HO_6:  ${decimalFormat(resultHO_6!!)}")
        textViewRes3 !!. setText("M_HO_7:  ${decimalFormat(resultHO_7!!)}")

        Toast.makeText(act, message, Toast.LENGTH_LONG).show()

        if(!isOnLine)
            Toast.makeText(act, "vérifier votre connexion pour l'envoi de fichiers au Serveur ", Toast.LENGTH_LONG).show()
    }

    override fun doInBackground(vararg p0: BluetoothDevice?): Void? {
        try {
            sendSC()
            message="Scan terminé avec succès"
        } catch (e: IOException) {
            //e.printStackTrace()
            print("Connection Scan exception!")
            message="Une erreur s'est produite lors du Scan !! Vérifiez si le capteur est correctement alimenté"
            progressDialog.dismiss()
            closeSocket()
        }

        return null
    }

    @Throws(IOException::class)
    fun sendSC() {
        val msg = "sc\r\n"
        if(mmSocket != null) {
            val mmOutputStream: OutputStream = mmSocket!!.getOutputStream()
            mmOutputStream.write(msg.toByteArray(StandardCharsets.UTF_8))
            receiveSC()
        }else{
            //closeSocket()
            message="Une erreur s'est produite lors du Scan !! Vérifiez si le capteur est correctement alimenté"
            progressDialog.dismiss()
        }
    }

    @Throws(IOException::class)
    fun receiveSC() {
        val mmInputStream: InputStream = mmSocket!!.getInputStream()
        val reader = BufferedReader(InputStreamReader(mmInputStream))

        try {
            val readMessage = reader.readLine()
            println("Received: $readMessage")
            val result: Pair<Int, List<Pair<Float, Float>>> = utilsMethod.transformMessage(readMessage)
            //val filename: String = utilsMethod.saveReadingValue(result)
            val filename: String = utilsMethod.saveReadingValueXlsx(result)
            println("SpectroId: ${result.first.toString()}")

            try {
                val data: ArrayList<String> = utilsMethod.readXlsxFile(pathname + "/sensorDirectory/" + filename)
                println("Data: $data")
                val Tarray = DoubleArray(data.size)
                println("Tarray_1: $Tarray")
                for (i in 0 until data.size) {
                    Tarray[i] = data[i].toDouble()
                    println("Tarray: ${Tarray[i]}")
                }

                resultHO_5 = calculResult.calculRes(Tarray, Models.MC_acd5, Models.intercept_acid5)
                resultHO_6 = calculResult.calculRes(Tarray, Models.MC_acid6, Models.intercept_acid6)
                resultHO_7 = calculResult.calculRes(Tarray, Models.MC_acid7, Models.intercept_acid7)

                //resultHO_11 = calculResult.calculRes2(Tarray, Models.MMC11_1, Models.MMC11_2)
                //resultHO_1 = calculResult.calculRes2(Tarray, Models.MMC_1, Models.MMC_2)

                val fileToSend = File(pathname + "/sensorDirectory/" + filename)

                if(isNetworkAvailable(act)) {
                    AsyncCallerSensor(result.first.toString(), fileToSend, act).execute()
                    isOnLine=true
                }else
                    isOnLine=false

                //val filenameToSend: String = calculResult.createCSV(pathname + "/resultDirectory", resultHO_5, resultHO_6, resultHO_7, result.first.toString())
                //val filenameToSend: String = utilsMethod.createResultFile(resultHO_5, resultHO_7, resultHO_11, resultHO_1, result.first.toString())
                val filenameToSend: String = utilsMethod.createResultFile(resultHO_5, resultHO_6, resultHO_7, result.first.toString())

                if (filenameToSend != "") {
                    val resultfile = File(pathname + "/resultDirectory/" + filenameToSend)
                    //don't execute and send this specific file until there is conx
                    if(isNetworkAvailable(act)) {
                        AsyncCallerResult(result.first.toString(),resultfile, act).execute()
                        isOnLine=true
                    }else
                        isOnLine=false
                }
            }catch (e: FileNotFoundException){
                e.printStackTrace()
            }
            stopBtn!!.setOnClickListener(View.OnClickListener {
                closeSocket()
                act.runOnUiThread(Runnable {
                    scanBtn!!.setVisibility(View.GONE)
                    stopBtn!!.setVisibility(View.GONE)
                    view!!.setVisibility(View.GONE)
                    cardView!!.setVisibility(View.GONE)
                    cardView2!!.setVisibility(View.GONE)
                    cardView3!!.setVisibility(View.GONE)
                    calibrBtn!!.setClickable(true)
                    calibrBtn!!.setBackgroundResource(R.drawable.btn_custom)
                })
            })
            //mmSocket.close();
        } catch (e: IOException) {
            print("Problems occurred!")
            return
        }
    }
}

/*fun closeSocket() {
    if (mmSocket != null) {
        try {
            mmSocket!!.inputStream.close()
            mmSocket!!.inputStream.close()
            mmSocket!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

fun doubleToInt(DoubleValue: Double) : Int{
    val IntValue = Math.round(DoubleValue).toInt()
    return IntValue
}
fun decimalFormat(res: Double): String? {
    return DecimalFormat("##.##").format(res)
}
fun isNetworkAvailable(act: Activity): Boolean {
    val connectivityManager = act.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}*/