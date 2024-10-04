package com.example.serialCRCP

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.util.UUID

class BLEConnection {

    companion object {
        @Volatile
        private var instance: BLEConnection? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: BLEConnection().also { instance = it }
        }
    }

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var BLEscanCallback: ScanCallback

    var bleScanListener: DeviceScanListener? = null
    var BLEGatt: BluetoothGatt? = null

    fun init_BLE(context: Context) {
        bluetoothManager = context.getSystemService(Service.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.getAdapter()

        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(context, "Bluetooth not turned on", Toast.LENGTH_SHORT).show()
        }

    }

    interface DeviceScanListener {

        fun onBLEConnected(
            connected: Boolean
        )
    }

    fun setDeviceScanListener(listener: DeviceScanListener) {
        bleScanListener = listener
    }

    @SuppressLint("MissingPermission")
    fun scanLeDevice(ctx : Context) {

        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        BLEscanCallback = object : ScanCallback() {
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)

            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                if (result?.scanRecord?.serviceUuids.toString().contains("4fafc201")
                ) {
                    bluetoothLeScanner.stopScan(BLEscanCallback)
                    connectBLE(ctx, result?.device)
                } else {
                    return
                }
            }
        }

        bluetoothLeScanner.startScan(BLEscanCallback)

    }

    @SuppressLint("MissingPermission")
    fun connectBLE(ctx: Context, device: BluetoothDevice?) {

        BLEGatt = device?.connectGatt(ctx, false, object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                    bleScanListener?.onBLEConnected(true)
                    gatt?.discoverServices()
                } else {
                    scanLeDevice(ctx)
                    bleScanListener?.onBLEConnected(false)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
            }

            override fun onServiceChanged(gatt: BluetoothGatt) {
                super.onServiceChanged(gatt)
            }

            override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
                super.onReliableWriteCompleted(gatt, status)
            }

            override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
                super.onReadRemoteRssi(gatt, rssi, status)
            }

            override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
                super.onPhyUpdate(gatt, txPhy, rxPhy, status)
            }

            override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
                super.onPhyRead(gatt, txPhy, rxPhy, status)
            }

            override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                super.onMtuChanged(gatt, mtu, status)
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {
                super.onDescriptorWrite(gatt, descriptor, status)
            }

            override fun onDescriptorRead(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int,
                value: ByteArray
            ) {
                super.onDescriptorRead(gatt, descriptor, status, value)
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                super.onCharacteristicRead(gatt, characteristic, value, status)
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                super.onCharacteristicChanged(gatt, characteristic, value)

                println(value.decodeToString())
            }


        })

    }
}