package com.mascir.oliveoilsensor.utils

import android.app.Activity
import android.os.AsyncTask
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
//import com.mascir.oliveoilsensor.SensorActivity
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException


val url = "http://iot.mascir.ma:1412"
//val url = "http://192.168.43.252:1412"
class AsyncCallerSensor(
        spectroId: String,
        file: File,
        act: Activity
) : AsyncTask<String, Void, Void>() {

    companion object {
        var isSuccess = ""
    }

    val spectroId: String? = spectroId
    val file: File? = file
    val act: Activity = act

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        println(isSuccess)
    }

    override fun doInBackground(vararg p0: String?): Void? {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("userId", "MAScIR_Test")
                .addFormDataPart("spectroId", spectroId)
                .addFormDataPart(
                        "file", file?.name,
                        RequestBody.create(MediaType.parse("text/csv"), file)
                )
                .build()
        val request = Request.Builder()
                .url(url + "/mobilesensor")
                .post(body)
                .build()


        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isSuccess = "failed"
                println("onFailure : " + e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    isSuccess = response.body()?.string().toString()
                    println("onResponse : " + isSuccess)
                    act.runOnUiThread(java.lang.Runnable {
                        if (isSuccess.equals("success")) {
                            if (file!!.delete())
                                Toast.makeText(act, "Réussite de l'envoi du fichier", Toast.LENGTH_LONG).show()
                            else
                                Toast.makeText(act, " Echec de suppression du fichier", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(act, "Echec d'envoi de fichier !! Merci de contacter le responsable du Serveur", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        })
        println("done")
        return null
    }
}

class AsyncCallerResult(
        spectroId: String,
        file: File,
        act: Activity
) : AsyncTask<String, Void, String>() {

    companion object {
        var response = ""
    }

    val spectroId: String? = spectroId
    val file: File? = file
    val act: Activity = act

    override fun onPostExecute(aVoid: String?) {
        super.onPostExecute(aVoid)
    }

    override fun doInBackground(vararg p0: String?): String? {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("userId", "MAScIR_Test")
                .addFormDataPart("spectroId", spectroId)
                .addFormDataPart(
                        "file", file?.name,
                        RequestBody.create(MediaType.parse("text/csv"), file)
                )
                .build()
        val request = Request.Builder()
                .url(url + "/mobileresult")
                .post(body)
                .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                response = "failed"
                println("onFailure : " + e.message)
            }

            override fun onResponse(call: Call, serverResponse: Response) {
                if (serverResponse.isSuccessful) {
                    response = serverResponse.body()?.string().toString()
                    println("onResponse : " + response)
                    act.runOnUiThread(java.lang.Runnable {
                        if (response.equals("success")) {
                            if (file!!.delete())
                                Toast.makeText(act, "Réussite de l'envoi du fichier", Toast.LENGTH_LONG).show()
                            else
                                Toast.makeText(act, " Echec de suppression du fichier", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(act, "Echec d'envoi de fichier !! Merci de contacter le responsable du Serveur", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        })
        println("done")

        return response
    }
}

class AsyncCallerNormal(
        file: File,
        act: Activity,
        text1: TextView,
        text2: TextView,
        text3: TextView

) : AsyncTask<String, Void, String>() {

    companion object {
        var res = "";
        var result1 = "";
        var result2 = "";
        var result3 = "";
        var objData: JSONObject = JSONObject();
    }

    val file: File? = file
    val act: Activity = act
    val textViewRes1: TextView = text1
    val textViewRes2: TextView = text2
    val textViewRes3: TextView = text3


    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        var done = false
        while (!done) {
            if (res != "") {
                done = true
                if (res == "failed") {
                    res = ""
                    val builder = AlertDialog.Builder(act)
                    builder.setTitle("un problème est survenue au niveau Serveur")
                    builder.setMessage("Veuillez continuer vos scan, les fichiers seront envoyés une fois connecté au Serveur")
                    builder.setPositiveButton("OK") { dialog, which ->
                    }
                    builder.show()
                }  /*else {
                    Toast.makeText(
                            act as AppCompatActivity,
                            "Traitement Mixing est réussi", Toast.LENGTH_LONG
                    ).show()
                   val resultat1: String =
                            objData.getString("resPred1")
                    val resultat2: String =
                            objData.getString("resPred2")
                    val resultat3: String =
                            objData.getString("resPred3")

                    textViewRes1.text = "Resultat Pred1: " + resultat1
                    textViewRes2.text = "Resultat Pred2: " + resultat2
                    textViewRes3.text = "Resultat Pred3: " + resultat3

                    println("Résultat Pred1 = "+resultat1)
                    println("Résultat Pred2 = "+resultat2)
                    println("Résultat Pred3 = "+resultat3)

                }*/
            }
        }
    }

    override fun doInBackground(vararg p0: String?): String?  {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file", file?.name,
                        RequestBody.create(MediaType.parse("text/csv"), file)
                )
                .addFormDataPart("user", p0[0]!!)
                .addFormDataPart("sensor", p0[1]!!)
                .addFormDataPart("date", p0[2]!!)
                .build()
        val request = Request.Builder()
                .url(url + "/mixing")
                .post(body)
                .build()


        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                res =  "failed"
                println("onFailure : " + e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    res = "success"
                    val jsonArr = JSONArray(response.body()?.string().toString())
                    objData = jsonArr.getJSONObject(0)

                    result1 = objData.getString("resPred1")
                    result2 = objData.getString("resPred2")
                    result3 = objData.getString("resPred3")
                    act.runOnUiThread(java.lang.Runnable {
                        println(objData)
                        println(result1)
                        println(result2)
                        println(result3)
                        textViewRes1.text = "Resultat Pred1: " + result1
                        textViewRes2.text = "Resultat Pred2: " + result2
                        textViewRes3.text = "Resultat Pred3: " + result3
                        if (file!!.delete())
                            Toast.makeText(act, "Traitement de Mixing réussi", Toast.LENGTH_LONG).show()
                        else
                            Toast.makeText(act, " Echec de suppression du fichier", Toast.LENGTH_LONG).show()
                    })

                }
            }
        })
        println("done")
        return result1
    }
}

