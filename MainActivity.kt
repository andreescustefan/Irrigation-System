package com.example.kotliniot

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val handler = Handler()
    private fun asd(x: TextView, y: TextView, z: TextView) {
         val runnable: Runnable = object : Runnable {
            override fun run() {
                getData(x, "2", 2)
                getData(y, "1", 1)
                getData(z, "3", 3)
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(runnable);
    }
    private fun sendDada(field: String) {
        val asyncTask: AsyncTask<*, *, *> = object : AsyncTask<Any?, Any?, Any?>() {
            override fun doInBackground(objects: Array<Any?>): Any? {
                val client = OkHttpClient()
                val request: Request = Request.Builder()
                        .url("https://api.thingspeak.com/update?api_key=TABU7D2OUI5TVGG5&field" + field)
                        .build()
                var response: Response? = null
                try {
                    response = client.newCall(request).execute()
                    return response.body().string()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return null
            }
        }.execute()
    }
    private fun getData(txt: TextView, field: String, z: Int){
        val asyncTask: AsyncTask<*, *, *> = object : AsyncTask<Any?, Any?, Any?>() {
            override fun doInBackground(objects: Array<Any?>): Any? {
                val client = OkHttpClient()
                val request: Request = Request.Builder()
                        .url("https://api.thingspeak.com/channels/939407/fields/" + field + "/last.json?api_key=GATCL5J9R1VLH7ML&results=2")
                        .build()
                var response: Response? = null
                try {
                    response = client.newCall(request).execute()
                    return response.body().string()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return null
            }

            override fun onPostExecute(o: Any?) {
                val data = o.toString()
                var hum: String
                if(z == 2) {
                    txt.text = (o.toString().substring(61, data.lastIndexOf("\""))) + " \u2103"
                }
                if(z == 1) {
                    hum = o.toString().substring(61, data.lastIndexOf("\"")) + "%"
                    txt.text = hum
                }
                if(z ==3){
                    val soil = o.toString().substring(61, data.lastIndexOf("\"")).toInt()
                    if(soil==0){
                        txt.text = "Dry"
                    }
                    else{
                        txt.text = "Wet"
                    }
                }
            }
        }.execute()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        val toggleAuto = findViewById<ToggleButton>(R.id.toggleButton)
        val togglePump = findViewById<ToggleButton>(R.id.toggleButtonPump)
        val textViewT = findViewById<TextView>(R.id.textViewTemp)
        val textViewH = findViewById<TextView>(R.id.textViewHum)
        val textViewDW = findViewById<TextView>(R.id.textViewSoil)

        togglePump.isEnabled = false;
        button.setOnClickListener {
            getData(textViewT, "2", 2)
            getData(textViewH, "1", 1)
            getData(textViewDW, "3", 3)
        }
        toggleAuto.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendDada("4=1")
                togglePump.isEnabled = false;
            } else {
                sendDada("4=0")
                togglePump.isEnabled = true;
            }
        }
        togglePump.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendDada("5=1")
            } else {
                sendDada("5=0")
            }
        }
        asd(textViewT, textViewH, textViewDW)
    }
}