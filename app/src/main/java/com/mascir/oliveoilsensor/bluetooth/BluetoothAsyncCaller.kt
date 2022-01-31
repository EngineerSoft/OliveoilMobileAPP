package com.mascir.oliveoilsensor.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.mascir.oliveoilsensor.SensorActivity.mmDevice
import com.mascir.oliveoilsensor.R
import com.mascir.oliveoilsensor.bluetooth.BluetoothCalibrate.Companion.calibrBtn
import com.mascir.oliveoilsensor.bluetooth.BluetoothCalibrate.Companion.mmSocket
import com.mascir.oliveoilsensor.bluetooth.BluetoothCalibrate.Companion.scanBtn
import com.mascir.oliveoilsensor.bluetooth.BluetoothCalibrate.Companion.printBtn
import com.mascir.oliveoilsensor.utils.*
import java.io.*
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class BluetoothCalibrate(
        act: Activity,
        mBluetoothAdapter: BluetoothAdapter,
        progressBar: ProgressBar,
        textView: TextView,
        bluetoothHelper: BluetoothHelper1,
) : AsyncTask<BluetoothDevice, Void, Void>() {

    companion object{
        var response = ""
        var message = ""
        var mmSocket: BluetoothSocket? = null
        var calibrBtn: Button? = null
        var scanBtn: Button? = null
        var printBtn: Button? = null
    }

    private val activity: Activity = act
    private val progressBar: ProgressBar = progressBar
    private val textView: TextView = textView
    private val bluetoothHelper: BluetoothHelper1 = bluetoothHelper
    private val mBluetoothAdapter: BluetoothAdapter = mBluetoothAdapter

    override fun onPreExecute() {
        super.onPreExecute()
        calibrBtn = activity.findViewById(R.id.calibr_btn)
        scanBtn = activity.findViewById(R.id.scan_btn)
        printBtn = activity.findViewById(R.id.print_btn)
        progressBar.setVisibility(View.VISIBLE)
        textView.setText(R.string.text_view_progress_calibr)
        calibrBtn!!.setVisibility(View.VISIBLE)
        calibrBtn!!.setEnabled(false)
        calibrBtn!!.setClickable(false)
        calibrBtn!!.setBackgroundResource(R.drawable.btn_shape_deactive)
    }

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        if(response == "DONE") {
            progressBar.setVisibility(View.GONE)
            textView.setText(message)
            //calibrBtn!!.setVisibility(View.VISIBLE)
            //calibrBtn!!.setEnabled(false)
            //calibrBtn!!.setClickable(false)
            //calibrBtn!!.setBackgroundResource(R.drawable.btn_shape_deactive)
            scanBtn!!.setVisibility(View.VISIBLE)
            scanBtn!!.setClickable(true)
            scanBtn!!.setEnabled(true)
            scanBtn!!.setBackgroundResource(R.drawable.btn_custom)
        }

        //bluetoothHelper.showToast(message)
    }

    override fun doInBackground(vararg p0: BluetoothDevice?): Void? {
        var tmp: BluetoothSocket? = null
        try {
            if (mmSocket != null) {
                mmSocket!!.close()
            }
            tmp = mmDevice!!.createRfcommSocketToServiceRecord(bluetoothHelper.getUuid())
            print("create socket: $tmp")
        } catch (e: IOException) {
            print("Socket's create() method failed: $e")
            //progressDialog.dismiss()
            //progressBar.setVisibility(View.GONE)
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
                //progressDialog.dismiss()
                    //progressBar.setVisibility(View.GONE)
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
                //progressDialog.dismiss()
                    //progressBar.setVisibility(View.GONE)
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

class BluetoothScanAcidity(
        act: Activity,
        progressBar: ProgressBar,
        textView: TextView
) : AsyncTask<BluetoothDevice, Void, Void>() {

    private val act: Activity = act
    private val progressBar: ProgressBar = progressBar
    private val textView: TextView = textView
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
        //var resultHO_11:Double?=null
        //var resultHO_1:Double?=null
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
        progressBar.setVisibility(View.VISIBLE)
        printBtn!!.setVisibility(View.GONE)
        stopBtn!!.setEnabled(false)
        cardView!!.setVisibility(View.GONE)
        cardView2!!.setVisibility(View.GONE)
        cardView3!!.setVisibility(View.GONE)
        textView.setText(R.string.text_view_progress_scan)
    }

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        progressBar.setVisibility(View.GONE)
        textView.setText(message)
        scanBtn!!.setVisibility(View.VISIBLE)
        scanBtn!!.setEnabled(true)
        scanBtn!!.setClickable(true)
        scanBtn!!.setText("Lancer le scan")
        view!!.setVisibility(View.VISIBLE)
        stopBtn!!.setVisibility(View.VISIBLE)
        stopBtn!!.setEnabled(true)
        cardView!!.setVisibility(View.VISIBLE)
        cardView2!!.setVisibility(View.VISIBLE)
        cardView3!!.setVisibility(View.VISIBLE)
        printBtn!!.setVisibility(View.VISIBLE)

        textViewRes1!!.setText("M_HO_5:  ${decimalFormat(resultHO_5!!)}")
        textViewRes2!!.setText("M_HO_6:  ${decimalFormat(resultHO_6!!)}")
        textViewRes3 !!. setText("M_HO_7:  ${decimalFormat(resultHO_7!!)}")

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
            //progressBar.setVisibility(View.GONE)
            //textView.setText(message)
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
            progressBar.setVisibility(View.GONE)
            textView.setText(message)
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
            SharedPrefManager.getInstance(act).saveSpectre(result.first.toString())
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
                    printBtn!!.setVisibility(View.GONE)
                    calibrBtn!!.setClickable(true)
                    calibrBtn!!.setEnabled(true)
                    calibrBtn!!.setBackgroundResource(R.drawable.btn_custom)
                    textView.setText("Relancer un nouveau calibrage")
                })
            })
            //mmSocket.close();
        } catch (e: IOException) {
            print("Problems occurred!")
            return
        }
    }
}

