package com.specknet.pdiotapp.live

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.AssetManager
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.specknet.pdiotapp.ml.Model
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.collections.ArrayList

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.ImageView
import com.specknet.pdiotapp.ml.Model5
import com.specknet.pdiotapp.MainActivity
import android.media.RingtoneManager

import android.media.Ringtone
import android.net.Uri
import android.os.Vibrator





class LiveDataActivity5 : AppCompatActivity() {


    lateinit var live1: TextView
    lateinit var live2: TextView
    lateinit var home: TextView
    lateinit var output1: TextView
    lateinit var image1: ImageView
    lateinit var output2: TextView
    lateinit var image2: ImageView
    lateinit var output3: TextView
    lateinit var image3: ImageView
    lateinit var output4: TextView
    lateinit var image4: ImageView
    lateinit var output5: TextView
    lateinit var image5: ImageView
    lateinit var t1: TextView
    lateinit var t2: TextView
    lateinit var t3: TextView
    lateinit var t4: TextView
    lateinit var t5: TextView

    val hashMap: HashMap<Int,Int> = HashMap()




    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet

    val INDEX_TO_NAME_MAPPING = mapOf(
        0 to "Falling",
        1 to "Lying down",
        2 to "Running",
        3 to "Standing/Sitting",
        4 to "Walking",
    )
    var time = 0f

    var a_max1 = 0
    var a_max2 = 0
    var a_max3 = 0
    var a_max4 = 0
    var a_max5 = 0

    var tfinput = FloatArray(50 * 6) { 0.toFloat() }
    var counter = 0


    lateinit var allRespeckData: LineData


    lateinit var respeckChart: LineChart

