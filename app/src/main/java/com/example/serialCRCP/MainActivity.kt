package com.example.serialCRCP

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.serialCRCP.databinding.ActivityMainBinding
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.telemedc.d2scanner.utils.ArduinoSerial
import java.io.IOException


class MainActivity : AppCompatActivity(), SerialInputOutputManager.Listener {

    private var port: UsbSerialPort? = null
    private var usbIoManager: SerialInputOutputManager? = null
    private var handler: Handler? = null
    var initial = true


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        initList()
    }

    private fun initList() {

        binding.one.setOnClickListener {
            ArduinoSerial.port?.write("1\n".toByteArray(Charsets.UTF_8), 0)

        }
        binding.two.setOnClickListener {
            ArduinoSerial.port?.write("2\n".toByteArray(Charsets.UTF_8), 0)

        }
        binding.three.setOnClickListener {
            ArduinoSerial.port?.write("3\n".toByteArray(Charsets.UTF_8), 0)

        }
        binding.all.setOnClickListener {
            ArduinoSerial.port?.write("4\n".toByteArray(Charsets.UTF_8), 0)

        }
        binding.connect.setOnClickListener {
            connectSerial()

        }

    }

    private fun connectSerial() {
        if (initial) {
            try {
                ArduinoSerial.findUSBDevice(this@MainActivity)
                initial = false
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                setupSerial()
            }
        }
    }

    fun setupSerial() {
        if (ArduinoSerial.port != null) {
            port = ArduinoSerial.port
            usbIoManager = SerialInputOutputManager(port, this@MainActivity)
            usbIoManager?.start()
            handler = Handler(Looper.getMainLooper())
        }
    }

    override fun onNewData(data: ByteArray?) {
        Toast.makeText(this, "new data", Toast.LENGTH_SHORT).show()
        handler?.post { receive(data) }
    }

    private fun receive(data: ByteArray?) {
        Toast.makeText(this, String(data!!), Toast.LENGTH_SHORT).show()
    }

    override fun onRunError(e: Exception?) {
        Toast.makeText(this, e?.message, Toast.LENGTH_SHORT).show()
    }
}