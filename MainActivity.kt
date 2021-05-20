package com.example.kotliniot

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity(){
    private val handler = Handler()
    private val hand = Handler()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getInitialState()
        onClickTime()
        onClickDuration()

        val buttonSync = findViewById<Button>(R.id.buttonSync)

        buttonSync.setOnClickListener {
            getInitialState()
        }
        toggleAuto.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendData("4=1")
                toggleManual.isChecked = false
                toggleSchedule.isChecked = false
                //verify(1)
                ver1()
                textViewMode.text = "Auto mode"
                textViewMode.setTextColor(Color.RED)
            }
        }
        toggleManual.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendData("4=0")
                toggleAuto.isChecked = false
                toggleSchedule.isChecked = false
                turnManualOptions(1)
                //verify(0)
                ver()
                textViewMode.text = "Manual mode"
                textViewMode.setTextColor(Color.RED)
            }
            else{
                turnManualOptions(0)
            }
        }
        toggleSchedule.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendData("4=2")
                toggleAuto.isChecked = false
                toggleManual.isChecked = false
                turnScheduleOptions(1)
                //verify(2)
                ver2()
                textViewMode.text = "Schedule mode"
                textViewMode.setTextColor(Color.RED)
            }
            else {
                turnScheduleOptions(0)
            }
        }
        togglePump.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendData("5=1")
                //verify(3)
            } else {
                sendData("5=0")
                //verify(4)
            }
        }
        getAllData()
    }
    private fun ver(){
        val runnable: Runnable = object : Runnable {
            override fun run() {
                getData(textViewInfoAuto, "4", 4)
                val valueAuto = textViewInfoAuto.text.toString().toIntOrNull()
                if(valueAuto == 0){
                    hand.removeCallbacks(this)
                }
                sendData("4=0")
                hand.postDelayed(this, 2000)
            }
        }
        hand.post(runnable)
    }
    private fun ver1(){
        val runnable: Runnable = object : Runnable {
            override fun run() {
                getData(textViewInfoAuto, "4", 4)
                val valueAuto = textViewInfoAuto.text.toString().toIntOrNull()
                if(valueAuto == 1){
                    hand.removeCallbacks(this)
                }
                sendData("4=1")
                hand.postDelayed(this, 2000)
            }
        }
        hand.post(runnable)
    }
    private fun ver2(){
        val runnable: Runnable = object : Runnable {
            override fun run() {
                getData(textViewInfoAuto, "4", 4)
                val valueAuto = textViewInfoAuto.text.toString().toIntOrNull()
                if(valueAuto == 2){
                    hand.removeCallbacks(this)
                }
                sendData("4=2")
                hand.postDelayed(this, 2000)
            }
        }
        hand.post(runnable)
    }
    private fun verify(id: Int) {
        if (id == 0) {
            var verified = 0
            while (verified == 0) {
                getData(textViewInfoAuto, "4", 4)
                val valueAuto = textViewInfoAuto.text.toString().toIntOrNull()
                verified = if (valueAuto == 0) {
                    1
                } else {
                    0
                }
                Handler().postDelayed({
                    sendData("4=0")
                }, 5000)
            }
        }
        if (id == 1) {
            var verified = 0
            while (verified == 0) {
                getData(textViewInfoAuto, "4", 4)
                val valueAuto = textViewInfoAuto.text.toString().toIntOrNull()
                verified = if (valueAuto == 1) {
                    1
                } else {
                    0
                }
                sendData("4=1")
            }
        }
        if (id == 2) {
            var verified = 0
            while (verified == 0) {
                getData(textViewInfoAuto, "4", 4)
                val valueAuto = textViewInfoAuto.text.toString().toIntOrNull()
                verified = if (valueAuto == 2) {
                    1
                } else {
                    0
                }
                sendData("4=2")
            }
        }
        if (id == 3) {
            var verified = 0
            while (verified == 0) {
                getData(textViewInfoPump, "5", 5)
                val valuePump = textViewInfoPump.text.toString().toIntOrNull()
                verified = if (valuePump == 1) {
                    1
                } else {
                    0
                }
                sendData("5=1")
            }
        }
        if (id == 4) {
            var verified = 0
            while (verified == 0) {
                getData(textViewInfoPump, "5", 5)
                val valuePump = textViewInfoPump.text.toString().toIntOrNull()
                verified = if (valuePump == 0) {
                    1
                } else {
                    0
                }
                sendData("5=0")
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun getInitialState(){
        getData(textViewInfoAuto, "4", 4)
        getData(textViewInfoPump, "5", 5)

        val valueAuto = textViewInfoAuto.text.toString().toIntOrNull()
        val valuePump = textViewInfoPump.text.toString().toIntOrNull()

        if(valueAuto == 0){
            toggleManual.isChecked = true
            toggleAuto.isChecked = false
            toggleSchedule.isChecked = false

            turnManualOptions(1)
            turnScheduleOptions(0)

            textViewMode.text = "Manual mode"
            textViewMode.setTextColor(Color.RED)
        }
        else if(valueAuto == 1){
            toggleManual.isChecked = false
            toggleAuto.isChecked = true
            toggleSchedule.isChecked = false

            turnManualOptions(0)
            turnScheduleOptions(0)

            textViewMode.text = "Auto mode"
            textViewMode.setTextColor(Color.RED)
        }
        else if(valueAuto == 2){
            toggleManual.isChecked = false
            toggleAuto.isChecked = false
            toggleSchedule.isChecked = true

            turnManualOptions(0)
            turnScheduleOptions(1)

            textViewMode.text = "Schedule mode"
            textViewMode.setTextColor(Color.RED)
        }
        if(valuePump == 0){
            togglePump.isChecked = false
        }
        else if(valuePump == 1){
            togglePump.isChecked = true
        }
    }
    private fun getAllData() {
        val runnable: Runnable = object : Runnable {
            override fun run() {
                getData(textViewTemp, "2", 2)
                getData(textViewHum, "1", 1)
                getData(textViewSoil, "3", 3)
                handler.postDelayed(this, 2000)
            }
        }
        handler.post(runnable)
    }
    private fun sendData(field: String) {
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
                if(z == 2) {
                    txt.text = o.toString().substring(data.lastIndexOf("\":\"") + 3, data.lastIndexOf("\"")) + " \u2103"
                }
                if(z == 1) {
                    txt.text = o.toString().substring(data.lastIndexOf("\":\"") + 3, data.lastIndexOf("\"")) + "%"
                }
                if(z ==3){
                    val soil = o.toString().substring(data.lastIndexOf("\":\"") + 3, data.lastIndexOf("\"")).toInt()
                    if(soil==0){
                        txt.text = "Dry"
                    }
                    else{
                        txt.text = "Wet"
                    }
                }
                if(z == 4) {
                    txt.text = o.toString().substring(data.lastIndexOf("\":\"") + 3, data.lastIndexOf("\""))
                }
                if(z == 5) {
                    txt.text = o.toString().substring(data.lastIndexOf("\":\"") + 3, data.lastIndexOf("\""))
                }
            }
        }.execute()

    }
    private fun onClickTime() {
        timePicker.setIs24HourView(true)
        timePicker.setOnTimeChangedListener { _, hour, minute -> var hour = hour
            if (textViewTypeTime != null) {
                val hour = hour
                val min = if (minute < 10) "0$minute" else minute

                val msg = "$hour : $min "
                textViewTypeTime.text = msg
                textViewTypeTime.visibility = ViewGroup.VISIBLE
            }
        }

    }
    private fun onClickDuration() {
        durationPicker.setIs24HourView(true)
        durationPicker.setOnTimeChangedListener { _, hour, minute -> var hour = hour
            if (textViewTypeDuration != null) {
                val hour = hour
                val min = if (minute < 10) "0$minute" else minute

                val msg = "$hour h and $min min"
                textViewTypeDuration.text = msg
                textViewTypeDuration.visibility = ViewGroup.VISIBLE
            }
        }

    }
    private fun turnScheduleOptions(turn: Int){
        if(turn == 0){
            textViewStartTime.visibility = View.INVISIBLE
            textViewDuration.visibility = View.INVISIBLE
            textViewRepeat.visibility = View.INVISIBLE
            textViewTypeDuration.visibility = View.INVISIBLE
            durationPicker.visibility = View.INVISIBLE
            textViewTypeTime.visibility = View.INVISIBLE
            timePicker.visibility = View.INVISIBLE
            checkBoxMo.visibility = View.INVISIBLE
            checkBoxTu.visibility = View.INVISIBLE
            checkBoxWe.visibility = View.INVISIBLE
            checkBoxTh.visibility = View.INVISIBLE
            checkBoxFr.visibility = View.INVISIBLE
            checkBoxSa.visibility = View.INVISIBLE
            checkBoxSu.visibility = View.INVISIBLE
        }
        else{
            textViewStartTime.visibility = View.VISIBLE
            textViewDuration.visibility = View.VISIBLE
            textViewRepeat.visibility = View.VISIBLE
            textViewTypeDuration.visibility = View.VISIBLE
            durationPicker.visibility = View.VISIBLE
            textViewTypeTime.visibility = View.VISIBLE
            timePicker.visibility = View.VISIBLE
            checkBoxMo.visibility = View.VISIBLE
            checkBoxTu.visibility = View.VISIBLE
            checkBoxWe.visibility = View.VISIBLE
            checkBoxTh.visibility = View.VISIBLE
            checkBoxFr.visibility = View.VISIBLE
            checkBoxSa.visibility = View.VISIBLE
            checkBoxSu.visibility = View.VISIBLE
        }

    }
    private fun turnManualOptions(turn: Int){
        if(turn == 0){
            textViewPump.visibility = View.INVISIBLE
            togglePump.visibility = View.INVISIBLE
        }
        else{
            textViewPump.visibility = View.VISIBLE
            togglePump.visibility = View.VISIBLE
        }

    }

}