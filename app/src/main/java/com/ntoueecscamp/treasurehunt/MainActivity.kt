package com.ntoueecscamp.treasurehunt


import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ntoueecscamp.treasurehunt.databinding.ActivityMainBinding
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

class Treasure(val treasureName:String){
    private val sdf = SimpleDateFormat("HH:mm:ss")
    private val currentDate = sdf.format(Date())
    private val foundTime= currentDate
    fun getName():String{
        return (treasureName)
    }
    fun getTime():String{
        return foundTime
    }
}

class MainActivity : AppCompatActivity(),LocationListener {
    private lateinit var locationManager: LocationManager
    private var treasureList: MutableSet<String> = mutableSetOf()
    private lateinit var binding: ActivityMainBinding


    override fun onStop() {
        super.onStop()
        //write(treasureList)
        Log.d("MainActivity", "onStop Called")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume Called")
        read()
        startLocationUpdates()
        display()
    }
    
    override fun onPause() {
        super.onPause()
        //write(treasureList)
        Log.d("MainActivity", "onPause Called")
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy Called")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("MainActivity", "onRestart Called")
        //read()
        //display()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        read()
        display()
        Log.d("MainActivity", "onCreate Called")
        val rollButton: Button = findViewById(R.id.button1)
        rollButton.setOnClickListener {
            clear()
            display()
            //getLocation()
            //binding.numberOfTreasure.text=retrieveNFCMessage(intent)
        }
        //read()
        //display()
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val sdf = SimpleDateFormat("HH:mm:ss")
            val currentDate = sdf.format(Date())
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                // Process the messages array.
                Log.d("MainActivity", "onNewIntent Called")
                read()
                treasureList.add(currentDate + "  " +retrieveNFCMessage(intent))
                display()
                save(treasureList)

            }
        }
    }


    private fun getNDefMessages(intent: Intent): Array<NdefMessage> {

        val rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        rawMessage?.let {
            return rawMessage.map {
                it as NdefMessage
            }.toTypedArray()
        }
        // Unknown tag type
        val empty = byteArrayOf()
        val record = NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty)
        val msg = NdefMessage(arrayOf(record))
        return arrayOf(msg)
    }

    private fun retrieveNFCMessage(intent: Intent?): String {
        intent?.let {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
                val nDefMessages = getNDefMessages(intent)
                nDefMessages[0].records?.let { it ->
                    it.forEach { it ->
                        it?.payload.let {
                            it?.let {
                                Log.d("MainActivity", "retrieveNFCMessage Called")
                                //treasureList.add(String(it))
                                return String(it)
                            }
                        }
                    }
                }

            } else {
                return "Touch NFC tag to read data"
            }
        }
        return "Touch NFC tag to read data"
    }

    private fun save(s: MutableSet<String>) {
        /**創建SharedPreferences，索引為"Data" */
        val sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE)

        /**取得SharedPreferences.Editor編輯內容 */
        val editor = sharedPreferences.edit()
        /**放入字串，並定義索引為"Saved" */
        //editor.putString("Saved", s.toString())
        editor.putString("Saved", s.joinToString())
        /**提交；提交結果將會回傳一布林值*/
        /**若不需要提交結果，則可使用.apply()*/
        Log.d("MainActivity", "write Called")
        Log.d("MainActivity", s.toString())
        editor.apply()
    }

    private fun read() {
        /**創建SharedPreferences，索引為"Data"*/
        val sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE)

        /**回傳在"Saved"索引之下的資料；若無儲存則回傳"未存任何資料"*/
        //Log.d("MainActivity",sharedPreferences.getString("Saved","未存任何資料").toString())
        var tem = sharedPreferences.getString("Saved", null)?.split(",")?.map { it.trim() }
        if (tem != null) {
            treasureList = tem.toMutableSet()
        }
    }

    private fun clear() {
        /**創建SharedPreferences */
        val sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE)

        /**取得SharedPreferences.Editor */
        val editor = sharedPreferences.edit()
        /**利用clear清除掉所有資料 */
        editor.clear()
        /**不返回結果的提交 */
        /**若需要提交結果，則可使用.commit() */
        editor.apply()
        treasureList.clear()
    }

    private fun display() {

        var temp = ""
        for (treasure in treasureList) {
            temp += treasure + "\n"
        }
        binding.treasureListView.text = temp
        binding.treasureListTitle.text = "已收集 (${treasureList.size.toString()}/21)"
    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "定位中", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "定位權限被拒，請先至設定開啟權限", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                })
                finish()
            }
        }
    }
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else {
            Toast.makeText(this, "定位中", Toast.LENGTH_SHORT).show()
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, this

            )
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1f, this)
        }
    }
    override fun onLocationChanged(location: Location) {
        binding.position.text="緯度: " + location.latitude + "\n經度: " + location.longitude
    }
    private fun stopLocationUpdates() {
        locationManager.removeUpdates(LocationListener {})
    }
    private fun startLocationUpdates() {
        getLocation()
    }


    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

}


