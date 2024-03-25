package com.example.serialCRCP

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
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

        binding.xp.setOnClickListener {
            ArduinoSerial.port?.write("X + 1\n".toByteArray(Charsets.UTF_8), 0)
        }
        binding.xn.setOnClickListener {
            ArduinoSerial.port?.write("X - 1\n".toByteArray(Charsets.UTF_8), 0)
        }

        binding.yp.setOnClickListener {
            ArduinoSerial.port?.write("Y + 1\n".toByteArray(Charsets.UTF_8), 0)
        }
        binding.yn.setOnClickListener {
            ArduinoSerial.port?.write("Y - 1\n".toByteArray(Charsets.UTF_8), 0)
        }

        binding.zp.setOnClickListener {
            ArduinoSerial.port?.write("Z + 1\n".toByteArray(Charsets.UTF_8), 0)
        }
        binding.zn.setOnClickListener {
            ArduinoSerial.port?.write("Z - 1\n".toByteArray(Charsets.UTF_8), 0)
        }

        binding.ep.setOnClickListener {
            ArduinoSerial.port?.write("E + 1\n".toByteArray(Charsets.UTF_8), 0)
        }
        binding.en.setOnClickListener {
            ArduinoSerial.port?.write("E - 1\n".toByteArray(Charsets.UTF_8), 0)
        }

        binding.ledOne.setOnClickListener {
            ArduinoSerial.port?.write("Light 1\n".toByteArray(Charsets.UTF_8), 0)
        }
        binding.ledTwo.setOnClickListener {
            ArduinoSerial.port?.write("Light 2\n".toByteArray(Charsets.UTF_8), 0)
        }

        binding.motorMode.setOnClickListener {
            ArduinoSerial.port?.write("1\n".toByteArray(Charsets.UTF_8), 0)
            binding.ledModeCommand.visibility = View.GONE
            binding.motorModeCommand.visibility = View.VISIBLE
        }

        binding.ledMode.setOnClickListener {
            ArduinoSerial.port?.write("2\n".toByteArray(Charsets.UTF_8), 0)
            binding.ledModeCommand.visibility = View.VISIBLE
            binding.motorModeCommand.visibility = View.GONE
        }

        binding.stop.setOnClickListener {
            ArduinoSerial.port?.write("S + 1\n".toByteArray(Charsets.UTF_8), 0)
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

    private fun setupSerial() {
        if (ArduinoSerial.port != null) {
            port = ArduinoSerial.port
            usbIoManager = SerialInputOutputManager(port, this@MainActivity)
            usbIoManager?.start()
            handler = Handler(Looper.getMainLooper())
        }
    }

    override fun onNewData(data: ByteArray?) {
        println("new Data")
        handler?.post { receive(data) }
    }

    private fun receive(data: ByteArray?) {
        binding.current.text = String(data!!)
    }

    override fun onRunError(e: Exception?) {
//        println(e?.message)
    }
}