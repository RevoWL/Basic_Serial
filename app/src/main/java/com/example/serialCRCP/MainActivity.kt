package com.example.serialCRCP

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
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

    private val Permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
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
        } else {
            Toast.makeText(this, "Require permission granting", Toast.LENGTH_SHORT).show()
        }

    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun init() {

        val BLEinstance = BLEConnection.getInstance()
        BLEinstance.init_BLE(this)
        val BLEAdapter = bleDeviceListAdapter()

        binding.deviceRecyclerList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity) // Use appropriate context
            adapter = BLEAdapter // Ensure the adapter is set
        }

        BLEinstance.setDeviceScanListener(object : BLEConnection.DeviceScanListener {
            override fun onScanResultsAvailable(scanResult: ScanResult?) {
                BLEAdapter.updateDeviceList(scanResult)
            }

            override fun onBLEConnected(connected: Boolean) {
                runOnUiThread {
                    if (connected) {
                        Toast.makeText(this@MainActivity, "Connected to BLE", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Disconnected from BLE",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            }

        })

        binding.scanBtn.setOnClickListener {
            BLEinstance.scanLeDevice()
            BLEAdapter.clearList()
        }

        binding.send0.setOnClickListener {
            BLEinstance.BLEGatt?.writeCharacteristic(
                BLEinstance.BLEGatt!!.getService(
                    UUID.fromString(
                        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
                    )
                ).getCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")),
                "0".toByteArray(),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        }

        binding.send1.setOnClickListener {
            BLEinstance.BLEGatt?.writeCharacteristic(
                BLEinstance.BLEGatt!!.getService(
                    UUID.fromString(
                        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
                    )
                ).getCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")),
                "1".toByteArray(),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        }
    }
}