    // global broadcast receiver so we can unregister it
    lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperRespeck: Looper

    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.specknet.pdiotapp.R.layout.activity_live_data)
        setupCharts()
        live1 = findViewById(com.specknet.pdiotapp.R.id.live1)
        live2 = findViewById(com.specknet.pdiotapp.R.id.live2)
        home = findViewById(com.specknet.pdiotapp.R.id.home)
        val mgr = assets
        val tf = Typeface.createFromAsset(mgr, "ahronbd.ttf")
        live1.typeface = tf
        live2.typeface = tf
        home.typeface = tf


        // set up the broadcast receiver
        respeckLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)
                output1 = findViewById(com.specknet.pdiotapp.R.id.output1)
                val action = intent.action
                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {

                    val liveData =
                        intent.getSerializableExtra(Constants.RESPECK_LIVE_DATA) as RESpeckLiveData
                    Log.d("Live", "onReceive: liveData = " + liveData)

                    // get all relevant intent contents
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ
                    val gx = liveData.gyro.x
                    val gy = liveData.gyro.y

                    val gz = liveData.gyro.z

                    modelinput(x, y, z, gx, gy, gz)
                    time += 1
                    updateGraph("respeck", x, y, z)

                }
            }
        }

        // register receiver on another thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive")
        handlerThreadRespeck.start()
        looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        this.registerReceiver(respeckLiveUpdateReceiver, filterTestRespeck, null, handlerRespeck)

    }


    fun setupCharts() {
        val mgr = assets
        val tf = Typeface.createFromAsset(mgr, "ahronbd.ttf")
        output1 = findViewById(com.specknet.pdiotapp.R.id.output1)
        output2 = findViewById(com.specknet.pdiotapp.R.id.output2)
        output3 = findViewById(com.specknet.pdiotapp.R.id.output3)
        output4 = findViewById(com.specknet.pdiotapp.R.id.output4)
        output5 = findViewById(com.specknet.pdiotapp.R.id.output5)
        output1.typeface = tf
        output2.typeface = tf
        output3.typeface = tf
        output4.typeface = tf
        output5.typeface = tf
        t1 = findViewById(com.specknet.pdiotapp.R.id.t1)
        t2 = findViewById(com.specknet.pdiotapp.R.id.t2)
        t3 = findViewById(com.specknet.pdiotapp.R.id.t3)
        t4 = findViewById(com.specknet.pdiotapp.R.id.t4)
        t5 = findViewById(com.specknet.pdiotapp.R.id.t5)
        t1.typeface = tf
        t2.typeface = tf
        t3.typeface = tf
        t4.typeface = tf
        t5.typeface = tf
        respeckChart = findViewById(com.specknet.pdiotapp.R.id.respeck_chart)

        // Respeck
        time = 0f
        val entries_res_accel_x = ArrayList<Entry>()
        val entries_res_accel_y = ArrayList<Entry>()
        val entries_res_accel_z = ArrayList<Entry>()

        dataSet_res_accel_x = LineDataSet(entries_res_accel_x, "Accel X")
        dataSet_res_accel_y = LineDataSet(entries_res_accel_y, "Accel Y")
        dataSet_res_accel_z = LineDataSet(entries_res_accel_z, "Accel Z")

        dataSet_res_accel_x.setDrawCircles(false)
        dataSet_res_accel_y.setDrawCircles(false)
        dataSet_res_accel_z.setDrawCircles(false)

        dataSet_res_accel_x.setColor(
            ContextCompat.getColor(
                this,
                com.specknet.pdiotapp.R.color.red
            )
        )
        dataSet_res_accel_y.setColor(
            ContextCompat.getColor(
                this,
                com.specknet.pdiotapp.R.color.green
            )
        )
        dataSet_res_accel_z.setColor(
            ContextCompat.getColor(
                this,
                com.specknet.pdiotapp.R.color.blue
            )
        )

        val dataSetsRes = ArrayList<ILineDataSet>()
        dataSetsRes.add(dataSet_res_accel_x)
        dataSetsRes.add(dataSet_res_accel_y)
        dataSetsRes.add(dataSet_res_accel_z)

        allRespeckData = LineData(dataSetsRes)
        respeckChart.data = allRespeckData
        respeckChart.invalidate()
    }

    fun updateGraph(graph: String, x: Float, y: Float, z: Float) {
        // take the first element from the queue
        // and update the graph with it
        if (graph == "respeck") {
            dataSet_res_accel_x.addEntry(Entry(time, x))
            dataSet_res_accel_y.addEntry(Entry(time, y))
            dataSet_res_accel_z.addEntry(Entry(time, z))

            runOnUiThread {
                allRespeckData.notifyDataChanged()
                respeckChart.notifyDataSetChanged()
                respeckChart.invalidate()
                respeckChart.setVisibleXRangeMaximum(150f)
                respeckChart.moveViewToX(respeckChart.lowestVisibleX + 40)
            }
        }
    }

    fun modelinput(x: Float, y: Float, z: Float, x1: Float, y1: Float, z1: Float) {
        if (counter <= 294) {
            this.tfinput.set(counter, x)
            this.tfinput.set(counter + 1, y)
            this.tfinput.set(counter + 2, z)
            this.tfinput.set(counter + 3, x1)
            this.tfinput.set(counter + 4, y1)
            this.tfinput.set(counter + 5, z1)
            counter += 6
            Log.d("input", "$tfinput")
        } else if (counter > 294) {
            val model = Model5.newInstance(this)

            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 50, 6), DataType.FLOAT32)
            inputFeature0.loadArray(tfinput)

            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            Log.d("testtest", "${outputFeature0.floatArray[0]}")
            var floatarray = FloatArray(5) { 0.toFloat() }
            for (i in 0..4) {
                floatarray[i] = outputFeature0.floatArray[i]
            }
            a_max1 = 0
            a_max2 = 0
            a_max3 = 0
            a_max4 = 0
            a_max5 = 0

            var f1 = floatarray.maxOrNull()
            var floatarray_1 = floatarray.clone()
            floatarray_1.sortDescending()
            var f2 = floatarray_1[1]
            var f3 = floatarray_1[2]
            var f4 = floatarray_1[3]
            var f5 = floatarray_1[4]
            if (f1 != null) {
                a_max1 = floatarray.indexOf(f1)
                a_max2 = floatarray.indexOf(f2)
                a_max3 = floatarray.indexOf(f3)
                a_max4 = floatarray.indexOf(f4)
                a_max5 = floatarray.indexOf(f5)
            }
            Log.d("index of max", "${a_max1}")

            if(this.hashMap.containsKey(a_max1)) {
                val a = hashMap.get(a_max1)?.plus(1)
                if (a != null) {
                    this.hashMap.put(a_max1, a)
                }
            }else{
                this.hashMap.put(a_max1, 0)
            }

            if(hashMap.get(3) != null && hashMap.get(3)!! > 10){
                AlertDialog.Builder(
                    this@LiveDataActivity5
                )
                    .setTitle("Health Care")
                    .setMessage("Sitting too long, please relax for a while:)")
                    .setPositiveButton("Get it", null)
                    .show()

                val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val rt = RingtoneManager.getRingtone(applicationContext, uri)


                rt.play()

                hashMap.put(3,0)

            }

            val stringPrediction1 = INDEX_TO_NAME_MAPPING[a_max1]
            val stringPrediction2 = INDEX_TO_NAME_MAPPING[a_max2]
            val stringPrediction3 = INDEX_TO_NAME_MAPPING[a_max3]
            val stringPrediction4 = INDEX_TO_NAME_MAPPING[a_max4]
            val stringPrediction5 = INDEX_TO_NAME_MAPPING[a_max5]
            output1 = findViewById(com.specknet.pdiotapp.R.id.output1)
            image1 = findViewById(com.specknet.pdiotapp.R.id.image1)
            output2 = findViewById(com.specknet.pdiotapp.R.id.output2)
            image2 = findViewById(com.specknet.pdiotapp.R.id.image2)
            output3 = findViewById(com.specknet.pdiotapp.R.id.output3)
            image3 = findViewById(com.specknet.pdiotapp.R.id.image3)
            output4 = findViewById(com.specknet.pdiotapp.R.id.output4)
            image4 = findViewById(com.specknet.pdiotapp.R.id.image4)
            output5 = findViewById(com.specknet.pdiotapp.R.id.output5)
            image5 = findViewById(com.specknet.pdiotapp.R.id.image5)
            t1 = findViewById(com.specknet.pdiotapp.R.id.t1)
            t2 = findViewById(com.specknet.pdiotapp.R.id.t2)
            t3 = findViewById(com.specknet.pdiotapp.R.id.t3)
            t4 = findViewById(com.specknet.pdiotapp.R.id.t4)
            t5 = findViewById(com.specknet.pdiotapp.R.id.t5)
            var drawable1 = getDrawAble(a_max1)
            var drawable2 = getDrawAble(a_max2)
            var drawable3 = getDrawAble(a_max3)
            var drawable4 = getDrawAble(a_max4)
            var drawable5 = getDrawAble(a_max5)
            if (stringPrediction1 != null) {
                setText(output1, stringPrediction1)
                setImage(image1, drawable1)
                if (stringPrediction2 != null) {
                    setText(output2, stringPrediction2)
                }
                setImage(image2, drawable2)
                if (stringPrediction3 != null) {
                    setText(output3, stringPrediction3)
                }
                setImage(image3, drawable3)
                if (stringPrediction4 != null) {
                    setText(output4, stringPrediction4)
                }
                setImage(image4, drawable4)
                if (stringPrediction5 != null) {
                    setText(output5, stringPrediction5)
                }
                setImage(image5, drawable5)
                if (f1 != null) {
                    setTextInt(t1, f1)
                    setTextInt(t2, f2)
                    setTextInt(t3, f3)
                    setTextInt(t4, f4)
                    setTextInt(t5, f5)
                }
            }

            model.close()
            var mask = FloatArray(50 * 6) { 0.toFloat() }
            for (i in 0..149){
                mask[i] = tfinput[150+i]
            }
            this.tfinput = mask.clone()
            counter = 150
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(respeckLiveUpdateReceiver)
        looperRespeck.quit()
    }

    private fun setText(text: TextView, value: String) {
        val mgr = assets
        val tf = Typeface.createFromAsset(mgr, "ahronbd.ttf")
        runOnUiThread {
            text.text = value
            text.typeface = tf
        }
    }

    private fun setImage(image: ImageView, value: Drawable) {
        runOnUiThread { image.setImageDrawable(value) }
    }

    private fun getDrawAble(a_max : Int): Drawable {
        var drawable: Drawable
        when (a_max) {
            0 -> {
                drawable = resources.getDrawable(com.specknet.pdiotapp.R.mipmap.fallingontheback4)
            }
            1 -> {
                drawable =
                    resources.getDrawable(com.specknet.pdiotapp.R.mipmap.lyingdownonback8)
            }
            2 -> {
                drawable = resources.getDrawable(com.specknet.pdiotapp.R.mipmap.running12)
            }
            3 -> {
                drawable = resources.getDrawable(com.specknet.pdiotapp.R.mipmap.standing16)
            }
            4 -> {
                drawable =
                    resources.getDrawable(com.specknet.pdiotapp.R.mipmap.walkatnormalspeed17)
            }
            else -> {
                drawable = resources.getDrawable(com.specknet.pdiotapp.R.mipmap.detect)
            }
        }
        return drawable
    }

    private fun setTextInt(text: TextView, value: Float) {
        val mgr = assets
        val tf = Typeface.createFromAsset(mgr, "ahronbd.ttf")
        runOnUiThread {
            text.text = String.format("%.1f", (value * 100)) + '%'
            text.typeface = tf
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25F + 10F*value)

        }
    }

}

