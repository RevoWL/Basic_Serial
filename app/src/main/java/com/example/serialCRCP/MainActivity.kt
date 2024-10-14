package com.example.serialCRCP

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serialCRCP.databinding.ActivityMainBinding
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val BLEinstance = BLEConnection.getInstance()


    private val Permission = if (VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.CAMERA,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkpermission()
    }

    private fun checkpermission() {
        for (permission in Permission) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println(permission)
                ActivityCompat.requestPermissions(this, Permission, 100)
            } else {
                init()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.sum() == 0) {
            init()
        }
    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun init() {

        BLEinstance.init_BLE(this)

        BLEinstance.setDeviceScanListener(object : BLEConnection.DeviceScanListener {

            override fun onBLEConnected(connected: Boolean) {
                runOnUiThread {
                    if (connected) {
                        Toast.makeText(this@MainActivity, "Connected to BLE", Toast.LENGTH_SHORT)
                            .show()

                        try {
                            val characteristic = BLEinstance.BLEGatt!!.getService(
                                UUID.fromString(
                                    "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
                                )
                            )
                                .getCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"))
                            if (BLEinstance.BLEGatt!!.setCharacteristicNotification(
                                    characteristic,
                                    true
                                )
                            ) {
                                for (descriptor in characteristic.descriptors) {

                                    if (VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                        descriptor.value =
                                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                        BLEinstance.BLEGatt!!.writeDescriptor(descriptor)

                                    } else {
                                        BLEinstance.BLEGatt?.writeDescriptor(
                                            descriptor,
                                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                        )
                                    }
                                }
                                binding.notiTxt.text = "Notify enabled waiting for notification"
                            } else {
                                binding.notiTxt.text = "Notification channel not found"

                            }
                        } catch (e: NullPointerException) {
                            Toast.makeText(
                                this@MainActivity,
                                "No characteristic found",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
//                        val intent =
//                            Intent(this@MainActivity, AnteriorEyeRecordingActivity::class.java)
//                        this@MainActivity.startActivity(intent)

                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Disconnected from BLE",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            }

            override fun onCharNotification(value: String) {
                binding.notiTxt.text = value
            }

        })

        binding.connect.setOnClickListener {
            if (BLEinstance.BLEGatt == null) {
                BLEinstance.scanLeDevice(this@MainActivity)
            } else {
                BLEinstance.bleScanListener?.onBLEConnected(true)
            }
        }


        binding.XMinus.setOnClickListener {
            sendValue("X - 1")
        }

        binding.XPlus.setOnClickListener {
            sendValue("X + 1")
        }

        binding.YMinus.setOnClickListener {
            sendValue("Y - 1")
        }

        binding.YPlus.setOnClickListener {
            sendValue("Y + 1")
        }

        binding.ZMinus.setOnClickListener {
            sendValue("Z - 1")
        }

        binding.ZPlus.setOnClickListener {
            sendValue("Z + 1")
        }

        binding.LED1.setOnClickListener {
            sendValue("Light 1")
        }

        binding.LED2.setOnClickListener {
            sendValue("Light 2")
        }

        binding.LED3.setOnClickListener {
            sendValue("Light 3")
        }

        binding.LED4.setOnClickListener {
            sendValue("Light 4")
        }

        binding.LED5.setOnClickListener {
            sendValue("Light 5")
        }

        binding.LED6.setOnClickListener {
            sendValue("Light 6")
        }

    }

    @SuppressLint("MissingPermission", "NewApi")
    fun sendValue(value: String) {
        try {
            val characteristic = BLEinstance.BLEGatt!!.getService(
                UUID.fromString(
                    "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
                )
            ).getCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"))
            if (VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                characteristic.value = value.toByteArray()
                BLEinstance.BLEGatt?.writeCharacteristic(characteristic)

            } else {
                BLEinstance.BLEGatt?.writeCharacteristic(
                    characteristic,
                    value.toByteArray(),
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            }
        } catch (e: NullPointerException) {
            Toast.makeText(this@MainActivity, "No characteristic found", Toast.LENGTH_SHORT)
                .show()
        }
    }
}