class BluetoothScanMixing(
        act: Activity,
        progressBar: ProgressBar,
        textView: TextView
) : AsyncTask<BluetoothDevice, Void, Void>() {

    private val act: Activity = act
    private val progressBar: ProgressBar = progressBar
    private val textView: TextView = textView
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
        progressBar.setVisibility(View.VISIBLE)
        printBtn!!.setVisibility(View.GONE)
        stopBtn!!.setEnabled(false)
        cardView!!.setVisibility(View.GONE)
        cardView2!!.setVisibility(View.GONE)
        cardView3!!.setVisibility(View.GONE)
        textView.setText(R.string.text_view_progress_scan)
    }

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        progressBar.setVisibility(View.GONE)
        textView.setText(message)
        scanBtn!!.setVisibility(View.VISIBLE)
        scanBtn!!.setEnabled(true)
        scanBtn!!.setClickable(true)
        scanBtn!!.setText("Lancer le scan")
        view!!.setVisibility(View.VISIBLE)
        stopBtn!!.setVisibility(View.VISIBLE)
        stopBtn!!.setEnabled(true)
        cardView!!.setVisibility(View.VISIBLE)
        cardView2!!.setVisibility(View.VISIBLE)
        cardView3!!.setVisibility(View.VISIBLE)
        printBtn!!.setVisibility(View.VISIBLE)


        if(!isOnLine)
            Toast.makeText(act, "vérifier votre connexion INTERNET pour l'envoi des fichiers au Serveur ", Toast.LENGTH_LONG).show()
    }

    override fun doInBackground(vararg p0: BluetoothDevice?): Void? {
        try {
            sendSC()
            message="Scan terminé avec succès"
        } catch (e: IOException) {
            //e.printStackTrace()
            print("Connection Scan exception!")
            closeSocket()
            message="Une erreur s'est produite lors du Scan !! Vérifiez si le capteur est correctement alimenté"
            progressBar.setVisibility(View.GONE)
            textView.setText(message)
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
            progressBar.setVisibility(View.GONE)
            textView.setText(message)
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
            val filename: String = utilsMethod.saveReadingValue(result)
            //val filename: String = utilsMethod.saveReadingValueXlsx(result)
            SharedPrefManager.getInstance(act).saveSpectre(result.first.toString())
            println("SpectroId: ${result.first.toString()}")

            try {
                val data: ArrayList<String> = calculResult.readFileData(pathname + "/sensorDirectory/" + filename)
                //val data: ArrayList<String> = utilsMethod.readXlsxFile(pathname + "/sensorDirectory/" + filename)
                println("Data: $data")
                val Tarray = DoubleArray(data.size)
                println("Tarray_1: $Tarray")
                for (i in 0 until data.size) {
                    Tarray[i] = data[i].toDouble()
                    println("Tarray: ${Tarray[i]}")
                }

                val fileToSend = File(pathname + "/sensorDirectory/" + filename)
                val dateTimeFormate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                val dateTime = dateTimeFormate.format(Date())
                if(isNetworkAvailable(act)) {
                    //AsyncCallerSensor(result.first.toString(), fileToSend, act).execute
                    AsyncCallerNormal(fileToSend, act, textViewRes1!!, textViewRes2!!, textViewRes3!!).execute("MAScIR_Test", result.first.toString(), dateTime)
                    isOnLine=true
                }else
                    isOnLine=false

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
                    printBtn!!.setVisibility(View.GONE)
                    calibrBtn!!.setClickable(true)
                    calibrBtn!!.setEnabled(true)
                    calibrBtn!!.setBackgroundResource(R.drawable.btn_custom)
                    textView.setText("Relancer un nouveau calibrage")
                })
            })
            //mmSocket.close();
        } catch (e: IOException) {
            print("Problems occurred!")
            return
        }
    }
}


fun closeSocket() {
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
}