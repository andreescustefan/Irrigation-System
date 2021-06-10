package com.example.kotliniot

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity(){
    private val handler = Handler()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getInitialState()
        if (numberPickerStartHour != null) {
            numberPickerStartHour.minValue = 0
            numberPickerStartHour.maxValue = 24
            numberPickerStartHour.wrapSelectorWheel = true
            numberPickerStartHour.setOnValueChangedListener { picker, oldVal, newVal ->
                val text = "Changed from $oldVal to $newVal"
                textViewTypeTime.text = "$newVal"
                Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
            }
        }
        if (numberPickerDurationHour != null) {
            numberPickerDurationHour.minValue = 0
            numberPickerDurationHour.maxValue = 50
            numberPickerDurationHour.wrapSelectorWheel = true
            numberPickerDurationHour.setOnValueChangedListener { picker, oldVal, newVal ->
                val text = "Changed from $oldVal to $newVal"
                textViewTypeDuration.text = "$newVal"
                Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
            }
        }
        buttonConfigure.setOnClickListener {
            sendDataSchedule("8=0")
            verifySch("8=0")
        }
        buttonSave.setOnClickListener {
            sendDataSchedule("8=1")
            verifySch("8=1")
        }
        buttonSendStart.setOnClickListener {
            var hour = textViewTypeTime.text
            sendData("6=$hour")
            verify("6=$hour")
        }
        buttonSendDuration.setOnClickListener {
            var hour = textViewTypeDuration.text
            sendData("7=$hour")
            verify("7=$hour")
        }
        buttonSync.setOnClickListener {
            getInitialState()
        }
        buttonSync.performClick()
        buttonSendAuto.setOnClickListener {
            sendData("4=1")
            verify("4=1")
            textViewMode.text = "Auto mode"
            textViewMode.setTextColor(Color.RED)
            turnManualOptions(0)
            turnScheduleOptions(0)
            buttonSendManual.setBackgroundColor(Color.RED)
            buttonSendAuto.setBackgroundColor(Color.GREEN)
            buttonSendSchedule.setBackgroundColor(Color.RED)
        }
        buttonSendManual.setOnClickListener {
            sendData("4=0")
            verify("4=0")
            textViewMode.text = "Manual mode"
            textViewMode.setTextColor(Color.RED)
            turnManualOptions(1)
            turnScheduleOptions(0)
            buttonSendManual.setBackgroundColor(Color.GREEN)
            buttonSendAuto.setBackgroundColor(Color.RED)
            buttonSendSchedule.setBackgroundColor(Color.RED)

        }
        buttonSendSchedule.setOnClickListener {
            sendData("4=2")
            verify("4=2")
            textViewMode.text = "Schedule mode"
            textViewMode.setTextColor(Color.RED)
            turnScheduleOptions(1)
            turnManualOptions(0)
            buttonSendManual.setBackgroundColor(Color.RED)
            buttonSendAuto.setBackgroundColor(Color.RED)
            buttonSendSchedule.setBackgroundColor(Color.GREEN)
        }
        buttonSendPumpOn.setOnClickListener {
            sendData("5=1")
            verify("5=1")
            buttonSendPumpOff.setBackgroundColor(Color.RED)
            buttonSendPumpOn.setBackgroundColor(Color.GREEN)
            }
        buttonSendPumpOff.setOnClickListener {
            sendData("5=0")
            verify("5=0")
            buttonSendPumpOff.setBackgroundColor(Color.GREEN)
            buttonSendPumpOn.setBackgroundColor(Color.RED)
        }
        checkBoxMo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendDataSchedule("1=11")
                verifySch("1=11")
            }
            if (!isChecked) {
                sendDataSchedule("1=10")
                verifySch("1=10")
            }
        }
        checkBoxTu.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendDataSchedule("2=21")
                verifySch("2=21")
            }
            if (!isChecked) {
                sendDataSchedule("2=20")
                verifySch("2=20")
            }
        }
        checkBoxWe.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendDataSchedule("3=31")
                verifySch("3=31")
            }
            if (!isChecked) {
                sendDataSchedule("3=30")
                verifySch("3=30")
            }
        }
        checkBoxTh.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendDataSchedule("4=41")
                verifySch("4=41")
            }
            if (!isChecked) {
                sendDataSchedule("4=40")
                verifySch("4=40")
            }
        }
        checkBoxFr.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendDataSchedule("5=51")
                verifySch("5=51")
            }
            if (!isChecked) {
                sendDataSchedule("5=50")
                verifySch("5=50")
            }
        }
        checkBoxSa.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendDataSchedule("6=61")
                verifySch("6=61")
            }
            if (!isChecked) {
                sendDataSchedule("6=60")
                verifySch("6=60")
            }
        }
        checkBoxSu.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sendDataSchedule("7=71")
                verifySch("7=71")
            }
            if (!isChecked) {
                sendDataSchedule("7=70")
                verifySch("7=70")
            }
        }
        getAllData()
    }
    private fun verify(field: String){
        for(i in 3 downTo 0)
        {
            sendData(field)
            Thread.sleep(16_000)
        }
    }
    private fun verifySch(field: String){
        for(i in 3 downTo 0)
        {
            sendDataSchedule(field)
            Thread.sleep(16_000)
        }
    }
    @SuppressLint("SetTextI18n")
    private fun getInitialState(){
        getData(textViewInfoAuto, "4", 4)
        getData(textViewInfoPump, "5", 5)

        val valueAuto = textViewInfoAuto.text.toString().toIntOrNull()
        val valuePump = textViewInfoPump.text.toString().toIntOrNull()

        if(valueAuto == 0){
            buttonSendManual.setBackgroundColor(Color.GREEN)
            buttonSendAuto.setBackgroundColor(Color.RED)
            buttonSendSchedule.setBackgroundColor(Color.RED)

            turnManualOptions(1)
            turnScheduleOptions(0)

            textViewMode.text = "Manual mode"
            textViewMode.setTextColor(Color.RED)
        }
        if(valueAuto == 1){
            buttonSendManual.setBackgroundColor(Color.RED)
            buttonSendAuto.setBackgroundColor(Color.GREEN)
            buttonSendSchedule.setBackgroundColor(Color.RED)

            turnManualOptions(0)
            turnScheduleOptions(0)

            textViewMode.text = "Auto mode"
            textViewMode.setTextColor(Color.RED)
        }
        if(valueAuto == 2){
            buttonSendManual.setBackgroundColor(Color.RED)
            buttonSendAuto.setBackgroundColor(Color.RED)
            buttonSendSchedule.setBackgroundColor(Color.GREEN)

            turnManualOptions(0)
            turnScheduleOptions(1)

            textViewMode.text = "Schedule mode"
            textViewMode.setTextColor(Color.RED)
        }
        if(valuePump == 0){
            buttonSendPumpOn.setBackgroundColor(Color.RED)
            buttonSendPumpOff.setBackgroundColor(Color.GREEN)
        }
        if(valuePump == 1){
            buttonSendPumpOn.setBackgroundColor(Color.GREEN)
            buttonSendPumpOff.setBackgroundColor(Color.RED)
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
    private fun sendDataSchedule(field: String) {
        val asyncTask: AsyncTask<*, *, *> = object : AsyncTask<Any?, Any?, Any?>() {
            override fun doInBackground(objects: Array<Any?>): Any? {
                val client = OkHttpClient()
                val request: Request = Request.Builder()
                        .url("https://api.thingspeak.com/update?api_key=RYJZ0Z5C6TH9U5MC&field" + field)
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
    private fun getDataSchedule(txt: TextView, field: String){
        val asyncTask: AsyncTask<*, *, *> = object : AsyncTask<Any?, Any?, Any?>() {
            override fun doInBackground(objects: Array<Any?>): Any? {
                val client = OkHttpClient()
                val request: Request = Request.Builder()
                        .url("https://api.thingspeak.com/channels/1396075/fields/" + field + "/last.json?api_key=1H2NTVVL6RYRDV8Y&results=2")
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
                txt.text = o.toString().substring(data.lastIndexOf("\":\"") + 3, data.lastIndexOf("\""))
            }
        }.execute()
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
    private fun getData(txt: TextView, field: String, id: Int){
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
                if(id == 2) {
                    txt.text = o.toString().substring(data.lastIndexOf("\":\"") + 3, data.lastIndexOf("\"")) + " \u2103"
                }
                if(id == 1) {
                    txt.text = o.toString().substring(data.lastIndexOf("\":\"") + 3, data.lastIndexOf("\"")) + "%"
                }
                if(id == 3){
                    val soil = o.toString().substring(data.lastIndexOf("\":\"") + 3, data.lastIndexOf("\"")).toInt()
                    if(soil==0){
                        txt.text = "Dry"
                    }
                    else{
                        txt.text = "Wet"
                    }
                }
                if(id == 4) {
                    txt.text = o.toString().substring(data.lastIndexOf("\":\"") + 3, data.lastIndexOf("\""))
                }
                if(id == 5) {
                    txt.text = o.toString().substring(data.lastIndexOf("\":\"") + 3, data.lastIndexOf("\""))
                }
            }
        }.execute()
    }
    private fun turnScheduleOptions(turn: Int){
        if(turn == 0){
            textViewStartTime.visibility = View.INVISIBLE
            textViewDuration.visibility = View.INVISIBLE
            textViewTypeDuration.visibility = View.INVISIBLE
            textViewTypeTime.visibility = View.INVISIBLE
            checkBoxMo.visibility = View.INVISIBLE
            checkBoxTu.visibility = View.INVISIBLE
            checkBoxWe.visibility = View.INVISIBLE
            checkBoxTh.visibility = View.INVISIBLE
            checkBoxFr.visibility = View.INVISIBLE
            checkBoxSa.visibility = View.INVISIBLE
            checkBoxSu.visibility = View.INVISIBLE
            numberPickerStartHour.visibility = View.INVISIBLE
            numberPickerDurationHour.visibility = View.INVISIBLE
            buttonSendStart.visibility = View.INVISIBLE
            buttonSendDuration.visibility = View.INVISIBLE
            buttonConfigure.visibility = View.INVISIBLE
            buttonSave.visibility = View.INVISIBLE
        }
        else{
            textViewStartTime.visibility = View.VISIBLE
            textViewDuration.visibility = View.VISIBLE
            textViewTypeDuration.visibility = View.VISIBLE
            textViewTypeTime.visibility = View.VISIBLE
            checkBoxMo.visibility = View.VISIBLE
            checkBoxTu.visibility = View.VISIBLE
            checkBoxWe.visibility = View.VISIBLE
            checkBoxTh.visibility = View.VISIBLE
            checkBoxFr.visibility = View.VISIBLE
            checkBoxSa.visibility = View.VISIBLE
            checkBoxSu.visibility = View.VISIBLE
            numberPickerStartHour.visibility = View.VISIBLE
            numberPickerDurationHour.visibility = View.VISIBLE
            buttonSendStart.visibility = View.VISIBLE
            buttonSendDuration.visibility = View.VISIBLE
            buttonConfigure.visibility = View.VISIBLE
            buttonSave.visibility = View.VISIBLE
        }
    }
    private fun turnManualOptions(turn: Int){
        if(turn == 0){
            textViewPump.visibility = View.INVISIBLE
            buttonSendPumpOn.visibility = View.INVISIBLE
            buttonSendPumpOff.visibility = View.INVISIBLE
        }
        else{
            textViewPump.visibility = View.VISIBLE
            buttonSendPumpOn.visibility = View.VISIBLE
            buttonSendPumpOff.visibility = View.VISIBLE
        }
    }
}